package com.scarlat.marius.chatapp.threads;


import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.general.Constants;

public class SendFriendRequestTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SendFriendRequestTask";

    private Context context;

    private String userID;
    private String friendID;

    /* Views */
    private Button sendFriendRequestButton, declineFriendRequestButton;

    /* Firebase */
    private DatabaseReference dbReference;

    public SendFriendRequestTask(Context context, String friendID,
                                 Button sendFriendRequestButton, Button declineFriendRequestButton) {
        this.context = context;
        this.friendID = friendID;

        /* Setup views */
        this.sendFriendRequestButton = sendFriendRequestButton;
        this.declineFriendRequestButton = declineFriendRequestButton;
    }

    @Override
    protected void onPreExecute() {
        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        userID = FirebaseAuth.getInstance().getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

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

                                sendFriendRequestButton.setText("Friend Request Sent");
                                sendFriendRequestButton.setBackgroundColor(Color.parseColor("#55EEEEEE"));


                                declineFriendRequestButton.setVisibility(View.VISIBLE);

                            } else {
                                Log.d(TAG, "Set REQUEST_TYPE_RECEIVED Failed: " + task.getException().getMessage());
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Log.d(TAG, "Set REQUEST_TYPE_SENT Failed: " + task.getException().getMessage());
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return null;
    }
}
