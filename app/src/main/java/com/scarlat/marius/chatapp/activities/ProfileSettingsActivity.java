package com.scarlat.marius.chatapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;
import com.scarlat.marius.chatapp.util.GalleryMedia;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSettingsActivity";

    private CircleImageView avatarCircleImageView;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText statusEditText;
    private EditText birthdayEditText;
    private EditText genderEditText;

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        /* Request Camera and Gallery permissions */
        requestPermissions();


        /* Setup Android Fields */
        avatarCircleImageView = (CircleImageView) findViewById(R.id.avatarCircleImageView);
        avatarCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePictureOptions(v);
            }
        });
        fullNameEditText = (EditText) findViewById(R.id.fullNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        statusEditText = (EditText) findViewById(R.id.statusEditText);
        birthdayEditText = (EditText) findViewById(R.id.birthdayEditText);
        genderEditText = (EditText) findViewById(R.id.genderEditText);


        /* Setup firebase User and Database */
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String userID = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String fullname = dataSnapshot.child(Constants.FULLNAME).getValue().toString();
                final String email = dataSnapshot.child(Constants.EMAIL).getValue().toString();
                final String profileImage = dataSnapshot.child(Constants.PROFILE_IMAGE).getValue().toString();
                final String thumbImage = dataSnapshot.child(Constants.THUMBNAIL_PROFILE_IMAGE).getValue().toString();
                final String status = dataSnapshot.child(Constants.STATUS).getValue().toString();
                final String birthday = dataSnapshot.child(Constants.BIRTHDAY).getValue().toString();
                final String gender = dataSnapshot.child(Constants.GENDER).getValue().toString();

                statusEditText.setText(status);
                fullNameEditText.setText(fullname);
                emailEditText.setText(email);
                birthdayEditText.setText(birthday);
                genderEditText.setText(gender);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*Marshmallow*/) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_GALLERY);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(Constants.PERMISSIONS_TAG, "onRequestPermissionsResult: Gallery permissions GRANTED");
                } else {
                    requestPermissions();
                }
                break;

            case Constants.REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(Constants.PERMISSIONS_TAG, "onRequestPermissionsResult: Camera permissions GRANTED");
                } else {
                    requestPermissions();
                }
                break;

            default:
                Log.d(TAG, "onRequestPermissionsResult: Received code = " + requestCode);
                break;
        }
    }

    // Called by AvatarCircleImageView
    public void changePictureOptions(View view) {
        final String[] availableOptions = {
                "Take a photo",
                "Choose from gallery",
                "Cancel"
        };

        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(availableOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:     /* Take a photo using the camera */
                                importFromCamera();
                                break;

                            case 1:     /* Import from gallery */
                                importFromGallery();
                                break;

                            case 2:     /* Cancel */
                                break;
                            default:
                                Log.d(TAG, "Change profile picture: Invalid item");
                                break;
                        }
                    }
                })
                .show();
    }

    private void importFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    private void importFromGallery() {
        Intent galleryIntent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_GALLERY:
                    extractImageFromGallery(data);
                    break;
                case Constants.REQUEST_CODE_CAMERA:
                    extractImagefromCamera(data);
                    break;
                default:
                    Log.d(TAG, "onActivityResult: Received code = " + Integer.toString(requestCode));
                    break;
            }
        }
    }

    private void extractImagefromCamera(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        avatarCircleImageView.setImageBitmap(imageBitmap);

        // TODO: save image

        // TODO: upload to server
    }

    private void extractImageFromGallery(Intent data) {
        // Create a link to the image
        Uri selectedImage = data.getData();

        try {
            Bitmap bitmap = GalleryMedia.extractBitmap(this, selectedImage);

            // TODO: upload to server

            // Update avatar image
            avatarCircleImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
