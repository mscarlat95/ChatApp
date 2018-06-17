package com.scarlat.marius.chatapp.adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.ChatActivity;
import com.scarlat.marius.chatapp.activities.UserProfileActivity;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Friend;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder>{

    private static final String TAG = "FriendListAdapter";

    private Context context;
    private List<Friend> friends;

    private final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();

    /* Getters and setters */
    public Context getContext() { return context; }
    public void setContext(Context context) { this.context = context; }

    public List<Friend> getFriends() { return friends; }
    public void setFriends(List<Friend> newList) {
        this.friends = new ArrayList<>();
        this.friends.addAll(newList);
    }


    /* Constructor */
    public FriendListAdapter(Context context, List<Friend> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_info_layout, parent, false);

        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        /* Obtain information about the current friend */
        updateFriendInfo(holder, position);

        /* Display alert dialog info */
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] availableOptions = {
                        "View Profile",
                        "Start chat",
                };

                new AlertDialog.Builder(getContext())
                        .setTitle("Choose Action")
                        .setItems(availableOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:     /* View user profile */
                                        Intent profileIntent = new Intent(getContext(), UserProfileActivity.class);
                                        profileIntent.putExtra(Constants.USER_ID, friends.get(position).getFriendId());
                                        getContext().startActivity(profileIntent);

                                        break;
                                    case 1:     /* Start chat */
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra(Constants.USER_ID, friends.get(position).getFriendId());
                                        getContext().startActivity(chatIntent);

                                        break;

                                    default:
                                        break;
                                }
                            }
                        }).show();
            }
        });
    }


    private void updateFriendInfo(final FriendViewHolder holder, int position) {
        Log.d(TAG, "updateFriendInfo: Method was invoked!");

         /* Retrieve friend information and update his layout profile */
        final String friendId = friends.get(position).getFriendId();

        rootDatabaseRef.child(Constants.USERS_TABLE).child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Method was invoked!");

                if (dataSnapshot.exists()) {
                    if (! ((Activity) context).isDestroyed()) {
                        final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                        final String profileImageUrl = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();

                        if (dataSnapshot.hasChild(Constants.ONLINE)) {
                            boolean online = Boolean.valueOf(dataSnapshot.child(Constants.ONLINE).getValue().toString());

                            if (online) {
                                holder.getOnlineImageView().setVisibility(View.VISIBLE);
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

        /* Set the date since the users are friends */
        holder.getDateTextView().setText( "Friends since " +
                DateFormat.format("MMM-dd-yyyy", friends.get(position).getFriendshipDate()));
    }


    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + friends.size());
        return friends.size();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {

        private View rootView;

        private CircleImageView avatarCircleImageView;
        private TextView fullNameTextView;
        private TextView dateTextView;
        private ImageView onlineImageView;

        /* Setters and getters */
        View getRootView() { return rootView; }
        CircleImageView getAvatarCircleImageView() { return avatarCircleImageView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        TextView getDateTextView() { return dateTextView; }
        ImageView getOnlineImageView() { return onlineImageView; }

        /* Constructor */
        public FriendViewHolder(View itemView) {
            super(itemView);

            /* Setup views */
            rootView = itemView;
            avatarCircleImageView = rootView.findViewById(R.id.avatarCircleImageView);
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            dateTextView = rootView.findViewById(R.id.statusTextView);
            onlineImageView = rootView.findViewById(R.id.onlineImageView);
        }
    }
}
