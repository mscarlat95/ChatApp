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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.adapter.FriendRequestsAdapter;
import com.scarlat.marius.chatapp.general.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestsFragment extends Fragment {
    private static final String TAG = "FriendRequestsFragment";

    private View rootView;
    private RecyclerView friendRequestsRecylerView;
    private FriendRequestsAdapter adapter;

    private List<String> requestsFriendID;

    public FriendRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: Method was invoked!");

           /* Inflate the framgent layout */
        rootView = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        /* Init friends list */
        requestsFriendID = new ArrayList<>();

        /* Setup views */
        friendRequestsRecylerView = rootView.findViewById(R.id.friendRequestsRecylerView);
        friendRequestsRecylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendRequestsAdapter(getContext(), requestsFriendID);
        friendRequestsRecylerView.setAdapter(adapter);
        loadRequests();

        return rootView;
    }

    private void loadRequests() {
        Log.d(TAG, "loadRequests: Method was invoked!");

        FirebaseDatabase.getInstance().getReference().child(Constants.FRIEND_REQUESTS_TABLE)
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                            requestsFriendID.clear();

                            for (DataSnapshot friend : dataSnapshot.getChildren()) {
                                requestsFriendID.add(friend.getKey());
                                Log.d(TAG, "onDataChange: " + friend.getKey());
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
    }

}
