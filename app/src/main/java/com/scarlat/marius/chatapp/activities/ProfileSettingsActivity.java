package com.scarlat.marius.chatapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.threads.ChangeStatusTask;
import com.scarlat.marius.chatapp.threads.GetUserInfoTask;
import com.scarlat.marius.chatapp.threads.UploadProfilePhotoTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettingsActivity extends AppCompatActivity {

    /* Activity Tag */
    private static final String TAG = "ProfileSettingsActivity";

    /* Android Fields */
    private CircleImageView avatarCircleImageView;
    private EditText fullNameEditText, emailEditText, statusEditText;
    private Button changeStatusButton, changePhotoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        /* Setup Android Fields */
        avatarCircleImageView = (CircleImageView) findViewById(R.id.avatarCircleImageView);
        fullNameEditText = (EditText) findViewById(R.id.fullNameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        statusEditText = (EditText) findViewById(R.id.statusEditText);
        changeStatusButton = (Button) findViewById(R.id.changeStatusButton);

        /* Retrieve User Data */
        new GetUserInfoTask(this, avatarCircleImageView, fullNameEditText, emailEditText, statusEditText).execute();

    }

    /* Change Status Listener */
    public void changeUserStatus(View view) {
        if (statusEditText.isEnabled()) {
            new ChangeStatusTask(ProfileSettingsActivity.this, FirebaseAuth.getInstance())
                    .execute(statusEditText.getText().toString());
            statusEditText.setEnabled(false);
            changeStatusButton.setText("Change Status");
        } else {
                    /* Request focus and activate the keyboard */
            statusEditText.setEnabled(true);
            statusEditText.requestFocus();
            statusEditText.setSelection(statusEditText.length());

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(statusEditText, InputMethodManager.SHOW_IMPLICIT);

            changeStatusButton.setText("Publish Status");
        }
    }

    /* Change Photo Listener */
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
                                break;
                        }
                    }
                }).show();
    }


    private Uri profilePhotoUri;
    private void importFromCamera() {
        Log.d(TAG, "importFromCamera: Method was invoked!");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            /* Get reference to the storage */
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            /* Obtain image file */
            String pictureName = getPictureName();

            /* Create image file */
            File imageFile = new File(pictureDirectory, pictureName);

            /* Convert into URI */
            profilePhotoUri = FileProvider.getUriForFile(this, getResources().getString(R.string.authorities), imageFile);

            /* Start camera activity */
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profilePhotoUri);
            startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    private String getPictureName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = simpleDateFormat.format(new Date());

        return "profilePicture" + timestamp + ".jpg";
    }

    private void importFromGallery() {
        Log.d(TAG, "importFromGallery: Method was invoked!");

        Intent galleryIntent = new Intent();

        /* Open gallery */
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select profile image"), Constants.REQUEST_CODE_READ_EXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_CODE_READ_EXT:
                    Log.d(TAG, "onActivityResult: REQUEST_CODE_READ_EXT (gallery)");
                    extractImageFromGallery(data);
                    break;

                case Constants.REQUEST_CODE_CAMERA:
                    Log.d(TAG, "onActivityResult: REQUEST_CODE_CAMERA");
                    data = new Intent().putExtra(Constants.PROFILE_IMAGE, String.valueOf(profilePhotoUri));
                    extractImagefromCamera(data);
                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    Log.d(TAG, "onActivityResult: REQUEST_CODE_CROP_IMAGE");
                    processCroppedImage(data);
                    break;

                default:
                    Log.d(TAG, "onActivityResult: Received code = " + Integer.toString(requestCode));
                    break;
            }
        }
    }

    private void extractImagefromCamera(Intent data) {
        Log.d(TAG, "extractImagefromCamera: Method was invoked!");
        
        Uri imageUri = Uri.parse(data.getStringExtra(Constants.PROFILE_IMAGE));
        cropImage(imageUri);
    }

    private void extractImageFromGallery(Intent data) {
        Log.d(TAG, "extractImageFromGallery: Method was invoked!");

        Uri imageUri = data.getData();
        cropImage(imageUri);
    }

    private void cropImage(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1 /*X ratio*/, 1 /*Y ratio*/)
                .setRequestedSize(Constants.MAX_WIDTH, Constants.MAX_HEIGHT, CropImageView.RequestSizeOptions.RESIZE_EXACT)
                .start(this);
    }

    private void processCroppedImage(Intent data) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        Uri imageUri = result.getUri();

        /* Upload it to the server */
        new UploadProfilePhotoTask(this).execute(imageUri);
    }
}
