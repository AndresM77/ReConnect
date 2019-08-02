package com.example.reconnect;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationCompat;

import com.example.reconnect.Activities.NotificationActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.parse.ParseUser;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    /* This method decides what to do when a notification is received */
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //TODO check
       // if(remoteMessage.getData().get("recipient").equals(ParseUser.getCurrentUser().getObjectId())) {

            Log.d("FCM", "message received");

            Intent i = new Intent(this, NotificationActivity.class);
            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = new Random().nextInt(5000);

            // set up channels if the Android version requires it
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setUpChannels(nManager);
            }

            // intent stuff that I do not understand right now
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);

            //create a notification
            //TODO more customization here
            NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.reconnect_icon)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setContentIntent(pendingIntent);

            // notify manager that the notification has been built
            nManager.notify(notificationId, nBuilder.build());
      //  }

        // When the notification is clicked on
        //Log.d(TAG, "Notification Click Action: " + remoteMessage.getNotification().getClickAction());

    }

    @Override
    public void onMessageSent(String var1) {

    }

    @Override
    public void onSendError(String var1, Exception var2) {

    }

    @Override
    public void onNewToken(String s) {
        Log.d("FCM Service", "Token refreshed to " + s);
        super.onNewToken(s);
        ParseUser.getCurrentUser().put("deviceId", s);
        ParseUser.getCurrentUser().saveInBackground();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setUpChannels(NotificationManager nManager) {
        if (nManager != null) {
            CharSequence adminChannelName = "New notification";
            String adminChannelDescription = "Schedule events with other Users";

            NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
            adminChannel.setDescription(adminChannelDescription);
            //TODO more customization here

            nManager.createNotificationChannel(adminChannel);
        }
    }
}
