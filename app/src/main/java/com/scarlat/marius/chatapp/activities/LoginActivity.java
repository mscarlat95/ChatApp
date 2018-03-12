package com.scarlat.marius.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;

public class LoginActivity extends AppCompatActivity {

    // Login Activity Tag
    private static final String TAG = "LoginActivity";

    // Android fields
    private TextView registerTextView;
    private TextView forgotPasswordTextView;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private Toolbar toolbar;
    private ProgressDialog loginProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check intent extra values
        final Intent intent = getIntent();
        if (!intent.getBooleanExtra("login", true)) {
            Log.d(TAG, "onCreate: LoginActivity was accessed incorrectly");
            finish();
        }

        // Set up toolbar
        toolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS - Login");

        // Initialize Firebase authentification
        mAuth = FirebaseAuth.getInstance();

        // Set Android view fields
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);

        forgotPasswordTextView = (TextView) findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.USER_LOGIN_TAG, "Forgot password. Perform password reset");

                // Launch Register Activity
                Intent forgotPassIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                forgotPassIntent.putExtra("forgotPassword", true);
                startActivity(forgotPassIntent);
            }
        });

        registerTextView = (TextView) findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.USER_LOGIN_TAG, "User does not exists. Perform registration");

                // Launch Register Activity
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                registerIntent.putExtra("register", true);
                startActivity(registerIntent);
            }
        });

        loginProgress = new ProgressDialog(this);

        loginButton = (Button) findViewById(R.id.recoverButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailInputLayout.getEditText().getText().toString();
                final String password = passwordInputLayout.getEditText().getText().toString();

                if (email.equals("") || password.equals("")) {
                    Toast.makeText(LoginActivity.this, "You cannot leave any field empty!", Toast.LENGTH_SHORT).show();
                } else {

                    loginProgress.setTitle("Logging In");
                    loginProgress.setMessage("Please wait while your creditentials are checked");
                    loginProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
                    loginProgress.show();

                    loginUser (email, password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(Constants.USER_LOGIN_TAG, "Successfull");

                    // Dismiss progress dialog
                    loginProgress.dismiss();

                    // Launch Main Activity
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

                    // Clear all previous tasks
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mainIntent.putExtra("main", true);
                    startActivity(mainIntent);

                    // User cannot go back to register activity
                    finish();
                } else {
                    Log.d(Constants.USER_LOGIN_TAG, "Failed: " + task.getException().toString());

                    // Hide progress dialog in case of any error
                    loginProgress.hide();

                    // Display error in UI
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
