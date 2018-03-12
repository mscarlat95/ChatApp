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

public class RegisterActivity extends AppCompatActivity {

    // Register Activity Tag
    private static final String TAG = "RegisterActivity";

    // Android fields
    private TextInputLayout emailInputLayout;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextView loginTextView;
    private Button registerButton;
    private Toolbar toolbar;
    private ProgressDialog registerProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Check intent extra values
        final Intent intent = getIntent();
        if (!intent.getBooleanExtra("register", true)) {
            Log.d(TAG, "onCreate: RegisterActivity was accessed incorrectly");
            finish();
        }

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.registerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS - Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for back button

        // Initialize Firebase authentification
        mAuth = FirebaseAuth.getInstance();

        // Set Android field views
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);

        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(Constants.USER_REGISTER_TAG, "User already exists. Perform login.");

                // Launch Register Activity
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginIntent.putExtra("login", true);
                startActivity(loginIntent);
            }
        });

        registerProgress = new ProgressDialog(this);

        registerButton = (Button) findViewById(R.id.recoverButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = emailInputLayout.getEditText().getText().toString();
                final String username = usernameInputLayout.getEditText().getText().toString();
                final String password = passwordInputLayout.getEditText().getText().toString();

                if (email.equals("") || username.equals("") || password.equals("")) {
                    Toast.makeText(RegisterActivity.this, "You cannot leave any field empty!", Toast.LENGTH_SHORT).show();
                } else {

                    registerProgress.setTitle("Registering User");
                    registerProgress.setMessage("Please wait until the account is created !");
                    registerProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
                    registerProgress.show();

                    registerUser (email, username, password);
                }
            }
        });


    }

    private void registerUser(String email, String username, String password) {

        mAuth   .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(Constants.USER_REGISTER_TAG, "Successfull");

                            // Dismiss progress dialog
                            registerProgress.dismiss();

                            // Launch Main Activity
                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

                            // Clear all previous tasks
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mainIntent.putExtra("main", true);
                            startActivity(mainIntent);

                            // User cannot go back to register activity
                            finish();
                        } else {
                            Log.d(Constants.USER_REGISTER_TAG, "Failed: " + task.getException().toString());

                            // Hide progress dialog in case of any error
                            registerProgress.hide();

                            // Display error in UI
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
