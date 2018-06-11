package com.scarlat.marius.chatapp.threads;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.general.Constants;

public class ChangeStatusTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "ChangeStatusTask";

    private Context context;

    /* Progress dialog */
    private ProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    public ChangeStatusTask(Context context, FirebaseAuth mAuth) {
        this.context = context;
        this.mAuth = mAuth;
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
        progressDialog.setTitle("Changing User Status");
        progressDialog.setMessage("Please wait until the status is updated");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        dbReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid());
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");
        final String status = params[0];

        dbReference.child(Constants.STATUS)
                .setValue(status)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Changing Status: Success");
                            Toast.makeText(context, "Status Updating successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Changing Status Failed: " + task.getException().getMessage());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });

        return null;
    }

}
