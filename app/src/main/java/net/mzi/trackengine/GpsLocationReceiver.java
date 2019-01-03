package net.mzi.trackengine;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 8/21/2017.
 */

public class GpsLocationReceiver extends BroadcastReceiver {
    Map<String, String> mGpsInfo = new HashMap<String, String>();
    String currentDateTimeString, sDeviceId;
    ApiInterface apiInterface;
    SharedPreferences pref;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
//        h.postDelayed(check2MinTask, 2 * 60 * 1000);
        pref = context.getSharedPreferences("login", 0);
        sDeviceId = pref.getString("DeviceId", "0");
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            //Toast.makeText(context, "Location Ofgfggff", Toast.LENGTH_LONG).show();

            try {

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                mGpsInfo.put("UserId", MainActivity.LOGINID);
                mGpsInfo.put("DeviceId", sDeviceId);
                mGpsInfo.put("RealTimeUpdate", "true");
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Date cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss").format(cDate);
                    Log.e("GPS_RECEIVER: ", "Enabled");
                    mGpsInfo.put("Enabled", "true");
                    mGpsInfo.put("ActionDate", currentDateTimeString);
                    Map<Long, Map<String, String>> gpsMap = MyApp.getApplication().readGPSData();
                    long insertKey = System.currentTimeMillis();
                    gpsMap.put(insertKey, mGpsInfo);
                    MyApp.getApplication().writeGPSData(gpsMap);
                    GPSEnableOperation(mGpsInfo, context, insertKey);

                } else {
                    Log.e("GPS_RECEIVER: ", "Disabled");
                    Date cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss").format(cDate);
                    mGpsInfo.put("Enabled", "false");
                    mGpsInfo.put("ActionDate", currentDateTimeString);

                    Map<Long, Map<String, String>> gpsMap = MyApp.getApplication().readGPSData();
                    long insertKey = System.currentTimeMillis();
                    gpsMap.put(insertKey, mGpsInfo);
                    MyApp.getApplication().writeGPSData(gpsMap);
                    GPSEnableOperation(mGpsInfo, context, insertKey);
                    //showAlert
                }
                // react on GPS provider change action
            } catch (Exception e) {
                Log.e("onReceive: ", e.getMessage());
            }
        }
    }

    public void GPSEnableOperation(Map mGpsInfo, final Context context, final long key) {
        try {
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            final ApiResult apiResult = new ApiResult();
            final ApiResult.User_GPS user_GPS = apiResult.new User_GPS("-1", mGpsInfo.get("UserId").toString(), mGpsInfo.get("DeviceId").toString(), mGpsInfo.get("Enabled").toString(), mGpsInfo.get("ActionDate").toString());
            Call<ApiResult.User_GPS> call1 = apiInterface.PostGpsStatus(user_GPS);
            call1.enqueue(new Callback<ApiResult.User_GPS>() {
                @Override
                public void onResponse(Call<ApiResult.User_GPS> call, Response<ApiResult.User_GPS> response) {
                    ApiResult.User_GPS iData = response.body();
                    try {
                        if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                            try {
                                Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                            }
                        } else {
                            Map<Long, Map<String, String>> gpsMap = MyApp.getApplication().readGPSData();
                            try {
                                gpsMap.remove(key);
                                MyApp.getApplication().writeGPSData(gpsMap);
                            } catch (Exception e) {
                            }
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onFailure(Call<ApiResult.User_GPS> call, Throwable t) {
                    call.cancel();

                }
            });
        } catch (Exception e) {

        }
    }
}
