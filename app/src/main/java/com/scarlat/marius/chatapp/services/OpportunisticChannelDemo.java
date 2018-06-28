package com.scarlat.marius.chatapp.services;


import android.content.Intent;
import android.util.Log;

import com.scarlat.marius.chatapp.general.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ro.pub.acs.hyccups.opportunistic.Channel;

public class OpportunisticChannelDemo extends Channel{

    private static final String TAG = "OpportunisticDemo";

    @Override
    public String getName() {
        return "OpportunisticChannelDemo";
    }

    @Override
    public List<String> getInterests() {
        return new ArrayList<>();
    }

    /**
     *
     * @param deviceId  identifies the device the user was signed in on
     * @param userId    identifies the user that we are interacting with
     */
    @Override
    public void onPeerConnected(String deviceId, String userId) {
        Log.d(TAG, "onPeerConnected: deviceId = " + deviceId + "; userId = " + userId);

        Intent intent = new Intent(Constants.ACTION_PEER_CONNECTED);
        intent.putExtra(Constants.USER_ID, userId);
        sendBroadcast(intent);
    }


    /**
     *
     * @param deviceId  identifies the device the user was signed in on
     * @param userId    identifies the user that we are interacting with
     */
    @Override
    public void onPeerDisconnected(String deviceId, String userId) {
        Log.d(TAG, "onPeerDisconnected: deviceId = " + deviceId + "; userId = " + userId);

        Intent intent = new Intent(Constants.ACTION_PEER_DISCONNECTED);
        intent.putExtra(Constants.USER_ID, userId);

//        TODO: uncomment this

//        sendBroadcast(intent);
    }


    /**
     *
     * @param sourceUserId          is the ID of the user that sent the message
     * @param destinationUserId     is the ID of the user that received the message (in this case the current user’s ID)
     * @param message               is the actual content of the forwarding operation
     * @param timestamp             is the time the message was generated expressed in UNIX time
     */
    @Override
    public void onMessageReceived(String sourceUserId, String destinationUserId, String message, long timestamp) {
        Log.d(TAG, "onMessageReceived: sourceUserId = " + sourceUserId + "; destinationUserId = " + destinationUserId);
        Log.d(TAG, "onMessageReceived: message = " + message + "; timestamp = " + timestamp);

        Intent intent = new Intent(Constants.ACTION_MESSAGE_RECEIVED);
        intent.putExtra(Constants.USER_ID, sourceUserId);
        intent.putExtra(Constants.MESSAGE_CONTENT, message);
        intent.putExtra(Constants.TIMESTAMP, timestamp);

        sendBroadcast(intent);
    }

    /**
     *
     * @param sourceUserId      is the ID of the user that sent the message
     * @param message           is the actual content of the forwarding operation
     * @param timestamp         is the time the message was generated expressed in UNIX time
     * @param tags              is an array of interests that were used to tag the dissemination
     */
    @Override
    public void onDisseminationReceived(String sourceUserId, String message, long timestamp, String[] tags) {
        Log.d(TAG, "onDisseminationReceived: sourceUserId = " + sourceUserId);
        Log.d(TAG, "onDisseminationReceived: message = " + message + "; timestamp = " + timestamp);
        Log.d(TAG, "onDisseminationReceived: tags = " + Arrays.toString(tags));
    }


    /* Provide customized social information about each user */
//    @Override
//    public SocialInfo getSocialInfo() {
//        Log.d(TAG, "getSocialInfo: Method was invoked !");
//
//        /* Obtain user id */
//        SharedPref.setup(getApplicationContext());
//        String userId = SharedPref.getString(Constants.USER_ID);
//
//        if (userId.equals("")) {
//            userId = FirebaseAuth.getInstance().getUid();
//        }
//        Log.d(TAG, "getSocialInfo: Current userId = " + userId);
//
//        return new SocialInfo(userId);
//    }

    /* Used for troubleshooting */
    @Override
    public void onDisconnected(String error) {
        Log.d(TAG, "onDisconnected: Error = " + error);
    }
}
