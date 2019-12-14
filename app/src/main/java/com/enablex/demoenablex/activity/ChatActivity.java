package com.enablex.demoenablex.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.utilities.OnDragTouchListener;
import com.enablex.demoenablex.web_communication.WebConstants;
import com.enablex.demoenablex.web_communication.WebResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import enx_rtc_android.Controller.EnxBandwidthObserver;
import enx_rtc_android.Controller.EnxLogsUtil;
import enx_rtc_android.Controller.EnxPlayerStatsObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStatsObserver;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;

import static com.enablex.demoenablex.web_communication.WebConstants.AUDIO_CALL;
import static com.enablex.demoenablex.web_communication.WebConstants.VIDEO_CALl;

public class ChatActivity extends AppCompatActivity implements EnxRoomObserver, EnxReconnectObserver, EnxStreamObserver, WebResponse, EnxStatsObserver, EnxPlayerStatsObserver, EnxBandwidthObserver {

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
    TextView topText;
    ImageView video, audio;
    Button audVid, vid, disconn,muteAudio;
    View view;
    View audioView;
    HashMap<Integer, DataToUI> map = new HashMap<>();
    HashMap<Integer, Integer> idMapping = new HashMap<>();
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    EnxPlayerView enxPlayerViewRemote;
    Toolbar toolbar;
    TextView status;
    int type;
    boolean isAudioMuted=false;

    String token;
    String name;
    boolean isForeGround;
    private boolean isfcm;
    ImageView disc;
    EnxStream activetalkerStream;

    TextView selfPeerConnectionValue,activeTalkerStatValue,localStatValue;
    JSONObject playerOptions;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);
        getPreviousIntent();
        getSupportActionBar().hide();


        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        imageView = (ImageView) findViewById(R.id.btn_send);
        editText = (EditText) findViewById(R.id.edit_text);
        llChat = (LinearLayout) findViewById(R.id.ll_chat);
        audio = (ImageView) findViewById(R.id.audio);
        video = (ImageView) findViewById(R.id.video);
        view = (View) findViewById(R.id.videoConf);
        audVid = (Button) findViewById(R.id.aud_vid);
        vid = (Button) findViewById(R.id.vid);
        disconn = (Button) findViewById(R.id.disconn);
        audioView = (View) findViewById(R.id.audioView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        topText = (TextView) findViewById(R.id.toptext);
        disc = (ImageView) findViewById(R.id.dis);
        status = (TextView) findViewById(R.id.status);
        status = (TextView) findViewById(R.id.status);
        status = (TextView) findViewById(R.id.status);
        status = (TextView) findViewById(R.id.status);
        muteAudio=(Button) findViewById(R.id.muteAudio);


        selfPeerConnectionValue = (TextView) findViewById(R.id.selfPeerConnectionValue);
        activeTalkerStatValue = (TextView) findViewById(R.id.activeTalkerStatValue);
        localStatValue = (TextView) findViewById(R.id.localStatValue);

        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);

        moderator.setOnTouchListener(new OnDragTouchListener(moderator));

        enxRtc = new EnxRtc(this, this, null);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObject(), getReconnectInfo(), null);
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_FILL, true);
        playerOptions = getPlayerOptions(true, "#ff669900", 15, 1,
                "#00FFFFFF");
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        chatLayout = (LinearLayout) findViewById(R.id.test_layout);


        progressDialog = new ProgressDialog(this);

        if (isfcm) {
            audVid.setVisibility(View.VISIBLE);
            vid.setVisibility(View.VISIBLE);
        } else {
            audVid.setVisibility(View.GONE);
            vid.setVisibility(View.GONE);
        }

        audVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localStream.muteSelfVideo(false);
                localStream.muteSelfAudio(false);
            }
        });

        vid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localStream.muteSelfVideo(true);
                localStream.muteSelfAudio(false);
            }
        });

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   toolbar.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
                audioView.setVisibility(View.VISIBLE);
                hideSoftKeyboard();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("message", "mgc-audio");
                    enxRooms.sendMessage(jsonObject.toString(), true, null);
                    if(enxRooms!=null) {
                        enxRooms.publish(localStream);
                    }
                    localStream.muteSelfAudio(false);
                    localStream.muteSelfVideo(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        disconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*roomDisconnect();
                finish();*/
                toolbar.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.GONE);
                view.setVisibility(View.GONE);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("message", "mgc-endcall");
                    if(enxRooms!=null) {
                        enxRooms.unpublish();
                    }
                    localStream.muteSelfAudio(true);
                    localStream.muteSelfVideo(true);

                    enxRooms.sendMessage(jsonObject.toString(), true, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        muteAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(true);
                        isAudioMuted=true;
                        muteAudio.setText("unmute");
                    } else {
                        isAudioMuted=false;
                        muteAudio.setText("mute");
                        localStream.muteSelfAudio(false);
                    }
                }
            }
        });

        disc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*roomDisconnect();
                finish()*/
                ;
                toolbar.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.GONE);
                view.setVisibility(View.GONE);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("message", "mgc-endcall");
                    if(enxRooms!=null) {
                        enxRooms.unpublish();
                    }
                    localStream.muteSelfAudio(true);
                    localStream.muteSelfVideo(true);

                    enxRooms.sendMessage(jsonObject.toString(), true, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    toolbar.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.GONE);
                hideSoftKeyboard();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("message", "mgc-video");
                    localStream.muteSelfAudio(false);
                    localStream.muteSelfVideo(false);
                    if(enxRooms!=null) {
                        enxRooms.publish(localStream);
                    }
                    enxRooms.sendMessage(jsonObject.toString(), true, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    DataToUI dataToUI = new DataToUI();
                    dataToUI.setData(editText.getText().toString());
                    dataToUI.setReceivedStatus(false);
                    // JSONArray jsonArray = enxRooms.getUserList();
                    dataToUI.setReceivedData(false);
                    try {
                        ArrayList<String> arrayList = new ArrayList<>();
                        //arrayList.add(jsonArray.getJSONObject(0).get("clientId").toString());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("message", editText.getText().toString());
                        jsonObject.put("isSignal", false);

                        if (mAdapter == null) {
                            ArrayList<DataToUI> dataToUIS = new ArrayList<>();
                            dataToUIS.add(dataToUI);
                            mAdapter = new MessagesDetailAdapter(ChatActivity.this, dataToUIS);
                            recyclerView.setAdapter(mAdapter);
                        } else {
                            updateAdapter(dataToUI);
                        }
                        int id = createID();
                        jsonObject.put("id", createID());
                        enxRooms.sendMessage(jsonObject.toString(), true, arrayList);

                        map.put(id, dataToUI);
                        idMapping.put(id, mAdapter.getItemCount());
                        //         }
                        //    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(ChatActivity.this, "message cannot be empty", Toast.LENGTH_LONG).show();
                }
                editText.setText("");
            }
        });
    }

    @Override
    public void onReconnect(String s) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
      /*  try {
            if (s.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {*/
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        status.setText("Reconnecting");
         /*   }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        /*if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }*/
        Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
        status.setText("connected");
        enxPlayerViewRemote = null;
        View temp = participant.getChildAt(0);
        participant.removeView(temp);

    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        enxRooms = enxRoom;
//        enxRooms.enableStats(true,this);
        enxRooms.setBandwidthObserver(this);
//        enxPlayerView.enablePlayerStats(true,this);

       /* if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(enxRooms.getClientName());
        }*/
        Toast.makeText(this, "roomConnected", Toast.LENGTH_SHORT).show();
        EnxLogsUtil enxLogsUtil = EnxLogsUtil.getInstance();
        enxLogsUtil.enableLogs(false); // To disable logging
        status.setText("connected");
        topText.setText(enxRoom.getClientName());
        if (enxRooms != null) {
            enxRooms.setReconnectObserver(this);
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        Toast.makeText(this, "room connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {

    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        roomDisconnect();

    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
        enxRooms.enableStats(true,this);
        enxPlayerView.enablePlayerStats(true,this);
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
        this.finish();
    }

    @Override
    public void onActiveTalkerList(JSONObject jsonObject) {
        //received when Active talker update happens
        Log.d("onActiveTalkerList", jsonObject.toString());
        try {
            Map<String, EnxStream> map = enxRooms.getRemoteStreams();
            JSONArray jsonArray = jsonObject.getJSONArray("activeList");
            if (jsonArray.length() == 0) {
                View temp = participant.getChildAt(0);
                participant.removeView(temp);
                return;
            } else {
                JSONObject jsonStreamid = jsonArray.getJSONObject(0);
                String streamID = jsonStreamid.getString("streamId");
                activetalkerStream = map.get(streamID);

                if (enxPlayerViewRemote == null) {
                    enxPlayerViewRemote = new EnxPlayerView(ChatActivity.this, EnxPlayerView.ScalingType.SCALE_ASPECT_FILL, false);
                    activetalkerStream.attachRenderer(enxPlayerViewRemote);
                    participant.addView(enxPlayerViewRemote);


                    enxPlayerViewRemote.setConfigureOption(playerOptions);
                }
                enxPlayerViewRemote.enablePlayerStats(true, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEventError(JSONObject jsonObject) {
        Log.d("onEventError", jsonObject.toString());
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

    @Override
    public void onMessageReceived(JSONObject jsonObject) {
        try {
/*
            {"broadcast":true,"sender":"Android 2","senderId":"c7c7d7ea-154c-467e-8ee7-3194ad94d2f9","type":"user_data","message":"{\"message\":\"hjjj\",\"isSignal\":false,\"id\":21161140}","timestamp":1574332901910}
*/
            Log.d("CHAT Activty", jsonObject.toString());
            String jsonObject1 = jsonObject.getString("message");
            JSONObject message = new JSONObject(jsonObject1);
            if (message.has("message") && message.get("message").equals("mgc-video")) {
                if (isfcm) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Click on Aud/vid button below to share your video and audio or click on audio to share just your audio with the caller")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                }
                            })
                            .setNegativeButton("Chat Only", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    toolbar.setVisibility(View.VISIBLE);
                                    audioView.setVisibility(View.GONE);
                                    view.setVisibility(View.GONE);

                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("message", "mgc-endcall");
                                        if(enxRooms!= null) {
                                            enxRooms.unpublish();
                                        }
                                        localStream.muteSelfAudio(true);
                                        localStream.muteSelfVideo(true);

                                        enxRooms.sendMessage(jsonObject.toString(), true, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                view.setVisibility(View.VISIBLE);
                audioView.setVisibility(View.GONE);
                hideSoftKeyboard();
                //   toolbar.setVisibility(View.GONE);
                if(enxRooms!=null) {
                    enxRooms.publish(localStream);
                }
                return;
            }
            if (message.has("message") && message.get("message").equals("mgc-audio")) {
                if (isfcm) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Click on Aud/vid button below to share your video and audio or click on audio to share just your audio with the caller")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                }
                            })
                            .setNegativeButton("Chat Only", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    toolbar.setVisibility(View.VISIBLE);
                                    audioView.setVisibility(View.GONE);
                                    view.setVisibility(View.GONE);

                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("message", "mgc-endcall");
                                        if(enxRooms!=null) {
                                            enxRooms.unpublish();
                                        }
                                        localStream.muteSelfAudio(true);
                                        localStream.muteSelfVideo(true);

                                        enxRooms.sendMessage(jsonObject.toString(), true, null);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                audioView.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                //    toolbar.setVisibility(View.GONE);
                hideSoftKeyboard();
                if(enxRooms!=null) {
                    enxRooms.publish(localStream);
                }
                localStream.muteSelfAudio(false);
                localStream.muteSelfVideo(true);

                return;
            }

            if (message.has("message") && message.get("message").equals("mgc-endcall")) {
                audioView.setVisibility(View.GONE);
                view.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                // enxRooms.publish(localStream);
                if(enxRooms!=null) {
                    enxRooms.unpublish();
                }
                localStream.muteSelfVideo(true);
                localStream.muteSelfAudio(true);
                return;
            }
            Log.d("ChatActivity", String.valueOf(message.optBoolean("isForeground")));
            int ids = message.has("id") ? message.getInt("id") : 0;
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
                //        enxRooms.sendMessage(jsonObject2.toString(), true, arrayList); //TODO for signalling
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
            if (getIntent().hasExtra("IsFcm"))
                isfcm = getIntent().getBooleanExtra("IsFcm", false);
        }
    }

    private JSONObject getLocalStreamJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", true);
            jsonObject.put("maxVideoBW", 600); //2048
            jsonObject.put("minVideoBW", 40);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 45);
            videoSize.put("maxVideoLayers",1);
            videoSize.put("minHeight", 80);
            videoSize.put("maxWidth", 45);
            videoSize.put("maxHeight", 80);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", true);
            jsonObject.put("videoMuted", true);
            jsonObject.put("name", "android 1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getPlayerOptions(boolean enable, String textcolor,
                                        int textsize, int textstyle, String text_background){
        JSONObject jsonObject = new JSONObject();
        try{

            JSONObject overlay = new JSONObject();
            overlay.put("enable",enable);

            JSONObject properties = new JSONObject();
            properties.put("textColor",textcolor);
            properties.put("textSize",textsize);
            properties.put("textStyle",textstyle);
            properties.put("backgroundColor",text_background);

            overlay.put("properties",properties);
            jsonObject.put("overlay",overlay);

        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect", true);
            jsonObject.put("number_of_attempts", 5);
            jsonObject.put("timeout_interval", 5000);
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
        Log.d("onRemoteStreamAudioMute", jsonObject.toString());

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
            mAdapter = new MessagesDetailAdapter(ChatActivity.this, dataToUIS);
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
                    intent = new Intent(ChatActivity.this, VideoConferenceActivity.class);
                } else if (type == AUDIO_CALL) {
                    intent = new Intent(ChatActivity.this, AudioCall.class);
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

    private void roomDisconnect() {
        if (enxRooms != null) {
            if (enxPlayerView != null) {
                enxPlayerView.release();
                enxPlayerView = null;
            }
            if (enxPlayerViewRemote != null) {
                enxPlayerViewRemote.release();
                enxPlayerViewRemote = null;
            }
            enxRooms.disconnect();
        } else {
            this.finish();
        }
    }

    void onClick() {
        roomDisconnect();
        finish();
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    @Override
    public void onAcknowledgeStats(JSONObject jsonObject) {
        Log.d("onAcknowledgeStats",jsonObject.toString());
    }

    @Override
    public void onReceivedStats(JSONObject jsonObject) {
        Log.d("onReceivedStats",jsonObject.toString());

    }

    @Override
    public void onPlayerStats(JSONObject jsonObject) {
        Log.d("onPlayerStats",jsonObject.toString());
        Iterator<String> iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = iter.next();

            try {
                if (key.equalsIgnoreCase(localStream.getId())) {
                    String allstats = "Total:";
                    JSONObject stats = ((JSONObject) (jsonObject.getJSONArray(localStream.getId())).get(0));
                    if (stats.has("total")) {
                        JSONObject senderStatsAudioJson = stats.getJSONObject("total");
                        String totalbitRate = senderStatsAudioJson.getString("bitrateCalculated");
                        allstats = allstats + ": " + Integer.parseInt(totalbitRate)/1000 + " Kbps\n";
                    }

                    if (stats.has("audioStats")) {
                        JSONObject senderStatsAudioJson = stats.getJSONObject("audioStats");
                        String audiobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                        allstats = allstats + "Audio" + ": " + Integer.parseInt(audiobitRate)/1000 + " Kbps\n";
                    }

                    if (stats.has("videoStats")) {
                        JSONObject senderStatsAudioJson = stats.getJSONObject("videoStats");
                        String videobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                        allstats = allstats + "Video" + ": " + Integer.parseInt(videobitRate)/1000 + " Kbps";
                    }

                    localStatValue.setText(allstats);
                }else {

                    if(!key.equalsIgnoreCase(activetalkerStream.getId())){
                        return;
                    }
                    for (int i = 0; i < jsonObject.getJSONArray(activetalkerStream.getId()).length(); i++){
                        JSONObject statsObject = ((JSONObject) (jsonObject.getJSONArray(activetalkerStream.getId())).getJSONObject(i));
                        String streamType = statsObject.getString("streamType");
                        if (streamType.equalsIgnoreCase("selfPcStat")) {
                            String allselfPcStat = "Total:";
                            if (statsObject.has("total")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("total");
                                String totalbitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allselfPcStat = allselfPcStat + ": " + Integer.parseInt(totalbitRate)/1000 + " Kbps\n";
                            }

                            if (statsObject.has("audioStats")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("audioStats");
                                String audiobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allselfPcStat = allselfPcStat + "Audio" + ": " + Integer.parseInt(audiobitRate)/1000 + " Kbps\n";
                            }

                            if (statsObject.has("videoStats")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("videoStats");
                                String videobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allselfPcStat = allselfPcStat + "Video" + ": " + Integer.parseInt(videobitRate)/1000 + " Kbps";
                            }

                            selfPeerConnectionValue.setText(allselfPcStat);
                        }else {
                            String allActStat = "Total:";
                            if (statsObject.has("total")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("total");
                                String totalbitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allActStat = allActStat + ": " + Integer.parseInt(totalbitRate)/1000 + " Kbps\n";
                            }

                            if (statsObject.has("audioStats")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("audioStats");
                                String audiobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allActStat = allActStat + "Audio" + ": " + Integer.parseInt(audiobitRate)/1000 + " Kbps\n";
                            }

                            if (statsObject.has("videoStats")) {
                                JSONObject senderStatsAudioJson = statsObject.getJSONObject("videoStats");
                                String videobitRate = senderStatsAudioJson.getString("bitrateCalculated");
                                allActStat = allActStat + "Video" + ": " + Integer.parseInt(videobitRate)/1000 + " Kbps";
                            }

                            activeTalkerStatValue.setText(allActStat);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBandWidthUpdated(JSONArray jsonArray) {
        Toast.makeText(this,jsonArray.toString(),Toast.LENGTH_LONG).show();
        Log.d("onBandWidthUpdated",jsonArray.toString());

    }

    @Override
    public void onShareStreamEvent(JSONObject jsonObject) {
        Log.d("onShareStreamEvent",jsonObject.toString());


    }

    @Override
    public void onCanvasStreamEvent(JSONObject jsonObject) {
        Log.d("onCanvasStreamEvent",jsonObject.toString());

    }
}
