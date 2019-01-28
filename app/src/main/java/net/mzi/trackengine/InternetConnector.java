package net.mzi.trackengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import net.mzi.trackengine.model.TicketInfoClass;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Poonam on 5/1/2017.
 */

public class InternetConnector {
    Cursor cquery;
    String sDeviceId;

    SharedPreferences pref;

    SQLiteDatabase sql;

    public void offlineSyncing(final Context context, int Flag) {
        sql = context.openOrCreateDatabase("MZI.sqlite", context.MODE_PRIVATE, null);
        pref = context.getSharedPreferences("login", 0);
        sDeviceId = pref.getString("DeviceId", "0");
        try {
            if (Flag == 1) {
                Map<Long, Map<String, String>> mobileData = MyApp.getApplication().readMobileData();
                if (mobileData.keySet().size() > 0) {
                    for (long key : mobileData.keySet()) {
                        MainActivity m = new MainActivity();
                        m.PushMobileData(mobileData.get(key), context, key);
                    }
                }

                Map<Long, Map<String, String>> gpsData = MyApp.getApplication().readGPSData();
                if (gpsData.keySet().size() > 0) {
                    for (long key : gpsData.keySet()) {
                        GpsLocationReceiver m = new GpsLocationReceiver();
                        m.GPSEnableOperation(gpsData.get(key), context, key);
                    }
                }

            } else {
                try {
                    Toast.makeText(context, "Network not found", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
            cquery.close();
        } catch (Exception e) {
//            e.getMessage().toString();
        }

        try {
            Map<String, TicketInfoClass> map = MyApp.getApplication().readTicketCapture();
            for (String key : map.keySet()) {
                if (!map.get(key).isCaptured) {
                    Firstfrag f = new Firstfrag();
                    f.callApiToMakeCapture(key);
                } else {
                    map.remove(key);
                }
            }
            MyApp.getApplication().writeTicketCapture(map);
        } catch (Exception e) {
        }

    }
}
