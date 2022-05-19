package com.example.android.griesol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class ComplaintDetail extends AppCompatActivity {
    Intent receivedIntent;
    RelativeLayout statusLayout;
    JSONObject receivedObject;
    TextView textViewDept;
    TextView textViewVenue;
    TextView textViewAsset;
    TextView textViewDetail;
    ImageView imageViewComplaint;
    Spinner statusSpinner;
    CheckBox checkBox;
    Boolean isChecked;
    ArrayAdapter<String> statusAdapter;
    ArrayList<String> statusEntries = new ArrayList<>();
    Button updateButton, deleteButton;
    String selectedStatus, auth;
    SharedPreferences sp;
    JSONObject user;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);
        sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        receivedIntent = getIntent();
        try {
            receivedObject = new JSONObject(receivedIntent.getStringExtra("complaint"));
            user = new JSONObject(sp.getString("user", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        statusEntries.add("Not Started");
        statusEntries.add("Ongoing");
        statusEntries.add("Addressed");
        statusEntries.add("Irrelevant");

        Log.d(TAG, "onCreate: " + statusEntries);

        progressBar = findViewById(R.id.update_complaint_loading_pb);
        textViewDept = (TextView) findViewById(R.id.departmentText);
        textViewVenue = (TextView) findViewById(R.id.venueText);
        textViewAsset = (TextView) findViewById(R.id.assetText);
        textViewDetail = (TextView) findViewById(R.id.detailText);
        imageViewComplaint = (ImageView) findViewById(R.id.imageComplaint);
        statusSpinner = (Spinner) findViewById(R.id.statusSpinner);
        checkBox = (CheckBox) findViewById(R.id.checkboxResolve);
        updateButton = (Button) findViewById(R.id.updateButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        statusLayout = (RelativeLayout) findViewById(R.id.statusLayout);
        statusAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, statusEntries);
        statusSpinner.setAdapter(statusAdapter);
        auth = sp.getString("auth", "");
        try {
            if (auth.equals("dept") && user.getString("department").equals(receivedObject.getString("department"))) {
                deleteButton.setVisibility(View.GONE);
                updateButton.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.GONE);
            } else if (auth.equals("student") && sp.getString("roll", "").equals(receivedObject.getString("roll"))) {
                updateButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
                statusLayout.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
            } else if (auth.equals("admin")) {
                deleteButton.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
            } else {
                deleteButton.setVisibility(View.GONE);
                updateButton.setVisibility(View.GONE);
                statusLayout.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isChecked = b;
            }
        });
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                selectedStatus = adapterView.getItemAtPosition(pos).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        try {
            initFields();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void initFields() throws Exception {
        textViewDetail.setText(receivedObject.getString("detail"));
        textViewAsset.setText(receivedObject.getString("asset"));
        textViewVenue.setText(receivedObject.getString("venue"));
        textViewDept.setText(receivedObject.getString("department"));
        if (!receivedObject.getString("image").equals("")) {
            Picasso.get().load(receivedObject.getString("image"))
                    .into(imageViewComplaint);
        }

        selectedStatus = receivedObject.getString("status");
        statusSpinner.setSelection(statusEntries.indexOf(selectedStatus));
        if(!selectedStatus.equals("Addressed")){
            checkBox.setEnabled(false);
        }else {
            checkBox.setEnabled(true);
        }
        if(selectedStatus.equals("Irrelevant") || (selectedStatus.equals("Addressed") && receivedObject.getBoolean("resolved_student"))){
            deleteButton.setEnabled(true);
        }else{
            deleteButton.setEnabled(false);
        }
        checkBox.setChecked(receivedObject.getBoolean("resolved_student"));
    }

    public void updateStatus(View view) throws Exception {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/complaint/update";

        JSONObject updateComplaintObj = new JSONObject();
        updateComplaintObj.put("_id", receivedObject.getString("_id"));

        if (auth.equals("dept")) {
            updateComplaintObj.put("status", selectedStatus);
            updateComplaintObj.put("resolved_student", receivedObject.getBoolean("resolved_student"));
            if (selectedStatus.equals("Addressed")) {
                updateComplaintObj.put("resolved_department", true);
            } else {
                updateComplaintObj.put("resolved_department", false);
            }
        } else if (auth.equals(("student"))) {
            updateComplaintObj.put("resolved_student", isChecked);
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), updateComplaintObj.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                ComplaintDetail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ComplaintDetail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);

                    ComplaintDetail.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ComplaintDetail.this, "Complaint is updated", Toast.LENGTH_SHORT).show();
                            try {
                                if (auth.equals("dept")) {
                                    receivedObject.put("status", selectedStatus);
                                    pushNoti(receivedObject);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if(auth.equals("student")){
                                finish();
                            }

                        }
                    });

                }
            }
        });
    }

    private void pushNoti(JSONObject complaintObj) throws Exception {
        if (auth.equals("student")) {
            return;
        }
        OkHttpClient client = new OkHttpClient();
        String url = "https://b71tjruaw7.execute-api.ap-south-1.amazonaws.com/pushNoti";
        JSONObject notiObj = new JSONObject();
        notiObj.put("purpose", "update");
        notiObj.put("complaint", complaintObj);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), notiObj.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                }
            }
        });
    }

    public void deleteComplaint(View view) {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/complaint/delete";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), receivedObject.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                ComplaintDetail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ComplaintDetail.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);

                    ComplaintDetail.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ComplaintDetail.this, "Removed the Complaint", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }
        });
    }

    public void expandImage(View view) {
        String img = "";
        try {
            img = receivedObject.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (img.equals("")) {
            return;
        }
        loadPhoto(img);
    }

    private void loadPhoto(String img) {
        ArrayList<String> images = new ArrayList<>();
        images.add(img);
        new StfalconImageViewer.Builder<String>( this, images, new ImageLoader<String>() {
            @Override
            public void loadImage(ImageView imageView, String imageUrl) {
                Glide.with(ComplaintDetail.this).load(imageUrl).into(imageView);
            }
        }).show();
    }


}