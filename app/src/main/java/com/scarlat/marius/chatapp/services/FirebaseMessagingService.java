package com.scarlat.marius.chatapp.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

import java.util.Map;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServic";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Method was invoked!");
        super.onCreate();

//        playNotificationSound();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: Method was invoked!");
        super.onMessageReceived(remoteMessage);

        if (remoteMessage == null || remoteMessage.getData() == null) {
            Log.d(TAG, "onMessageReceived: NULL data");
            return;
        }

        /* Get sender data */
        final Map<String, String> data = remoteMessage.getData();
        notifyUser(data);
    }


    private void notifyUser(Map<String, String> data) {
        Log.d(TAG, "onMessageReceived: Data = " + data.toString());

        final String title = data.get("title");
        final String body = data.get("body");
        final String click_action = data.get("click_action");
        final String sender_id = data.get("sender_id");

        /* Create a new notification id */
        final int notificationId = (int) (System.currentTimeMillis() / 1000);

        /* Setup priority */
        int priority = NotificationCompat.PRIORITY_HIGH;

        /* Configure notification */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#f45342"))
                .setPriority(priority);

        /* Setup pending intent */
        Intent intent = new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.USER_ID, sender_id);

        PendingIntent pendingIntent =  PendingIntent.getActivity(
                getApplicationContext(),
                12,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            priority = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(  Constants.CHANNEL_ID,
                                                                    Constants.CHANNEL_NAME, priority);
            channel.setDescription(Constants.CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.enableVibration(true);
            mNotificationManager.createNotificationChannel(channel);
        }

        if (mNotificationManager.areNotificationsEnabled()) {
            mNotificationManager.notify(notificationId, mBuilder.build());
            playNotificationSound();
        }
    }

//     TODO: check sounds in priority
    private void playNotificationSound() {
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        ringtone.play();
    }
}
