package com.scarlat.marius.chatapp.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.scarlat.marius.chatapp.general.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SharedPref {
    private static final String TAG = "SharedPref";

    // Data storage
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor preferencesEditor;

    private static Set<String> friendsSet = new HashSet<>();

    public static Set<String> getFriendsSet() {
        return  sharedPreferences.getStringSet(Constants.FRIENDS_TABLE, new HashSet<String>());
    }

    public static void setup (Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        }
    }

    /* Used for saving user credentials at login/register */
    public static void saveCredentials (final String email, final String password) {
        Log.d(TAG, "saveCredentials: Save email and pass for " + email);

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.EMAIL, email);
        preferencesEditor.putString(Constants.PASSWORD, password);
        preferencesEditor.putBoolean(Constants.CREDENTIALS_CHECKBOX, true);
        preferencesEditor.apply();
    }

    /* Clear saved credentials */
    public static void clearCredentials() {
        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.putBoolean(Constants.CREDENTIALS_CHECKBOX, false);
        preferencesEditor.apply();
    }

    /* Save user ID and device token ID */
    public static void saveUserId(final String userId, final String fullname) {
        Log.d(TAG, "saveUserId: " + userId + " > " + fullname);

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.USER_ID, userId);
        preferencesEditor.putString(userId, fullname);
        preferencesEditor.apply();
    }

    public static void saveDeviceId(final String deviceId) {
        Log.d(TAG, "saveDeviceId: " + deviceId);

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.TOKEN_ID, deviceId);
        preferencesEditor.apply();
    }


    public static void saveFriend(final String friendId, final String friendName) {
        Log.d(TAG, "saveFriend: " + friendId + "; " + friendName);

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(friendId, friendName);
        preferencesEditor.apply();

        addFriendInSet(friendId);
    }

    public static String getString (String key) {
        return sharedPreferences.getString(key, "");
    }

    public static Boolean getBoolean (String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public static void saveUserLogged(final String userId) {
        Log.d(TAG, "saveUserLogged: " + userId);

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.ONLINE, userId);
        preferencesEditor.apply();
    }

    public static void saveCoordinates(double latitude, double longitude) {
        Log.d(TAG, "saveCoordinates: Method was invoked!");

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(Constants.USER_LATITUDE, String.valueOf(latitude));
        preferencesEditor.putString(Constants.USER_LONGITUDE, String.valueOf(longitude));
        preferencesEditor.apply();
    }

    public static void addFriendInSet(final String userId) {
        Log.d(TAG, "addFriendInSet: " + userId);

        preferencesEditor = sharedPreferences.edit();
        friendsSet.add(userId);
        preferencesEditor.putStringSet(Constants.FRIENDS_TABLE, friendsSet);
        preferencesEditor.apply();

        Log.d(TAG, "addFriendInSet: Set = " + Arrays.toString(friendsSet.toArray()));
    }

}
