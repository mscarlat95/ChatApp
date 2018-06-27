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
import com.google.firebase.iid.FirebaseInstanceId;
import com.scarlat.marius.chatapp.general.Constants;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserTask {
    private static final String TAG = "RegisterUserTask";

    /* Application Context */
    private Context context;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    public RegisterUserTask(Context context, FirebaseAuth mAuth) {
        this.context = context;
        this.mAuth = mAuth;
    }

    public void execute (final String email, final String fullName, final String photoUrl) {
        Log.d(TAG, "execute: Method was invoked!");

        /* Setup Firebase */
        dbReference = FirebaseDatabase.getInstance().getReference();

        /* Save info in database */
        final String userID = mAuth.getUid();
        final String tokenID = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put(Constants.FULLNAME, fullName);
        userInfo.put(Constants.EMAIL, email);
        userInfo.put(Constants.STATUS, Constants.DEFAULT_STATUS_VAL);
        userInfo.put(Constants.PROFILE_IMAGE, photoUrl);
        userInfo.put(Constants.NUMBER_OF_FRIENDS, 0);
        userInfo.put(Constants.TOKEN_ID, tokenID);

        Log.d(TAG, "User ID Token = " + userID);
        Log.d(TAG, "User Information = " + userInfo.toString());

        dbReference.child(Constants.USERS_TABLE).child(userID)
                .setValue(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Add info in database success");
                            Toast.makeText(context, "Register succeeded !", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Add info in database failed. " + task.getException().toString());
                            Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
