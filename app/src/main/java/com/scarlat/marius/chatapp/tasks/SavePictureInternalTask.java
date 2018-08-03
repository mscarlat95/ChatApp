package com.scarlat.marius.chatapp.tasks;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavePictureInternalTask extends AsyncTask<Bitmap, Void, Void> {

    private static final String TAG = "SavePictureInternalTask";

    private Context context;

    public SavePictureInternalTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Bitmap... params) {
        Log.d(TAG, "doInBackground: Method was invoked!");

        Bitmap bitmap = params[0];
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/LilChat");
        directory.mkdirs();

        // Create image directory
        String fileName = "pic_" + new SimpleDateFormat("ddMMyyyyhhmmss").format(new Date()) + ".jpg";
        File mypath=new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
