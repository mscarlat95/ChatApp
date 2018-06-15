package com.scarlat.marius.chatapp.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.FriendListAdapter;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    /* Android Views */
    private View rootView;
    private RecyclerView friendsRecyclerView;
    private FriendListAdapter adapter;

    private List<Friend> friends;

    final DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();

    public FriendsFragment() {
        Log.d(TAG, "FriendsFragment: Constructor has been invoked!");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Method was invoked!");

        /* Inflate the framgent layout */
        rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        /* Init friends list */
        friends = new ArrayList<>();

        /* Setup views */
        friendsRecyclerView = rootView.findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendListAdapter(getContext(), friends);
        friendsRecyclerView.setAdapter(adapter);
        populateFriendsList();

        return rootView;
    }

    private void populateFriendsList() {
        Log.d(TAG, "populateFriendsList: Method was invoked!");

        rootDatabaseRef.child(Constants.FRIENDS_TABLE).keepSynced(true);

        rootDatabaseRef.child(Constants.FRIENDS_TABLE)
                .child(FirebaseAuth.getInstance().getUid())
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onChildAdded: Friend = " + dataSnapshot.toString());

                    /* Setup friend information */
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    friend.setFriendId(dataSnapshot.getKey());
                    friends.add(friend);

                    adapter.setFriends(friends);
                    adapter.notifyItemInserted(friends.size() -1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }


}
