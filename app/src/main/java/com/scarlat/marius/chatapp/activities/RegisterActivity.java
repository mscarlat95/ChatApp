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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.util.Constants;
import com.scarlat.marius.chatapp.util.SharedPref;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    // Register Activity Tag
    private static final String TAG = "RegisterActivity";

    // Android fields
    private TextInputLayout emailInputLayout;
    private TextInputLayout fullNameInputLayout;
    private TextInputLayout passwordInputLayout;

    private EditText birthDayEditText, birthMonthEditText, birthYearEditText;
    private RadioGroup genderRadioGroup;

    private TextView loginTextView;
    private Button registerButton;

    private Toolbar toolbar;
    private ProgressDialog registerProgress;

    // Firebase Auth
    private FirebaseAuth mAuth;

    // Firebase Realtime databse
    private DatabaseReference refDatabase;

    private View.OnClickListener userLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(Constants.USER_REGISTER_TAG, "User already exists. Perform login.");

            // Launch Register Activity
            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            loginIntent.putExtra("login", true);
            startActivity(loginIntent);
        }
    };

    private View.OnClickListener userRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String email = emailInputLayout.getEditText().getText().toString();
            final String fullName = fullNameInputLayout.getEditText().getText().toString();
            final String password = passwordInputLayout.getEditText().getText().toString();

            final String birthDay = birthDayEditText.getText().toString();
            final String birthMonth = birthMonthEditText.getText().toString();
            final String birthYear = birthYearEditText.getText().toString();

            if (email.equals("") || fullName.equals("") || password.equals("") ||
                    birthDay.equals("") || birthMonth.equals("") || birthYear.equals("")) {

                Toast.makeText(RegisterActivity.this, "You cannot leave any field empty!",
                        Toast.LENGTH_SHORT).show();

            } else {

                /* Setup progress dialog */
                registerProgress.setTitle("Registering User");
                registerProgress.setMessage("Please wait until the account is created !");
                registerProgress.setCanceledOnTouchOutside(false); // Don't stop it when screen is touched
                registerProgress.show();

                /* Concatenate birthday items */
                final String birthday = birthDay + "/" + birthMonth + "/" + birthYear;

                /* Check genre */
                String gender = "Custom";

                switch (genderRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.maleRadioButton:
                        gender = "Male";
                        break;

                    case R.id.femaleRadioButton:
                        gender = "Female";
                        break;
                }

                registerUser (email, fullName, password, birthday, gender);
            }
        }
    };


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

        // Initialize Firebase database
        refDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize SharedPreferences
        SharedPref.setup(getApplicationContext());

        // Set Android field views
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        fullNameInputLayout = (TextInputLayout) findViewById(R.id.fullNameInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);

        birthDayEditText = (EditText) findViewById(R.id.dayEditText);
        birthMonthEditText = (EditText) findViewById(R.id.monthEditText);
        birthYearEditText = (EditText) findViewById(R.id.yearEditText);

        genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);

        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(userLoginListener);

        registerProgress = new ProgressDialog(this);
        registerButton = (Button) findViewById(R.id.signUpButton);
        registerButton.setOnClickListener(userRegisterListener);

    }

    private void registerUser(final String email, final String fullName, final String password,
                              final String birthday, final String gender) {

        mAuth   .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(Constants.USER_REGISTER_TAG, "Successfull");

                            // Obtain user and database information
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String userID = currentUser.getUid();
                            Log.d(Constants.USER_REGISTER_TAG, "User ID = " + userID);

                            HashMap<String, String> userInfo = new HashMap<String, String>();
                            userInfo.put(Constants.FULLNAME, fullName);
                            userInfo.put(Constants.EMAIL, email);
                            userInfo.put(Constants.BIRTHDAY, birthday);
                            userInfo.put(Constants.GENDER, gender);
                            userInfo.put(Constants.STATUS, "Available");
                            userInfo.put(Constants.PROFILE_IMAGE, "default");
                            userInfo.put(Constants.THUMBNAIL_PROFILE_IMAGE, "default");

                            refDatabase.child("Users").child(userID)
                                    .setValue(userInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Dismiss progress dialog
                                        registerProgress.dismiss();

                                        // launch Main Activity
                                        launchMainActivity();
                                    } else {
                                        Log.d(Constants.USER_REGISTER_TAG, "Add info in database failed. " + task.getException().toString());
                                    }
                                }
                            });

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

    private void launchMainActivity() {
        // Launch Main Activity
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

        // Clear all previous tasks
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("main", true);
        startActivity(mainIntent);
        finish();
    }
}
