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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.storage.SharedPref;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    /* Android fields */
    private TextView registerTextView;
    private TextView forgotPasswordTextView;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private CheckBox rememberCredentialsCheckBox;

    private Toolbar toolbar;
    private ProgressDialog loginProgress;

    /* Firebase Auth */
    private FirebaseAuth mAuth;

    /* Setup Listeners */
    private View.OnClickListener forgotPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Register: Forgot password. Perform password reset");

            /* Launch ForgotPassword Activity */
            Intent forgotPassIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(forgotPassIntent);
        }
    };
    private View.OnClickListener userRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Register: User does not exists. Perform registration");

            /* Launch Register Activity */
            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        }
    };
    private View.OnClickListener emailAndPassLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String email = emailInputLayout.getEditText().getText().toString();
            final String password = passwordInputLayout.getEditText().getText().toString();

            if (email.equals("") || password.equals("")) {
                Toast.makeText(LoginActivity.this, "You cannot leave any field empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            displayProgressDialog();
            emailAndPassLogin(email, password);
        }
    };

    private void displayProgressDialog() {
        loginProgress.setTitle("Logging In");
        loginProgress.setMessage("Please wait while your credentials are checked");
        loginProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
        loginProgress.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Set up toolbar */
        toolbar = (Toolbar) findViewById(R.id.loginToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS - Login");

        /* Initialize Firebase authentification */
        mAuth = FirebaseAuth.getInstance();

        /* Initialize SharedPreferences */
        SharedPref.setup(getApplicationContext());
        SharedPref.saveUserLogged(null);

        /* Set Android view fields */
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);
        forgotPasswordTextView = (TextView) findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(forgotPasswordListener);
        registerTextView = (TextView) findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(userRegisterListener);
        loginProgress = new ProgressDialog(this);
        loginButton = (Button) findViewById(R.id.signInButton);
        loginButton.setOnClickListener(emailAndPassLoginListener);
        rememberCredentialsCheckBox = (CheckBox) findViewById(R.id.rememberCredentialsCheckBox);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /* Check shared preferences and update UI */
        if (SharedPref.getBoolean(Constants.CREDENTIALS_CHECKBOX)) {
            Log.d(TAG, "onStart: Credentials Checkbox is checked");

            emailInputLayout.getEditText().setText(SharedPref.getString(Constants.EMAIL));
            passwordInputLayout.getEditText().setText(SharedPref.getString(Constants.PASSWORD));
            rememberCredentialsCheckBox.setChecked(true);
        } else {
            Log.d(TAG, "onStart: Credentials Checkbox is NOT checked");
        }
    }

    private void emailAndPassLogin(final String email, final String password) {
        Log.d(TAG, "emailAndPassLogin: Method was invoked!");

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Login Successful");
                    loginProgress.dismiss();


                    final String tokenID = FirebaseInstanceId.getInstance().getToken();

                    /* Store user ID and device ID in the cache memory */
                    SharedPref.saveUserId(mAuth.getUid(), mAuth.getCurrentUser().getDisplayName());
                    SharedPref.saveDeviceId(tokenID);

                    /* Update token id in the database */
                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(mAuth.getUid())
                            .child(Constants.TOKEN_ID).setValue(tokenID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                /* Remember username and password if checkbox is checked */
                                if (rememberCredentialsCheckBox.isChecked()) {
                                    SharedPref.saveCredentials(email, password);
                                } else {
                                    SharedPref.clearCredentials();
                                }

                                SharedPref.saveUserLogged(mAuth.getUid());

                                launchMainActivity();

                            } else {
                                Log.d(TAG, "Cannot update the new token ID");
                                Toast.makeText(LoginActivity.this, "Updating tokenID failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Login Failed: " + task.getException().toString());

                    /* Display errors */
                    loginProgress.hide();
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

        /* Clear all previous tasks */
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

