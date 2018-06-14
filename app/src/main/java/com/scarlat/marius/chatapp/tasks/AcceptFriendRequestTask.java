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

import java.util.Date;
import java.util.concurrent.ExecutionException;

public class AcceptFriendRequestTask extends AsyncTask<Void, Void, Void>{

    private static final String TAG = "AcceptFriendRequestTask";

    private Context context;
    private String userID;
    private String friendID;

    /* Views */
    private ProgressDialog progressDialog;
    private Button sendFriendRequestButton;

    /* Firebase */
    private DatabaseReference dbReference;

    public AcceptFriendRequestTask(Context context, String friendID, Button sendFriendRequestButton) {
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

        final String timeStamp = android.icu.text.DateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG, "doInBackground: Timestamp = " + timeStamp);

        dbReference.child(userID).child(friendID).setValue(timeStamp)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, userID + " is now friend with "  + friendID);

                            dbReference.child(friendID).child(userID).setValue(timeStamp)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Log.d(TAG, friendID + " is now friend with "  + userID);

                                                try {

                                                    /* Wait for removing friends requests */
                                                    new RemoveFriendRequestTask(context, friendID, null)
                                                            .execute().get();

                                                    sendFriendRequestButton.setText(R.string.unfriend_person);
                                                    sendFriendRequestButton.setEnabled(true);

                                                } catch (InterruptedException | ExecutionException e) {
                                                    e.printStackTrace();
                                                }


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