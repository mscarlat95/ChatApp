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

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    
    /*  Android Views */
    private EditText emailEditText;
    private Button recoverPasswordButton, openEmailButton;
    private TextView resultTextView;
    private Toolbar toolbar;
    private ProgressDialog resetProgress;

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    private View.OnClickListener passwordRecoverListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String email = emailEditText.getText().toString();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please, complete your email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            resetProgress = new ProgressDialog(ForgotPasswordActivity.this);
            resetProgress.setTitle("Password Reset");
            resetProgress.setMessage("Please wait until an email is sent to you");
            resetProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
            resetProgress.show();

            resetPassword(email);
        }
    };

    private View.OnClickListener openEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /* Launch Email */
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        /* Setup toolbar */
        toolbar = (Toolbar) findViewById(R.id.forgotPassToolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name) + " - Password Recover");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for back button

        /* Initialize Firebase authentication */
        mAuth = FirebaseAuth.getInstance();

        /* Setup Android views */
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        recoverPasswordButton = (Button) findViewById(R.id.recoverButton);
        openEmailButton = (Button) findViewById(R.id.openEmailButton);

        recoverPasswordButton.setOnClickListener(passwordRecoverListener);
        openEmailButton.setOnClickListener(openEmailListener);
        resultTextView.setVisibility(View.INVISIBLE);
        openEmailButton.setVisibility(View.INVISIBLE);
    }

    private void resetPassword(final String email) {
        if (!email.equals("")) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "resetPassword: Successfull!");
                        Toast.makeText(ForgotPasswordActivity.this, "Successful", Toast.LENGTH_SHORT).show();

                        emailEditText.setText("");

                        resultTextView.setText("An email was sent to " + email);
                        resultTextView.setVisibility(View.VISIBLE);

                        openEmailButton.setVisibility(View.VISIBLE);

                    } else {
                        Log.d(TAG, "resetPassword: Failed: " + task.getException().toString());
                        resultTextView.setText("An error ocurred. Please, try again later!");
                        Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    /* Dismiss progress dialog */
                    resetProgress.dismiss();
                }
            });
        }
    }
}
