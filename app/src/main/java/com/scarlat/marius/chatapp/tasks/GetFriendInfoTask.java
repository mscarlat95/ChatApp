package com.scarlat.marius.chatapp.tasks;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.DisplayImageActivity;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.CustomProgressDialog;

public class GetFriendInfoTask {
    private static final String TAG = "GetFriendInfoTask";
    private Context context;

    private String userID;

    /* Views */
    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;

    /* Progress dialog */
    private CustomProgressDialog progressDialog;

    /* Firebase */
    private DatabaseReference rootDatabaseRef;

    public GetFriendInfoTask(Context context, String userID, ImageView profileImageView,
                             TextView fullNameEditText, TextView statusEditText,
                             TextView friendsNumberEditText) {
        this.context = context;
        this.userID = userID;

        this.profileImageView = profileImageView;
        this.fullNameTextView = fullNameEditText;
        this.statusTextView = statusEditText;
        this.friendsNumberTextView = friendsNumberEditText;
    }

    private void setup() {
        Log.d(TAG, "setup: Method was invoked!");

        /* Setup progress dialog */
        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Retrieving User Information", "Please wait until the server provides the required data");

        /* Setup Firebase */
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public void execute() {
        Log.d(TAG, "execute: Method was invoked!");

        setup();

        rootDatabaseRef.child(Constants.USERS_TABLE).child(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: Method was invoked!");

                        if (((Activity) context).isDestroyed()) {
                            Log.d(TAG, "onComplete: Activity is not available");
                            return;
                        }

                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "onDataChange: Snapshot = " + dataSnapshot.toString());

                            final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                            final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                            final String status = dataSnapshot.child(Constants.STATUS).getValue().toString();

                            statusTextView.setText("Status: " + status);
                            if (userID.equals(FirebaseAuth.getInstance().getUid())) {
                                fullNameTextView.setText(fullname + " (yourself) ");
                            } else {
                                fullNameTextView.setText(fullname);
                            }

                            Glide.with(context)
                                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.loading))
                                    .load(profileImage)
                                    .into(profileImageView);

                            profileImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(context, DisplayImageActivity.class);
                                    intent.putExtra(Constants.IMAGE_URI, profileImage);
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });

        rootDatabaseRef.child(Constants.FRIENDS_TABLE).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (((Activity) context).isDestroyed()) {
                    Log.d(TAG, "onComplete: Activity is not available");
                    return;
                }

                if (dataSnapshot.exists()) {
                    long numberOfFriends = dataSnapshot.getChildrenCount();
                    friendsNumberTextView.setText("Total Friends: " + numberOfFriends);
                }  else {
                    friendsNumberTextView.setText("Total Friends: 0");
                }

                progressDialog.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                progressDialog.hide();
            }
        });
    }
}
