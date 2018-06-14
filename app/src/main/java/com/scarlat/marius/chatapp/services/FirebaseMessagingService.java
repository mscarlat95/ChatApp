package com.scarlat.marius.chatapp.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.scarlat.marius.chatapp.R;
import com.scarlat.marius.chatapp.general.Constants;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServic";



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: Method was invoked!");

        /* Get sender data */
        final String title = remoteMessage.getNotification().getTitle();
        final String body = remoteMessage.getNotification().getBody();
        final String click_action = remoteMessage.getNotification().getClickAction();
        final String sender_id = remoteMessage.getData().get("sender_id");

        /* Create a new notification id */
        final int notificationId = (int) System.currentTimeMillis();

        /* Configure notification */
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_ID);

        mBuilder.setSmallIcon(R.drawable.ic_stat_notification);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(body);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

        /* Setup pending intent */
        Intent intent = new Intent(click_action);
        intent.putExtra(Constants.USER_ID, sender_id);
        PendingIntent pendingIntent =  PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
