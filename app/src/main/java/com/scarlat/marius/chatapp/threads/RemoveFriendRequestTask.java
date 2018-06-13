package com.scarlat.marius.chatapp.threads;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;

public class RemoveFriendRequestTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "RemoveFriendRequestTask";

    private Context context;
    private String userID;
    private String friendID;

    /* Views */
    private ProgressDialog progressDialog;
    private Button sendFriendRequestButton;

    /* Firebase */
    private DatabaseReference dbReference;

    public RemoveFriendRequestTask(Context context, String friendID, Button sendFriendRequestButton) {
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
        progressDialog.setTitle("Cancelin Friend Request");
        progressDialog.setMessage("Please wait until the friend request is canceled");
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

        dbReference.child(friendID).child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.d(TAG, "Removed REQUEST_TYPE_RECEIVED Successful");

                dbReference.child(userID).child(friendID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d(TAG, "Removed REQUEST_TYPE_SENT Successful");

                        sendFriendRequestButton.setEnabled(true);
                        sendFriendRequestButton.setText(R.string.send_friend_request);

                        hideProgressDialog();
                    }
                });
            }
        });
        return null;
    }

}
