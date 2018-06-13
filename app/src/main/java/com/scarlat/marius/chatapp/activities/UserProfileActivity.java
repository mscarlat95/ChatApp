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
import com.scarlat.marius.chatapp.threads.AcceptFriendRequestTask;
import com.scarlat.marius.chatapp.threads.GetFriendInfoTask;
import com.scarlat.marius.chatapp.threads.RemoveFriendRequestTask;
import com.scarlat.marius.chatapp.threads.SendFriendRequestTask;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;
    private Button sendFriendRequestButton, declineFriendRequestButton;

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
        sendFriendRequestButton.setOnClickListener(new FriendRequestsListener());

        declineFriendRequestButton = (Button) findViewById(R.id.declineFriendRequestButton);
        declineFriendRequestButton.setOnClickListener(new DeclineRequestListener());

        /* Check Request States */
        checkRequests();

        /* Extract user information */
        new GetFriendInfoTask(this, friendID, profileImageView, fullNameTextView,
            statusTextView, friendsNumberTextView).execute();
    }

    private void checkRequests() {
        Log.d(TAG, "checkRequests: Method was invoked!");

        sendFriendRequestButton.setVisibility(View.INVISIBLE);

        String userID = FirebaseAuth.getInstance().getUid();
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

                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                        Toast.makeText(UserProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        sendFriendRequestButton.setVisibility(View.VISIBLE);
                    }
                });
    }

    private class FriendRequestsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendFriendRequestButton.setEnabled(false);

            // TODO: set requestState from Aync Tasks using execute().get() and a getter

            switch (Constants.REQUEST_STATES[requestState]) {
                case Constants.UNSET:                       // Send Friend Request
                    new SendFriendRequestTask(UserProfileActivity.this, friendID, sendFriendRequestButton).execute();
                    requestState = 1;       // Request sent
                    break;

                case Constants.REQUEST_TYPE_SENT:           // Cancel Friend Request
                    new RemoveFriendRequestTask(UserProfileActivity.this, friendID, sendFriendRequestButton).execute();
                    requestState = 0;       // Unset State
                    break;

                case Constants.REQUEST_TYPE_RECEIVED:       // Accept Friend Request
                    new AcceptFriendRequestTask(UserProfileActivity.this, friendID, sendFriendRequestButton).execute();
                    requestState = 3;       // Users are friends now
                    break;

                case Constants.STATE_FRIENDS:               // Unfriend
                    // TODO: delete records from Friends table
                    requestState = 0;
                    break;
            }
        }
    };

    private class DeclineRequestListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }
    }

}
