package com.scarlat.marius.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private String friendID;

    private Toolbar toolbar;

    private DatabaseReference rootDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* Get Intent extra content */
        final Intent intent = getIntent();

        friendID = intent.getStringExtra(Constants.USER_ID);
        if (friendID == null) {
            Log.d(TAG, "onCreate: FriendID is NULL");
            
            /* TODO: Check from notification bundle data */
            finish();
        }

        /* Setup toolbar */
        toolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.friend_actionbar_layout, null);
        actionBar.setCustomView(actionBarView);

        final TextView fullnameTitle = (TextView) actionBarView.findViewById(R.id.fullnameTextView);
        final TextView lastSeenDescription = (TextView) actionBarView.findViewById(R.id.lastSeenTextView);
        final CircleImageView avatarCircleImageView = (CircleImageView) actionBarView.findViewById(R.id.avatarCircleImageView);


        /* Setup firebase database */
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        rootDatabaseRef.child(Constants.USERS_TABLE).child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Retrieve Friend information: " + dataSnapshot.toString());

                    final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                    final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();

                    fullnameTitle.setText(fullname);
                    Glide.with(ChatActivity.this)
                            .load(profileImage)
                            .into(avatarCircleImageView);

                    if (dataSnapshot.hasChild(Constants.ONLINE)) {
                        final String online = dataSnapshot.child(Constants.ONLINE).getValue().toString();

                        if (online.equals("true")) {
                            lastSeenDescription.setText(Constants.ONLINE);
                        } else {
                            lastSeenDescription.setText("Last seen: " + DateFormat.format("MMM-dd-yyyy, HH:mm", Long.valueOf(online)));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "Retrieve Friend information failed: " + databaseError.getMessage());
            }
        });
    }
}
