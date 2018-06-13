package com.scarlat.marius.chatapp.general;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.activities.UserProfileActivity;
import com.scarlat.marius.chatapp.model.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private static final String TAG = "UserListAdapter";
    
    private Context context;
    private List<User> users;

    public UserListAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Method was invoked!");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_info_layout, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: Method was invoked!");

        if (!((Activity) context).isDestroyed()) {
            holder.getFullNameTextView().setText(users.get(position).getFullname());
            holder.getStatusTextView().setText(users.get(position).getStatus());

            Glide.with(context)
                    .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_avatar))
                    .load(users.get(position).getProfileImage())
                    .into(holder.getAvatarCircleImageView());

            /* View user profile on click */
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfileActivity.class);

                    intent.putExtra(Constants.USER_ID, users.get(position).getUserId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + users.size());
        return users.size();
    }
    
    public void setFilter (List<User> filteredUsers) {
        Log.d(TAG, "setFilter: Method was invoked!");

        users = new ArrayList<>();
        users.addAll(filteredUsers);

        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        private View rootView;

        private CircleImageView avatarCircleImageView;
        private TextView fullNameTextView;
        private TextView statusTextView;

        /* Setters and getters */
        View getRootView() { return rootView; }
        CircleImageView getAvatarCircleImageView() { return avatarCircleImageView; }
        TextView getFullNameTextView() { return fullNameTextView; }
        TextView getStatusTextView() { return statusTextView; }

        /* Constructor */
        UserViewHolder(View itemView) {
            super(itemView);

            /* Setup views */
            rootView = itemView;
            avatarCircleImageView = rootView.findViewById(R.id.avatarCircleImageView);
            fullNameTextView = rootView.findViewById(R.id.fullNameTextView);
            statusTextView = rootView.findViewById(R.id.statusTextView);
        }

    }

}
