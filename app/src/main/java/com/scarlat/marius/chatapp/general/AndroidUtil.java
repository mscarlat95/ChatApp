package com.scarlat.marius.chatapp.general;


import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class AndroidUtil {
    private static final String TAG = "AndroidUtil";

    public static boolean isAppInstalled(Context context, final String appPackageName) {
        Log.d(TAG, "isAppInstalled: " + appPackageName);
        PackageManager packageManager = context.getPackageManager();

        try {
            packageManager.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "isAppInstalled: [Exception] " + e.getMessage());
        }

        return false;
    }

}
