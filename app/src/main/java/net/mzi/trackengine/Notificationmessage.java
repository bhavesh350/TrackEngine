package net.mzi.trackengine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Poonam on 3/12/2018.
 */

public class Notificationmessage extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("onReceive: ", "he he he he ");
        showNotification(context);

    }


    private void showNotification(Context context) {
        Log.i("notification", "visible");

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Notificationmessage.class), 0);
        NotificationCompat.BigTextStyle bigStyle =
                new NotificationCompat.BigTextStyle();
        bigStyle.setBigContentTitle("Alert!!!");
        bigStyle.bigText("You are currently checked in. Please remember to checkout when your working hours are over to mark your attendance! ");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.som)
                        .setOngoing(true) // Again, THIS is the important line
//                        .setContentTitle("Alert!!!")
                        .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
//                        .setContentText("You are currently checked in. Please remember to checkout when your working hours are over to mark your attendance! ");
                        .setStyle(bigStyle);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(12345, mBuilder.build());
    }
}
