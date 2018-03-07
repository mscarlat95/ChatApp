package com.scarlat.marius.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) { // User is not logged in
            Log.d(Constants.USER_STATUS_TAG, "Not signed in");
            launchLoginActivity();
        } else { // User is logged in
            Log.d(Constants.USER_STATUS_TAG, "Signed in");
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
            launchLoginActivity();
        }

        return true;
    }


}
