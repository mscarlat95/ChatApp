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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.OpportunisticMessagesAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Message;
import com.scarlat.marius.chatapp.storage.SharedPref;

import java.util.ArrayList;
import java.util.List;

import ro.pub.acs.hyccups.opportunistic.Connection;

public class OpportunisticChatActivity extends AppCompatActivity {
    private static final String TAG = "OpportunisticChatActivi";

    /* Android Views */
    private Toolbar toolbar;
    private ImageButton sendImageButton;
    private EditText messageEditText;
    private RecyclerView messagesRecylerView;

    private OpportunisticMessagesAdapter adapter;

    /* For opportunistic communication */
    private String friendId, friendName;
    private Connection connection;
    private BroadcastReceiver broadcastReceiver;

    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opportunistic_chat);

        /* Obtain friend information */
        friendId = getIntent().getStringExtra(Constants.USER_ID);
        friendName = getIntent().getStringExtra(Constants.FULLNAME);
        if (friendId == null) {
            Log.d(TAG, "onCreate: Friend ID is null");
            finish();
        }

        /* Init shared preferences */
        SharedPref.setup(this);

        /* Init broadcast receiver */
        setupBroadcastReceiver();

        /*  Setup Toolbar */
        toolbar = (Toolbar) findViewById(R.id.opportunisticChatToolbar);
        setSupportActionBar(toolbar);
        if (friendName.equals("")) {
            getSupportActionBar().setTitle("Friend " + friendId);
        } else {
            getSupportActionBar().setTitle(friendName);
        }

        /* Init messages */
        messages = new ArrayList<>();

        /* Init Views */
        messagesRecylerView = (RecyclerView) findViewById(R.id.messagesRecylerView);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendImageButton = (ImageButton) findViewById(R.id.sendImageButton);
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        /* Setup RecyclerView */
        adapter = new OpportunisticMessagesAdapter(this, messages);
        messagesRecylerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecylerView.setAdapter(adapter);

        /* Init communication channel */
        connection = new Connection(getApplicationContext(), "OpportunisticChannelDemo");
    }


    private void sendMessage() {
        final String messageContent = messageEditText.getText().toString();

        if (messageContent.trim().length() == 0) {
            Toast.makeText(OpportunisticChatActivity.this, "You cannot send an empty message!", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Update message list */
        Message message = new Message();
        message.setFrom(SharedPref.getString(Constants.USER_ID));
        message.setMessage(messageContent);
        message.setType(Constants.MESSAGE_TYPE_TEXT);
        message.setTimestamp(System.currentTimeMillis());
        messages.add(message);
        adapter.notifyDataSetChanged();

        /* Send message to friend */
        connection.forward(friendId, messageContent);

        messageEditText.setText("");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Method was invoked!");
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(Constants.ACTION_MESSAGE_RECEIVED));
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Method was invoked!");
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private void setupBroadcastReceiver() {
        Log.d(TAG, "setupBroadcastReceiver: Method was invoked!");

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "onReceive: Method was invoked!");
                    final String action = intent.getAction();

                    if (action.equals(Constants.ACTION_MESSAGE_RECEIVED)) {
                        /* Retrieve incoming messages */
                        receiveMessage(intent);
                    }
                }
            };
        }
    }

    private void receiveMessage(Intent intent) {
        Log.d(TAG, "receiveMessage: Method was invoked!");

        final String friendId = intent.getStringExtra(Constants.USER_ID);
        final String messageContent = intent.getStringExtra(Constants.MESSAGE_CONTENT);
        final long timestamp = intent.getLongExtra(Constants.TIMESTAMP, 0);

        Message message = new Message();
        message.setFrom(friendId);
        message.setMessage(messageContent);
        message.setType(Constants.MESSAGE_TYPE_TEXT);
        message.setTimestamp(timestamp);

        messages.add(message);
        adapter.notifyDataSetChanged();

        messagesRecylerView.smoothScrollToPosition(messages.size() - 1);
    }

}
