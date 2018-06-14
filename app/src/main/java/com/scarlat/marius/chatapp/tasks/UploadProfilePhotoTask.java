package com.scarlat.marius.chatapp.tasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scarlat.marius.chatapp.general.Constants;

public class UploadProfilePhotoTask extends AsyncTask<Uri, Void, Void> {

    private static final String TAG = "UploadProfilePhotoTask";

    /* Application context */
    private Context context;

    /* Firebase */
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private DatabaseReference dbReference;

    /* Progress dialog */
    private ProgressDialog progressDialog;

    public UploadProfilePhotoTask(Context context) {
        this.context = context;
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
        progressDialog.setTitle("Uploading Profile Photo");
        progressDialog.setMessage("Please wait until the upload is done!");
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();

        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbReference = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getUid());
    }

    @Override
    protected Void doInBackground(Uri... params) {

        Uri imageUri = params[0];
        final String fileName = mAuth.getUid() + ".jpg";

        storageReference.child("profile_images").child(fileName)
                .putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                /* Image Upload successful */
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Image upload success");

                    /* Update Image URL in user record from database */
                    FirebaseStorage.getInstance().getReference().child("profile_images").child(fileName)
                            .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            dbReference.child(Constants.PROFILE_IMAGE).setValue(task.getResult().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    /* Image URL updated */
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: Updated Image Field in user record");
                                        Toast.makeText(context, "Image uploading successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "onComplete: Image upload failed" + task.getException().getMessage());
                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                } else {
                    Log.d(TAG, "onComplete: Image upload failed" + task.getException().getMessage());
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }
        });

        return null;

    }
}
