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
import android.widget.Button;
import android.widget.EditText;

import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;
import com.scarlat.marius.chatapp.util.GalleryMedia;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSettingsActivity";

    private CircleImageView avatarCircleImageView;
    private EditText changeNameEditText;
    private EditText changePasswordEditText;
    private Button changeNameButton;
    private Button changePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        avatarCircleImageView = (CircleImageView) findViewById(R.id.avatarCircleImageView);
        avatarCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePictureOptions(v);
            }
        });

        changeNameEditText = (EditText) findViewById(R.id.changeNameEditText);
        changePasswordEditText = (EditText) findViewById(R.id.changePasswordEditText);
        changeNameButton = (Button) findViewById(R.id.changeNameButton);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
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

                                // Request user permissions
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*Marshmallow*/) {
                                    if (checkSelfPermission(Manifest.permission.CAMERA) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA}, Constants.REQUEST_CODE_CAMERA);
                                    } else {
                                        importFromCamera();
                                    }
                                } else {
                                    importFromCamera();
                                }

                                break;
                            case 1:     /* Import from gallery */

                                // Request user permissions
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M /*Marshmallow*/) {
                                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_GALLERY);
                                    } else {
                                        importFromGallery();
                                    }
                                } else {
                                    importFromGallery();
                                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Constants.REQUEST_CODE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // start Gallery Intent
                    importFromGallery();
                }
                break;
            case Constants.REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // start Camera Intent
                    importFromCamera();
                }

                break;
            default:
                Log.d(TAG, "onRequestPermissionsResult: Received code = " + requestCode);
                break;
        }
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

    private void importFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    private void extractImagefromCamera(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) extras.get("data");
        avatarCircleImageView.setImageBitmap(imageBitmap);

        // TODO: save image

        // TODO: upload to server
    }

    private void importFromGallery() {
        Intent galleryIntent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.REQUEST_CODE_GALLERY);
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
