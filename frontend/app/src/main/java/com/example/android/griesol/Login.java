package com.example.android.griesol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;

import org.json.JSONException;
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

public class Login extends AppCompatActivity{

    TextView loginLogo, verifyEmailText;
    PinView pinView;
    LinearLayout  et_email_layout;
    EditText et_email;
    EditText et_password;
    Button loginButton;
    LinearLayout otpLayout;
    Boolean emailVerified = false, nitcEmail = false;
    String OTP = "", verifiedEmailString;
    ProgressBar progressBar, sendOTPPB;
    Boolean studentUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkIfLoggedIn();

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        sendOTPPB = findViewById(R.id.sendOTP_loading_pb);
        pinView = (PinView)findViewById(R.id.pinView);
        loginButton = (Button) findViewById(R.id.btn_login);
        loginLogo = (TextView) findViewById(R.id.login_logo);
        verifyEmailText = (TextView) findViewById(R.id.verify_email_text);
        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);
        otpLayout = (LinearLayout)findViewById(R.id.otp_layout);
        et_email_layout = (LinearLayout)findViewById(R.id.et_email_layout);

        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                    String exprsn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@nitc.ac.in";
                    if (et_email.getText().toString().matches(exprsn) && et_email.getText().toString().length() > 10) {
                        et_email_layout.setBackground(getResources().getDrawable(R.drawable.et_custom));
                        if(studentUser){
                            verifyEmailText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        et_email_layout.setBackground(getResources().getDrawable(R.drawable.email_bg_invalid));
                        verifyEmailText.setVisibility(View.GONE);
                    }
            }
        });

        pinView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String pin = charSequence.toString();
                if (pin.equals(OTP)){
                    emailVerified = true;
                    loginTheStudent();
                }
                else if(pin.length() == 6){
                    Toast.makeText(Login.this, "Try Again", Toast.LENGTH_SHORT).show();
                    pinView.setText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        makeChanges();
        if (!isInternetAvailable()){
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void loginTheStudent() {
        Toast.makeText(Login.this, "Successful", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "loginTheStudent: " + verifiedEmailString);
        String []a = verifiedEmailString.split("@");
        a = a[0].split("_");
        String roll = a[1].toUpperCase();

        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        myEdit.putString("auth", "student");
        myEdit.putString("roll", roll);

        myEdit.apply();

        Intent intent = new Intent(getApplicationContext(), ShowAllComplaints.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }

    private void checkIfLoggedIn() {
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString("auth", "");
        if (s1.equals("admin")){
            Intent intent = new Intent(getApplicationContext(), AdminHomepage.class);
            startActivity(intent);
            finish();
        }
        else if(!s1.equals("")){
            Intent intent = new Intent(getApplicationContext(), ShowAllComplaints.class);
            startActivity(intent);
            finish();
        }
    }


    public void login(View view) throws Exception {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if (email.length() == 0 || password.length() == 0) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        JSONObject loginData = new JSONObject();
        loginData.put("email", email);
        loginData.put("password", password);

        OkHttpClient client = new OkHttpClient();
        String url = "https://grie-sol.herokuapp.com/login/";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), loginData.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Login.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Login.this, "Couldn't make the connection", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    JSONObject res = null;
                    try {
                        res = new JSONObject(myResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject finalRes = res;
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            if (finalRes == null) {
                                Toast.makeText(Login.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject user = (JSONObject) finalRes.get("message");
                                    Log.d(TAG, "run: login--->" + user);
                                    Toast.makeText(Login.this, "Successful", Toast.LENGTH_SHORT).show();

                                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                                    myEdit.putString("auth", user.getString("auth"));
                                    myEdit.putString("user", user.toString());
                                    myEdit.apply();

                                    if(user.getString("auth").equals("admin")){
                                        Intent intent = new Intent(getApplicationContext(), AdminHomepage.class);
                                        startActivity(intent);
                                        finish();
                                    }else{
                                        Intent intent = new Intent(getApplicationContext(), ShowAllComplaints.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                } catch (JSONException e) {
                                    Toast.makeText(Login.this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                } else {
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public void switchUser(View view) {
        studentUser = !studentUser;
        makeChanges();
    }


    private void makeChanges() {
        if (studentUser){
            loginLogo.setText("Student Login");
            et_email.setHint("Enter NITC email to login");
            et_password.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
        }
        else{
            loginLogo.setText("Admin Login");
            et_password.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }
    }


    public void sendOtp(View view) throws Exception{
        sendOTPPB.setVisibility(View.VISIBLE);
        verifiedEmailString = et_email.getText().toString();
        JSONObject loginData = new JSONObject();
        loginData.put("email", et_email.getText().toString());

        OkHttpClient client = new OkHttpClient();
        String url = "https://2uv9f06amb.execute-api.ap-south-1.amazonaws.com/sendEmailOtp";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), loginData.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Login.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Login.this, "Couldn't make the connection", Toast.LENGTH_SHORT).show();
                        sendOTPPB.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    JSONObject res = null;
                    try {
                        res = new JSONObject(myResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        OTP = res.getString("otp");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            verifyEmailText.setVisibility(View.GONE);
                            otpLayout.setVisibility(View.VISIBLE);
                            sendOTPPB.setVisibility(View.GONE);
                        }
                    });

                } else {
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "run: " + response.toString());
                            Toast.makeText(Login.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            sendOTPPB.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

}