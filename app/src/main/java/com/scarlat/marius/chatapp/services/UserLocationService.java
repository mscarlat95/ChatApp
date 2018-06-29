package com.scarlat.marius.chatapp.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.scarlat.marius.chatapp.general.Constants;

public class UserLocationService extends Service {

    private static final String TAG = "UserLocationService";

    public static boolean status = Constants.INACTIVE;

    private boolean firstLocationSetup = false;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started.");

        status = Constants.ACTIVE;
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service stopped");

        status = Constants.INACTIVE;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Method was invoked!");

        setupLocationSettings();
    }

    /*
        Disable checking missing permissions
        Setup location settings (location manager, location listener etc.)
     */
    @SuppressWarnings({"MissingPermission"})
    private void setupLocationSettings() {
        Log.d(TAG, "setupLocationSettings: Method was invoked!");

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sendBcast(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        /* Choose criteria */
        Criteria criteria = new Criteria();

        criteria.setPowerRequirement(Criteria.POWER_HIGH); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(true); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money

        /* Update the position each 3 seconds */
        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true), 3000, 0, mLocationListener);

        /* At first run, get last known location */
        if (! firstLocationSetup) {
            String[] providers = {  LocationManager.GPS_PROVIDER,
                                    LocationManager.NETWORK_PROVIDER };
            Location lastKnownLocation = null;

            for (String provider : providers) {
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l != null && (lastKnownLocation == null || l.getAccuracy() < lastKnownLocation.getAccuracy())) {
                    lastKnownLocation = l;
                }
            }

            if (lastKnownLocation != null) {
                sendBcast(lastKnownLocation);
            }
            firstLocationSetup = true;
        }
    }


    /* Send broadcast updates containing the user location */
    private void sendBcast(Location location) {
        Log.d(TAG, "sendBcast: Method was invoked");
        Intent intent = new Intent (Constants.ACTION_LOCATION_UPDATE);

        intent.putExtra(Constants.USER_LONGITUDE, location.getLongitude());
        intent.putExtra(Constants.USER_LATITUDE, location.getLatitude());

        sendBroadcast(intent);
    }

    public static void setStatus (boolean newStatus) {
        status = newStatus;
    }


}
