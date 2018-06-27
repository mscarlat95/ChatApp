package com.scarlat.marius.chatapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.InRangeFriendsAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Friend;
import com.scarlat.marius.chatapp.storage.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class OfflineFeaturesActivity extends AppCompatActivity {

    private static final String TAG = "OfflineFeaturesActivity";

    private Toolbar toolbar;
    private RecyclerView inRangeUsersRecyclerView;
    private BroadcastReceiver broadcastReceiver;


    private List<Friend> inRangeFriends;
    private InRangeFriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked !");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_features);

        /*  Setup Toolbar */
        toolbar = (Toolbar) findViewById(R.id.offlineFeaturesToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Friends Near You");

        /* Initialize shared preferences */
        SharedPref.setup(getApplicationContext());

        /* Initialize broadcast receiver which will communicate with the Opportunistic channel */
        setupBroadcastReceiver();

        /* Init list of friends */
        inRangeFriends = new ArrayList<>();

        /* Initialize recycler view */
        adapter = new InRangeFriendsAdapter(this, inRangeFriends);
        inRangeUsersRecyclerView = (RecyclerView) findViewById(R.id.inRangeUsersRecyclerView);
        inRangeUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inRangeUsersRecyclerView.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Method was invoked!");
        super.onResume();
        addFilters();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Method was invoked!");
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void addFilters() {
        Log.d(TAG, "addFilters: Method was invoked!");
        IntentFilter filters = new IntentFilter();
        filters.addAction(Constants.ACTION_PEER_CONNECTED);
        filters.addAction(Constants.ACTION_PEER_DISCONNECTED);
        filters.addAction(Constants.ACTION_MESSAGE_RECEIVED);

        registerReceiver(broadcastReceiver, filters);
    }

    private void setupBroadcastReceiver() {
        Log.d(TAG, "setupBroadcastReceiver: Method was invoked!");
        
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();

                    switch (action) {
                        case Constants.ACTION_PEER_CONNECTED:
                            Log.d(TAG, "onReceive: ACTION_PEER_CONNECTED");
                            addFriend(intent);
                            break;

                        case Constants.ACTION_PEER_DISCONNECTED:
                            Log.d(TAG, "onReceive: ACTION_PEER_DISCONNECTED");
                            removeFriend(intent);
                            break;

                        case Constants.ACTION_MESSAGE_RECEIVED:
                            messageNotify(intent);
                            Log.d(TAG, "onReceive: ACTION_MESSAGE_RECEIVED");
                            break;

                        default:
                            Log.d(TAG, "onReceive: Broadcast receiver retrieve unkown action: " + action);
                            break;
                    }

                    String id = intent.getStringExtra(Constants.USER_ID);
                    Toast.makeText(OfflineFeaturesActivity.this, id, Toast.LENGTH_SHORT).show();
                }
            };
        }
    }


    private void addFriend(Intent intent) {
        final String friendId = intent.getStringExtra(Constants.USER_ID);
        final String friendName = SharedPref.getString(friendId);
        Log.d(TAG, "addFriend: "  + friendId + " > " + friendName);

        Friend friend = new Friend();
        friend.setFriendId(friendId);
        friend.setFriendshipDate(System.currentTimeMillis());

        inRangeFriends.add(friend);
        adapter.notifyDataSetChanged();
    }


    private void removeFriend(Intent intent) {
        final String friendId = intent.getStringExtra(Constants.USER_ID);
        final String friendName = SharedPref.getString(friendId);
        Log.d(TAG, "removeFriend: "  + friendId + " > " + friendName);

        int index = -1;
        for (int i = 0; i < inRangeFriends.size(); ++i) {
            if (inRangeFriends.get(i).getFriendId().equals(friendId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            inRangeFriends.remove(index);
            adapter.notifyDataSetChanged();
        }
    }

    private void messageNotify(Intent intent) {
        final String friendId = intent.getStringExtra(Constants.USER_ID);
        final String message = intent.getStringExtra(Constants.MESSAGE_CONTENT);
        final String timestamp = intent.getStringExtra(Constants.TIMESTAMP);
        final String friendName = SharedPref.getString(friendId);

        Log.d(TAG, "messageNotify: From = "  + friendId + " > " + friendName);
        Log.d(TAG, "messageNotify: Message = " + message);
        Log.d(TAG, "messageNotify: Timestamp = " + timestamp);

        Toast.makeText(this, "You have received a new message from "
                + friendName + " [" + friendId + "]", Toast.LENGTH_SHORT).show();
    }

    
}
