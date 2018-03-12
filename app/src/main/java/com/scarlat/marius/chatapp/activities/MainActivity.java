package com.scarlat.marius.chatapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;
import com.scarlat.marius.chatapp.util.MainTabsAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    private ViewPager viewPager;
    private MainTabsAdapter pagerAdapter;
    private TabLayout mainTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase authentification
        mAuth = FirebaseAuth.getInstance();

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS");

        // Set up tabs
        viewPager = (ViewPager) findViewById(R.id.mainTabsViewPager);

        // Link each tab to a fragment (Requests, Chat, Friends)
        pagerAdapter = new MainTabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Link viewPager with tab layout
        mainTabLayout = (TabLayout) findViewById(R.id.mainTabLayout);
        mainTabLayout.setupWithViewPager(viewPager);
        mainTabLayout.setTabTextColors(Color.WHITE /*normal color*/, Color.YELLOW /*selected color*/);
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // User is not logged in
            Log.d(Constants.USER_LOGIN_TAG, "User is not signed in");
            launchLoginActivity();
        } else { // User is logged in
            Log.d(Constants.USER_LOGIN_TAG, "User is signed in. Current user: " + currentUser.getEmail());
        }
    }

    private void launchLoginActivity() {
        // Launch Login Activity
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.putExtra("login", true);
        startActivity(loginIntent);

        // User cannot go back to main activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate main_menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menuLogoutButton) {
            FirebaseAuth.getInstance().signOut();
            Log.d(Constants.USER_LOGOUT_TAG, "Successful");

            launchLoginActivity();
        }

        return true;
    }


}
