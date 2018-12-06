package net.mzi.trackengine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import net.mzi.trackengine.model.TicketInfoClass;

import java.util.List;

public class ReadMessageService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("SMSTRACK","onCreate Called");

        SMSReceiver.bindListener(new SmsListner() {
            @Override
            public void messageReceived(TicketInfoClass messageText) {

            }
        });
    }



    @Override
    public void onStart(Intent intent, int startid) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SMSTRACK", "Received start id " + startId + ": " + intent);
//        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("SMSTRACK","onDestroy called");
        super.onDestroy();
    }

}