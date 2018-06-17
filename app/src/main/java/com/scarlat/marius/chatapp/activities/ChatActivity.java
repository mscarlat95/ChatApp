package com.scarlat.marius.chatapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.MessageListAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.GetTimeAgo;
import com.scarlat.marius.chatapp.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    /* Firebase */
    private DatabaseReference rootDatabaseRef;
    private String friendID, userID;

    /* Android Views */
    private Toolbar toolbar;
    private ImageButton sendImageButton, attachImageButton;
    private EditText messageEditText;
    private RecyclerView messagesRecylerView;
    private SwipeRefreshLayout messageSwipeRefreshLayout;

    /* For messages layout */
    private MessageListAdapter adapter;
    private List<Message> messages;

    /* Messages pagination */
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        /* Get Intent extra content */
        friendID = getIntent().getStringExtra(Constants.USER_ID);
        if (friendID == null) {
            Log.d(TAG, "onCreate: FriendID is NULL");
            
            /* TODO: Check from notification bundle data */
            finish();
        }

        /* Init message list */
        messages = new ArrayList<>();

        /* Setup firebase database */
        userID = FirebaseAuth.getInstance().getUid();
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();

        /* Init views */
        attachImageButton = (ImageButton) findViewById(R.id.attachImageButton);
        sendImageButton = (ImageButton) findViewById(R.id.sendImageButton);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        messagesRecylerView = (RecyclerView) findViewById(R.id.messagesRecylerView);
        messageSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.messageSwipeRefreshLayout);

        /* Init message layouts */
        adapter = new MessageListAdapter(this, messages);
        messagesRecylerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecylerView.setHasFixedSize(true); // TODO: check if it is required or not
        messagesRecylerView.setAdapter(adapter);

        /* Setup listeners */
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachFile();
            }
        });

        messageSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage ++;

                messages.clear();

                displayMessages();
            }
        });

        /* Setup toolbar */
        initToolbar();

        /* Setup chat in case that the users didn't communicate until now */
        initChat();

        /* Display messages */
        displayMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* User appears ONLINE */
        rootDatabaseRef.child(Constants.USERS_TABLE).child(userID).child(Constants.ONLINE).setValue("true");
    }

    private void initToolbar() {
        Log.d(TAG, "initToolbar: Method was invoked!");
        toolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);

        /* Obtain toolbar */
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        /* Inflate toolbar layout */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.friend_actionbar_layout, null);
        actionBar.setCustomView(actionBarView);

        /* Initialize layout views */
        final TextView fullnameTitle = (TextView) actionBarView.findViewById(R.id.fullnameTextView);
        final TextView lastSeenDescription = (TextView) actionBarView.findViewById(R.id.lastSeenTextView);
        final CircleImageView avatarCircleImageView = (CircleImageView) actionBarView.findViewById(R.id.avatarCircleImageView);

        rootDatabaseRef.child(Constants.USERS_TABLE).child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (! ChatActivity.this.isDestroyed()) {
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
                                lastSeenDescription.setText("Last seen: " + GetTimeAgo.getTimeAgo(Long.valueOf(online)));
                            }
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

    private void initChat() {
        Log.d(TAG, "initChat: Method was invoked!");

        rootDatabaseRef.child(Constants.CHAT_TABLE).child(userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        /* Users didn't communicate before */
                        if (!dataSnapshot.hasChild(friendID)) {
                            Map<String, Object> chatInfoMap = new HashMap<>();
                            chatInfoMap.put(Constants.SEEN, false);
                            chatInfoMap.put(Constants.TIMESTAMP, ServerValue.TIMESTAMP);
                            // TODO: add message content type

                            Map<String, Object> userChatMap = new HashMap<>();
                            userChatMap.put(Constants.CHAT_TABLE + "/" + userID + "/" + friendID, chatInfoMap);
                            userChatMap.put(Constants.CHAT_TABLE + "/" + friendID + "/" + userID, chatInfoMap);

                            rootDatabaseRef.updateChildren(userChatMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.d(TAG, "Adding new chat map Failed: " + databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Retrieving Messages Failed: " + databaseError.getMessage());
                    }
                });
    }

    private void displayMessages() {
        Log.d(TAG, "loadMessages: Method was invoked!");

        Query query = rootDatabaseRef.child(Constants.MESSAGES_TABLE).child(userID).child(friendID)
                .limitToLast(currentPage * Constants.MAX_LOAD_MESSAGES);

        query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d(TAG, "Load messages onChildAdded: " + dataSnapshot.toString());
                        Message message = dataSnapshot.getValue(Message.class);

                        messages.add(message);
                        adapter.notifyDataSetChanged();

                        int position = Math.max(0, messages.size() - currentPage * Constants.MAX_LOAD_MESSAGES - 1);
                        messagesRecylerView.scrollToPosition(position);
                        messageSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "Loading Messages Failed: " + databaseError.getMessage());
                    }
                });

    }

    private void sendMessage() {
        Log.d(TAG, "sendMessage: Method was invoked!");

        final String message = messageEditText.getText().toString();

        /* Message must be filled */
        if (message.isEmpty()) {
            Toast.makeText(ChatActivity.this, "You cannot send an empty message!", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Add message into the database */
        final String userReference = Constants.MESSAGES_TABLE + "/" + userID + "/" + friendID + "/";
        final String friendReference = Constants.MESSAGES_TABLE + "/" + friendID + "/" + userID + "/";

        String messageID = rootDatabaseRef.child(Constants.MESSAGES_TABLE)
                                            .child(userID).child(friendID).push()
                                            .getKey();

        /* Store message info */
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(Constants.MESSAGE_CONTENT, message);
        messageMap.put(Constants.SEEN, false);
        messageMap.put(Constants.MESSAGE_TYPE, Constants.MESSAGE_TYPE_TEXT);
        messageMap.put(Constants.TIMESTAMP, ServerValue.TIMESTAMP);
        messageMap.put(Constants.SOURCE, userID);

        /* Add message to both of the users */
        Map<String, Object> usersMessageMap = new HashMap<>();
        usersMessageMap.put(userReference + messageID, messageMap);
        usersMessageMap.put(friendReference + messageID, messageMap);

        rootDatabaseRef.updateChildren(usersMessageMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, "Adding new messages into the database Failed: " + databaseError.getMessage());
                }
            }
        });
        messageEditText.setText("");
    }

    private void attachFile() {
        Log.d(TAG, "attachFile: Method was invoked!");
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Method was invoked!");
        super.onPause();
    }

    @Override
    protected void onStop() {
        rootDatabaseRef.child(Constants.USERS_TABLE).child(userID).child(Constants.ONLINE)
                .onDisconnect().setValue(ServerValue.TIMESTAMP);
        Log.d(TAG, "onStop: Method was invoked!");
        super.onStop();
    }
}
