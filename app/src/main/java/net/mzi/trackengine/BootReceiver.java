package net.mzi.trackengine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent myIntent = new Intent(context, ServiceLocation.class);
            context.startForegroundService(myIntent);

            myIntent = new Intent(context, ServiceBattery.class);
            context.startForegroundService(myIntent);
        } else {
            Intent myIntent = new Intent(context, ServiceLocation.class);
            context.startService(myIntent);

            myIntent = new Intent(context, ServiceBattery.class);
            context.startService(myIntent);
        }

    }
}
