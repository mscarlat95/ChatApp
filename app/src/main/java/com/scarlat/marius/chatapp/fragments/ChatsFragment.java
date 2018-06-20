package com.scarlat.marius.chatapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.ConversationListAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    
    private View rootView;
    private RecyclerView chatsRecylerView;
    private ConversationListAdapter adapter;

    private List<Conversation> conversations;

    private final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private final String userID = FirebaseAuth.getInstance().getUid();

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Method was invoked!");

        /* Inflate the framgent layout */
        rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        /* Init friends list */
        conversations = new ArrayList<>();

        /* Setup views */
        chatsRecylerView = rootView.findViewById(R.id.chatsRecyclerView);
        chatsRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ConversationListAdapter(getContext(), conversations);
        chatsRecylerView.setAdapter(adapter);

        loadConversations();

        return rootView;
    }

    private void loadConversations() {
        Log.d(TAG, "loadConversations: Method was invoked!");

        rootDatabaseRef.child(Constants.CHAT_TABLE).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "onDataChange: " + snapshot.toString());

                    conversations.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.exists()) {
                            final String friendID = dataSnapshot.getKey();
                            Conversation conversation = dataSnapshot.getValue(Conversation.class);

                            conversation.setFriendID(friendID);
                            conversations.add(conversation);
                        }
                    }

                    adapter.updateConversation(conversations);
//                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

}
