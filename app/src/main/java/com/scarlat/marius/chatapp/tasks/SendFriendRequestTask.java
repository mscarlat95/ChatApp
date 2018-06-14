package com.scarlat.marius.chatapp.tasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

public class SendFriendRequestTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SendFriendRequestTask";

    private Context context;
    private String userID;
    private String friendID;

    /* Views */
    private ProgressDialog progressDialog;
    private Button sendFriendRequestButton;

    /* Firebase */
    private DatabaseReference dbReference;

    public SendFriendRequestTask(Context context, String friendID, Button sendFriendRequestButton) {
        this.context = context;
        this.friendID = friendID;

        /* Setup views */
        this.sendFriendRequestButton = sendFriendRequestButton;
    }


    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        /* Setup progress dialog */
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Sending Friend Request");
        progressDialog.setMessage("Please wait until the friend request is sent");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

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
                                sendFriendRequestButton.setText(R.string.cancel_friend_request);
                            } else {
                                Log.d(TAG, "Set REQUEST_TYPE_RECEIVED Failed: " + task.getException().getMessage());
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            hideProgressDialog();
                        }
                    });

                } else {
                    Log.d(TAG, "Set REQUEST_TYPE_SENT Failed: " + task.getException().getMessage());
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }

                sendFriendRequestButton.setEnabled(true);
            }
        });


        return null;
    }
}
