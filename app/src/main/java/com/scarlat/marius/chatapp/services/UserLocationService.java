package com.scarlat.marius.chatapp.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.scarlat.marius.chatapp.util.Constants;

public class UserLocationService extends Service {

    private static final String TAG = "UserLocationService";

    public static boolean status = Constants.INACTIVE;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        status = Constants.ACTIVE;
        Log.d(TAG, "Service started.");


        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        status = Constants.INACTIVE;
        Log.d(TAG, "Service stopped");
        
        super.onDestroy();
    }
}
