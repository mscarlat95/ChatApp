package com.scarlat.marius.chatapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.FragmentTabsAdapter;
import com.scarlat.marius.chatapp.general.AndroidUtil;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.storage.SharedPref;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    /* Firebase */
    private FirebaseAuth mAuth;

    /* Android views */
    private Toolbar toolbar;
    private ViewPager viewPager;
    private FragmentTabsAdapter pagerAdapter;
    private TabLayout mainTabLayout;

    private DatabaseReference rootDatabaseRef;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.userLogoutItem:
                Log.d(TAG, "onOptionsItemSelected: Logout");
                logout();
                return true;

            case R.id.profileSettingsItem:
                Log.d(TAG, "onOptionsItemSelected: Profile Settings");
                launchActivity(MainActivity.this, ProfileSettingsActivity.class);
                return true;

            case R.id.allUsersItem:
                Log.d(TAG, "onOptionsItemSelected: All Users");
                launchActivity(MainActivity.this, UserListActivity.class);
                return true;

            case R.id.shareLocationItem:
                Log.d(TAG, "onOptionsItemSelected: Share Location");
                launchActivity(MainActivity.this, MapsActivity.class);
                return true;

            case R.id.offlineModeItem:
                Log.d(TAG, "onOptionsItemSelected: Offline Features");
                launchActivity(MainActivity.this, OfflineFeaturesActivity.class);
                return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize Firebase authentification */
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        }

        /* Init Shared preferences */
        SharedPref.setup(this);

        /*  Set Toolbar */
        toolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS");

        /* Setup Tabs */
        viewPager = (ViewPager) findViewById(R.id.mainTabsViewPager);
        pagerAdapter = new FragmentTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mainTabLayout.setupWithViewPager(viewPager);
        mainTabLayout.setTabTextColors( Color.parseColor(Constants.COLOR_INACTIVE_TAB),
                                        Color.parseColor(Constants.COLOR_ACTIVE_TAB));
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: Method was invoked!");
        super.onStart();

        /* Perform sign in if user is not logged in */
        if (mAuth.getCurrentUser() == null) {
            launchActivity(MainActivity.this, LoginActivity.class);
            finish();
        } else {
           /* Request permissions */
            enablePermissions();

            /* User appears ONLINE */
            rootDatabaseRef.child(Constants.USERS_TABLE).child(mAuth.getUid())
                    .child(Constants.ONLINE).setValue("true");

            /* Check if Hyccups is installed */
            if (AndroidUtil.isAppInstalled(this, Constants.HYCCUPS_PACKAGE_NAME)) {
                Log.d(TAG, "onStart: Hyccups is already installed");
            } else {
                Log.d(TAG, "onStart: Hyccups is not installed");
                installHyccups();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Method was invoked!");
        super.onStop();

        if (mAuth.getCurrentUser() != null) {
            rootDatabaseRef.child(Constants.USERS_TABLE).child(mAuth.getUid())
                    .child(Constants.ONLINE).setValue(ServerValue.TIMESTAMP);
        }
    }


    private void logout() {
        Log.d(TAG, "logout: Method was invoked!");

        /* First, save last timestamp */
        rootDatabaseRef.child(Constants.USERS_TABLE).child(FirebaseAuth.getInstance().getUid()).child(Constants.ONLINE)
                .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                /* Perform logout */
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Saved timestamp");
                    AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: Logout Successful");

                                launchActivity (MainActivity.this, LoginActivity.class);
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void launchActivity (Context srcContext, Class destClass) {
        Log.d(TAG, "launchActivity: Method was invoked!");
        Intent intent = new Intent(srcContext, destClass);
        startActivity(intent);
    }

    private void enablePermissions() {
        Log.d(TAG, "requestPermissions: Method was invoked!");

        if (Build.VERSION.SDK_INT >= 23) {
            // TODO: create a single request code
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, Constants.REQUEST_CODE_READ_EXT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Method was invoked!");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.REQUEST_CODE_READ_EXT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Read External permissions GRANTED");
                }
                break;

            case Constants.REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Camera permissions GRANTED");
                }
                break;

            case Constants.REQUEST_CODE_WRITE_EXT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Write External permissions GRANTED");
                }
                break;
        }
    }

    private void installHyccups() {
        Log.d(TAG, "installHyccups: Method was invoked!");

        if (SharedPref.getString(Constants.HYCCUPS_PREFERENCES).equals(Constants.HIDE_INSTALL)) {
            Log.d(TAG, "installHyccups: Hide dialog");
            return;
        }

        final String[] availableOptions = { "Install Hyccups", "Cancel",  "Don't remind me" };
        new AlertDialog.Builder(this)
                .setTitle("Hyccups was not found")
                .setItems(availableOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Log.d(TAG, "onClick: Install Hyccups");
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_HYCCUPS_HOME));
                                startActivity(browserIntent);
                                break;
                            case 1:
                                Log.d(TAG, "onClick: Cancel");
                                break;
                            case 2:
                                Log.d(TAG, "onClick: Don't remind me");
                                SharedPref.saveString(Constants.HYCCUPS_PREFERENCES, Constants.HIDE_INSTALL);
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }
}
