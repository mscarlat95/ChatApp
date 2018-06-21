package com.scarlat.marius.chatapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder> {
    private static final String TAG = "FriendRequestsAdapter";

    private Context context;
    private List<String> requestsFriendID;

    private DatabaseReference rootDatabaseRef;
    private String userID;

    public FriendRequestsAdapter(Context context, List<String> requestsFriendID) {
        this.context = context;
        this.requestsFriendID = requestsFriendID;

        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userID = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_layout, parent, false);

        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRequestViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        final String friendID = requestsFriendID.get(position);

        rootDatabaseRef.child(Constants.USERS_TABLE).child(friendID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (!((Activity) context).isDestroyed()) {
                                final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                                final String profilePicture = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();

                                holder.getFullNameTextView().setText(fullname);
                                Glide.with(context)
                                        .load(profilePicture)
                                        .into(holder.getAvatarCircleImageView());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });

        holder.getAcceptRequestButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest(userID, friendID);
                holder.getRootView().animate().alpha(0.0f).setDuration(1000);
            }
        });

        holder.getDeclineRequestButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineFriendRequest(userID, friendID);
                holder.getRootView().animate().alpha(0.0f).setDuration(1000);
            }
        });
    }


    private void acceptFriendRequest(final String userID, final String friendID) {
        Map<String, Object> acceptFriendRequestMap = new HashMap<>();
        acceptFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID, null);
        acceptFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID , null);
        acceptFriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + userID + "/" + friendID + "/" + Constants.FRIENDS_SINCE, ServerValue.TIMESTAMP);
        acceptFriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + friendID + "/" + userID + "/" + Constants.FRIENDS_SINCE, ServerValue.TIMESTAMP);

        rootDatabaseRef.updateChildren(acceptFriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "onComplete: Accept friend request Successful");
                    Toast.makeText(context, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: Accept Friend Request Failed: " + databaseError.getMessage());
                }
            }
        });
    }

    private void declineFriendRequest(final String userID, final String friendID) {
        Map<String, Object> unfriendRequestMap = new HashMap<>();
        unfriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID, null);
//        unfriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID , null);

        rootDatabaseRef.updateChildren(unfriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "onComplete: Decline friend requests Successful");
                    Toast.makeText(context, "Friend Request Declined", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: Decline friend request Failed " + databaseError.getMessage());
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + requestsFriendID.size());
        return requestsFriendID.size();
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        private View rootView;

        private CircleImageView avatarCircleImageView;
        private TextView fullNameTextView;
        private ImageButton acceptRequestButton, declineRequestButton;

        public View getRootView() { return rootView; }

        CircleImageView getAvatarCircleImageView() { return avatarCircleImageView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        ImageButton getAcceptRequestButton() { return acceptRequestButton; }
        ImageButton getDeclineRequestButton() { return declineRequestButton; }

        public FriendRequestViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;

            avatarCircleImageView = rootView.findViewById(R.id.avatarCircleImageView);
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            acceptRequestButton = rootView.findViewById(R.id.acceptFriendRequestButton);
            declineRequestButton = rootView.findViewById(R.id.declineFriendRequestButton);
        }
    }

}
