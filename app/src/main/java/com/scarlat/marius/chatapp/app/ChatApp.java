package com.scarlat.marius.chatapp.app;


import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.scarlat.marius.chatapp.general.Constants;

public class ChatApp extends Application {
    private static final String TAG = "ChatApp";

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate();

        /* Init Firebase Messaging */
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        /* Check if user is logged in */
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "onCreate: User is NULL");
            return;
        }


        /* Setup Listener for online user */
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(mAuth.getUid());
        usersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        /* On disconnect, set user OFFLINE */
                        if (dataSnapshot.getValue() != null) {
                            usersDatabaseRef.child(Constants.ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
    }


}
