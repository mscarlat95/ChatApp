package com.scarlat.marius.chatapp.activities;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.scarlat.marius.chatapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Android fields
    private TextInputLayout emailInputLayout;
    private TextInputLayout usernameInputLayout;
    private Button recoverPasswordButton;
    private CheckBox emailCheckBox, usernameCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Set Android field views
        emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
        usernameCheckBox = (CheckBox) findViewById(R.id.usernameCheckBox);
        emailInputLayout = (TextInputLayout) findViewById(R.id.emailInputLayout);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.usernameInputLayout);

        emailInputLayout.getEditText().addTextChangedListener(new TextListener(emailCheckBox));
        usernameInputLayout.getEditText().addTextChangedListener(new TextListener(usernameCheckBox));

        recoverPasswordButton = (Button) findViewById(R.id.recoverButton);
        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInputLayout.getEditText().getText().toString();
                String email = emailInputLayout.getEditText().getText().toString();

                if (username.equals("") && email.equals("")) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please, complete at least one field!", Toast.LENGTH_SHORT).show();
                } else {
                    resetPassword(username, email);
                }
            }
        });
    }

    private void resetPassword(String username, String email) {

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
