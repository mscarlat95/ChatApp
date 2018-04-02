package com.scarlat.marius.chatapp.util;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

public class GalleryMedia {

    private static String getRealPath(Context context, Uri uri) {

        int column_index = 0;
        String realPath = "";

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            /* Obtain real path */
            realPath = cursor.getString(column_index);

            /* Free up cursor */
            cursor.close();
        }

        return realPath;
    }

    private static int convertIntoDegrees(int exifRotation) {
        switch (exifRotation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    public static Bitmap extractBitmap (Context context, Uri imageUri) throws IOException {

        // Convert URI into a Bitmap image
        Bitmap sourceBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

        // Check image orientation before loading it
        ExifInterface exifInterface = new ExifInterface(getRealPath(context, imageUri));
        int exifRotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                                        ExifInterface.ORIENTATION_NORMAL);

        // Convert exifInterface orientation into Degrees
        int degreesRotation = convertIntoDegrees(exifRotation);
        Matrix matrix = new Matrix();

        if (degreesRotation != 0f) {
            matrix.preRotate(degreesRotation);
        }

        // return updated bitmap (rotated in case of Portret Orientation)
        return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
    }

}
