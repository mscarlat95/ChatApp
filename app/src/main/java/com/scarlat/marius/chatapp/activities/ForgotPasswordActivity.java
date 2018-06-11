package com.scarlat.marius.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    
    // Android fields
    private EditText emailEditText;
    private Button recoverPasswordButton;
    private TextView resultTextView;
    private Toolbar toolbar;
    private ProgressDialog resetProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

    private View.OnClickListener passwordRecoverListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String email = emailEditText.getText().toString();

            if (email.equals("")) {
                Toast.makeText(ForgotPasswordActivity.this, "Please, complete email field!", Toast.LENGTH_SHORT).show();
            } else {

                resetProgress.setTitle("Password Reset");
                resetProgress.setMessage("Please wait until an email is sent to you");
                resetProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
                resetProgress.show();

                resetPassword(email);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Check intent extra values
        final Intent intent = getIntent();
        if (!intent.getBooleanExtra("forgotPassword", true)) {
            Log.d(TAG, "onCreate: ForgotPasswordActivity was accessed incorrectly");
            finish();
        }

        // Set up toolbar
        toolbar = (Toolbar) findViewById(R.id.forgotPassToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS - Password Recover");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for back button

        // Initialize Firebase authentification
        mAuth = FirebaseAuth.getInstance();

        // Set Android field views
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        resetProgress = new ProgressDialog(this);
        recoverPasswordButton = (Button) findViewById(R.id.recoverButton);
        recoverPasswordButton.setOnClickListener(passwordRecoverListener);
    }

    private void resetPassword(final String email) {

        if (!email.equals("")) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "resetPassword: Successfull!");

                        // Dismiss progress dialog
                        resetProgress.dismiss();

                        resultTextView.setText("An email was sent to " + email);
                    } else {
                        Log.d(TAG, "resetPassword: Failed: " + task.getException().toString());

                        // Hide progress dialog in case of any error
                        resetProgress.hide();

                        resultTextView.setText("An error ocurred. Please, try again later!");
                        Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
