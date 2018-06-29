package com.scarlat.marius.chatapp.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.services.UserLocationService;
import com.scarlat.marius.chatapp.storage.SharedPref;
import com.scarlat.marius.chatapp.tasks.PopulateMapTask;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    /* User location */
    private GoogleMap mMap;
    private LatLng userLocation;

    /* Android views */
    private Toolbar toolbar;
    private Button refreshButton;

    /* Listener which retreive information from UserLocationService  */
    private BroadcastReceiver mBroadcastReceiver;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference rootDatabaseRef;

    /* Map users */
    PopulateMapTask mapPopulationTask = new PopulateMapTask(this);


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Method was invoked!");
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: Method was invoked!");
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.startShareLocationItem:
                Log.d(TAG, "onOptionsItemSelected: startShareLocationItem");
                final String startServiceRes = startLocationService();

                Toast.makeText(this, startServiceRes, Toast.LENGTH_SHORT).show();
                break;
            case R.id.stopShareLocationItem:
                Log.d(TAG, "onOptionsItemSelected: stopShareLocationItem");
                final String stopServiceRes = stopLocationService();

                Toast.makeText(this, stopServiceRes, Toast.LENGTH_SHORT).show();
                break;
            default:
                Log.d(TAG, "onOptionsItemSelected: Received unknown item id " + item.getItemId());
                break;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /* Obtain the SupportMapFragment and get notified when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* Initialize user location */
        SharedPref.setup(this);
        final String lastLatitude = SharedPref.getString(Constants.USER_LATITUDE);
        final String lastLongitude = SharedPref.getString(Constants.USER_LONGITUDE);

        if (lastLatitude.equals("") || lastLongitude.equals("")) {
            userLocation = null;
        } else {
            userLocation = new LatLng(Double.valueOf(lastLatitude), Double.valueOf(lastLongitude));
        }

        /* Firebase initialize */
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        /*  Setup Toolbar */
        toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Share Location With Friends");

        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserLocationService.status == Constants.ACTIVE) {
                    updateMapPosition();
                } else {
                    Toast.makeText(MapsActivity.this, "Please Start Location Sharing!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* Enable GPS */
        enableGPS();

        /* Initialize broadcast receiver in order to retrieve user location */
        setupBroadcastReceiver();
    }

    private void setupBroadcastReceiver() {
        Log.d(TAG, "setupBroadcastReceiver: Method was invoked");
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final double longitude = intent.getDoubleExtra(Constants.USER_LONGITUDE, -1f);
                    final double latitude = intent.getDoubleExtra(Constants.USER_LATITUDE, -1f);
                    Log.d(TAG, "onReceive: User location = " + longitude + "; " + latitude);

                    /* Update user location */
                    userLocation = new LatLng(latitude, longitude);

                    SharedPref.saveCoordinates(latitude, longitude);

                    updateLocationInDatabase(latitude, longitude);

                    updateMapPosition();
                }
            };
        }
    }

    private void updateLocationInDatabase(final double latitude, final double longitude) {
        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put(Constants.LAST_SEEN, ServerValue.TIMESTAMP);
        locationMap.put(Constants.USER_LATITUDE, latitude);
        locationMap.put(Constants.USER_LONGITUDE, longitude);

        rootDatabaseRef.child(Constants.USERS_TABLE).child(mAuth.getUid()).child(Constants.USER_LOCATION)
                .setValue(locationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Error in uploading user coordinates");
                }
            }
        });
    }

    private String startLocationService() {
        Log.d(TAG, "startLocationService: Method was invoked!");

        if (UserLocationService.status == Constants.INACTIVE) {
            Intent intent = new Intent(getApplicationContext(), UserLocationService.class);
            startService(intent);

            return "Location Sharing is now Active";
        } else {
            return  "Location Sharing is already Active";
        }
    }

    private boolean checkGpsAvailable() {
        Log.d(TAG, "checkGpsAvailable: Method was invoked!");
        
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void enableGPS() {
        Log.d(TAG, "enableGPS: Method was invoked!");
        
        if (!checkGpsAvailable()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Please enable GPS in order to find your location")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, Constants.REQUEST_CODE_ENABLE_GPS);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Method was invoked");

        if (requestCode == Constants.REQUEST_CODE_ENABLE_GPS) {
            if (checkGpsAvailable()) {
                Toast.makeText(MapsActivity.this, "GPS is now enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "You must enable GPS in order to track your location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Method was invoked!");
        mMap = googleMap;
    }

    private void updateMapPosition() {
        if (userLocation == null) {
            Log.d(TAG, "updateMapPosition: Received null position");
            return;
        }


        if (mapPopulationTask.isAvailable()) {
            mMap.clear();
            mapPopulationTask.execute(mMap);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14.0f));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Method was invoked!");
        super.onResume();
        registerReceiver(mBroadcastReceiver , new IntentFilter(Constants.ACTION_LOCATION_UPDATE));
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Method was invoked!");
        unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Method was invoked!");
        stopLocationService();

        super.onDestroy();
    }

    private String stopLocationService() {
        Log.d(TAG, "stopLocationService: Method was invoked!");

        if (UserLocationService.status == Constants.ACTIVE) {
            Log.d(TAG, "onDestroy: Stopping Location Service");
            Intent intent = new Intent(getApplicationContext(), UserLocationService.class);

            stopService(intent);
            return "Location Sharing is now Stopped";
        } else {
            return "Location Sharing is already Stopped";
        }
    }
}
