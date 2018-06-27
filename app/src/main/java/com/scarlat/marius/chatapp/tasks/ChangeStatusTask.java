package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.CustomProgressDialog;

public class ChangeStatusTask  {
    private static final String TAG = "ChangeStatusTask";

    private Context context;
    private CustomProgressDialog progressDialog;

    /* Firebase */
    private String userID;
    private DatabaseReference dbReference;

    public ChangeStatusTask(Context context) {
        this.context = context;
    }

    private void setup() {
        Log.d(TAG, "setup: Method was invoked!");

        /* Setup progress dialog */
        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Changing User Status", "Please wait until the status is updated");

        /* Setup Firebase */
        userID = FirebaseAuth.getInstance().getUid();
        dbReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.USERS_TABLE).child(userID);
    }

    public void execute(final String status) {
        Log.d(TAG, "execute: Method was invoked!");

        setup();

        dbReference.child(Constants.STATUS).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Changing Status: Success");
                            Toast.makeText(context, "Status Updating successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Changing Status Failed: " + task.getException().getMessage());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.hide();
                    }
                });
    }

}
