package com.example.android.griesol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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

public class ViewRequest extends AppCompatActivity implements RequestAdapter.OnViewClickListener{

    RecyclerView recyclerView;
    public static RequestAdapter requestAdapter;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);

        recyclerView = (RecyclerView) findViewById(R.id.request_recycler_view);
        progressBar = findViewById(R.id.view_request_loading_pb);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestAdapter = new RequestAdapter(null,this);
        recyclerView.setAdapter(requestAdapter);

        getRequests();
    }

    private void getRequests() {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/request/get";
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                ViewRequest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ViewRequest.this.runOnUiThread(new Runnable() {
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
                        Log.e("My App", "Could not parse malformed JSON: \"" + tx.toString() + "\"");
                    }

                    JSONArray finalJsonArray = jsonArray;
                    ViewRequest.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            requestAdapter.refresh(finalJsonArray);
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onImageClick(JSONObject object) {
        deleteRequest(object);
    }

    private void deleteRequest(JSONObject object) {
        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/request/delete";

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
                ViewRequest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ViewRequest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: "+ myResponse);

                    ViewRequest.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ViewRequest.this, "Removed the Request", Toast.LENGTH_SHORT).show();
                            getRequests();
                        }
                    });

                }
            }
        });
    }
}