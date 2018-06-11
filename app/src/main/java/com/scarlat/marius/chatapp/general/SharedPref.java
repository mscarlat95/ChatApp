package com.scarlat.marius.chatapp.general;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    // Data storage
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor preferencesEditor;

    public static void setup (Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        }
    }

    public static void saveCredentials (final String email, final String password) {
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.EMAIL, email);
        preferencesEditor.putString(Constants.PASSWORD, password);
        preferencesEditor.putBoolean(Constants.CREDENTIALS_CHECKBOX, true);

        preferencesEditor.apply();
    }

    public static void clearCredentials() {
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.putBoolean(Constants.CREDENTIALS_CHECKBOX, false);
        preferencesEditor.apply();
    }

    public static String getString (String key) {
        return sharedPreferences.getString(key, "");
    }

    public static Boolean getBoolean (String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}
