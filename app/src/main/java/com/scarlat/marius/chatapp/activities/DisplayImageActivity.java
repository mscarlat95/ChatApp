package com.scarlat.marius.chatapp.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.tasks.SavePictureInternalTask;

public class DisplayImageActivity extends AppCompatActivity {

    private static final String TAG = "DisplayImageActivity";

    private Toolbar toolbar;
    private PhotoView imageView;
    private String imageUri = Constants.UNSET;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: Method was invoked!");
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.downloadOption:
                Log.d(TAG, "onOptionsItemSelected: Download Image");
                savefile();
                return true;

            default:
                Log.d(TAG, "onOptionsItemSelected: Undefined menu option id = " + item.getItemId());
                return false;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        /* Obtain image uri */
        imageUri = getIntent().getStringExtra(Constants.IMAGE_URI);
        if (imageUri == null) {
            Log.d(TAG, "onCreate: NULL Image Uri --> Destroying activity");
            finish();
        }

        /* Init android views */
        toolbar = (Toolbar) findViewById(R.id.displayImageToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Photo Viewer");

        imageView = (PhotoView) findViewById(R.id.imageView);
        Glide.with(this)
                .load(imageUri)
                .into(imageView);
    }

    void savefile()
    {
        Log.d(TAG, "savefile:  Method was invoked!");

        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        // TODO: check if there is enough available space
        new SavePictureInternalTask(this).execute(bitmap);
    }






}
