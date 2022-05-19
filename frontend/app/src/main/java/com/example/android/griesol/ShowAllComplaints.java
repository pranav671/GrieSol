package com.example.android.griesol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.json.JSONArray;
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

public class ShowAllComplaints extends AppCompatActivity implements ComplaintAdapter.OnViewClickListener {

    private static final int REQUEST_CODE_VIEW_COMPLAINT = 200;
    private static final int REQUEST_CODE_ADD_COMPLAINT = 201;
    private static final int REQUEST_CODE_MAKE_REQUEST = 202;
    RecyclerView recyclerView;
    ComplaintAdapter complaintAdapter;
    JSONArray requiredComplaints = new JSONArray();
    JSONArray allComplaints;
    Switch yourComplaintSwitch;
    ImageView logOutImage;
    String auth, key, value;
    FloatingActionsMenu floatingActionButton;
    SharedPreferences sp;
    ProgressBar progressBar;
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_complaints);
        sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        auth = sp.getString("auth", "");

        emptyView = (TextView)findViewById(R.id.empty_text);
        floatingActionButton = (FloatingActionsMenu) findViewById(R.id.floatingAddComplaint);
        recyclerView = (RecyclerView) findViewById(R.id.complaint_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintAdapter = new ComplaintAdapter(this, null, this, sp.getString("roll", ""));
        recyclerView.setAdapter(complaintAdapter);
        yourComplaintSwitch = (Switch) findViewById(R.id.yourComplaintSwitch);
        logOutImage = (ImageView) findViewById(R.id.logOutImage);
        progressBar = findViewById(R.id.complaint_loading_pb);
        try {
            if ("dept".equals(auth)) {
                floatingActionButton.setVisibility(View.GONE);
                yourComplaintSwitch.setText("Department Complaints");
                key = "department";
                JSONObject user = new JSONObject(sp.getString("user", ""));
                value = user.getString("department");
            }
            else if("student".equals(auth)){
                yourComplaintSwitch.setText("My Complaints");
                key = "roll";
                value = sp.getString("roll","");
            }
        } catch (Exception ignored) {
        }

        if (auth.equals("admin")) {
            floatingActionButton.setVisibility(View.GONE);
            yourComplaintSwitch.setVisibility(View.GONE);
        } else {
            yourComplaintSwitch.setVisibility(View.VISIBLE);
        }
        yourComplaintSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                try {
                    if (!isChecked) {
                        filterComplaints(true);
                    } else {
                        filterComplaints(false);
                    }
                    complaintAdapter.refresh(requiredComplaints);
                } catch (Exception ignored) {
                }
            }
        });

        logOutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor myEdit = sp.edit();
                myEdit.clear();
                myEdit.apply();

                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
            }
        });

        getAllComplaints();
    }

    private void getAllComplaints() {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/complaint/get";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(ShowAllComplaints.this, "Couldn't fetch complaints", Toast.LENGTH_SHORT).show();
                ShowAllComplaints.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ShowAllComplaints.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    try {
                        allComplaints = new JSONArray(myResponse);
                    } catch (Throwable tx) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + myResponse + "\"");
                    }
                    try {
                        filterComplaints(!yourComplaintSwitch.isChecked());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ShowAllComplaints.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            complaintAdapter.refresh(requiredComplaints);
                            if(requiredComplaints.length() == 0){
                                emptyView.setVisibility(View.VISIBLE);
                            }else{
                                emptyView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });
    }

    private void filterComplaints(Boolean showAll) throws Exception {
        requiredComplaints = new JSONArray();
        for (int i = 0; i < allComplaints.length(); i++) {
            Log.d(TAG, "filterComplaints: " + key + " " + value);
            if (showAll || allComplaints.getJSONObject(i).getString(key).equals(value)) {
                requiredComplaints.put(allComplaints.getJSONObject(i));
            }
        }
    }

    @Override
    public void onImageClick(JSONObject object) {
        String img = "";
        try {
            img = object.getString("image");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (img.equals("")) {
            return;
        }
        loadPhoto(img);

    }

    @Override
    public void onViewClick(JSONObject object) {
        Intent updateIntent = new Intent(this, ComplaintDetail.class);
        updateIntent.putExtra("complaint", object.toString());
        startActivityForResult(updateIntent, REQUEST_CODE_VIEW_COMPLAINT);
    }

    @Override
    public void onUpVoteClick(JSONObject object, Boolean upVoted) {
        progressBar.setVisibility(View.VISIBLE);
        if(upVoted){
            try {
                int votes = object.getInt("votes");
                votes--;

                JSONArray votelistarray = object.getJSONArray("votelist");
                JSONArray newVoteList = new JSONArray();
                for (int i = 0; i < votelistarray.length(); i++) {
                    String val = votelistarray.getString(i);
                    if (!val.equals(sp.getString("roll", ""))) {
                        newVoteList.put(val);
                    }
                }
                updateVotes(object, newVoteList, votes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                int votes = object.getInt("votes");
                votes++;

                JSONArray newVoteList = object.getJSONArray("votelist");
                newVoteList.put(sp.getString("roll", ""));

                updateVotes(object, newVoteList, votes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

//    @Override
//    public void onDownVoteClick(JSONObject object) {
//        progressBar.setVisibility(View.VISIBLE);
//        try {
//            int votes = object.getInt("votes");
//            votes--;
//
//            JSONArray votelistarray = object.getJSONArray("votelist");
//            JSONArray newVoteList = new JSONArray();
//            for (int i = 0; i < votelistarray.length(); i++) {
//                String val = votelistarray.getString(i);
//                if (!val.equals(sp.getString("roll", ""))) {
//                    newVoteList.put(val);
//                }
//            }
//            updateVotes(object, newVoteList, votes);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private void updateVotes(JSONObject object, JSONArray newVoteList, int votes) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/complaint/update";

        JSONObject updateComplaintObj = new JSONObject();
        updateComplaintObj.put("_id", object.getString("_id"));
        updateComplaintObj.put("votes", votes);
        updateComplaintObj.put("votelist", newVoteList);
        Log.d(TAG, "updateVotes:-> " + newVoteList);

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
                ShowAllComplaints.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);

                    ShowAllComplaints.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ShowAllComplaints.this, "Votes are updated", Toast.LENGTH_SHORT).show();
                            getAllComplaints();
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getAllComplaints();
    }

    public void goToAddComplaint(View view) {
        Intent updateIntent = new Intent(this, MainActivity.class);
        startActivityForResult(updateIntent, REQUEST_CODE_ADD_COMPLAINT);
    }

    public void goToMakeRequest(View view) {
        Intent updateIntent = new Intent(this, MakeRequest.class);
        startActivityForResult(updateIntent, REQUEST_CODE_MAKE_REQUEST);
    }

    private void loadPhoto(String dloadlink) {
        ArrayList<String> images = new ArrayList<>();
        images.add(dloadlink);
        new StfalconImageViewer.Builder<String>( this, images, new ImageLoader<String>() {
            @Override
            public void loadImage(ImageView imageView, String imageUrl) {
                Glide.with(ShowAllComplaints.this).load(imageUrl).into(imageView);
            }

        }).show();

    }

    public void logOut(View view) {
        SharedPreferences sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sp.edit();
        myEdit.clear();
        myEdit.apply();

        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }
}