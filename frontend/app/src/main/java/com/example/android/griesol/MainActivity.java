package com.example.android.griesol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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


public class MainActivity extends AppCompatActivity {

    Spinner deptSpinner;
    Spinner venueSpinner;
    Spinner assetSpinner;
    EditText editTextDept;
    EditText editTextVenue;
    EditText editTextAsset;
    EditText editTextComplaint;
    JSONArray departments;
    ArrayList<String> dept = new ArrayList<>();
    ArrayList<String> venue = new ArrayList<>();
    ArrayList<String> asset = new ArrayList<>();
    ArrayAdapter<String> deptAdapter;
    ArrayAdapter<String> venueAdapter;
    ArrayAdapter<String> assetAdapter;
    String dloadLink = "";
    Uri imageUri;
    Button uploadComplaint;

    private static final int PICK_IMAGE_REQUEST = 111;
    ImageView chosenImage;
    Uri filePath;
    ProgressBar progressBar;
    ProgressBar circularPB;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deptSpinner = (Spinner)findViewById(R.id.deptSpinner);
        venueSpinner = (Spinner)findViewById(R.id.venueSpinner);
        assetSpinner = (Spinner)findViewById(R.id.assetSpinner);
        chosenImage = (ImageView)findViewById(R.id.chosenImage);
        progressBar = (ProgressBar)findViewById(R.id.ProgressBar);
        circularPB = findViewById(R.id.add_complaint_loading_pb);

        uploadComplaint = (Button)findViewById(R.id.upload_complaint);

        editTextDept = (EditText)findViewById(R.id.editTextDept);
        editTextVenue = (EditText)findViewById(R.id.editTextVenue);
        editTextAsset = (EditText)findViewById(R.id.editTextAsset);
        editTextComplaint = (EditText)findViewById(R.id.editTextComplaint);


        dept.add("Other");
        venue.add("Other");
        asset.add("Other");

        deptAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,dept);
        venueAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,venue);
        assetAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,asset);
        deptSpinner.setAdapter(deptAdapter);
        venueSpinner.setAdapter(venueAdapter);
        assetSpinner.setAdapter(assetAdapter);
        deptAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        venueAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        assetAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String selectedItem = adapterView.getItemAtPosition(pos).toString();
                if (!selectedItem.equals("Other")){
                    editTextDept.setVisibility(View.GONE);
                }else{
                    editTextDept.setVisibility(View.VISIBLE);
                }
                try {
                    deptSeleceted(selectedItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        venueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String selectedItem = adapterView.getItemAtPosition(pos).toString();
                if (!selectedItem.equals("Other")){
                    editTextVenue.setVisibility(View.GONE);
                }else{
                    editTextVenue.setVisibility(View.VISIBLE);
                }
                try {
                    deptSeleceted(selectedItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        assetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String selectedItem = adapterView.getItemAtPosition(pos).toString();
                if (!selectedItem.equals("Other")){
                    editTextAsset.setVisibility(View.GONE);
                }else{
                    editTextAsset.setVisibility(View.VISIBLE);
                }
                try {
                    deptSeleceted(selectedItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        makeRequest();


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deptSeleceted(String selectedItem) throws Exception{
        for (int i = 0; i < departments.length(); i++) {
            JSONObject row = departments.getJSONObject(i);
            if (row.getString("name").equals(selectedItem)){
                JSONArray jsonVenueArray = row.getJSONArray("venues");
                Log.d(TAG, "deptSeleceted: "+ jsonVenueArray);
                venue.clear();
                venue.add("Other");
                for (int j = 0; j < jsonVenueArray.length(); j++) {
                    venue.add(jsonVenueArray.getString(j));
                }
                JSONArray jsonAssetArray = row.getJSONArray("assets");
                asset.clear();
                asset.add("Other");
                for (int j = 0; j < jsonAssetArray.length(); j++) {
                    asset.add(jsonAssetArray.getString(j));
                }
                venueAdapter.notifyDataSetChanged();
                assetAdapter.notifyDataSetChanged();
            }
        }
    }


    private void makeRequest() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/department/get";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    try {
                        departments = new JSONArray(myResponse);
                        for (int i = 0; i < departments.length(); i++) {
                            JSONObject row = departments.getJSONObject(i);
                            dept.add(row.getString("name"));
                        }
                    } catch (Throwable tx) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + myResponse + "\"");
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deptAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        });
    }


    public void fileComplaint(View view) throws Exception{
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String rollNo = sh.getString("roll", "");
        String detailInfo = editTextComplaint.getText().toString();
        String deptInfo = deptSpinner.getSelectedItem().toString();
        String assetInfo = assetSpinner.getSelectedItem().toString();
        String venueInfo = venueSpinner.getSelectedItem().toString();

        if (detailInfo.length() == 0){
            Toast.makeText(this, "Enter the Complaint Detail Field", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadComplaint.setEnabled(false);

        if (deptInfo.equals("Other") && !editTextDept.getText().toString().equals("")){
            deptInfo = editTextDept.getText().toString();
        }
        if (assetInfo.equals("Other") && !editTextAsset.getText().toString().equals("")){
            assetInfo = editTextAsset.getText().toString();
        }if (venueInfo.equals("Other") && !editTextVenue.getText().toString().equals("")){
            venueInfo = editTextVenue.getText().toString();
        }

        circularPB.setVisibility(View.VISIBLE);
        JSONObject newComplaint = new JSONObject();
        newComplaint.put("roll",rollNo);
        newComplaint.put("department",deptInfo);
        newComplaint.put("asset",assetInfo);
        newComplaint.put("venue",venueInfo);
        newComplaint.put("detail",detailInfo);
        newComplaint.put("image",dloadLink);
        postRequest(newComplaint);

    }

    private void postRequest(JSONObject newComplaint) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/complaint/add";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), newComplaint.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        circularPB.setVisibility(View.GONE);
                        uploadComplaint.setEnabled(true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        circularPB.setVisibility(View.GONE);
                        uploadComplaint.setEnabled(true);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: "+ myResponse);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(MainActivity.this, "Complaint is uploaded", Toast.LENGTH_SHORT).show();
                            try {
                                pushNoti(newComplaint);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finish();
                        }
                    });

                }
            }
        });
    }

    private void pushNoti(JSONObject newComplaint) throws Exception{
        OkHttpClient client = new OkHttpClient();
        String url = "https://b71tjruaw7.execute-api.ap-south-1.amazonaws.com/pushNoti";
        JSONObject notiObj = new JSONObject();
        notiObj.put("purpose","add");
        notiObj.put("complaint",newComplaint);
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
                if (response.isSuccessful()){

                }
            }
        });
    }


    public void chooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);


                chosenImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                //Setting image to ImageView
                chosenImage.setImageBitmap(bitmap);
                uploadImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void uploadImage(Bitmap bitmap) {
        if (filePath != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,40,stream);
            byte[] imageByte = stream.toByteArray();

            progressBar.setVisibility(View.VISIBLE);

            long tsLong = System.currentTimeMillis()/1000;
            String fname = Long.toString(tsLong);
            StorageReference childRef = storageRef.child(fname);

            //uploading the image
            UploadTask uploadTask = childRef.putBytes(imageByte);


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> taskUri = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    taskUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            progressBar.setVisibility(View.GONE);
                            dloadLink = uri.toString();
                        }
                    });
                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    chosenImage.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Upload Failed" + e, Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentprogress = (int) progress;
                    progressBar.setProgress(currentprogress);
                }
            })
            ;
        } else {
            chosenImage.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }


}