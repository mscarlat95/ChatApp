package com.scarlat.marius.chatapp.threads;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

public class GetFriendInfoTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "GetFriendInfoTask";
    private Context context;

    private String userID;

    /* Views */
    private ImageView profileImageView;
    private TextView fullNameTextView, statusTextView, friendsNumberTextView;

    /* Progress dialog */
    private ProgressDialog progressDialog;

    /* Firebase */
    private DatabaseReference dbReference;

    public GetFriendInfoTask(Context context, String userID, ImageView profileImageView,
                             TextView fullNameEditText, TextView statusEditText,
                             TextView friendsNumberEditText) {
        this.context = context;
        this.userID = userID;

        this.profileImageView = profileImageView;
        this.fullNameTextView = fullNameEditText;
        this.statusTextView = statusEditText;
        this.friendsNumberTextView = friendsNumberEditText;
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        /* Setup progress dialog */
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Retreiving User Information");
        progressDialog.setMessage("Please wait until the server provides the required data");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        dbReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(userID);
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Method was invoked!");

                if (((Activity) context).isDestroyed()) {
                    Log.d(TAG, "onComplete: Activity is not available");
                    return;
                }

                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: Snapshot = " + dataSnapshot.toString());

                    final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                    final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                    final String status = dataSnapshot.child(Constants.STATUS).getValue().toString();
                    final String numberOfFriends = dataSnapshot.child(Constants.NUMBER_OF_FRIENDS).getValue().toString();

                    statusTextView.setText("Status: " + status);
                    fullNameTextView.setText(fullname);
                    friendsNumberTextView.setText("Total Friends: " + numberOfFriends);
                    Glide.with(context)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_avatar))
                            .load(profileImage)
                            .into(profileImageView);

                } else {
                    Log.d(TAG, "onDataChange: Cannot find snapshot of " + userID);
                    Toast.makeText(context, "User ID " + userID+ " doesn't exists", Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (((Activity) context).isDestroyed()) {
                    Log.d(TAG, "onComplete: Activity is not available");
                    return;
                }

                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                hideProgressDialog();
            }
        });

        return null;
    }
}
