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

public class UnfriendRequestTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "UnfriendRequestTask";


    private Context context;
    private String userID;
    private String friendID;

    /* Views */
    private ProgressDialog progressDialog;
    private Button sendFriendRequestButton;

    /* Firebase */
    private DatabaseReference dbReference;

    public UnfriendRequestTask(Context context, String friendID, Button sendFriendRequestButton) {
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
        progressDialog.setTitle("Accept Friend Request");
        progressDialog.setMessage("Please wait until the process is done");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        userID = FirebaseAuth.getInstance().getUid();
        dbReference = FirebaseDatabase.getInstance().getReference().child("Friends");
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

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

                                            } else {
                                                Log.d(TAG, friendID + " friendship with "  + userID + " Failed: " + task.getException().getMessage());
                                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, userID + " friendship with "  + friendID + " Failed: " + task.getException().getMessage());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });

        return null;
    }
}