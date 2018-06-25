package com.scarlat.marius.chatapp.adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Message;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>{

    private static final String TAG = "MessageListAdapter";

    private Context context;
    private List<Message> messages;

    public MessageListAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_layout, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        Message message = messages.get(position);

        final String content = message.getMessage();
        final String source = message.getFrom();
        final String type = message.getType();
        final long timestamp = message.getTimestamp();

         /* Different message content layout for user and friend */
        if (source.equals(FirebaseAuth.getInstance().getUid())) {
            holder.getMessageTextView().setBackgroundResource(R.drawable.user_message_layout);
        } else {
            holder.getMessageTextView().setBackgroundResource(R.drawable.friend_message_background);
        }

        /* Display message content */
        switch (type) {
            case Constants.MESSAGE_TYPE_TEXT:
                Log.d(TAG, "onBindViewHolder: Text content> " + content);
                holder.getMessageImageView().setVisibility(View.GONE);
                holder.getMessageTextView().setVisibility(View.VISIBLE);
                holder.getMessageTextView().setText(content);
                break;
            case Constants.MESSAGE_TYPE_IMAGE:
                Log.d(TAG, "onBindViewHolder: Image content> " + content);
                if (!((Activity) context).isDestroyed()) {
                    holder.getMessageTextView().setVisibility(View.GONE);
                    holder.getMessageImageView().setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(content)
                            .into(holder.getMessageImageView());
                }
                break;
            default:
                Log.d(TAG, "Undefined message type = " + type);
                break;
        }

        /* Set timestamp */
        holder.getTimestampTextView().setText(DateFormat.format("dd-MMM HH:mm", timestamp));

        /* Display user info: profile image and name */
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(source)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                            final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();

                            if (!((Activity)context).isDestroyed()) {
                                holder.getFullNameTextView().setText(fullname.split(" ")[0]);

                                Glide.with(context)
                                        .load(profileImage)
                                        .into(holder.getProfileCircleImageView());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: Couldn't retrieve user fullname and profile picture");
                    }
                });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + messages.size());
        return messages.size();
    }
    
    class MessageViewHolder extends RecyclerView.ViewHolder {
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

        class DisplayInfoListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {

                /* Toggle timestamp */
                if (timestampTextView.getAlpha() == 0.0f) {
                    timestampTextView.animate().alpha(1.0f).setDuration(500);

                } else {
                    timestampTextView.animate().alpha(0.0f).setDuration(500);
                }
            }
        }

        class CopyInfoListener implements View.OnLongClickListener {
            private void setClipboard(String text) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                                                context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                clipboard.setPrimaryClip(clip);
            }

            @Override
            public boolean onLongClick(View v) {
                if (v instanceof TextView) {
                    final String text = ((TextView) v).getText().toString();
                    final String[] availableOptions = { "Copy Text" };

                    new AlertDialog.Builder(context)
                            .setItems(availableOptions, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:     /* Copy message in clipboard */
                                            setClipboard(text);
                                            break;
                                        default:
                                            Log.d(TAG, "onClick: Undefined item clicked");
                                            break;
                                    }
                                }
                            }).show();
                    return true;
                }
                return false;
            }
        }

        /* Constructor */
        public MessageViewHolder(View itemView) {
            super(itemView);

            /* Setup layout views */
            rootView = itemView;
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            profileCircleImageView = rootView.findViewById(R.id.profileCircleImageView);
            messageTextView = rootView.findViewById(R.id.messageTextView);
            timestampTextView = rootView.findViewById(R.id.timestampTextView);
            messageImageView = rootView.findViewById(R.id.messageImageView);

            /* Setup adapters */
            messageTextView.setOnLongClickListener(new CopyInfoListener());
            messageTextView.setOnClickListener(new DisplayInfoListener());
            messageImageView.setOnClickListener(new DisplayInfoListener());
        }
    }
}
