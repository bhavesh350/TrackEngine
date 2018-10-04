//package net.mzi.trackengine;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.media.RingtoneManager;
//import android.net.Uri;
//import android.os.BatteryManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.ActivityCompat;
//import android.util.Log;
//
//import com.firebase.client.Firebase;
//import com.google.firebase.FirebaseApp;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import static net.mzi.trackengine.MainActivity.getBatteryPercentage;
//
///**
// * Created by deepshikha on 24/11/16.
// */
//
//public class GoogleService extends Service implements LocationListener {
//
//    boolean isGPSEnable = false;
//    boolean isNetworkEnable = false;
//    double latitude, longitude;
//    LocationManager locationManager;
//    Location location;
//    private Handler mHandler = new Handler();
//    private Timer mTimer = null;
//    long notify_interval = 60000;
////    Intent intent;
//
//    Map<String, String> batteryInfo = new HashMap<String, String>();
//    Map<String, String> locationInfo = new HashMap<String, String>();
//    ApiInterface apiInterface;
//    Gps gps;
//    Cursor cquery;
//    String sDeviceId, sDuration;
//    String nh_userid;
//    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
//    private static int LOCATION_INTERVAL = 10 * 2 * 1000;
//    private static final float LOCATION_DISTANCE = 100f;//100 meters
//    private LocationManager mLocationManager = null;
//    String TAG = "service_location";
//    SharedPreferences.Editor editor;
//    String currentDateTimeString;
//    // private DatabaseReference mDatabase;
//    SharedPreferences pref;
//    SQLiteDatabase sql;
//    Date localDate = null;
//    Date liveDate;
//    Date cDate = new Date();
//    private Context ctx;
//
//    public GoogleService() {
//    }
//
//    public GoogleService(Context c) {
//        ctx = c;
//        FirebaseApp.initializeApp(c);
//        apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Firebase.setAndroidContext(c);
//        pref = c.getSharedPreferences("login", 0);
//        editor = pref.edit();
//        sql = c.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
//        gps = new Gps(c);
//        Date cDate = new Date();
//        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//        //editor = pref.edit();
//        nh_userid = pref.getString("userid", "0");
//        sDeviceId = pref.getString("DeviceId", "0");
//        sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
//        liveDate = cDate;
//        mTimer = new Timer();
//        mTimer.schedule(new TimerTaskToGetLocation(), 60000, notify_interval);
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        FirebaseApp.initializeApp(getApplicationContext());
//        apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Firebase.setAndroidContext(getApplicationContext());
//        pref = getSharedPreferences("login", 0);
//        editor = pref.edit();
//        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
//        gps = new Gps(getApplicationContext());
//        Date cDate = new Date();
//        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//        //editor = pref.edit();
//        nh_userid = pref.getString("userid", "0");
//        sDeviceId = pref.getString("DeviceId", "0");
//        sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
//        gps = new Gps(getApplicationContext());
//        SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        String strUpdatedDate = sDuration;
//        liveDate = cDate;
//        mTimer = new Timer();
//        mTimer.schedule(new TimerTaskToGetLocation(), 60000, notify_interval);
////        intent = new Intent(str_receiver);
////        fn_getlocation();
//
//
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        if (isJustSent) {
//            return;
//        }
//
//        if (location != null) {
//            Log.e("latitude", location.getLatitude() + "");
//            Log.e("longitude", location.getLongitude() + "");
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            fn_update(location, "onLocationChanged");
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//
//    boolean isJustSent = false;
//
//    private void fn_getlocation() {
//        sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
//        if (isJustSent) {
//            return;
//        }
//        isJustSent = true;
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                isJustSent = false;
//            }
//        }, 10000);
//        try {
//            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//        } catch (Exception e) {
//            locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
//        }
//        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if (!isGPSEnable && !isNetworkEnable) {
//
//        } else {
//            try {
//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    return;
//                }
//            } catch (Exception e) {
//            }
//            if (isGPSEnable) {
//                location = null;
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50f, this);
//                if (locationManager != null) {
//                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    if (location != null) {
//                        Log.e("latitude", location.getLatitude() + "");
//                        Log.e("longitude", location.getLongitude() + "");
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                        fn_update(location, "GPS");
//                    }
//                }
//            } else if (isNetworkEnable) {
//                location = null;
//
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 50f, this);
//                if (locationManager != null) {
//                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                    if (location != null) {
//
//                        Log.e("latitude", location.getLatitude() + "");
//                        Log.e("longitude", location.getLongitude() + "");
//
//                        latitude = location.getLatitude();
//                        longitude = location.getLongitude();
//                        fn_update(location, "network");
//                    }
//                }
//
//            } else {
//                showSettingsAlert();
//            }
//
//
//        }
//
//    }
//
//    private class TimerTaskToGetLocation extends TimerTask {
//        @Override
//        public void run() {
//
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    fn_getlocation();
//
//
//                }
//            });
//
//        }
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//        return Service.START_STICKY;
//    }
//
//    private void fn_update(Location location, String provider) {
//        String sCheckInStatus = pref.getString("CheckedInStatus", "0");
//        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
//
//        } else {
//            mTimer.cancel();
//            return;
//        }
//        Log.e("locationssssss", "lat : " + latitude + " & lng : " + longitude + " & provider : " + provider);
//        cDate = new Date();
//        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//
//        {
//            Geocoder geocoder = null;
//            List<Address> addresses;
//            long lastGeoTime = MyApp.getSharedPrefLong("GEO");
//            if (lastGeoTime == 0) {
//                MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//            }
//            long differGeo = System.currentTimeMillis() - lastGeoTime;
//            if (differGeo > (10 * 60 * 1000)) {
//                MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                try {
//                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
//                } catch (Exception e) {
//                    geocoder = new Geocoder(ctx, Locale.getDefault());
//                }
//            }
//
//
//            try {
//                sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
//                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                if (addresses.size() > 0) {
//                    sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                    sCity = addresses.get(0).getLocality();
//                    sState = addresses.get(0).getAdminArea();
//                    sCountry = addresses.get(0).getCountryName();
//                    sPostalCode = addresses.get(0).getPostalCode();
//                    sKnownName = addresses.get(0).getFeatureName();
//                    sPremises = addresses.get(0).getPremises();
//                    sSubLocality = addresses.get(0).getSubLocality();
//                    sSubAdminArea = addresses.get(0).getSubAdminArea();
//                } else {
//                    sAddressLine = "NA";
//                    sCity = "NA";
//                    sState = "NA";
//                    sCountry = "NA";
//                    sPostalCode = "NA";
//                    sKnownName = "NA";
//                    sPremises = "NA";
//                    sSubLocality = "NA";
//                    sSubAdminArea = "NA";
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                sAddressLine = "NA";
//                sCity = "NA";
//                sState = "NA";
//                sCountry = "NA";
//                sPostalCode = "NA";
//                sKnownName = "NA";
//                sPremises = "NA";
//                sSubLocality = "NA";
//                sSubAdminArea = "NA";
//            }
//            locationInfo.put("RealTimeUpdate", "true");
//            locationInfo.put("UserId", nh_userid);
//            locationInfo.put("DeviceId", sDeviceId);
//            locationInfo.put("Latitude", String.valueOf(location.getLatitude()));
//            locationInfo.put("Longitude", String.valueOf(location.getLongitude()));
//            locationInfo.put("ActivityDate", currentDateTimeString);
//            locationInfo.put("AutoCaptured", "true");
//            locationInfo.put("AddressLine", sAddressLine);
//            locationInfo.put("Premises", sPremises);
//            locationInfo.put("SubLocality", sSubLocality);
//            locationInfo.put("SubAdminArea", sSubAdminArea);
//            locationInfo.put("PostalCode", sPostalCode);
//            locationInfo.put("City", sCity);
//            locationInfo.put("State", sState);
//            locationInfo.put("Country", sCountry);
//            locationInfo.put("KnownName", sKnownName);
//            locationInfo.put("Provider", "NA");
//            long lastLocTime = MyApp.getSharedPrefLong("LOC");
//            if (lastLocTime == 0) {
//                MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
//            }
//            long differLoc = System.currentTimeMillis() - lastLocTime;
//            if (differLoc < (1 * 60 * 1000)) {
//                return;
//            }
//            MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
//            try {
//                //Date cDate = new Date();
//                //currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss").format(cDate);
//                Log.e("onReceive: USERID", nh_userid);
//                //mDatabase.child("User_Location").child(nh_userid).setValue(user_location);
//                sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
//                        "('" + nh_userid + "','" + latitude + "','" + longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
//                Log.e("Location insertion", "Inserted by ServiceLocation at 490");
//                //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
//                Cursor cquery = sql.rawQuery("select * from User_Location ", null);
//                String sColumnId = null;
//                if (cquery.getCount() > 0) {
//                    cquery.moveToLast();
//                    sColumnId = cquery.getString(0).toString();
//                }
//                cquery.close();
//
//                LocationOperation(locationInfo, getApplicationContext(), sColumnId);
//            } catch (Exception e) {
//                try {
//                    LocationOperation(locationInfo, getApplicationContext(), "");
//                } catch (Exception ee) {
//                    LocationOperation(locationInfo, ctx, "");
//                }
//            }
//        }
//
//        cDate = new Date();
//        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//        editor.putString("CheckedInDuration", currentDateTimeString);
//        editor.commit();
//    }
//
//
//    public void LocationOperation(Map locationInfo, final Context ctx, final String sColumnId) {
//        String sCheckInStatus = pref.getString("CheckedInStatus", "0");
//        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
//
//            try {
//                if (locationInfo.get("UserId").toString().isEmpty() || locationInfo.get("UserId").toString().equals("0")
//                        || locationInfo.get("DeviceId").toString().isEmpty() || locationInfo.get("DeviceId").toString().equals("0")) {
//                    Log.e("LocationOperation: ", "Not executed dut to wrong user id");
//                    return;
//                }
//            } catch (Exception e) {
//                Log.e("LocationOperation: ", "Not executed dut to wrong user id");
//                return;
//            }
//            Log.e("LocationOperation: ", "Method called LocationOperation");
////            if (isLocationCalled) {
////                isLocationCalled = false;
////                return;
////            }
////            isLocationCalled = false;
//            apiInterface = ApiClient.getClient().create(ApiInterface.class);
//
//            sql = ctx.openOrCreateDatabase("MZI.sqlite", ctx.MODE_PRIVATE, null);
//            final ApiResult apiResult = new ApiResult();
//            try {
//                Log.e("LocationOperation: ", locationInfo.toString());
//                String sublocalityString = "";
//                try {
//                    sublocalityString = locationInfo.get("SubLocality").toString();
//                    if (sublocalityString.length() == 0 || sublocalityString.isEmpty()) {
//                        sublocalityString = "NA";
//                    }
//                } catch (Exception eee) {
//                    sublocalityString = "NA";
//                }
//
//                final ApiResult.User_Location user_location;
//
//                Log.e("postcoordinat", "ServiceLocation at 502");
//
//
//                Call<ApiResult.User_Location> call1;
//                if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
//                    user_location = apiResult.new User_Location("true",
//                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
//                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
//                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());
//
//                    call1 = apiInterface.PostCoordinatesShorten(user_location);
//                } else {
//                    user_location = apiResult.new User_Location("true",
//                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
//                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
//                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(),
//                            locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(),
//                            locationInfo.get("City").toString(), locationInfo.get("State").toString(),
//                            locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
//                    call1 = apiInterface.PostCoordinates(user_location);
//                }
//
//                final String finalColumnId = sColumnId;
//                call1.enqueue(new Callback<ApiResult.User_Location>() {
//                    @Override
//                    public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
//                        try {
//                            ApiResult.User_Location iData = response.body();
//                            if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
//
//                                ContentValues newValues = new ContentValues();
//                                newValues.put("SyncStatus", "false");
//                                if (!finalColumnId.isEmpty())
//                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
//                            } else {
//                                ContentValues newValues = new ContentValues();
//                                newValues.put("SyncStatus", "true");
//                                if (!finalColumnId.isEmpty())
//                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
//                            }
//                        } catch (Exception e) {
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ApiResult.User_Location> call, Throwable t) {
//                        call.cancel();
//
//                    }
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            long lastBatteryTime = MyApp.getSharedPrefLong("BAT");
//            if (lastBatteryTime == 0) {
//                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
//            }
//            long differ = System.currentTimeMillis() - lastBatteryTime;
//            if (differ >= (15 * 57 * 1000)) {
//                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
//
//                BatteryManager bm;
//                try {
//                    bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
//                } catch (Exception e) {
//                    bm = (BatteryManager) ctx.getSystemService(BATTERY_SERVICE);
//                }
//                int batLevel = 0;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//                }
//
//                try {
//                    Log.d("battery", batLevel + "%");
//                    Log.d("battery2", getBatteryPercentage(getApplicationContext()) + "%");
//                    if (batLevel > 0) {
//                        sendBatteryCheckinLevel(batLevel);
//                    } else {
//                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
//                    }
//                } catch (Exception e) {
//                    try {
//                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
//                    } catch (Exception eee) {
//                    }
//                }
//            }
//        }
//    }
//
//    private void sendBatteryCheckinLevel(int BatteryLevel) {
//        Date cDate = new Date();
//        try {
//            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//            batteryInfo.put("UserId", nh_userid);
//            batteryInfo.put("DeviceId", sDeviceId);
//            batteryInfo.put("Battery", String.valueOf(BatteryLevel));
//            batteryInfo.put("ActivityDate", currentDateTimeString);
//            batteryInfo.put("AutoCaptured", "true");
//            batteryInfo.put("RealTimeUpdate", "true");
//            sql.execSQL("INSERT INTO User_BatteryLevel(UserId,BatteryLevel,AutoCaptured,ActionDate,SyncStatus)VALUES('" + nh_userid + "','" + BatteryLevel + "','true','" + currentDateTimeString + "','-1')");
//            Cursor cquery = sql.rawQuery("select * from User_BatteryLevel ", null);
//            String sColumnId = null;
//            if (cquery.getCount() > 0) {
//                cquery.moveToLast();
//                sColumnId = cquery.getString(0).toString();
//            }
//            cquery.close();
//            BatteryOperation(batteryInfo, getApplicationContext(), sColumnId);
//            cDate = new Date();
//            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//            editor.putString("CheckedInDuration", currentDateTimeString);
//            editor.commit();
//        } catch (Exception e) {
//        }
//    }
//
//    public void BatteryOperation(Map batteryInfo, final Context ctx, final String sColumnId) {
//        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", getApplicationContext().MODE_PRIVATE, null);
//        apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Log.e("BatteryOperation: ", batteryInfo.toString());
//        final ApiResult apiResult = new ApiResult();
//
//        final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel("true", batteryInfo.get("UserId").toString(), batteryInfo.get("DeviceId").toString(), batteryInfo.get("Battery").toString(), batteryInfo.get("ActivityDate").toString(), batteryInfo.get("AutoCaptured").toString());
//        Call<ApiResult.User_BatteryLevel> call1 = apiInterface.PostBatteryLevel(userBatteryLevel);
//        final String finalColumnId = sColumnId;
//        call1.enqueue(new Callback<ApiResult.User_BatteryLevel>() {
//            @Override
//            public void onResponse(Call<ApiResult.User_BatteryLevel> call, Response<ApiResult.User_BatteryLevel> response) {
//                try {
//                    ApiResult.User_BatteryLevel iData = response.body();
//                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
//
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "false");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
//                    } else {
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "true");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
//                    }
//                } catch (Exception e) {
////                   MyApp.showMassage();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResult.User_BatteryLevel> call, Throwable t) {
//                call.cancel();
//
//            }
//        });
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//    public void showSettingsAlert() {
//        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        MyApp.showMassage(this, "GPS is off");
////        Toast.makeText(this, "GPS is off", Toast.LENGTH_SHORT).show();
//        Intent callGPSSettingIntent = new Intent(
//                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        //ctx.startActivity(callGPSSettingIntent);
//        //Intent inte = new Intent(ctx, MainActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), callGPSSettingIntent, 0);
//        Notification noti = new Notification.Builder(this).setContentTitle("GPS is not Enable!! Enable Now?")
//                .setContentText("Click Setting!!").setSmallIcon(R.drawable.som)
//                .setContentIntent(pIntent)
//                .setSound(soundUri)
//                .addAction(R.drawable.som, "Setting", pIntent).build();
//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        // hide the notification after its selected
//        noti.flags |= Notification.FLAG_AUTO_CANCEL;
//        notificationManager.notify(0, noti);
//    }
//}