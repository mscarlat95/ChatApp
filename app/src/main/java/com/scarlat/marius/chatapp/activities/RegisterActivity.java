package com.scarlat.marius.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;
import com.scarlat.marius.chatapp.storage.SharedPref;
import com.scarlat.marius.chatapp.tasks.RegisterUserTask;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    /* Android view fields */
    private Toolbar toolbar;
    private TextInputLayout emailInputLayout;
    private TextInputLayout fullNameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextView loginTextView;
    private Button registerButton;

    /* Firebase Auth and RealTime Database */
    private FirebaseAuth mAuth;
    private DatabaseReference refDatabase;

    /* Facebook Login*/
    private ImageButton facebookLoginBtn;
    private CallbackManager callbackManager;

    /* Google Login */
    private ImageButton googleLoginBtn;
    private GoogleApiClient googleApiClient;

    private View.OnClickListener userLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "User already exists. Perform login.");

            /* Launch Login Activity */
            Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            loginIntent.putExtra("login", true);
            startActivity(loginIntent);
            finish();
        }
    };

    private View.OnClickListener userRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String email = emailInputLayout.getEditText().getText().toString();
            final String fullName = fullNameInputLayout.getEditText().getText().toString();
            final String password = passwordInputLayout.getEditText().getText().toString();

            if (email.equals("") || fullName.equals("") || password.equals("")) {
                Toast.makeText(RegisterActivity.this, "You cannot leave any field empty!",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            registerWithEmailAndPass(email, fullName, password);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set Toolbar
        toolbar = (Toolbar) findViewById(R.id.registerToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatoS - Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // for back button

        /* Initialize Firebase authentification */
        mAuth = FirebaseAuth.getInstance();

        /* Initialize SharedPreferences */
        SharedPref.setup(getApplicationContext());

        /* Set Android field views */
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        fullNameInputLayout = (TextInputLayout) findViewById(R.id.fullNameInputLayout);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.passwordInputLayout);
        loginTextView = (TextView) findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(userLoginListener);
        registerButton = (Button) findViewById(R.id.signUpButton);
        registerButton.setOnClickListener(userRegisterListener);

        /* Alternatives for Email&Pass Register */
        enableGoogleLogin();
        enableFacebookLogin();
    }

    private void registerWithEmailAndPass(final String email, final String fullName, final String password) {
        Log.d(TAG, "registerWithEmailAndPass: Method was invoked!");
        mAuth   .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Adding new records about user into the database");

                            new RegisterUserTask(RegisterActivity.this, mAuth)
                                    .execute(email, fullName, Constants.UNSET);

                            launchMainActivity();

                        } else {
                            Log.d(TAG, "Failed: " + task.getException().toString());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void enableGoogleLogin() {
        Log.d(TAG, "enableGoogleLogin: Method was invoked!");
        /* Google Login */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
                        Toast.makeText(RegisterActivity.this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleLoginBtn = findViewById(R.id.googleImageButton);
        googleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, Constants.REQUEST_CODE_SIGN_IN);
            }
        });
    }

    private void enableFacebookLogin() {
        Log.d(TAG, "enableFacebookLogin: Method was invoked!");

         /* Facebook Login */
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();

        facebookLoginBtn = findViewById(R.id.facebookImageButton);
        facebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(RegisterActivity.this, Arrays.asList("email", "public_profile"));

                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "onSuccess: " + loginResult);
                        firebaseAuthWithFacebook(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel: Method was invoked!");
                        Toast.makeText(RegisterActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Method was invoked");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try { /* Perform Google Login */
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) { /* Google Login Failed */
                Log.d(TAG, "Google sign in failed", e);
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {

            /* Perform Facebook login */
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Fireabase - Google Login */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: Method was invoked");

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            recordDataFromAlternativesLogin();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /* Firebase - Facebook Login */
    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d(TAG, "firebaseAuthWithFacebook: Method was invoked!");

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            recordDataFromAlternativesLogin();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void recordDataFromAlternativesLogin() {
        Log.d(TAG, "recordDataFromAlternativesLogin: Method was invoked!");

        final String userID = mAuth.getUid();

        refDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(userID);
        refDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /* Check values are already set --> Avoid overwriting them */
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "User doesn't exists in database. Adding new records about him...");
                    
                    FirebaseUser user = mAuth.getCurrentUser();
                    String photoUrl = Constants.UNSET;
                    if (user.getPhotoUrl() != null) {
                        photoUrl = String.valueOf(user.getPhotoUrl());
                    }

                    new RegisterUserTask(RegisterActivity.this, mAuth)
                            .execute(user.getEmail(), user.getDisplayName(), photoUrl);
                } else {
                    Log.d(TAG, "User already exists in database. Perform simple logging in ...");

                    /* Store the new token ID */
                    final String tokenID = FirebaseInstanceId.getInstance().getToken();
                    FirebaseDatabase.getInstance().getReference().child(Constants.USERS_TABLE).child(userID)
                            .child(Constants.TOKEN_ID).setValue(tokenID).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                /* Do something */

                            } else {
                                Log.d(TAG, "Cannot update the new token ID");
                                Toast.makeText(RegisterActivity.this, "Updating tokenID failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        launchMainActivity();
    }



    private void launchMainActivity() {
        Log.d(TAG, "launchMainActivity: Method was invoked!");
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

        /* Clear all previous tasks */
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mainIntent.putExtra("main", true);
        startActivity(mainIntent);
        finish();
    }

}
