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
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 4/19/2017.
 */
public class ServiceBattery extends Service {
    Map<String,String> batteryInfo=new HashMap<String, String>();
    ApiInterface apiInterface;
    Cursor cquery;
    String currentDateTimeString;
    static int BatteryLevel;
    SharedPreferences pref;
    String nh_userid;
    String sDeviceId,sDuration;
    SQLiteDatabase sql;
    SharedPreferences.Editor editor;
    @Override
    public void onCreate() {
        super.onCreate();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", getApplicationContext().MODE_PRIVATE,null);
        pref = getSharedPreferences("login", 0);
        editor = pref.edit();
        //editor = pref.edit();
        nh_userid=pref.getString("userid","userid");
        sDeviceId=pref.getString("DeviceId","0");
        sDuration=pref.getString("CheckedInDuration",currentDateTimeString);
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Date cDate = new Date();
        if(BatteryLevel==0);
        else {
            SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date localDate = null;
            Date liveDate = cDate;
            String strUpdatedDate = sDuration;
            //SimpleDateFormat liveUpdatedate`   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                localDate = Updatedate.parse(strUpdatedDate);
                long different = liveDate.getTime() - localDate.getTime();
                long m = different;
                long elapsedMinutes = different / minutesInMilli;
                different = different % minutesInMilli;
                if (elapsedMinutes >= 15) {
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    batteryInfo.put("UserId", nh_userid);
                    batteryInfo.put("DeviceId", sDeviceId);
                    batteryInfo.put("Battery", String.valueOf(BatteryLevel));
                    batteryInfo.put("ActivityDate", currentDateTimeString);
                    batteryInfo.put("AutoCaptured", "true");
                    batteryInfo.put("RealTimeUpdate", "true");
                    sql.execSQL("INSERT INTO User_BatteryLevel(UserId,BatteryLevel,AutoCaptured,ActionDate,SyncStatus)VALUES('" + nh_userid + "','" + BatteryLevel + "','true','" + currentDateTimeString + "','-1')");
                    Cursor cquery = sql.rawQuery("select * from User_BatteryLevel ", null);
                    String sColumnId = null;
                    if (cquery.getCount() > 0) {
                        cquery.moveToLast();
                        sColumnId = cquery.getString(0).toString();
                    }
                    BatteryOperation(batteryInfo, getApplicationContext(), sColumnId);
                    cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    editor.putString("CheckedInDuration", currentDateTimeString);
                    editor.commit();
                }
            }catch (ParseException e){
                e.printStackTrace();
            }catch (Exception e){}
        }


        return Service.START_NOT_STICKY;
    }


    public BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent intent) {

            BatteryLevel = intent.getIntExtra("level", 0);
            Log.e("testlow", String.valueOf(BatteryLevel) + "%");
            unregisterReceiver(this);

        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "battryonDestrcalled" );
        //this.unregisterReceiver(this.mBatInfoReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    public void BatteryOperation(Map batteryInfo, final Context ctx, final String sColumnId) {
        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", getApplicationContext().MODE_PRIVATE,null);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Log.e("BatteryOperation: ", batteryInfo.toString());
        final ApiResult apiResult = new ApiResult();

        final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel("true", batteryInfo.get("UserId").toString(), batteryInfo.get("DeviceId").toString(), batteryInfo.get("Battery").toString(), batteryInfo.get("ActivityDate").toString(), batteryInfo.get("AutoCaptured").toString());
        Call<ApiResult.User_BatteryLevel> call1 = apiInterface.PostBatteryLevel(userBatteryLevel);
        final String finalColumnId = sColumnId;
        call1.enqueue(new Callback<ApiResult.User_BatteryLevel>() {
            @Override
            public void onResponse(Call<ApiResult.User_BatteryLevel> call, Response<ApiResult.User_BatteryLevel> response) {
                ApiResult.User_BatteryLevel iData = response.body();
                if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                    ContentValues newValues = new ContentValues();
                    newValues.put("SyncStatus", "false");
                    sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                } else {
                    ContentValues newValues = new ContentValues();
                    newValues.put("SyncStatus", "true");
                    sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                }
            }

            @Override
            public void onFailure(Call<ApiResult.User_BatteryLevel> call, Throwable t) {
                call.cancel();

            }
        });
    }
}
