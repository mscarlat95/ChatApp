package com.scarlat.marius.chatapp.app;


import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class ChatApp extends Application {

    private static final String TAG = "ChatApp";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
