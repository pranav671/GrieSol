package com.example.android.griesol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        try {
            invokeLambdaSubscription(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void invokeLambdaSubscription(String s) throws Exception{
        JSONObject tokenData = new JSONObject();
        tokenData.put("token", s);

        OkHttpClient client = new OkHttpClient();
        String url = " https://bq0zyc5q11.execute-api.ap-south-1.amazonaws.com/addSubscriptionGrieSol";
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), tokenData.toString());
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
            }
        });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("default"));

        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String auth = sh.getString("auth", "");
        if (auth.equals("") || auth.equals("admin")){
            return;
        }

        JSONObject jsonObject, complaintObj = null;
        String purpose = "";
        try {
            jsonObject = new JSONObject(remoteMessage.getData().get("default"));
            complaintObj = jsonObject.getJSONObject("complaint");
            purpose = jsonObject.getString("purpose");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(purpose.equals("update") && auth.equals("student")){
            try {
                String roll = sh.getString("roll","");
                if(!roll.equals(complaintObj.getString("roll"))){
                    return;
                }
                createNoti("Complaint status updated","Your complaint is "+complaintObj.getString("status"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else if(purpose.equals("add") && auth.equals("dept")){
            try {
                JSONObject user = new JSONObject(sh.getString("user",""));
                String department = user.getString("department");
                if(!department.equals(complaintObj.getString("department"))){
                    return;
                }
                createNoti("Complaint added","A complaint is added to your department");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void createNoti(String title, String detail) {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.gsoljpg)
                .setContentTitle(title)
                .setContentText(detail).setAutoCancel(true).setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }
}