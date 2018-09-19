package net.mzi.trackengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

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
    Map<String, String> loginInfo = new HashMap<String, String>();
    Map<String, String> gpsInfo = new HashMap<String, String>();
    Map<String, String> internetInfo = new HashMap<String, String>();
    Map<String, String> locationInfo = new HashMap<String, String>();
    Map<String, String> batteryInfo = new HashMap<String, String>();
    Map<String, String> appCheckInInfo = new HashMap<String, String>();
    Map<String, String> issueHistoryInfo = new HashMap<String, String>();
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;

    public void offlineSyncing(final Context context, int Flag) {
        sql = context.openOrCreateDatabase("MZI.sqlite", context.MODE_PRIVATE, null);
        pref = context.getSharedPreferences("login", 0);
        sDeviceId = pref.getString("DeviceId", "0");
        try {
            if (Flag == 1) {

                cquery = sql.rawQuery("select * from Issue_History", null);
                if (cquery.getCount() > 0) {

                    Log.e("InternetConnector: ", "I am in Issue_History");
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        if (cquery.getString(6).equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("Issue_History", "Id" + "=" + id, null);
                        } else {
                            issueHistoryInfo.put("RealtimeUpdate", "false");
                            String DepartmentId = null, sParentComapnyId = null;
                            sParentComapnyId = pref.getString("ParentCompanyId", "ParentCompanyId");
                            DepartmentId = pref.getString("DepartmentId", "DepartmentId");
                            issueHistoryInfo.put("UserId", cquery.getString(2).toString());
                            issueHistoryInfo.put("Id", cquery.getString(0).toString());
                            issueHistoryInfo.put("ParentCompanyId", sParentComapnyId);
                            issueHistoryInfo.put("TicketId", cquery.getString(1).toString());
                            issueHistoryInfo.put("StatusId", cquery.getString(3).toString());
                            issueHistoryInfo.put("Comment", cquery.getString(4).toString());
                            issueHistoryInfo.put("ActivityDate", cquery.getString(5).toString());
                            issueHistoryInfo.put("DeviceId", sDeviceId);
                            issueHistoryInfo.put("DepartmentId", DepartmentId);
                            issueHistoryInfo.put("Latitude", "-");
                            issueHistoryInfo.put("Longitude", "-");
                            issueHistoryInfo.put("AssetSerialNo", " ");
                            issueHistoryInfo.put("ModeOfTransport", "0");
                            issueHistoryInfo.put("Expense", "0");
                            issueHistoryInfo.put("AssignedUserId", "0");

                            SchedulingAdapter m = new SchedulingAdapter();
                            m.UpdateTask(context, issueHistoryInfo, cquery.getString(0).toString());
                        }

                    }
                }

                cquery = sql.rawQuery("select * from User_AppCheckIn", null);
                Log.e("offlineSyncing: ", "dgdfgdfgfdgfgfgfgdfgdfgdfg");
                if (cquery.getCount() > 0) {
                    Log.e("offlineSyncing: ", "dvvvvvvvvvvvvvvvvvvvvvvdfgdfgdfg");
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        if (cquery.getString(4).toString().equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_AppCheckIn", "Id" + "=" + id, null);
                        } else {
                            if (cquery.getString(4).toString().equals("-1") || cquery.getString(4).toString().equals("false")) {
                                appCheckInInfo.put("RealTimeUpdate", "false");
                            }
                            appCheckInInfo.put("UserId", cquery.getString(1).toString());
                            appCheckInInfo.put("DeviceId", sDeviceId);
                            appCheckInInfo.put("IsCheckedIn", cquery.getString(2).toString());
                            appCheckInInfo.put("ActivityDate", cquery.getString(3).toString());
                            String sAppCheckInInfo = new Gson().toJson(appCheckInInfo);
                            Log.e("onCheckedChanged: ", sAppCheckInInfo);
                            MainActivity m = new MainActivity();
                            m.appCheckINOperation(appCheckInInfo, cquery.getString(0).toString());
                        }

                    }
                }

                cquery = sql.rawQuery("select * from User_BatteryLevel", null);
                if (cquery.getCount() > 0) {

                    Log.e("InternetConnector: ", "I am in User_BatteryLevel");
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        if (cquery.getString(5).equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_BatteryLevel", "Id" + "=" + id, null);
                        } else {
                            if (cquery.getString(5).toString().equals("-1") || cquery.getString(5).toString().equals("false")) {
                                batteryInfo.put("RealTimeUpdate", "false");
                            }
                            batteryInfo.put("UserId", cquery.getString(1).toString());
                            batteryInfo.put("DeviceId", sDeviceId);
                            batteryInfo.put("Battery", cquery.getString(2).toString());
                            batteryInfo.put("ActivityDate", cquery.getString(4).toString());
                            batteryInfo.put("AutoCaptured", cquery.getString(3).toString());
                            String jsonString = new Gson().toJson(batteryInfo);
                            ServiceBattery m = new ServiceBattery();
                            m.BatteryOperation(batteryInfo, context, cquery.getString(0).toString());
                        }
                    }
                }
                //if (networkInfo.isConnected()) {
                Toast.makeText(context, "Offline data syncing!!!", Toast.LENGTH_LONG).show();
                int counter = 0;
                sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
                cquery = sql.rawQuery("select * from User_Location", null);
                if (cquery.getCount() > 0) {
                    String sCheckInStatus = pref.getString("CheckedInStatus", "0");
                    if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {

                    } else {
                        Cursor cquery = sql.rawQuery("select * from User_Location", null);
                        if (cquery.getCount() > 0) {

                            Log.e("InternetConnector: ", "I am in User_location" + cquery.getCount());

                            for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                                String id = cquery.getString(0).toString();
                                sql.delete("User_Location", "Id" + "=" + id, null);
                            }
                        }
                        return;
                    }

                    Log.e("InternetConnector: ", "I am in User_location" + cquery.getCount());

                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        ++counter;
                        if (counter >= 50) {
                            cquery.moveToLast();
                            break;
                        }
                        if (cquery.getString(15).toString().equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_Location", "Id" + "=" + id, null);
                        } else {
                            if (cquery.getString(15).toString().equals("-1")
                                    || cquery.getString(15).toString().equals("false")) {
                                locationInfo.put("RealTimeUpdate", "false");
                            }
                            locationInfo.put("UserId", cquery.getString(1).toString());
                            locationInfo.put("DeviceId", sDeviceId);
                            locationInfo.put("Latitude", cquery.getString(2).toString());
                            locationInfo.put("Longitude", cquery.getString(3).toString());
                            locationInfo.put("AutoCaptured", cquery.getString(4).toString());
                            locationInfo.put("ActivityDate", cquery.getString(5).toString());
                            locationInfo.put("AddressLine", "NA");
                            locationInfo.put("Premises", "NA");
                            locationInfo.put("SubLocality", "NA");
                            locationInfo.put("SubAdminArea", "NA");
                            locationInfo.put("PostalCode", "NA");
                            locationInfo.put("City", "NA");
                            locationInfo.put("State", "NA");
                            locationInfo.put("Country", "NA");
                            locationInfo.put("KnownName", "NA");
                            locationInfo.put("Provider", "NA");
                            ServiceLocation m = new ServiceLocation();
                            //insert

                            m.LocationOperationOffline(locationInfo, context, cquery.getString(0).toString());
                            String id = cquery.getString(0).toString();
                            Log.d("postcoordinat", "offline syncing with id = " + id);
                            sql.delete("User_Location", "Id" + "=" + id, null);
                        }
                        if (counter >= 50) {
                            break;
                        }
                    }

                    Cursor cquery = sql.rawQuery("select * from User_Location", null);
                    if (cquery.getCount() > 0) {

                        Log.e("InternetConnector: ", "I am in User_location" + cquery.getCount());

                        for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_Location", "Id" + "=" + id, null);
                        }
                    }

                }


                cquery = sql.rawQuery("select * from User_MobileData", null);
                if (cquery.getCount() > 0) {
                    Log.e("InternetConnector: ", "I am in User_MobileData");
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        if (cquery.getString(4).toString().equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_MobileData", "Id" + "=" + id, null);
                        } else {
                            if (cquery.getString(4).toString().equals("-1") || cquery.getString(4).toString().equals("false")) {
                                internetInfo.put("RealTimeUpdate", "false");
                            }
                            internetInfo.put("UserId", cquery.getString(1).toString());
                            internetInfo.put("Enabled", cquery.getString(2).toString());
                            internetInfo.put("ActionDate", cquery.getString(3).toString());
                            internetInfo.put("DeviceId", sDeviceId);
                            MainActivity m = new MainActivity();
                            m.PushMobileData(internetInfo, context, cquery.getString(0).toString());
                        }
                    }
                }

                cquery = sql.rawQuery("select * from User_Gps ", null);
                if (cquery.getCount() > 0) {
                    Log.e("InternetConnector: ", "I am in User_Gps");
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        if (cquery.getString(4).toString().equals("true")) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_Gps", "Id" + "=" + id, null);
                        } else {
                            if (cquery.getString(4).toString().equals("-1") || cquery.getString(4).toString().equals("false")) {
                                gpsInfo.put("RealTimeUpdate", "false");
                            }
                            gpsInfo.put("UserId", cquery.getString(1).toString());
                            gpsInfo.put("Enabled", cquery.getString(2).toString());
                            gpsInfo.put("ActionDate", cquery.getString(3).toString());
                            gpsInfo.put("DeviceId", sDeviceId);
                            String jsonString = new Gson().toJson(gpsInfo);
                            GpsLocationReceiver m = new GpsLocationReceiver();
                            m.GPSEnableOperation(gpsInfo, context, cquery.getString(0).toString());
                        }

                    }
                }

            } else {
                Toast.makeText(context, "Network not found", Toast.LENGTH_LONG).show();
            }
            cquery.close();
        } catch (Exception e) {
            e.getMessage().toString();

        }
    }
}
