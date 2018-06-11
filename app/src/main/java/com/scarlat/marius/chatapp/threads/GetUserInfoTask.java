package com.scarlat.marius.chatapp.threads;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.general.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class GetUserInfoTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "GetUserInfoTask";

    private Context context;

    /* Views */
    private CircleImageView avatarCircleImageView;
    private EditText fullNameEditText, emailEditText, statusEditText;

    /* Progress dialog */
    private ProgressDialog progressDialog;

    /* Firebase */
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    public GetUserInfoTask(Context context, CircleImageView avatarCircleImageView,
                           EditText fullNameEditText, EditText emailEditText, EditText statusEditText) {
        this.context = context;
        this.avatarCircleImageView = avatarCircleImageView;
        this.fullNameEditText = fullNameEditText;
        this.emailEditText = emailEditText;
        this.statusEditText = statusEditText;
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
        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid());
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Method was invoked!");

                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: Snapshot = " + dataSnapshot.toString());

                    final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                    final String email = dataSnapshot.child(Constants.EMAIL).getValue().toString();
                    final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                    final String thumbImage = dataSnapshot.child(Constants.THUMBNAIL_PROFILE_IMAGE).getValue().toString();
                    final String status = dataSnapshot.child(Constants.STATUS).getValue().toString();

                    statusEditText.setText(status);
                    fullNameEditText.setText(fullname);
                    emailEditText.setText(email);

                    /* TODO: Update photo image */

                } else {
                    Log.d(TAG, "onDataChange: Cannot find snapshot of " + mAuth.getUid());
                    Toast.makeText(context, "User ID " + mAuth.getUid() + " doesn't exists", Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                hideProgressDialog();
            }
        });

        return null;
    }



}
