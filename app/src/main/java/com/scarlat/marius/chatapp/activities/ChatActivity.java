package com.scarlat.marius.chatapp.activities;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.scarlat.marius.chatapp.general.DateTimeUtil;
import com.scarlat.marius.chatapp.model.Message;
import com.scarlat.marius.chatapp.tasks.SendMessageTask;
import com.scarlat.marius.chatapp.tasks.SendPhotoTask;

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

    private HashMap<Long, String> messageSet = new HashMap<>();

    /* Messages pagination */
    private int currentPage =  0;
    private boolean firstContact = true;

    private boolean isDuplicate (Message message) {
        if (messageSet.containsKey(message.getTimestamp())) {
            if (messageSet.get(message.getTimestamp()).equals(message.getMessage())) {
                return true;
            }
        }

        messageSet.put(message.getTimestamp(), message.getMessage());
        return false;
    }

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
                sendTextMessage();
            }
        });
        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachFromGallery();
            }
        });

        messageSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (messages.size() == 0) {
                    messageSwipeRefreshLayout.setRefreshing(false);
                    return ;
                }

                if (currentPage == 0) { currentPage = 1; }
                currentPage ++;

                messages.clear();
                messageSet = new HashMap<Long, String>();
                displayMessages();
            }
        });

        /* Setup toolbar */
        initToolbar();

        /* Setup chat in case that the users didn't communicate until now */
        initChat();

        /* Display messages */
        displayMessages();
        
        /* All messages are seen by user */
        rootDatabaseRef.child(Constants.CHAT_TABLE).child(userID).child(friendID).child(Constants.SEEN)
                .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: SEEN error");
                }
            }
        });
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
                                lastSeenDescription.setText("Last seen: " + DateTimeUtil.getTimeAgo(Long.valueOf(online)));
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

        avatarCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);

                intent.putExtra(Constants.USER_ID, friendID);
                startActivity(intent);
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

        Query query = null;

        if (firstContact) {
            query = rootDatabaseRef.child(Constants.MESSAGES_TABLE).child(userID).child(friendID)
                    .limitToLast( (currentPage + 1) * Constants.MAX_LOAD_MESSAGES);

            firstContact = false;
        } else {
            query = rootDatabaseRef.child(Constants.MESSAGES_TABLE).child(userID).child(friendID)
                    .limitToLast(currentPage * Constants.MAX_LOAD_MESSAGES);
        }


        query.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d(TAG, "Load messages onChildAdded: " + dataSnapshot.toString());
                        Message message = dataSnapshot.getValue(Message.class);

                        if (isDuplicate(message)) {
                            return;
                        }

                        messages.add(message);
                        adapter.notifyItemInserted(messages.size() - 1);

                        int position = Math.max(0, messages.size() - currentPage * Constants.MAX_LOAD_MESSAGES - 1);

                        messagesRecylerView.smoothScrollToPosition(position);
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

    private void sendTextMessage() {
        Log.d(TAG, "sendTextMessage: Method was invoked!");

        final String message = messageEditText.getText().toString();
        if (message.trim().length() == 0) {
            Toast.makeText(ChatActivity.this, "You cannot send an empty message!", Toast.LENGTH_SHORT).show();
            return;
        }

        new SendMessageTask(this, friendID).execute(message, Constants.MESSAGE_TYPE_TEXT);
        messageEditText.setText("");
        currentPage = 0;
    }

    private void attachFromGallery() {
        Log.d(TAG, "importFromGallery: Method was invoked!");
        Intent galleryIntent = new Intent();

        /* Open gallery */
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select image"), Constants.REQUEST_CODE_READ_EXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                /* Send image imported from gallery */
                case Constants.REQUEST_CODE_READ_EXT:
                    new SendPhotoTask(this, friendID).execute(data.getData());
                    break;

                default:
                    Log.d(TAG, "onActivityResult: Undefined request code " + requestCode);
                    break;
            }
        }
    }

    @Override
    protected void onStop() {
        rootDatabaseRef.child(Constants.USERS_TABLE).child(userID).child(Constants.ONLINE)
                .onDisconnect().setValue(ServerValue.TIMESTAMP);
        Log.d(TAG, "onStop: Method was invoked!");
        super.onStop();
    }
}
