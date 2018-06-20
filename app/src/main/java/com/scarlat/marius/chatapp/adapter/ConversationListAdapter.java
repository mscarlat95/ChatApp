package com.scarlat.marius.chatapp.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.ChatActivity;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Conversation;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationListAdapter extends RecyclerView.Adapter<ConversationListAdapter.ConversationViewHolder> {

    private static final String TAG = "ConversationListAdapter";

    private Context context;
    private List<Conversation> conversations;

    private final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private final String userID = FirebaseAuth.getInstance().getUid();

    public ConversationListAdapter(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    public void updateConversation (List<Conversation> newConversations) {
        Log.d(TAG, "updateConversation: Method was invoked!");
        conversations = new ArrayList<>();
        conversations.addAll(newConversations);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_info_layout, parent, false);

        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ConversationViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        final String friendID = conversations.get(position).getFriendID();

        /* Get friend information */
        rootDatabaseRef.child(Constants.USERS_TABLE).child(friendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Method was invoked!");

                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: Friend information: " + dataSnapshot.toString());
                    if (! ((Activity) context).isDestroyed()) {
                        final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                        final String profileImageUrl = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();

                        if (dataSnapshot.hasChild(Constants.ONLINE)) {
                            boolean online = Boolean.valueOf(dataSnapshot.child(Constants.ONLINE).getValue().toString());

                            if (online) {
                                holder.getOnlineImageView().setVisibility(View.VISIBLE);
                            } else {
                                holder.getOnlineImageView().setVisibility(View.INVISIBLE);
                            }
                        }

                        holder.getFullNameTextView().setText(fullname);
                        Glide.with(context)
                                .load(profileImageUrl)
                                .into(holder.getAvatarCircleImageView());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        Query lastMessageQuery = rootDatabaseRef.child(Constants.MESSAGES_TABLE).child(userID).child(friendID).limitToLast(1);

        /* Display last message */
        holder.getLastMessageTextView().setText("");
        lastMessageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onChildAdded: Last message: " + dataSnapshot.toString());
                    String message = dataSnapshot.child(Constants.MESSAGE_CONTENT).getValue().toString();

                    holder.getLastMessageTextView().setText(message);
                    if (!conversations.get(position).isSeen()) {
                        Log.d(TAG, "onChildAdded: Message not seen" );
                        holder.getLastMessageTextView().setTypeface(
                                holder.getLastMessageTextView().getTypeface(), Typeface.BOLD);
                    } else {
                        Log.d(TAG, "onChildAdded: Message seen");
                        holder.getLastMessageTextView().setTypeface(Typeface.DEFAULT);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

        /* Add listener */
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(Constants.USER_ID, friendID);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + conversations.size());
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        private View rootView;

        private CircleImageView avatarCircleImageView;
        private TextView fullNameTextView;
        private TextView lastMessageTextView;
        private ImageView onlineImageView;

        /* Setters and getters */
        View getRootView() { return rootView; }
        CircleImageView getAvatarCircleImageView() { return avatarCircleImageView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        TextView getLastMessageTextView() { return lastMessageTextView; }
        ImageView getOnlineImageView() { return onlineImageView; }

        /* Constructor */
        public ConversationViewHolder(View itemView) {
            super(itemView);
            
            rootView = itemView;

            avatarCircleImageView = rootView.findViewById(R.id.avatarCircleImageView);
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            lastMessageTextView = rootView.findViewById(R.id.statusTextView);
            onlineImageView = rootView.findViewById(R.id.onlineImageView);
        }
    }
}
