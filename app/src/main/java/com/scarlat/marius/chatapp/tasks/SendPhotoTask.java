package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.model.CustomProgressDialog;

public class SendPhotoTask {

    private static final String TAG = "SendPhotoTask";

    private Context context;
    private String userID, friendID;
    private DatabaseReference rootDatabaseRef;
    private CustomProgressDialog progressDialog;

    public SendPhotoTask(Context context, String friendID) {
        this.context = context;
        this.friendID = friendID;
    }
    
    public void setup() {
        Log.d(TAG, "setup: Method was invoked!");
        
        /* Setup Firebase */
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userID = FirebaseAuth.getInstance().getUid();

        /* Setup progress dialog */
        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Sending photo", "Wait until the photo is uploaded");
    }
    
    public void execute(Uri fileUri) {
        Log.d(TAG, "execute: Method was invoked!");

        setup();

        final String messageID = rootDatabaseRef.child(Constants.MESSAGES_TABLE).child(userID)
                .child(friendID).push().getKey();

        StorageReference imagesPathRef = FirebaseStorage.getInstance().getReference()
                .child(Constants.STORAGE_MESSAGE_IMAGES).child(messageID + ".jpg");

        imagesPathRef.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "Upload selected image Successful");

                    FirebaseStorage.getInstance().getReference()
                            .child(Constants.STORAGE_MESSAGE_IMAGES)
                            .child(messageID + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String imageUrl = String.valueOf(uri);
                            Log.d(TAG, "Download url = " + imageUrl);

                            new SendMessageTask(context, friendID).execute(imageUrl, Constants.MESSAGE_TYPE_IMAGE);

                            progressDialog.hide();
                        }
                    });
                }
            }
        });
    }

}
