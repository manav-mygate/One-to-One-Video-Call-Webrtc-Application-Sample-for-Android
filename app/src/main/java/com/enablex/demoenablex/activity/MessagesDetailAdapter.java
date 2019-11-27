package com.enablex.demoenablex.activity;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.enablex.demoenablex.R;

import java.util.List;

public class MessagesDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LEFT_MESSAGE_VIEW = 0;
    private static final int RIGHT_MESSAGE_VIEW = 1;
    Activity activity;
    private List<DataToUI> items;

    public MessagesDetailAdapter(Activity activity, List<DataToUI> items) {
        this.activity = activity;
        this.items = items;

    }

    @Override
    public int getItemViewType(int position) {
        if (items != null && items.size() > 0) {
            if (items.get(position).isReceivedData()) {
                return LEFT_MESSAGE_VIEW;
            } else {
                return RIGHT_MESSAGE_VIEW;
            }
        } else {
            return -1;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LEFT_MESSAGE_VIEW) {
            View messagesView = LayoutInflater.from(activity).inflate(R.layout.left_chat_ui, null, false);
            return new MessagesAdapterView(messagesView);
        } else if (viewType == RIGHT_MESSAGE_VIEW) {
            View messagesView = LayoutInflater.from(activity).inflate(R.layout.right_chat_ui, null, false);
            return new MessagesAdapterView(messagesView);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessagesAdapterView) {
            ((MessagesAdapterView) holder).setMessagesData(items.get(position).getData(),items.get(position).isReceivedStatus());
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }


    public class MessagesAdapterView extends RecyclerView.ViewHolder {
        public TextView question;
        public TextView status;

        public MessagesAdapterView(View view) {
            super(view);
            question = (TextView) view.findViewById(R.id.textView1);
            status=(TextView) view.findViewById(R.id.status);
        }


        public void setMessagesData(String localMessageObject,boolean isReceived) {
            if (localMessageObject == null) {
                return;
            }
            if (!TextUtils.isEmpty(localMessageObject)) {
                question.setText(localMessageObject);
            }
            if(isReceived){
                status.setText("seen");
            }
        }

    }

    void updateMessageList(DataToUI message) {
        items.add(message);
        //  notifyDataSetChanged();
    }
}