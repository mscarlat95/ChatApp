package com.scarlat.marius.chatapp.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.OpportunisticChatActivity;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.Friend;
import com.scarlat.marius.chatapp.storage.SharedPref;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InRangeFriendsAdapter extends RecyclerView.Adapter<InRangeFriendsAdapter.InRangeFriendsViewHolder>{

    private static final String TAG = "InRangeFriendsAdapter";
    private Context context;
    private List<Friend> inRangeFriends;


    public InRangeFriendsAdapter(Context context, List<Friend> inRangeFriends) {
        this.context = context;
        this.inRangeFriends = inRangeFriends;

        SharedPref.setup(context);
    }

    @NonNull
    @Override
    public InRangeFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_info_layout, parent, false);

        return new InRangeFriendsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InRangeFriendsViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        final String friendId = inRangeFriends.get(position).getFriendId();
        final String friendName = SharedPref.getString(friendId);
        final long lastSeen = inRangeFriends.get(position).getFriendshipDate();

        holder.getRootView().setTag(friendId);

        /* Init views */
        if (friendName.equals("")) {
            holder.getFullNameTextView().setText("Friend " + friendId);
        } else {
            holder.getFullNameTextView().setText(friendName);
        }
        holder.getDateTextView().setText( "Last seen: " + DateFormat.format("HH:mm:ss", lastSeen));

        /* Start chat */
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OpportunisticChatActivity.class);
                intent.putExtra(Constants.USER_ID, friendId);
                intent.putExtra(Constants.FULLNAME, friendName);

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + inRangeFriends.size());
        return inRangeFriends.size();
    }

    class InRangeFriendsViewHolder extends RecyclerView.ViewHolder {
        private View rootView;

        private CircleImageView avatarCircleImageView;
        private TextView fullNameTextView;
        private TextView dateTextView;

        /* Setters and getters */
        View getRootView() { return rootView; }
        CircleImageView getAvatarCircleImageView() { return avatarCircleImageView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        TextView getDateTextView() { return dateTextView; }

        /* Constructor */
        public InRangeFriendsViewHolder(View itemView) {
            super(itemView);

            /* Setup views */
            rootView = itemView;
            avatarCircleImageView = rootView.findViewById(R.id.avatarCircleImageView);
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            dateTextView = rootView.findViewById(R.id.statusTextView);

        }
    }
}
