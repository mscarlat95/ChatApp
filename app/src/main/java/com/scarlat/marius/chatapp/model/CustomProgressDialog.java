package com.scarlat.marius.chatapp.model;


import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

public class CustomProgressDialog {
    private static final String TAG = "CustomProgressDialog";
    private ProgressDialog progressDialog;
    private Context context;

    public CustomProgressDialog(Context context) {
        this.context = context;
    }


    public void init(String title, String message) {
        Log.d(TAG, "initDialog: Method was invoked");

        progressDialog = new ProgressDialog(context);

        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        progressDialog.show();
    }


    public void hide() {
        if (progressDialog == null) {
            Log.d(TAG, "hideProgressDialog: Dialog is not initialized");
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}
