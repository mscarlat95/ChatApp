package com.scarlat.marius.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.threads.GetFriendInfoTask;
import com.scarlat.marius.chatapp.threads.SendFriendRequestTask;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;
    private Button sendFriendRequestButton,declineFriendRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        /* Check intent extra values */
        final Intent intent = getIntent();
        final String friendID = intent.getStringExtra(Constants.USER_ID);
        Log.d(TAG, "onCreate: Friend Id = " + friendID);


        /* Setup Android Views */
        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        fullNameTextView = (TextView) findViewById(R.id.fullNameTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        friendsNumberTextView = (TextView) findViewById(R.id.friendsNumberTextView);
        sendFriendRequestButton = (Button) findViewById(R.id.sendFriendRequestButton);
        declineFriendRequestButton = (Button) findViewById(R.id.declineFriendRequestButton);

        declineFriendRequestButton.setVisibility(View.INVISIBLE);

        sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendFriendRequestTask(UserProfileActivity.this, friendID,
                        sendFriendRequestButton, declineFriendRequestButton).execute();
            }
        });



        /* Display user information */
        new GetFriendInfoTask(  this, friendID, profileImageView, fullNameTextView,
                                statusTextView, friendsNumberTextView).execute();

    }
}
