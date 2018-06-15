package com.scarlat.marius.chatapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
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

    /* Inflate the search bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Method was invoked!");
        getMenuInflater().inflate(R.menu.user_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.searchBarItem);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchViewListener());
        searchView.onActionViewExpanded();
        searchView.setQueryHint("Search Users");

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

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

        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE);

        dbReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    user.setUserId(dataSnapshot.getKey());

                    if (!FirebaseAuth.getInstance().getUid().equals(user.getUserId())) {
                        Log.d(TAG, "onChildAdded: Profile Image" + user.getProfileImage());
                        users.add(user);

                        adapter.setFilter(users);
                        adapter.notifyItemInserted(users.size() - 1);
                    }

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
            }
        });
    }


    /* Filter users using the search bar */
    class SearchViewListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) { return false; }

        @Override
        public boolean onQueryTextChange(String newText) {
            /* Update list */
            List<User> filteredUsers = new ArrayList<>();

            newText = newText.toLowerCase();
            for (User user : users) {
                String fullName = user.getFullname().toLowerCase();

                if (fullName.contains(newText)) {
                    filteredUsers.add(user);
                }
            }

            Log.d(TAG, "onQueryTextChange: " + filteredUsers.toString());

            adapter.setFilter(filteredUsers);
            return true;
        }
    };
}
