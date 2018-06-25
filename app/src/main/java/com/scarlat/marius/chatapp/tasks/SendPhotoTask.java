package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.general.CustomProgressDialog;

public class SendPhotoTask extends AsyncTask<Uri, Void, Void> {

    private static final String TAG = "SendPhotoTask";

    private Context context;
    private String userID, friendID;
    private DatabaseReference rootDatabaseRef;
    private CustomProgressDialog progressDialog;

    public SendPhotoTask(Context context, String friendID) {
        this.context = context;
        this.friendID = friendID;
    }

    @Override
    protected void onPreExecute() {
        /* Setup Firebase */
        FirebaseApp.initializeApp(context);
        rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        userID = FirebaseAuth.getInstance().getUid();

        progressDialog = new CustomProgressDialog(context);
        progressDialog.init("Sending photo", "Wait until the photo is uploaded");
    }

    @Override
    protected Void doInBackground(Uri... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        Uri fileUri = params[0];

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

        return null;
    }
}
