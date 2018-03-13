package com.scarlat.marius.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    
    // Android fields
    private TextInputLayout emailInputLayout;
    private TextInputLayout usernameInputLayout;
    private Button recoverPasswordButton;
    private CheckBox emailCheckBox, usernameCheckBox;
    private TextView resultTextView;
    private Toolbar toolbar;
    private ProgressDialog resetProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

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
        emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
        usernameCheckBox = (CheckBox) findViewById(R.id.usernameCheckBox);
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameInputLayout);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        emailInputLayout.getEditText().addTextChangedListener(new TextListener(emailCheckBox));
        usernameInputLayout.getEditText().addTextChangedListener(new TextListener(usernameCheckBox));

        resetProgress = new ProgressDialog(this);

        recoverPasswordButton = (Button) findViewById(R.id.recoverButton);
        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameInputLayout.getEditText().getText().toString();
                final String email = emailInputLayout.getEditText().getText().toString();

                if (username.equals("") && email.equals("")) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please, complete at least one field!", Toast.LENGTH_SHORT).show();
                } else {

                    resetProgress.setTitle("Password Reset");
                    resetProgress.setMessage("Please wait until an email is sent to you");
                    resetProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
                    resetProgress.show();

                    resetPassword(username, email);
                }
            }
        });
    }

    private void resetPassword(final String username, final String email) {

        if (!email.equals("")) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(Constants.PASSWORD_RECOVER, "Successfull!");

                        // Dismiss progress dialog
                        resetProgress.dismiss();

                        resultTextView.setText("An email was sent to " + email);
                    } else {
                        Log.d(Constants.PASSWORD_RECOVER, "Failed: " + task.getException().toString());

                        // Hide progress dialog in case of any error
                        resetProgress.hide();

                        resultTextView.setText("An error ocurred. Please, try again!");
                        Toast.makeText(ForgotPasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // TODO: also for username

    }

    class TextListener implements TextWatcher {
        private CheckBox checkbox;

        TextListener (CheckBox checkBox) {
            this.checkbox = checkBox;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().equals("")) {
                checkbox.setChecked(false);
            } else {
                checkbox.setChecked(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

}
