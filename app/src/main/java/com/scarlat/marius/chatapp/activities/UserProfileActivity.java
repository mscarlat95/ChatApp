package com.scarlat.marius.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.CustomProgressDialog;
import com.scarlat.marius.chatapp.tasks.GetFriendInfoTask;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private final Context context = this;

    /* Firebase */
    final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    final CustomProgressDialog dialog = new CustomProgressDialog(context);

    /* Android views */
    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;
    private Button sendFriendRequestButton, declineFriendRequestButton;

    private int requestState = 0;
    private String userID = Constants.UNSET;
    private String friendID = Constants.UNSET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        /* Check intent extra values */
        final Intent intent = getIntent();
        friendID = intent.getStringExtra(Constants.USER_ID);
        if (friendID == null) {
            Log.d(TAG, "onCreate: Friend ID is NULL. Trying to get from FirebaseMessaging Service ...");
            
            friendID = intent.getStringExtra("sender_id");
            if (friendID == null) {
                Log.d(TAG, "onCreate: Friend ID is still NULL. App is closing ...");
                finish();
            } 
        }

        userID = FirebaseAuth.getInstance().getUid();
        Log.d(TAG, "onCreate: Friend Id = " + friendID);
        Log.d(TAG, "onCreate: User Id = " + userID);

        /* Setup Android Views */
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) findViewById(R.id.fullNameTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        friendsNumberTextView = (TextView) findViewById(R.id.friendsNumberTextView);

        sendFriendRequestButton = (Button) findViewById(R.id.sendFriendRequestButton);
        sendFriendRequestButton.setOnClickListener(new FriendRequestsListener());

        declineFriendRequestButton = (Button) findViewById(R.id.declineFriendRequestButton);
        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);
        declineFriendRequestButton.setOnClickListener(new DeclineRequestListener());

         /* Display user information */
        new GetFriendInfoTask(this, friendID, profileImageView, fullNameTextView,
                statusTextView, friendsNumberTextView).execute();

        if (userID.equals(friendID)) {
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
        } else {
            /* Check Request States between the users */
            checkUsersRelationship();
        }
    }

    private class FriendRequestsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendFriendRequestButton.setEnabled(false);

            switch (Constants.REQUEST_STATES[requestState]) {
                case Constants.UNSET:                       // Send Friend Request
                    sendFriendRequest();
                    break;
                case Constants.REQUEST_TYPE_SENT:           // Cancel Friend Request
                    cancelFriendRequest();
                    break;
                case Constants.REQUEST_TYPE_RECEIVED:       // Accept Friend Request
                    acceptFriendRequest();
                    break;
                case Constants.STATE_FRIENDS:               // Unfriend
                    unfriendRequest();
                    break;
            }
        }
    }

    private class DeclineRequestListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            declineFriendRequest();
        }
    }


    // TODO: check database for a better assurance
    private void checkUsersRelationship() {
        Log.d(TAG, "checkRequests: Method was invoked!");
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child(Constants.FRIEND_REQUESTS_TABLE);

        sendFriendRequestButton.setVisibility(View.INVISIBLE);
        dbReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.toString());

                        /* Check if there exists any request between users */
                        if (dataSnapshot.exists() && dataSnapshot.hasChild(friendID)) {
                            String friendRequestType = dataSnapshot.child(friendID)
                                    .child(Constants.REQUEST_TYPE)
                                    .getValue().toString();

                            if (friendRequestType.equals(Constants.REQUEST_TYPE_RECEIVED)) {
                                requestState = 2;
                                sendFriendRequestButton.setText(R.string.accept_friend_request);
                                declineFriendRequestButton.setVisibility(View.VISIBLE);
                                declineFriendRequestButton.setEnabled(true);

                            } else {    // REQUEST_TYPE_SENT
                                requestState = 1;
                                sendFriendRequestButton.setText(R.string.cancel_friend_request);
                            }
                        } else { /* Check if users are already friends */
                            FirebaseDatabase.getInstance().getReference().child(Constants.FRIENDS_TABLE).child(userID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            /* They are already friends */
                                            if (dataSnapshot.hasChild(friendID)) {
                                                Log.d(TAG, "Users are already friends");
                                                requestState = 3;
                                                sendFriendRequestButton.setText(R.string.unfriend_person);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                                        }
                                    });
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

    private void sendFriendRequest() {
        dialog.init("Sending Friend Request", "Please wait until the friend request is sent");

        String notificationId = rootDatabaseRef.child(Constants.NOTIFICATIONS_TABLE)
                                    .child(friendID).push()
                                    .getKey();

        /* Create a new notification */
        HashMap<String, String> notificationMap = new HashMap<>();
        notificationMap.put(Constants.SOURCE, userID);
        notificationMap.put(Constants.NOTIFICATION_TYPE, Constants.NOTIFICATION_FRIEND_REQUEST);

        /* Create friend request */
        Map<String, Object> friendRequestMap = new HashMap<>();
        friendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID + "/" + Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT);
        friendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID + "/" + Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_RECEIVED);
        friendRequestMap.put(Constants.NOTIFICATIONS_TABLE + "/" + friendID + "/" + notificationId, notificationMap);

        rootDatabaseRef.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    sendFriendRequestButton.setText(R.string.cancel_friend_request);
                    sendFriendRequestButton.setEnabled(true);
                    requestState = 1; // 1 - Request sent
                } else {
                    Log.d(TAG, "FriendRequest Failed: " + databaseError.getMessage());
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.hide();
            }
        });
    }


    private void cancelFriendRequest() {
        dialog.init("Canceling Friend Request", "Please wait until the friend request is canceled");

         /* Cancel friend request */
        Map<String, Object> cancelFriendRequestMap = new HashMap<>();
        cancelFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID, null);
        cancelFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID , null);

        rootDatabaseRef.updateChildren(cancelFriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    sendFriendRequestButton.setText(R.string.send_friend_request);
                    sendFriendRequestButton.setEnabled(true);
                    requestState = 0; // 0 - Unset State
                } else {
                    Log.d(TAG, "Cancel Friend Request Failed: " + databaseError.getMessage());
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.hide();
            }
        });
    }

    private void acceptFriendRequest() {
        dialog.init("Accept Friend Request", "Please wait until the process is done");

        Map<String, Object> acceptFriendRequestMap = new HashMap<>();
        acceptFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID, null);
        acceptFriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID , null);
        acceptFriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + userID + "/" + friendID + "/" + Constants.FRIENDS_SINCE, ServerValue.TIMESTAMP);
        acceptFriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + friendID + "/" + userID + "/" + Constants.FRIENDS_SINCE, ServerValue.TIMESTAMP);

        rootDatabaseRef.updateChildren(acceptFriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    sendFriendRequestButton.setText(R.string.unfriend_person);
                    sendFriendRequestButton.setEnabled(true);

                    declineFriendRequestButton.setVisibility(View.INVISIBLE);
                    declineFriendRequestButton.setEnabled(false);

                    requestState = 3; // 3 - Users are friends now
                } else {
                    Log.d(TAG, "Accept Friend Request Failed: " + databaseError.getMessage());
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.hide();
            }
        });
    }

    private void unfriendRequest() {
        dialog.init("Unfriend", "Please wait until the process is done");

        Map<String, Object> unfriendRequestMap = new HashMap<>();
        unfriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + userID + "/" + friendID, null);
        unfriendRequestMap.put(Constants.FRIENDS_TABLE + "/" + friendID + "/" + userID , null);

        rootDatabaseRef.updateChildren(unfriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    sendFriendRequestButton.setEnabled(true);
                    sendFriendRequestButton.setText(R.string.send_friend_request);

                    requestState = 0; // 0 - Unset State
                } else {
                    Log.d(TAG, "Unfriend Request Failed: " + databaseError.getMessage());
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.hide();
            }
        });
    }

    private void declineFriendRequest() {
        dialog.init("Decline Friend Request", "Please wait until the process is done");

        Map<String, Object> unfriendRequestMap = new HashMap<>();
        unfriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + userID + "/" + friendID, null);
//        unfriendRequestMap.put(Constants.FRIEND_REQUESTS_TABLE + "/" + friendID + "/" + userID , null);

        rootDatabaseRef.updateChildren(unfriendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    sendFriendRequestButton.setEnabled(true);
                    sendFriendRequestButton.setText(R.string.send_friend_request);
                    declineFriendRequestButton.setVisibility(View.INVISIBLE);

                    requestState = 0; // 0 - Unset State
                } else {
                    Log.d(TAG, "Decline Friend Request Failed: " + databaseError.getMessage());
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                dialog.hide();
            }
        });
    }
}
