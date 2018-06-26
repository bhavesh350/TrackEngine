package net.mzi.trackengine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by Poonam on 1/30/2017.
 */

public class TwentyFourServiceBroadcastReceiver extends BroadcastReceiver {
    SQLiteDatabase sql;
    Cursor cquery;
    @Override
    public void onReceive(Context context, Intent intent) {
        sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE,null);
        /*Toast.makeText(context,"hi..my 24 service",Toast.LENGTH_SHORT).show();
        Log.e( "onReceive: ","hi..my 24 service" );*/
       //sql.execSQL("delete * from  User_BatteryLevel");
        //sql.execSQL("delete * from  User_Location");
    }
}
