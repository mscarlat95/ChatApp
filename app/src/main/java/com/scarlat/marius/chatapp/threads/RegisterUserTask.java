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

import java.util.HashMap;
import java.util.Map;

public class RegisterUserTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "RegisterUserTask";

    /* Application Context */
    private Context context;

    /* Progress dialog */
    private ProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    public RegisterUserTask(Context context, FirebaseAuth mAuth) {
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
        progressDialog.setTitle("Registering User");
        progressDialog.setMessage("Please wait until the account is created !");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        dbReference = FirebaseDatabase.getInstance().getReference();
    }



    @Override
    protected Void doInBackground(String... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        final String email = params[0];
        final String fullName = params[1];
        final String photoUrl = params[2];
        final String userID = mAuth.getUid();
        Map<String, String> userInfo = new HashMap<>();

        userInfo.put(Constants.FULLNAME, fullName);
        userInfo.put(Constants.EMAIL, email);
        userInfo.put(Constants.STATUS, Constants.DEFAULT_STATUS_VAL);
        userInfo.put(Constants.PROFILE_IMAGE, photoUrl);
        userInfo.put(Constants.THUMBNAIL_PROFILE_IMAGE, photoUrl);

        Log.d(TAG, "User ID Token = " + userID);
        Log.d(TAG, "User Information = " + userInfo.toString());

        dbReference.child("Users").child(userID)
                .setValue(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Add info in database success");
                            Toast.makeText(context, "Register Succedded!", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Add info in database failed. " + task.getException().toString());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });

        return null;
    }
}
