package net.mzi.trackengine;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 4/19/2017.
 */
public class ServiceBattery extends Service {

    public ServiceBattery(Context c) {
        super();
    }

    public ServiceBattery() {
    }

    Map<String, String> batteryInfo = new HashMap<String, String>();
    ApiInterface apiInterface;
    Cursor cquery;
    String currentDateTimeString;
    static int BatteryLevel;
    SharedPreferences pref;
    String nh_userid;
    String sDeviceId;
    SQLiteDatabase sql;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        try {
            sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", getApplicationContext().MODE_PRIVATE, null);
            pref = getSharedPreferences("login", 0);
            editor = pref.edit();
            nh_userid = pref.getString("userid", "userid");
            sDeviceId = pref.getString("DeviceId", "0");
            this.registerReceiver(this.mBatInfoReceiver,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception e) {
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Date cDate = new Date();
        if (BatteryLevel == 0) ;
        else {
//            SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date localDate = null;
//            Date liveDate = cDate;
//            String strUpdatedDate = sDuration;
            //SimpleDateFormat liveUpdatedate`   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
//                long secondsInMilli = 1000;
//                long minutesInMilli = secondsInMilli * 60;
//                localDate = Updatedate.parse(strUpdatedDate);
//                long different = liveDate.getTime() - localDate.getTime();
//                long m = different;
//                long elapsedMinutes = different / minutesInMilli;
//                different = different % minutesInMilli;
                long lastBatteryTime = MyApp.getSharedPrefLong("BAT");
                if (lastBatteryTime == 0) {
                    MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
                }
                long differ = System.currentTimeMillis() - lastBatteryTime;
                if (differ >= (15 * 60 * 1000)) {
                    MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    batteryInfo.put("UserId", nh_userid);
                    batteryInfo.put("DeviceId", sDeviceId);
                    batteryInfo.put("Battery", String.valueOf(BatteryLevel));
                    batteryInfo.put("ActivityDate", currentDateTimeString);
                    batteryInfo.put("AutoCaptured", "true");
                    batteryInfo.put("RealTimeUpdate", "true");
                    batteryInfo.put("syncStatus", "false");
                    Map<String, Map<String, String>> bMap = MyApp.getApplication().readBatteryHistory();
                    bMap.put(currentDateTimeString, batteryInfo);
                    MyApp.getApplication().writeBatteryHistory(bMap);
                    BatteryOperation(batteryInfo, getApplicationContext(), false);
//                    cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    editor.putString("CheckedInDuration", currentDateTimeString);
                    editor.commit();
                }
            } catch (Exception e) {
            }
        }


        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }


    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {

            BatteryLevel = intent.getIntExtra("level", 0);
            Log.e("testlow", String.valueOf(BatteryLevel) + "%");
            try {
                unregisterReceiver(this);
            } catch (Exception e) {
            }

        }
    };

    @Override
    public void onDestroy() {
//        super.onDestroy();
        Log.e("TAG", "battryonDestrcalled");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(new Intent(getApplicationContext(), ServiceBattery.class));
//        } else {
//            startService(new Intent(getApplicationContext(), ServiceBattery.class));
//        }
//        Intent broadcastIntent = new Intent("net.mzi.trackengine.BootReceiverBattery");
//        sendBroadcast(broadcastIntent);
//        //this.unregisterReceiver(this.mBatInfoReceiver);
//        Intent myIntent = new Intent(getApplicationContext(), ServiceBattery.class);
//        startService(myIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    public void BatteryOperation(final Map<String, String> batteryInfo, final Context ctx, boolean realTimeUpdate) {
        final Map<String, Map<String, String>> batMap = MyApp.getApplication().readBatteryHistory();
        long lastBatteryTime = MyApp.getSharedPrefLong("BAT");
        if (lastBatteryTime == 0) {
            MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
        }
        long differ = System.currentTimeMillis() - lastBatteryTime;
        if (differ > (15 * 60 * 1000)) {
//            try {
//                startService(new Intent(getApplicationContext(), ServiceLocation.class));
//            } catch (Exception e) {
//            }
            MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());

            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            final ApiResult apiResult = new ApiResult();

            final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel(realTimeUpdate ? "true" : "false", batteryInfo.get("UserId"), batteryInfo.get("DeviceId"), batteryInfo.get("Battery"), batteryInfo.get("ActivityDate"), batteryInfo.get("AutoCaptured"));
            Call<ApiResult.User_BatteryLevel> call1 = apiInterface.PostBatteryLevel(userBatteryLevel);
            call1.enqueue(new Callback<ApiResult.User_BatteryLevel>() {
                @Override
                public void onResponse(Call<ApiResult.User_BatteryLevel> call, Response<ApiResult.User_BatteryLevel> response) {
                    try {
                        ApiResult.User_BatteryLevel iData = response.body();
                        if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                            Map<String, String> bMap = batMap.get(batteryInfo.get("ActivityDate"));
                            bMap.put("syncStatus", "false");
                            batMap.put(batteryInfo.get("ActivityDate"), bMap);
                            MyApp.getApplication().writeBatteryHistory(batMap);
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "false");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                        } else {
                            batMap.remove(batteryInfo.get("ActivityDate"));
                            MyApp.getApplication().writeBatteryHistory(batMap);
                            int batteryCount = MyApp.getApplication().readBatteryHistory().keySet().size();

                            ((MainActivity) ctx).txt_battery_count.setText(batteryCount + "");
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "true");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ApiResult.User_BatteryLevel> call, Throwable t) {
                    call.cancel();
                    Log.d("battery>>>>>>>>>>>", "api failure");
                }
            });
        }
    }

    public void BatteryOffline(final Map<String, String> batteryInfo, final Context ctx, boolean realTimeUpdate) {
        final Map<String, Map<String, String>> batMap = MyApp.getApplication().readBatteryHistory();
        if (true) {
            try {
//                startService(new Intent(getApplicationContext(), ServiceLocation.class));
//
//            MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());

                apiInterface = ApiClient.getClient().create(ApiInterface.class);
                final ApiResult apiResult = new ApiResult();

                final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel(realTimeUpdate ? "true" : "false", batteryInfo.get("UserId"), batteryInfo.get("DeviceId"), batteryInfo.get("Battery"), batteryInfo.get("ActivityDate"), batteryInfo.get("AutoCaptured"));
                Call<ApiResult.User_BatteryLevel> call1 = apiInterface.PostBatteryLevel(userBatteryLevel);
                call1.enqueue(new Callback<ApiResult.User_BatteryLevel>() {
                    @Override
                    public void onResponse(Call<ApiResult.User_BatteryLevel> call, Response<ApiResult.User_BatteryLevel> response) {
                        try {
                            ApiResult.User_BatteryLevel iData = response.body();
                            if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                Map<String, String> bMap = batMap.get(batteryInfo.get("ActivityDate"));
                                bMap.put("syncStatus", "false");
                                batMap.put(batteryInfo.get("ActivityDate"), bMap);
                                MyApp.getApplication().writeBatteryHistory(batMap);
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "false");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                            } else {
                                final Map<String, Map<String, String>> batMap1 = MyApp.getApplication().readBatteryHistory();

                                batMap1.remove(batteryInfo.get("ActivityDate"));
                                MyApp.getApplication().writeBatteryHistory(batMap1);
                                int batteryCount = MyApp.getApplication().readBatteryHistory().keySet().size();

                                ((MainActivity) ctx).txt_battery_count.setText(batteryCount + "");
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "true");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult.User_BatteryLevel> call, Throwable t) {
                        call.cancel();
                        Log.d("battery>>>>>>>>>>>", "api failure");
                    }
                });
            } catch (Exception e) {
            }
        }
    }
}
