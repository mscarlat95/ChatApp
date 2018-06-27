package com.scarlat.marius.chatapp.tasks;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.CustomProgressDialog;

import de.hdodenhof.circleimageview.CircleImageView;

public class GetUserInfoTask {

    private static final String TAG = "GetUserInfoTask";

    private Context context;

    /* Views */
    private CircleImageView avatarCircleImageView;
    private EditText fullNameEditText, emailEditText, statusEditText, friendsNumberEditText;

    /* Progress dialog */
    private CustomProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference rootDatabaseRef;

    public GetUserInfoTask(Context context, CircleImageView avatarCircleImageView,
                           EditText fullNameEditText, EditText emailEditText, EditText statusEditText,
                           EditText friendsNumberEditText) {

        this.context = context;
        this.avatarCircleImageView = avatarCircleImageView;
        this.fullNameEditText = fullNameEditText;
        this.emailEditText = emailEditText;
        this.statusEditText = statusEditText;
        this.friendsNumberEditText = friendsNumberEditText;
    }

    private void setup() {
        Log.d(TAG, "setup: Method was invoked!");

        /* Setup progress dialog */
        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Retreiving User Information", "Please wait until the server provides the required data");

        /* Setup Firebase */
        mAuth = FirebaseAuth.getInstance();
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }


    public void execute() {
        Log.d(TAG, "execute: Method was invoked!");

        setup();

        rootDatabaseRef.child(Constants.USERS_TABLE).child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
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
                    final String email = dataSnapshot.child(Constants.EMAIL).getValue().toString();
                    final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                    final String status = dataSnapshot.child(Constants.STATUS).getValue().toString();

                    statusEditText.setText(status);
                    fullNameEditText.setText(fullname);
                    emailEditText.setText(email);
                    Glide.with(context)
                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.loading))
                            .load(profileImage)
                            .into(avatarCircleImageView);

                } else {
                    Log.d(TAG, "onDataChange: Cannot find snapshot of " + mAuth.getUid());
                    Toast.makeText(context, "User ID " + mAuth.getUid() + " doesn't exists", Toast.LENGTH_SHORT).show();
                }

                progressDialog.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (((Activity) context).isDestroyed()) {
                    Log.d(TAG, "onComplete: Activity is not available");
                    return;
                }

                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                progressDialog.hide();
            }
        });

        rootDatabaseRef.child(Constants.FRIENDS_TABLE).child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (((Activity) context).isDestroyed()) {
                    Log.d(TAG, "onComplete: Activity is not available");
                    return;
                }
                if (dataSnapshot.exists()) {
                    long numberOfFriends = dataSnapshot.getChildrenCount();
                    friendsNumberEditText.setText(numberOfFriends + " Friends");
                } else {
                    friendsNumberEditText.setText("0 Friends");
                }
                progressDialog.hide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                progressDialog.hide();
            }
        });
    }

}
