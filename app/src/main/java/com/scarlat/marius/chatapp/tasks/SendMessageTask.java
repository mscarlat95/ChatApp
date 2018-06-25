package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.scarlat.marius.chatapp.general.Constants;

import java.util.HashMap;
import java.util.Map;

public class SendMessageTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "SendMessageTask";

    private Context context;
    private String userID, friendID;
    private DatabaseReference rootDatabaseRef;

    public SendMessageTask(Context context, String friendID) {
        this.context = context;
        this.friendID = friendID;
    }

    @Override
    protected void onPreExecute() {
        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userID = FirebaseAuth.getInstance().getUid();
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        String content = params[0];
        String contentType = params[1];

        /* Add message into the database */
        String messageID = rootDatabaseRef.child(Constants.MESSAGES_TABLE)
                .child(userID).child(friendID).push()
                .getKey();

        final String userReference = Constants.MESSAGES_TABLE + "/" + userID + "/" + friendID + "/";
        final String friendReference = Constants.MESSAGES_TABLE + "/" + friendID + "/" + userID + "/";

        /* Store message info */
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put(Constants.MESSAGE_CONTENT, content);
        messageMap.put(Constants.SEEN, false);
        messageMap.put(Constants.MESSAGE_TYPE, contentType);
        messageMap.put(Constants.TIMESTAMP, ServerValue.TIMESTAMP);
        messageMap.put(Constants.SOURCE, userID);

        /* Add message to both of the users */
        Map<String, Object> usersMessageMap = new HashMap<>();
        usersMessageMap.put(userReference + messageID, messageMap);
        usersMessageMap.put(friendReference + messageID, messageMap);

        rootDatabaseRef.child(Constants.CHAT_TABLE).child(userID).child(friendID).child(Constants.SEEN).setValue(true);
        rootDatabaseRef.child(Constants.CHAT_TABLE).child(userID).child(friendID).child(Constants.TIMESTAMP).setValue(ServerValue.TIMESTAMP);

        rootDatabaseRef.child(Constants.CHAT_TABLE).child(friendID).child(userID).child(Constants.SEEN).setValue(false);
        rootDatabaseRef.child(Constants.CHAT_TABLE).child(friendID).child(userID).child(Constants.TIMESTAMP).setValue(ServerValue.TIMESTAMP);

        rootDatabaseRef.updateChildren(usersMessageMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d(TAG, "Adding new messages into the database Failed: " + databaseError.getMessage());
                }
            }
        });

        return null;
    }
}
