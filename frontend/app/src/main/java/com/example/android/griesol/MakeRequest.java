package com.example.android.griesol;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
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

public class MakeRequest extends AppCompatActivity {
    Spinner requestKeySpinner;
    EditText editRequestValue;
    TextView textRequestValue;
    ArrayList<String> requestKeys = new ArrayList<>();
    ArrayList<String> valuesList = new ArrayList<>();
    ArrayAdapter<String> requestKeyAdapter;
    ProgressBar progressBar;
    private LinearLayout parentLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);

        requestKeySpinner = (Spinner)findViewById(R.id.request_key_spinner);
        editRequestValue = (EditText)findViewById(R.id.editRequestValue);
        textRequestValue = (TextView)findViewById(R.id.textRequestValue);
        progressBar = findViewById(R.id.make_request_loading_pb);

        parentLinearLayout=(LinearLayout) findViewById(R.id.parent_linear_layout);

        requestKeys.add("Department");
        requestKeys.add("Asset");
        requestKeys.add("Venue");

        requestKeyAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,requestKeys);
        requestKeySpinner.setAdapter(requestKeyAdapter);

        requestKeySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

//    private void clearValues() {
//        editRequestValue.setText("");
//        valuesList.clear();
//        textRequestValue.setText("");
//    }

    public void addValue(View view) {
        String val = editRequestValue.getText().toString();
        if (val.equals("")){return;}
        valuesList.add(val);

        LayoutInflater inflater=(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView=inflater.inflate(R.layout.field, null);
        TextView temp = (TextView)rowView.findViewById(R.id.textRequestValue);
        temp.setText(val);
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);
        editRequestValue.setText("");

//        String val = editRequestValue.getText().toString();
//        if (val.equals("")){return;}
//        valuesList.add(val);
//        int i = valuesList.size();
//        textRequestValue.append(i + ") " + val + "\n");
//        editRequestValue.setText("");
    }

    public void onDeleteFieldNew(View view) {
        View v = (View) view.getParent().getParent();
        TextView t = (TextView)v.findViewById(R.id.textRequestValue);
        String s = t.getText().toString();
        valuesList.remove(s);
        parentLinearLayout.removeView((View) view.getParent().getParent());
    }

    public void onDeleteField(View v) {
        parentLinearLayout.removeView((View) v.getParent());
    }

    public void uploadRequest(View view) throws Exception{
        if (valuesList.size() == 0){
            Toast.makeText(this, "Add Values", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        JSONArray jsonArrayValue = new JSONArray(valuesList);
        JSONObject requestObj = new JSONObject();
        requestObj.put("key",requestKeySpinner.getSelectedItem().toString());
        requestObj.put("value",jsonArrayValue);
        addRequestPost(requestObj);


    }

    private void addRequestPost(JSONObject requestObj) {
        OkHttpClient client = new OkHttpClient();
        String url;
        url = "https://grie-sol.herokuapp.com/request/add";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestObj.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                MakeRequest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                MakeRequest.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: "+ myResponse);

                    MakeRequest.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MakeRequest.this, "Request is uploaded", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                }
            }
        });
    }

//    public void callClearAll(View view) {
//        clearValues();
//    }


}