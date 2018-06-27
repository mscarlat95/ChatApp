package com.scarlat.marius.chatapp.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Message;
import com.scarlat.marius.chatapp.storage.SharedPref;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OpportunisticMessagesAdapter extends RecyclerView.Adapter<OpportunisticMessagesAdapter.OpportunisticMessagesViewHolder>{

    private static final String TAG = "OpportunisticMsgAdapter";

    private Context context;
    private List<Message> messages;

    public OpportunisticMessagesAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;

        SharedPref.setup(context);
    }

    @NonNull
    @Override
    public OpportunisticMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_layout, parent, false);

        return new OpportunisticMessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpportunisticMessagesViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        Message message = messages.get(position);

        final String sourceId = message.getFrom();
        final String messageContent = message.getMessage();
        final String timestamp = DateFormat.format("dd-MMM HH:mm", message.getTimestamp()).toString();
        final String fullname = SharedPref.getString(sourceId);

        if (sourceId.equals(SharedPref.getString(Constants.USER_ID))) {
            holder.getMessageTextView().setBackgroundResource(R.drawable.user_message_layout);
        } else {
            holder.getMessageTextView().setBackgroundResource(R.drawable.friend_message_background);
        }

        holder.getFullNameTextView().setText(fullname);
        holder.getMessageTextView().setText(messageContent);
        holder.getTimestampTextView().setText(timestamp);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + messages.size());
        return messages.size();
    }


    class OpportunisticMessagesViewHolder extends RecyclerView.ViewHolder {

        private View rootView;

        private TextView fullNameTextView;
        private CircleImageView profileCircleImageView;
        private TextView messageTextView;
        private TextView timestampTextView;
        private ImageView messageImageView;

        /* Getters and setters */
        View getRootView() { return rootView; }
        CircleImageView getProfileCircleImageView() { return profileCircleImageView; }
        TextView getMessageTextView() { return messageTextView; }
        TextView getTimestampTextView() { return timestampTextView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        ImageView getMessageImageView() { return messageImageView; }


        public OpportunisticMessagesViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            profileCircleImageView = rootView.findViewById(R.id.profileCircleImageView);
            messageTextView = rootView.findViewById(R.id.messageTextView);
            timestampTextView = rootView.findViewById(R.id.timestampTextView);
            messageImageView = rootView.findViewById(R.id.messageImageView);

            messageImageView.setVisibility(View.GONE);
        }
    }
}
