package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.activities.UserProfileActivity;
import com.scarlat.marius.chatapp.adapter.MapMarkerInformationAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.DateTimeUtil;
import com.scarlat.marius.chatapp.model.MapMarker;
import com.scarlat.marius.chatapp.model.User;
import com.scarlat.marius.chatapp.storage.SharedPref;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class PopulateMapTask {
    private static final String TAG = "PopulateMapTask";

    private Context context;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();

    private boolean available = false;

    public boolean isAvailable() { return available; }

    public PopulateMapTask(Context context) {
        this.context = context;
        available = true;
    }

    public void execute(final GoogleMap map) {
        Log.d(TAG, "addMarkers: Method was invoked!");

        available = false;
        Log.d(TAG, "Map is locked");

        /* Add current user on the map */
        rootDatabaseRef.child(Constants.USERS_TABLE).child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    user.setUserId(dataSnapshot.getKey());
                    addMarker(map, user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "addMarkers: onCancelled: " + databaseError.getMessage());
            }
        });

        /* Add user friends on the map */
        rootDatabaseRef.child(Constants.FRIENDS_TABLE).child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    /* For each user check get last location */
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Log.d(TAG, "addMarkers: onDataChange: Checking for child " + child.getKey());

                        rootDatabaseRef.child(Constants.USERS_TABLE).child(child.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final User user = dataSnapshot.getValue(User.class);

                                user.setUserId(dataSnapshot.getKey());
                                addMarker(map, user);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "addMarkers: onCancelled: " + databaseError.getMessage());
                            }
                        });
                    }

                    available = true;
                    Log.d(TAG, "Map is available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "addMarkers: onCancelled: " + databaseError.getMessage());
                available = true;
            }
        });
    }

    private LatLng getCurrentUserLocation() {
        Log.d(TAG, "getCurrentUserLocation: Method was invoked!");

        SharedPref.setup(context);
        final String latitude = SharedPref.getString(Constants.USER_LATITUDE);
        final String longitude = SharedPref.getString(Constants.USER_LONGITUDE);

        if (latitude.equals("") || longitude.equals("")) {
            Log.d(TAG, "getCurrentUserLocation: Invalid coordinates");
            return null;
        }

        return new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }

    private String decodeAddress(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(context);
        String address = Constants.UNSET;

        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

            address =   getValue(addressList.get(0).getThoroughfare(), ", ") +
                        getValue(addressList.get(0).getLocality(), ", ") +
                        getValue(addressList.get(0).getCountryName(), "");
        } catch (IOException e) {
            Log.d(TAG, "decodeAddress: [Exception] "  + e.getMessage());
        }

        return address;
    }

    private void addMarker(final GoogleMap map, final User user) {
        Log.d(TAG, "addMarker: Method was invoked!");

        if (user.getLocation() == null) {
            Log.d(TAG, "addMarker: Current friend hasn't accessed map, yet.");
            return;
        }

        /* Friend information */
        final String title = user.getFullname();
        final LatLng location = new LatLng(
                Double.valueOf(user.getLocation().get(Constants.USER_LATITUDE).toString()),
                Double.valueOf(user.getLocation().get(Constants.USER_LONGITUDE).toString()));

        /* Get address */
        final String address = decodeAddress(location.latitude, location.longitude);

        /* Calculate distance between current user and its friend */
        float[] resultDistance = new float[1];
        final LatLng currentUserLocation = getCurrentUserLocation();
        if (currentUserLocation != null) {
            Location.distanceBetween(
                    currentUserLocation.latitude,
                    currentUserLocation.longitude,
                    location.latitude,
                    location.longitude,
                    resultDistance);
        }
        final String distance = String.format("%.2f", (resultDistance[0] / 1000)) + "km";

        String lastUpdate = DateTimeUtil.getTimeAgo(Long.parseLong(user.getLocation().get(Constants.LAST_SEEN).toString()));
        if (lastUpdate.equals(Constants.UNSET)) {
            /* Prevent server time differences */
            lastUpdate = "just now";
        }

        /* Build snippet */
        String snippet =    "Email: " + user.getEmail() +
                            "\nLast Update: " + lastUpdate;
        if (!user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
            snippet += "\nApproximate distance: " + distance;
        }
        if (address != null && !address.equals(Constants.UNSET)) {
            snippet += "\nAddress: " + address;
        }

        /* Display marker on map */
        Marker marker = map.addMarker(new MarkerOptions()
                .title(title)
                .snippet(snippet)
                .anchor(0.5f, 1)
                .position(location));

        marker.setTag(user.getUserId());

        map.setInfoWindowAdapter(new MapMarkerInformationAdapter(context));

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() != null) {
                    final String userID = marker.getTag().toString();
                    Intent intent = new Intent(context, UserProfileActivity.class);

                    intent.putExtra(Constants.USER_ID, userID);
                    context.startActivity(intent);
                }
            }
        });

        // TODO: https://stackoverflow.com/questions/44101664/marker-seticon-throws-java-lang-illegalargumentexception-unmanaged-descriptor
        try {

            MapMarker point = new MapMarker(context, marker);
            Picasso.with(context)
                    .load(Uri.parse(user.getProfileImage()))
                    .into(point);

            if (user.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                marker.setZIndex(1.0f);
            }

        } catch (Exception e) {}
    }


    private String getValue(String str, String suffix) {
        if (str == null) {
            return "";
        }
        return str + suffix;
    }
}
