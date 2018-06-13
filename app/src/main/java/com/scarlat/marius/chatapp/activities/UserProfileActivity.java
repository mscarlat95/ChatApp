package com.scarlat.marius.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.threads.GetFriendInfoTask;
import com.scarlat.marius.chatapp.threads.RemoveFriendRequestTask;
import com.scarlat.marius.chatapp.threads.SendFriendRequestTask;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;
    private Button sendFriendRequestButton;

    private int requestState = 0;   /* Unset */
    private String friendID = Constants.UNSET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        /* Check intent extra values */
        final Intent intent = getIntent();
        friendID = intent.getStringExtra(Constants.USER_ID);
        Log.d(TAG, "onCreate: Friend Id = " + friendID);

        /* Setup Android Views */
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) findViewById(R.id.fullNameTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        friendsNumberTextView = (TextView) findViewById(R.id.friendsNumberTextView);
        sendFriendRequestButton = (Button) findViewById(R.id.sendFriendRequestButton);

        /* Add button listener */
        sendFriendRequestButton.setOnClickListener(new FriendRequestsListener());

        /* Check Request States */
        checkRequests();

       /* Extract user information */
        new GetFriendInfoTask(  this, friendID, profileImageView, fullNameTextView,
                statusTextView, friendsNumberTextView).execute();
    }

    private void checkRequests() {
        Log.d(TAG, "checkRequests: Method was invoked!");
        String userID = FirebaseAuth.getInstance().getUid();

        sendFriendRequestButton.setEnabled(false);
        if (userID == null) {
            Log.d(TAG, "checkRequests: USER_ID is null");

            Toast.makeText(this, "Encountered problems. Please restart the application", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference().child("FriendRequests")
                .child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        if (dataSnapshot.exists() && dataSnapshot.hasChild(friendID)) {
                            String friendRequestType = dataSnapshot.child(friendID)
                                    .child(Constants.REQUEST_TYPE)
                                    .getValue().toString();

                            if (friendRequestType.equals(Constants.REQUEST_TYPE_RECEIVED)) {
                                requestState = 2;
                                sendFriendRequestButton.setText(R.string.accept_friend_request);
                            } else {    // REQUEST_TYPE_SENT
                                requestState = 1;
                                sendFriendRequestButton.setText(R.string.cancel_friend_request);
                            }
                        }

                        sendFriendRequestButton.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                        Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class FriendRequestsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendFriendRequestButton.setEnabled(false);

            switch (Constants.REQUEST_STATES[requestState]) {
                case Constants.UNSET:
                    new SendFriendRequestTask(UserProfileActivity.this, friendID, sendFriendRequestButton).execute();
                    requestState = 1; /* Request sent */
                    break;

                case Constants.REQUEST_TYPE_SENT:
                    new RemoveFriendRequestTask(UserProfileActivity.this, friendID, sendFriendRequestButton).execute();
                    requestState = 0;   /* Cancel Request --> Unset State */
                    break;

                case Constants.REQUEST_TYPE_RECEIVED:
                    // TODO: process Decline or Accept
                    // TODO: process Friends Table

                    break;
            }
        }
    };

}
