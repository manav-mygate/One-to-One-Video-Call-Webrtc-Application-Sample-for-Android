package com.enablex.demoenablex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.enablex.demoenablex.activity.AudioCall;
import com.enablex.demoenablex.activity.ChatActivitityForFCM;
import com.enablex.demoenablex.activity.ChatActivity;
import com.enablex.demoenablex.activity.VideoConferenceActivity;
import com.enablex.demoenablex.web_communication.WebConstants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import static com.enablex.demoenablex.activity.DashboardActivity.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, remoteMessage.getNotification().getBody().toString());

        Intent intent = null;
      /*  if (remoteMessage.getNotification().getTitle().equals("2")){
            intent = new Intent(this, VideoConferenceActivity.class);
        } else if (remoteMessage.getNotification().getTitle().equals("3")) {
            intent = new Intent(this, ChatActivity.class);
        } else if(remoteMessage.getNotification().getTitle().equals("1")) {
            intent = new Intent(this, AudioCall.class);
        } else{*/
            intent = new Intent(this, ChatActivity.class);

     //   }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            JSONObject jsonObject = new JSONObject(remoteMessage.getNotification().getBody());
            intent.putExtra("token", jsonObject.getString("token"));
            intent.putExtra("name", "Android 2");
            intent.putExtra("IsFcm",true);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        SharedPreferences sharedPreferences = ApplicationController.getSharedPrefs();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fcm_id", s);
        editor.commit();
    }
}
