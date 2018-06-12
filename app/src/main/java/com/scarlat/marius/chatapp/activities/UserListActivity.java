package com.scarlat.marius.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.UserListAdapter;
import com.scarlat.marius.chatapp.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    /* Android views */
    private Toolbar toolbar;
    private RecyclerView usersListRecylerView;

    private UserListAdapter adapter;
    private List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        /* Check intent extra values */
        final Intent intent = getIntent();
        if (!intent.getBooleanExtra("all_users", true)) {
            Log.d(TAG, "onCreate: UserListActivity was accessed incorrectly");
            finish();
        }

        /* Setup toolbar */
        toolbar = (Toolbar) findViewById(R.id.userListToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         /* Init users list */
        users = new ArrayList<User>();

        /* Setup recycler view */
        usersListRecylerView = (RecyclerView) findViewById(R.id.usersListRecyclerView);
        usersListRecylerView.setLayoutManager(new LinearLayoutManager(this));

        /* Add adapter to the recyler view */
        adapter = new UserListAdapter(this, users);
        usersListRecylerView.setAdapter(adapter);

        populateUserList();
    }


    private void populateUserList() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Retrieving users from database");
        progressDialog.setMessage("Please wait until the users are obtained");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child("Users");

        dbReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.d(TAG, "onChildAdded: " + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    Log.d(TAG, "onChildAdded: Profile Image" + user.getProfileImage());
                    users.add(user);
                    adapter.notifyItemInserted(users.size() - 1);

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                progressDialog.dismiss();
//                Toast.makeText(UserListActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
