package com.scarlat.marius.chatapp.activities;

import android.content.Context;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.CustomProgressDialog;
import com.scarlat.marius.chatapp.tasks.GetFriendInfoTask;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private final Context context = this;

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

        /* Check Request States between the users */
        checkUsersRelationship();
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
    };

    private class DeclineRequestListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

        }

    }

    private void checkUsersRelationship() {
        Log.d(TAG, "checkRequests: Method was invoked!");
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

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
                            FirebaseDatabase.getInstance().getReference().child("Friends").child(userID)
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
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        final CustomProgressDialog dialog = new CustomProgressDialog(context);

        dialog.init("Sending Friend Request", "Please wait until the friend request is sent");
        dbReference.child(userID).child(friendID).child(Constants.REQUEST_TYPE)
                .setValue(Constants.REQUEST_TYPE_SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    dbReference.child(friendID).child(userID).child(Constants.REQUEST_TYPE)
                            .setValue(Constants.REQUEST_TYPE_RECEIVED).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Set REQUEST_TYPE_RECEIVED Successful");
                                sendNotification(userID, friendID, Constants.NOTIFICATION_FRIEND_REQUEST);
                            } else {
                                Log.d(TAG, "Set REQUEST_TYPE_RECEIVED Failed: " + task.getException().getMessage());
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            dialog.hide();
                        }
                    });

                } else {
                    Log.d(TAG, "Set REQUEST_TYPE_SENT Failed: " + task.getException().getMessage());
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.hide();
                }
                sendFriendRequestButton.setEnabled(true);
            }
        });

    }

    private void cancelFriendRequest() {
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        final CustomProgressDialog dialog = new CustomProgressDialog(context);

        dialog.init("Canceling Friend Request", "Please wait until the friend request is canceled");
        dbReference.child(friendID).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            Log.d(TAG, "Removed REQUEST_TYPE_RECEIVED Successful");

            if (task.isSuccessful()) {
                dbReference.child(userID).child(friendID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Removed REQUEST_TYPE_SENT Successful");

                            sendFriendRequestButton.setEnabled(true);
                            sendFriendRequestButton.setText(R.string.send_friend_request);

                            requestState = 0; // 0 - Unset State
                        } else {
                            Log.d(TAG, "Removed REQUEST_TYPE_SENT Successful Failed: " + task.getException().getMessage());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        dialog.hide();
                    }
                });
            } else {
                Log.d(TAG, "Removed REQUEST_TYPE_RECEIVED Successful Failed: " + task.getException().getMessage());
                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                dialog.hide();
            }
            }
        });

    }

    private void acceptFriendRequest() {
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        final CustomProgressDialog dialog = new CustomProgressDialog(context);
        final Map<String, String> timestamp = ServerValue.TIMESTAMP;

        dialog.init("Accept Friend Request", "Please wait until the process is done");
        dbReference.child(userID).child(friendID).setValue(timestamp)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, userID + " is now friend with "  + friendID);

                    dbReference.child(friendID).child(userID).setValue(timestamp)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, friendID + " is now friend with "  + userID);

                                /* Wait for removing friends requests */
                                FirebaseDatabase.getInstance().getReference().child("FriendRequests")
                                    .child(userID).child(friendID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                FirebaseDatabase.getInstance().getReference().child("FriendRequests")
                                                    .child(friendID).child(userID).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                sendFriendRequestButton.setText(R.string.unfriend_person);
                                                                sendFriendRequestButton.setEnabled(true);

                                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                declineFriendRequestButton.setEnabled(false);

                                                                requestState = 3; // 3 - Users are friends now
                                                            }
                                                        }
                                                    });
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, friendID + " friendship with "  + userID + " Failed: " + task.getException().getMessage());
                                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                    dialog.hide();
                                }
                            });
                } else {
                    Log.d(TAG, userID + " friendship with "  + friendID + " Failed: " + task.getException().getMessage());
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.hide();
                }
            }
        });
    }

    private void unfriendRequest() {
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        final CustomProgressDialog dialog = new CustomProgressDialog(context);

        dialog.init("Unfriend", "Please wait until the process is done");
        dbReference.child(userID).child(friendID).removeValue()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, userID + " removed from friends "  + friendID);

                        dbReference.child(friendID).child(userID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, friendID + " removed from friends "  + userID);
                                        Toast.makeText(context, "Unfriend Successful", Toast.LENGTH_SHORT).show();

                                        sendFriendRequestButton.setEnabled(true);
                                        sendFriendRequestButton.setText(R.string.send_friend_request);

                                        requestState = 0; // 0 - Unset State
                                    } else {
                                        Log.d(TAG, friendID + " friendship with "  + userID + " Failed: " + task.getException().getMessage());
                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.hide();
                                }
                            });
                    } else {
                        Log.d(TAG, userID + " friendship with "  + friendID + " Failed: " + task.getException().getMessage());
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.hide();
                    }
                }
            });
    }


    private void sendNotification(final String source, final String destination, final String notificationType) {
        final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.SOURCE, source);
        data.put(Constants.NOTIFICATION_TYPE, notificationType);

        dbReference.child(destination).push().setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    sendFriendRequestButton.setText(R.string.cancel_friend_request);
                    requestState = 1; // 1 - Request sent
                } else {
                    Log.d(TAG, "Sending Notification Failed: " + task.getException().getMessage());
                }

            }
        });
    }


}
