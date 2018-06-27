package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.CustomProgressDialog;

public class UploadProfilePhotoTask {

    private static final String TAG = "UploadProfilePhotoTask";

    /* Application context */
    private Context context;

    /* Firebase */
    private String userID;
    private StorageReference storageReference;
    private DatabaseReference dbReference;

    /* Progress dialog */
    private CustomProgressDialog progressDialog;

    public UploadProfilePhotoTask(Context context) {
        this.context = context;
    }

    protected void setup() {
        /* Setup progress dialog */
        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Uploading Profile Photo", "Please wait until the upload is done!");

        /* Setup Firebase */
        userID = FirebaseAuth.getInstance().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        dbReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USERS_TABLE).child(userID);
    }

    public void execute (Uri imageUri) {
        Log.d(TAG, "execute: Method was invoked!");

        setup();

        final String fileName = userID + ".jpg";

        storageReference.child(Constants.STORAGE_PROFILE_IMAGES).child(fileName)
                .putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                /* Image Upload successful */
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Image upload success");

                    /* Update Image URL in user record from database */
                    FirebaseStorage.getInstance().getReference().child(Constants.STORAGE_PROFILE_IMAGES).child(fileName)
                            .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {

                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            dbReference.child(Constants.PROFILE_IMAGE).setValue(task.getResult().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                               /* Image URL updated */
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

                progressDialog.hide();
            }
        });
    }

}
