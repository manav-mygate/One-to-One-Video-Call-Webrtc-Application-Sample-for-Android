package com.enablex.demoenablex.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enablex.demoenablex.ApplicationController;
import com.enablex.demoenablex.R;
import com.enablex.demoenablex.web_communication.WebCall;
import com.enablex.demoenablex.web_communication.WebConstants;
import com.enablex.demoenablex.web_communication.WebResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import static com.enablex.demoenablex.web_communication.WebConstants.SendToOPPO;
import static com.enablex.demoenablex.web_communication.WebConstants.sendToRedmiGo;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, WebResponse {
    private EditText name;
    private EditText roomId;
    private Button joinRoom;
    private Button createRoom;
    private Button audioCall;
    private String token;
    private SharedPreferences sharedPreferences;
    private String room_Id;
    private Button chat;
    private int type;
    static final int AUDIO_CALL = 1;
    static final int VIDEO_CALl = 2;
    static final int MESSAGE = 3;
    public static final String TAG = "DashboardActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getSupportActionBar().setTitle(R.string.app_name);
        sharedPreferences = ApplicationController.getSharedPrefs();
        setView();
        setClickListener();
        getSupportActionBar().setTitle(R.string.app_name);
        setSharedPreference();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        // Log and toast
                        Log.d(TAG, token);
                        saveFCMID(token);
                        Toast.makeText(DashboardActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFCMID(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fcm_id", token);
        editor.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createRoom:

                // new WebCall(this, this, null, WebConstants.getRoomId, WebConstants.getRoomIdCode, false, true).execute();
                type = VIDEO_CALl;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_id", sendToRedmiGo);
                    jsonObject.put("type",VIDEO_CALl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new WebCall(this, this, jsonObject, WebConstants.createToken, WebConstants.getToken, false, true).execute();

                break;
            case R.id.joinRoom:
                room_Id = roomId.getText().toString();
                if (validations()) {
                    type = VIDEO_CALl;
                    validateRoomIDWebCall();
                }
                break;

            case R.id.chat:
               /* room_Id = roomId.getText().toString();
                if (validations()) {
                    type = MESSAGE;
                    validateRoomIDWebCall();
                }*/
                type = MESSAGE;
                JSONObject jsonObject1 = new JSONObject();
                try {
                    jsonObject1.put("user_id", sendToRedmiGo);
                    jsonObject1.put("type",MESSAGE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new WebCall(this, this, jsonObject1, WebConstants.createToken, WebConstants.chatConst, false, true).execute();
                break;

            case R.id.audio_call:
                /*room_Id = roomId.getText().toString();
                if (validations()) {
                    type = AUDIO_CALL;
                    validateRoomIDWebCall();
                }*/
                type = AUDIO_CALL;
                JSONObject jsonObject2 = new JSONObject();
                try {
                    jsonObject2.put("user_id", sendToRedmiGo);
                    jsonObject2.put("type",AUDIO_CALL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new WebCall(this, this, jsonObject2, WebConstants.createToken, WebConstants.AudioCall, false, true).execute();
                break;
        }
    }

    private boolean validations() {
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Enter name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (roomId.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please create Room Id.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validateRoomIDWebCall() {
        new WebCall(this, this, null, WebConstants.validateRoomId + room_Id, WebConstants.validateRoomIdCode, true, false).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
//            menuBuilder.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                if (!roomId.getText().toString().equalsIgnoreCase("")) {
                    String shareBody = "Hi,\n" + name.getText().toString() + " has invited you to join room with Room Id " + roomId.getText().toString();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(sharingIntent);
                } else {
                    Toast.makeText(this, "Please create Room first.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWebResponse(String response, int callCode) {
        switch (callCode) {
            case WebConstants.getRoomIdCode:
                onGetRoomIdSuccess(response);
                break;
            case WebConstants.getTokenURLCode:
                onGetTokenSuccess(response);
                break;
            case WebConstants.validateRoomIdCode:
                onVaidateRoomIdSuccess(response);
                break;

            case WebConstants.getToken:
                onGetTokenSuccess(response);
                break;

            case WebConstants.chatConst:
                onGetTokenSuccess(response);
                break;

            case WebConstants.AudioCall:
                onGetTokenSuccess(response);
                break;


        }

    }

    private void onVaidateRoomIdSuccess(String response) {
        Log.e("responsevalidate", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").trim().equalsIgnoreCase("40001")) {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            } else {
                savePreferences();
                getRoomTokenWebCall();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onGetTokenSuccess(String response) {
        Log.e("responseToken", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("token")) {
                token = jsonObject.optString("token");
                Log.e("token", token);
                Intent intent = null;
                if (type == VIDEO_CALl) {
                    intent = new Intent(DashboardActivity.this, VideoConferenceActivity.class);
                } else if (type == MESSAGE) {
                    intent = new Intent(DashboardActivity.this, ChatActivity.class);

                } else if(type==AUDIO_CALL) {
                    intent = new Intent(DashboardActivity.this, AudioCall.class);
                } else{
                    intent = new Intent(DashboardActivity.this, ChatActivity.class);

                }
                intent.putExtra("token", token);
                intent.putExtra("name", name.getText().toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetRoomIdSuccess(String response) {
        Log.e("responseDashboard", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            room_Id = jsonObject.optJSONObject("room").optString("room_id");
        } catch (JSONException e) {

            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomId.setText(room_Id);
            }
        });
    }

    @Override
    public void onWebResponseError(String error, int callCode) {
        Log.e("errorDashboard", error);
    }

    private void setSharedPreference() {
        if (sharedPreferences != null) {
            if (!sharedPreferences.getString("name", "").isEmpty()) {
                name.setText(sharedPreferences.getString("name", ""));
            }
            if (!sharedPreferences.getString("room_id", "").isEmpty()) {
                roomId.setText(sharedPreferences.getString("room_id", ""));
            }
        }
    }

    private void setClickListener() {
        createRoom.setOnClickListener(this);
        joinRoom.setOnClickListener(this);
        chat.setOnClickListener(this);
        audioCall.setOnClickListener(this);
    }

    private void setView() {
        name = (EditText) findViewById(R.id.name);
        roomId = (EditText) findViewById(R.id.roomId);
        createRoom = (Button) findViewById(R.id.createRoom);
        joinRoom = (Button) findViewById(R.id.joinRoom);
        chat = (Button) findViewById(R.id.chat);
        audioCall = (Button) findViewById(R.id.audio_call);
    }

    private JSONObject jsonObjectToSend() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Test Dev Room");
            jsonObject.put("settings", getSettingsObject());
            jsonObject.put("data", getDataObject());
            jsonObject.put("sip", getSIPObject());
            jsonObject.put("owner_ref", "fadaADADAAee");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSIPObject() {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    private JSONObject getDataObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSettingsObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", "Testing");
            jsonObject.put("scheduled", false);
            jsonObject.put("scheduled_time", "");
            jsonObject.put("duration", 50);
            jsonObject.put("participants", 10);
            jsonObject.put("billing_code", 1234);
            jsonObject.put("auto_recording", false);
            jsonObject.put("active_talker", true);
            jsonObject.put("quality", "HD");
            jsonObject.put("wait_moderator", false);
            jsonObject.put("adhoc", false);
            jsonObject.put("mode", "group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getRoomTokenWebCall() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
            jsonObject.put("role", "participant");
            jsonObject.put("user_ref", "2236");
            jsonObject.put("roomId", room_Id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!name.getText().toString().isEmpty() && !roomId.getText().toString().isEmpty()) {
            new WebCall(this, this, jsonObject, WebConstants.getTokenURL, WebConstants.getTokenURLCode, false, false).execute();
        }
    }


    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name.getText().toString());
        editor.putString("room_id", room_Id);
        editor.commit();

    }
}
