package com.scarlat.marius.chatapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.FragmentTabsAdapter;
import com.scarlat.marius.chatapp.general.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    private ViewPager viewPager;
    private FragmentTabsAdapter pagerAdapter;
    private TabLayout mainTabLayout;

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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            launchActivity (MainActivity.this, LoginActivity.class);
            finish();
        }

        /* Request permissions */
        enablePermissions();

        /* User appears ONLINE */
        FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(mAuth.getUid())
                .child(Constants.ONLINE).setValue(true);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: Method was invoked!");
        super.onStop();

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(mAuth.getUid())
                .child(Constants.ONLINE).setValue(false);
    }


    private void logout() {
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

    private void launchActivity (Context srcContext, Class destClass) {
        Log.d(TAG, "launchActivity: Method was invoked!");
        Intent intent = new Intent(srcContext, destClass);
        startActivity(intent);
    }

    private void enablePermissions() {
        Log.d(TAG, "requestPermissions: Method was invoked!");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*Marshmallow*/) {
//            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_READ_EXT);
//            }
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CODE_CAMERA);
//            }
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_WRITE_EXT);
//            }
//        }

        requestPermissions(new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, Constants.REQUEST_CODE_READ_EXT);
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
}
