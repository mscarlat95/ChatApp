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

    private static Set<String> offlineDiscoveredFriends = new HashSet<>();

    public static Set<String> getFriendsSet() {
        return  sharedPreferences.getStringSet(Constants.FRIENDS_TABLE, new HashSet<String>());
    }

    public static Set<String> getOfflineDiscoveredFriends() {
        return sharedPreferences.getStringSet(Constants.CHAT_TABLE, new HashSet<String>());
    }

    public static void setup (Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Constants.USER_PREFERENCES, Context.MODE_PRIVATE);
        }
    }

    public static void saveString (final String key, final String value) {
        Log.d(TAG, "saveString: Method was invoked!");

        preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putString(key, value);
        preferencesEditor.apply();
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
        saveString(Constants.USER_ID, userId);
        saveString(userId, fullname);
    }

    public static void saveDeviceId(final String deviceId) {
        Log.d(TAG, "saveDeviceId: " + deviceId);
        saveString(Constants.TOKEN_ID, deviceId);
    }

    public static void saveFriend(final String friendId, final String friendName) {
        Log.d(TAG, "saveFriend: " + friendId + "; " + friendName);

        saveString(friendId, friendName);
        addFriendInSet(friendId);
    }

    public static String getString (final String key) { return sharedPreferences.getString(key, ""); }

    public static Boolean getBoolean (final String key) { return sharedPreferences.getBoolean(key, false); }

    public static Long getLong (final String key) { return sharedPreferences.getLong(key, 0); }

    public static void saveUserLogged(final String userId) {
        Log.d(TAG, "saveUserLogged: " + userId);
        saveString(Constants.ONLINE, userId);
    }

    public static void saveCoordinates(double latitude, double longitude) {
        Log.d(TAG, "saveCoordinates: Method was invoked!");

        saveString(Constants.USER_LATITUDE, String.valueOf(latitude));
        saveString(Constants.USER_LONGITUDE, String.valueOf(longitude));
    }

    public static void addFriendInSet(final String userId) {
        Log.d(TAG, "addFriendInSet: " + userId);

        preferencesEditor = sharedPreferences.edit();
        friendsSet.add(userId);
        preferencesEditor.putStringSet(Constants.FRIENDS_TABLE, friendsSet);
        preferencesEditor.apply();

        Log.d(TAG, "addFriendInSet: Set = " + Arrays.toString(friendsSet.toArray()));
    }


    public static void saveDiscoveredFriends(final String friendId, final long timestamp) {
        Log.d(TAG, "saveDiscoveredFriends: " + friendId + "; " + timestamp);

        preferencesEditor = sharedPreferences.edit();

        offlineDiscoveredFriends.add(friendId);
        preferencesEditor.putStringSet(Constants.CHAT_TABLE, offlineDiscoveredFriends);
        preferencesEditor.putLong(Constants.CHAT_TABLE + friendId, timestamp);
        preferencesEditor.apply();

        Log.d(TAG, "saveDiscoveredFriends: Set = " + Arrays.toString(offlineDiscoveredFriends.toArray()));
    }


}
