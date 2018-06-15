package com.scarlat.marius.chatapp.app;


import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.scarlat.marius.chatapp.general.Constants;

public class ChatApp extends Application {

    private static final String TAG = "ChatApp";

    private DatabaseReference usersDatabaseRef;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate();

        /* Firebase Messaging Service */
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        /* Offline database */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /* Setup Listener for online user */
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE)
                .child(FirebaseAuth.getInstance().getUid());

        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Updating user Status: Method was invoked!");

                        /* On disconnect, set user OFFLINE */
                        if (dataSnapshot.exists()) {
                            usersDatabaseRef.child(Constants.ONLINE).onDisconnect().setValue(false);
//                            usersDatabaseRef.child(Constants.ONLINE).setValue(true);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }
}
