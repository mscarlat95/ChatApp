package com.scarlat.marius.chatapp.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class MapMarker implements Target {
    private static final String TAG = "MapMarker";

    private Marker marker;
    private Context context;

    public MapMarker(Context context, Marker marker) {
        this.context = context;
        this.marker = marker;
    }

    private Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Log.d(TAG, "addWhiteBorder: Method was invoked!");

        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2,
                bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);

        canvas.drawColor(Color.GRAY);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Log.d(TAG, "onBitmapLoaded: Method was invoked!");

        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (100 /*dp*/ * scale * 0.5f);

        // TODO: Unmanaged descriptor
        if (marker != null) {
            try {
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, pixels, pixels, true);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(addWhiteBorder(resized, 3)));
            } catch (Exception e) {
                Log.d(TAG, "onBitmapLoaded: [Exception] " + e.getMessage());
            }
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {}

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {}
}
