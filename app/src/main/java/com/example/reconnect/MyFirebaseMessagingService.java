package com.example.reconnect;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.reconnect.Activities.MapActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    /* This method decides what to do when a notification is received */
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //TODO delete
        super.onMessageReceived(remoteMessage);

        Intent i = new Intent(this, MapActivity.class);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(5000);

        // set up channels if the Android version requires it
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setUpChannels(nManager);
        }

        // intent stuff that I do not understand right now
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

        //create a notification
        //TODO add all of the elements that we want
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setContentIntent(pendingIntent);

        // notify manager taht the notification has been built
        nManager.notify(notificationId, nBuilder.build());
    }

    //TODO create setUpChannels method


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    public void setUpChannels(NotificationManager nManager) {

    }
}
