package com.enablex.demoenablex.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.web_communication.WebCall;
import com.enablex.demoenablex.web_communication.WebConstants;
import com.enablex.demoenablex.web_communication.WebResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;

import static com.enablex.demoenablex.web_communication.WebConstants.AUDIO_CALL;
import static com.enablex.demoenablex.web_communication.WebConstants.SendToOPPO;
import static com.enablex.demoenablex.web_communication.WebConstants.VIDEO_CALl;

public class ChatActivitityForFCM extends AppCompatActivity implements EnxRoomObserver, EnxReconnectObserver, EnxStreamObserver, WebResponse {

    private RecyclerView recyclerView;
    private MessagesDetailAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ImageView imageView;
    EditText editText;
    ProgressDialog progressDialog;
    EnxRoom enxRooms;
    EnxRtc enxRtc;
    EnxStream localStream;
    LinearLayout llChat;
    LinearLayout chatLayout;
    ImageView video,audio;
    HashMap<Integer, DataToUI> map = new HashMap<>();
    HashMap<Integer, Integer> idMapping = new HashMap<>();
    int type;

    String token;
    String name;
    boolean isForeGround;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_fcm);
        getPreviousIntent();
        enxRtc = new EnxRtc(this, this, null);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObject(), getReconnectInfo(), null);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        imageView = (ImageView) findViewById(R.id.btn_send);
        editText = (EditText) findViewById(R.id.edit_text);
        llChat=(LinearLayout) findViewById(R.id.ll_chat);
        audio=(ImageView) findViewById(R.id.audio);
        video=(ImageView) findViewById(R.id.video);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        chatLayout = (LinearLayout) findViewById(R.id.test_layout);


        progressDialog = new ProgressDialog(this);

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type=AUDIO_CALL;
                if (localStream != null) {
                    localStream.detachRenderer();
                }
                if (enxRooms != null) {
                    enxRooms = null;
                }
                if (enxRtc != null) {
                    enxRtc = null;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_id", SendToOPPO);
                    jsonObject.put("type",AUDIO_CALL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new WebCall(ChatActivitityForFCM.this, ChatActivitityForFCM.this, jsonObject, WebConstants.createToken, WebConstants.getToken, false, true).execute();

            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type=VIDEO_CALl;
                if (localStream != null) {
                    localStream.detachRenderer();
                }
                if (enxRooms != null) {
                    enxRooms = null;
                }
                if (enxRtc != null) {
                    enxRtc = null;
                }
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("user_id", SendToOPPO);
                    jsonObject.put("type",VIDEO_CALl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new WebCall(ChatActivitityForFCM.this, ChatActivitityForFCM.this, jsonObject, WebConstants.createToken, WebConstants.getToken, false, true).execute();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    DataToUI dataToUI = new DataToUI();
                    dataToUI.setData(editText.getText().toString());
                    dataToUI.setReceivedStatus(false);
                    JSONArray jsonArray = enxRooms.getUserList();
                    dataToUI.setReceivedData(false);
                    try {
                        ArrayList<String> arrayList = new ArrayList<>();
                      //  arrayList.add(jsonArray.getJSONObject(0).get("clientId").toString());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("message", editText.getText().toString());
                        jsonObject.put("isSignal", false);

                        if (mAdapter == null) {
                            ArrayList<DataToUI> dataToUIS = new ArrayList<>();
                            dataToUIS.add(dataToUI);
                            mAdapter = new MessagesDetailAdapter(ChatActivitityForFCM.this, dataToUIS);
                            recyclerView.setAdapter(mAdapter);
                        } else {
                            updateAdapter(dataToUI);
                        }
                        int id = createID();
                        jsonObject.put("id", createID());
                        enxRooms.sendUserData(jsonObject, true, arrayList);

                        map.put(id, dataToUI);
                        idMapping.put(id, mAdapter.getItemCount());
                        //         }
                        //    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(ChatActivitityForFCM.this, "message cannot be empty", Toast.LENGTH_LONG).show();
                }
                editText.setText("");
            }
        });
    }

    @Override
    public void onReconnect(String s) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
        try {
            if (s.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        enxRooms = enxRoom;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(enxRooms.getClientName());
        }
        if (enxRooms != null) {
            enxRooms.publish(localStream);
            enxRooms.setReconnectObserver(this);
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {

    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {

    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {

    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {

    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {

    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {

    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {

    }

    @Override
    public void onRoomDisConnected(JSONObject jsonObject) {

    }

    @Override
    public void onActiveTalkerList(JSONObject jsonObject) {

    }

    @Override
    public void onEventError(JSONObject jsonObject) {

    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {

    }

    @Override
    public void onNotifyDeviceUpdate(String s) {

    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {
        Log.d("Chat Activity", jsonObject.toString());
    }


    public ChatActivitityForFCM() {
        super();
    }


    @Override
    public void onMessageReceived(JSONObject jsonObject) {
        try {
/*
            {"broadcast":true,"sender":"Android 2","senderId":"c7c7d7ea-154c-467e-8ee7-3194ad94d2f9","type":"user_data","message":"{\"message\":\"hjjj\",\"isSignal\":false,\"id\":21161140}","timestamp":1574332901910}
*/
            String jsonObject1 = jsonObject.getString("message");
            JSONObject message = new JSONObject(jsonObject1);
            Log.d("ChatActivity", String.valueOf(message.optBoolean("isForeground")));
            int ids = message.has("id")?message.getInt("id"):0;
            if (message.has("isSignal") && message.getBoolean("isSignal")) {
                DataToUI dtu = map.get(ids);
                if (message.optBoolean("isForeground")) {
                    dtu.setReceivedStatus(true);

                } else {
                    dtu.setReceivedStatus(false);
                }
                //  if (idMapping.containsKey(ids)) {
                //mAdapter.notifyItemChanged(idMapping.get(ids), dtu);
                mAdapter.notifyDataSetChanged();
                //  }

            } else {
                String textMessage = message.getString("message");
                DataToUI dataToUI = new DataToUI();
                dataToUI.setData(textMessage);
                dataToUI.setReceivedData(true);
                dataToUI.setReceivedStatus(false);
                updateAdapter(dataToUI);
                Log.d("onReceivedChatData", textMessage);
                ArrayList<String> arrayList = new ArrayList<>();
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("isSignal", true);
                jsonObject2.put("id", ids);
                if (llChat != null && !isVisible(llChat)) {
                    jsonObject2.put("isForeground", false);
                } else {
                    jsonObject2.put("isForeground", true);
                }
                int idw = createID();
                dataToUI.setId(idw);
                enxRooms.sendUserData(jsonObject2, true, arrayList);
                map.put(idw, dataToUI);
                //  idMapping.put(idw, mAdapter.getItemCount());


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUserDataReceived(JSONObject jsonObject) {

    }

    @Override
    public void onSwitchedUserRole(JSONObject jsonObject) {

    }

    @Override
    public void onUserRoleChanged(JSONObject jsonObject) {

    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    private JSONObject getLocalStreamJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audio", false);
            jsonObject.put("video", false);
            jsonObject.put("data", true);
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect", true);
            jsonObject.put("number_of_attempts", 3);
            jsonObject.put("timeout_interval", 15);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localStream != null) {
            localStream.detachRenderer();
        }
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {

    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {

    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {

    }

    private void updateAdapter(DataToUI message) {
        if (mAdapter == null) {
            ArrayList<DataToUI> dataToUIS = new ArrayList<>();
            dataToUIS.add(message);
            mAdapter = new MessagesDetailAdapter(ChatActivitityForFCM.this, dataToUIS);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.updateMessageList(message);
            recyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            mAdapter.notifyDataSetChanged();
        }
    }

    public int createID() {
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss", Locale.US).format(now));
        return id;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeGround = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isForeGround = true;
    }

    public boolean isVisible(final View view) {
        if (view == null) {
            return false;
        }
        if (!view.isShown()) {
            return false;
        }
        final Rect actualPosition = new Rect();
        view.getGlobalVisibleRect(actualPosition);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        final Rect screen = new Rect(0, 0, width, height);
        return actualPosition.intersect(screen);
    }

    @Override
    public void onWebResponse(String response, int callCode) {
        switch (callCode) {

            case WebConstants.getToken:
                onGetTokenSuccess(response);
                break;


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
                    intent = new Intent(ChatActivitityForFCM.this, VideoConferenceActivity.class);
                }  else if(type==AUDIO_CALL) {
                    intent = new Intent(ChatActivitityForFCM.this, AudioCall.class);
                }
                intent.putExtra("token", token);
                intent.putExtra("name", name);
                startActivity(intent);
            } else {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebResponseError(String error, int callCode) {
        Log.e("errorDashboard", error);
    }
}

