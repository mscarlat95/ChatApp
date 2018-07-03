package com.scarlat.marius.chatapp.general;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtil {

    private static final String TAG = "NetworkUtil";

    public static boolean checkWifiConnection(Context context) {
        Log.d(TAG, "checkWifiConnection: Method was invoked!");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        /* Wi-Fi adapter is ON */
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            /* Device is connected to an access point */
            return (wifiInfo.getNetworkId() != -1);
        }
        else {
            /* Wi-Fi adapter is OFF */
            return false;
        }
    }


    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
