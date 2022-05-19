package com.example.android.griesol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class AdminHomepage extends AppCompatActivity implements CustomAdapter.OnViewClickListener{
    public static final int REQUEST_CODE_UPDATE_NOTE = 102;
    private static final int REQUEST_CODE_ADD_NOTE = 101;

    ProgressBar progressBar;
    RecyclerView recyclerView;
    public static CustomAdapter customAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_homepage);
        progressBar = findViewById(R.id.dept_loading_pb);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        customAdapter = new CustomAdapter(null,this);
        recyclerView.setAdapter(customAdapter);
        getDepartmentsRequest();
    }

    private void getDepartmentsRequest() {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/department/get";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(AdminHomepage.this, "Couldn't fetch departments", Toast.LENGTH_SHORT).show();
                AdminHomepage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AdminHomepage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(myResponse);
                    } catch (Throwable tx) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + myResponse + "\"");
                    }

                    JSONArray finalJsonArray = jsonArray;
                    AdminHomepage.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            customAdapter.refresh(finalJsonArray);
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onImageClick(JSONObject object) {
        deleteDepartmentRequest(object);
    }

    private void deleteDepartmentRequest(JSONObject object) {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/department/delete";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), object.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Toast.makeText(AdminHomepage.this, "Couldn't delete the department", Toast.LENGTH_SHORT).show();
                AdminHomepage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                AdminHomepage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: "+ myResponse);

                    AdminHomepage.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AdminHomepage.this, "Removed the Department", Toast.LENGTH_SHORT).show();
                            getDepartmentsRequest();
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onViewClick(JSONObject object) {
        Intent updateIntent = new Intent(this, AddDepartment.class);
        updateIntent.putExtra("department", object.toString());
        startActivityForResult(updateIntent, REQUEST_CODE_UPDATE_NOTE);
    }

    public void goToAddDept(View view) {
        Intent addIntent = new Intent(this, AddDepartment.class);
        startActivityForResult(addIntent, REQUEST_CODE_ADD_NOTE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getDepartmentsRequest();
    }

    public void goToAllComplaints(View view) {
        Intent intent = new Intent(getApplicationContext(), ShowAllComplaints.class);
        startActivity(intent);
    }

    public void logOutAdmin(View view) {
        SharedPreferences sp = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sp.edit();
        myEdit.clear();
        myEdit.apply();

        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    public void goToViewRequest(View view) {
        Intent intent = new Intent(getApplicationContext(), ViewRequest.class);
        startActivity(intent);
    }

}