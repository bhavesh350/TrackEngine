package net.mzi.trackengine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import net.mzi.trackengine.model.User_Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mzi.trackengine.MainActivity.getBatteryPercentage;

/**
 * Created by Poonam on 4/19/2017.
 */
public class ServiceLocation extends Service {

    public ServiceLocation(Context c) {
        super();
    }

    public ServiceLocation() {
    }

    Map<String, String> batteryInfo = new HashMap<String, String>();
    Map<String, String> locationInfo = new HashMap<String, String>();
    ApiInterface apiInterface;
    Gps gps;
    Cursor cquery;
    String sDeviceId, sDuration;
    String nh_userid;
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    private static int LOCATION_INTERVAL = 10 * 2 * 1000;
    private static final float LOCATION_DISTANCE = 100f;//100 meters
    private LocationManager mLocationManager = null;
    String TAG = "service_location";
    SharedPreferences.Editor editor;
    String currentDateTimeString;
    // private DatabaseReference mDatabase;
    static double latitude, longitude;
    SharedPreferences pref;
    SQLiteDatabase sql;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e("TAG", "LocationListener " + provider);
            sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
            mLastLocation = new Location(provider);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onLocationChanged(Location location) {
            String sProvider = location.getProvider();
            Log.e(TAG, "onLocationChanged: " + sProvider);
            Log.e(TAG, "onLocationChanged: " + String.valueOf(location.getLatitude()));

            mLastLocation.set(location);
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            MyApp.setSharedPrefString("lat", latitude + "");
            MyApp.setSharedPrefString("lng", longitude + "");

//            if (isFirstTime && false) {
//
//                {
//                    Geocoder geocoder = null;
//                    List<Address> addresses;
//                    long lastLocTime = MyApp.getSharedPrefLong("GEO");
//                    if (lastLocTime == 0) {
//                        MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//
//                    }
//                    long differLoc = System.currentTimeMillis() - lastLocTime;
//                    if (differLoc > (10 * 60 * 1000)) {
//                        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
//                        MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                    }
//
//
//                    try {
//                        sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
//                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                        if (addresses.size() > 0) {
//                            sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                            sCity = addresses.get(0).getLocality();
//                            sState = addresses.get(0).getAdminArea();
//                            sCountry = addresses.get(0).getCountryName();
//                            sPostalCode = addresses.get(0).getPostalCode();
//                            sKnownName = addresses.get(0).getFeatureName();
//                            sPremises = addresses.get(0).getPremises();
//                            sSubLocality = addresses.get(0).getSubLocality();
//                            sSubAdminArea = addresses.get(0).getSubAdminArea();
//                        } else {
//                            sAddressLine = "NA";
//                            sCity = "NA";
//                            sState = "NA";
//                            sCountry = "NA";
//                            sPostalCode = "NA";
//                            sKnownName = "NA";
//                            sPremises = "NA";
//                            sSubLocality = "NA";
//                            sSubAdminArea = "NA";
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        sAddressLine = "NA";
//                        sCity = "NA";
//                        sState = "NA";
//                        sCountry = "NA";
//                        sPostalCode = "NA";
//                        sKnownName = "NA";
//                        sPremises = "NA";
//                        sSubLocality = "NA";
//                        sSubAdminArea = "NA";
//                    }
//                    locationInfo.put("RealTimeUpdate", "true");
//                    locationInfo.put("UserId", nh_userid);
//                    locationInfo.put("DeviceId", sDeviceId);
//                    locationInfo.put("Latitude", String.valueOf(location.getLatitude()));
//                    locationInfo.put("Longitude", String.valueOf(location.getLongitude()));
//                    locationInfo.put("ActivityDate", currentDateTimeString);
//                    locationInfo.put("AutoCaptured", "true");
//                    locationInfo.put("AddressLine", sAddressLine);
//                    locationInfo.put("Premises", sPremises);
//                    locationInfo.put("SubLocality", sSubLocality);
//                    locationInfo.put("SubAdminArea", sSubAdminArea);
//                    locationInfo.put("PostalCode", sPostalCode);
//                    locationInfo.put("City", sCity);
//                    locationInfo.put("State", sState);
//                    locationInfo.put("Country", sCountry);
//                    locationInfo.put("KnownName", sKnownName);
//                    locationInfo.put("Provider", "NA");
//
//                    try {
//                        //Date cDate = new Date();
//                        //currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss").format(cDate);
//                        Log.e("onReceive: USERID", nh_userid);
//                        //mDatabase.child("User_Location").child(nh_userid).setValue(user_location);
//                        sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
//                                "('" + nh_userid + "','" + latitude + "','" + longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
//                        Log.e("Location insertion", "Inserted by ServiceLocation at 180");
//                        //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
//                        Cursor cquery = sql.rawQuery("select * from User_Location ", null);
//                        String sColumnId;
//                        if (cquery.getCount() > 0) {
//                            cquery.moveToLast();
//                            sColumnId = cquery.getString(0).toString();
//                        } else {
//                            sColumnId = "";
//                        }
//
//                        LocationOperationOffline(locationInfo, getApplicationContext(), sColumnId);
//                    } catch (Exception e) {
//                        LocationOperationOffline(locationInfo, getApplicationContext(), "");
//                    }
//                }
//            } else {
//                isFirstTime = true;
//            }

        }

        private boolean isFirstTime = false;

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider + status);
        }

        @Override
        public void onProviderEnabled(String provider) {

            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate() {
        super.onCreate();
        Log.d("LocationService", "Oncreate for locatoin service has been called");
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        isLocationCalled = false;
//        h.postDelayed(check2MinTask, 60 * 1000);

        initializeLocationManager();
//        try {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                    mLocationListeners[1]);
//        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
//        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        /*SchedulingAdapter s= new SchedulingAdapter();
        s.getLocation();*/
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        FirebaseApp.initializeApp(getApplicationContext());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Firebase.setAndroidContext(getApplicationContext());
        pref = getSharedPreferences("login", 0);
        editor = pref.edit();
        try {
            sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        } catch (SQLiteDatabaseLockedException e) {
            flags = START_STICKY;
            return super.onStartCommand(intent, flags, startId);
        } catch (RuntimeException ee) {
            return super.onStartCommand(intent, flags, startId);
        } catch (Exception eee) {
            return super.onStartCommand(intent, flags, startId);
        }

        gps = new Gps(getApplicationContext());
        Date cDate = new Date();
        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
        //editor = pref.edit();
        nh_userid = pref.getString("userid", "0");
        sDeviceId = pref.getString("DeviceId", "0");
        sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
        gps = new Gps(getApplicationContext());
        initializeLocationManager();
//        SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date localDate = null;
//        Date liveDate = cDate;
        String strUpdatedDate = sDuration;
        //SimpleDateFormat liveUpdatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
//            long secondsInMilli = 1000;
//            long minutesInMilli = secondsInMilli * 60;
//            localDate = Updatedate.parse(strUpdatedDate);
//            long different = liveDate.getTime() - localDate.getTime();
//            long m = different;
//            long elapsedMinutes = different / minutesInMilli;
            long lastLocTimee = MyApp.getSharedPrefLong("LOC");
            if (lastLocTimee == 0) {
                MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
            }
            long differLocc = System.currentTimeMillis() - lastLocTimee;

            if (differLocc >= (2 * 60 * 1000)) {
                MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
//                try {
//                    mLocationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                            mLocationListeners[1]);
//                } catch (SecurityException ex) {
//                    Log.i(TAG, "fail to request location update, ignore", ex);
//                    showSettingsAlert();
//                } catch (IllegalArgumentException ex) {
//                    showSettingsAlert();
//                    Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//                }
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            mLocationListeners[0]);
                } catch (SecurityException ex) {
                    showSettingsAlert();
                    Log.e(TAG, "fail to request location update, ignore", ex);
                } catch (IllegalArgumentException ex) {
                    showSettingsAlert();
                    Log.e(TAG, "gps provider does not exist " + ex.getMessage());
                }
                if (gps.canGetLocation()) {
                    cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    User_Location user_location = new User_Location();
               /*user_location.ActionDate=currentDateTimeString;
                user_location.AutoCaptured="true";
                user_location.DeviceId=SplashActivity.deviceId;*/
                    user_location.Latitude = latitude;
                    user_location.Longitude = longitude;

                    if (user_location.Latitude == 0.0 || user_location.Latitude == 0) {
                        if (MyApp.getSharedPrefString("lat").isEmpty()) {

                        } else {
                            user_location.Longitude = Double.parseDouble(MyApp.getSharedPrefString("lng"));
                            user_location.Latitude = Double.parseDouble(MyApp.getSharedPrefString("lat"));
                            Geocoder geocoder = null;
                            List<Address> addresses;
                            long lastLocTime = MyApp.getSharedPrefLong("GEO");
                            if (lastLocTime == 0) {
                                MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());

                            }
                            long differLoc = System.currentTimeMillis() - lastLocTime;
                            if (differLoc > (10 * 60 * 1000)) {
                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                                MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
                            }


                            try {
                                sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
                                addresses = geocoder.getFromLocation(user_location.Latitude, user_location.Longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                if (addresses.size() > 0) {
                                    sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                    sCity = addresses.get(0).getLocality();
                                    sState = addresses.get(0).getAdminArea();
                                    sCountry = addresses.get(0).getCountryName();
                                    sPostalCode = addresses.get(0).getPostalCode();
                                    sKnownName = addresses.get(0).getFeatureName();
                                    sPremises = addresses.get(0).getPremises();
                                    sSubLocality = addresses.get(0).getSubLocality();
                                    sSubAdminArea = addresses.get(0).getSubAdminArea();
                                } else {
                                    sAddressLine = "NA";
                                    sCity = "NA";
                                    sState = "NA";
                                    sCountry = "NA";
                                    sPostalCode = "NA";
                                    sKnownName = "NA";
                                    sPremises = "NA";
                                    sSubLocality = "NA";
                                    sSubAdminArea = "NA";
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                sAddressLine = "NA";
                                sCity = "NA";
                                sState = "NA";
                                sCountry = "NA";
                                sPostalCode = "NA";
                                sKnownName = "NA";
                                sPremises = "NA";
                                sSubLocality = "NA";
                                sSubAdminArea = "NA";
                            }
                            locationInfo.put("RealTimeUpdate", "true");
                            locationInfo.put("UserId", nh_userid);
                            locationInfo.put("DeviceId", sDeviceId);
                            locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
                            locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
                            locationInfo.put("ActivityDate", currentDateTimeString);
                            locationInfo.put("AutoCaptured", "true");
                            locationInfo.put("AddressLine", sAddressLine);
                            locationInfo.put("Premises", sPremises);
                            locationInfo.put("SubLocality", sSubLocality);
                            locationInfo.put("SubAdminArea", sSubAdminArea);
                            locationInfo.put("PostalCode", sPostalCode);
                            locationInfo.put("City", sCity);
                            locationInfo.put("State", sState);
                            locationInfo.put("Country", sCountry);
                            locationInfo.put("KnownName", sKnownName);
                            locationInfo.put("Provider", "NA");

                            try {
                                //Date cDate = new Date();
                                //currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss").format(cDate);
                                Log.e("onReceive: USERID", nh_userid);
                                //mDatabase.child("User_Location").child(nh_userid).setValue(user_location);
                                sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
                                        "('" + nh_userid + "','" + latitude + "','" + longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                                Log.e("Location insertion", "Inserted by ServiceLocation at 400");
                                //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                                Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                                String sColumnId = null;
                                if (cquery.getCount() > 0) {
                                    cquery.moveToLast();
                                    sColumnId = cquery.getString(0).toString();
                                }
                                cquery.close();
                                LocationOperation(locationInfo, getApplicationContext(), sColumnId, false);
                            } catch (Exception e) {
                                LocationOperation(locationInfo, getApplicationContext(), "", false);
                            }
                        }
                    } else {
                        Geocoder geocoder = null;
                        List<Address> addresses;
                        long lastLocTime = MyApp.getSharedPrefLong("GEO");
                        if (lastLocTime == 0) {
                            MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
                        }
                        long differLoc = System.currentTimeMillis() - lastLocTime;
                        if (differLoc > (10 * 60 * 1000)) {
                            MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        }

                        try {
                            sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
                            addresses = geocoder.getFromLocation(user_location.Latitude, user_location.Longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            if (addresses.size() > 0) {
                                sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                sCity = addresses.get(0).getLocality();
                                sState = addresses.get(0).getAdminArea();
                                sCountry = addresses.get(0).getCountryName();
                                sPostalCode = addresses.get(0).getPostalCode();
                                sKnownName = addresses.get(0).getFeatureName();
                                sPremises = addresses.get(0).getPremises();
                                sSubLocality = addresses.get(0).getSubLocality();
                                sSubAdminArea = addresses.get(0).getSubAdminArea();
                            } else {
                                sAddressLine = "NA";
                                sCity = "NA";
                                sState = "NA";
                                sCountry = "NA";
                                sPostalCode = "NA";
                                sKnownName = "NA";
                                sPremises = "NA";
                                sSubLocality = "NA";
                                sSubAdminArea = "NA";
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            sAddressLine = "NA";
                            sCity = "NA";
                            sState = "NA";
                            sCountry = "NA";
                            sPostalCode = "NA";
                            sKnownName = "NA";
                            sPremises = "NA";
                            sSubLocality = "NA";
                            sSubAdminArea = "NA";
                        }
                        locationInfo.put("RealTimeUpdate", "true");
                        locationInfo.put("UserId", nh_userid);
                        locationInfo.put("DeviceId", sDeviceId);
                        locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
                        locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
                        locationInfo.put("ActivityDate", currentDateTimeString);
                        locationInfo.put("AutoCaptured", "true");
                        locationInfo.put("AddressLine", sAddressLine);
                        locationInfo.put("Premises", sPremises);
                        locationInfo.put("SubLocality", sSubLocality);
                        locationInfo.put("SubAdminArea", sSubAdminArea);
                        locationInfo.put("PostalCode", sPostalCode);
                        locationInfo.put("City", sCity);
                        locationInfo.put("State", sState);
                        locationInfo.put("Country", sCountry);
                        locationInfo.put("KnownName", sKnownName);
                        locationInfo.put("Provider", "NA");

                        try {
                            //Date cDate = new Date();
                            //currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy hh:mm:ss").format(cDate);
                            Log.e("onReceive: USERID", nh_userid);
                            //mDatabase.child("User_Location").child(nh_userid).setValue(user_location);
                            sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
                                    "('" + nh_userid + "','" + latitude + "','" + longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                            Log.e("Location insertion", "Inserted by ServiceLocation at 490");
                            //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                            Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                            String sColumnId = null;
                            if (cquery.getCount() > 0) {
                                cquery.moveToLast();
                                sColumnId = cquery.getString(0).toString();
                            }
                            cquery.close();
                            LocationOperation(locationInfo, getApplicationContext(), sColumnId, false);
                        } catch (Exception e) {
                            LocationOperation(locationInfo, getApplicationContext(), "", false);
                        }
                    }
                } else {
                    showSettingsAlert();
                }
                cDate = new Date();
                currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                editor.putString("CheckedInDuration", currentDateTimeString);
                editor.commit();
            }

           /* if (localDate.getTime() > liveDate.getTime()) {

                t.UpdatedDate = strUpdatedDate;
                t.StatusId = cqueryTempForLatestStatusId.getString(0).toString();
            } else {
                t.UpdatedDate = resData.IssueDetail[i].UpdatedOn;
                t.StatusId = resData.IssueDetail[i].StatusId;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
//        return Service.START_STICKY;
    }
//
//    private Handler h = new Handler();
//    private boolean isLocationCalled = false;
//    private Runnable check2MinTask = new Runnable() {
//        @Override
//        public void run() {
//            if (isLocationCalled) {
//                isLocationCalled = false;
//            }
//            h.postDelayed(check2MinTask, 30 * 1000);
//        }
//    };

    @Override
    public void onDestroy() {
//        super.onDestroy();
//        h.removeCallbacks(check2MinTask);
//        Intent broadcastIntent = new Intent("net.mzi.trackengine.BootReceiver");
//        sendBroadcast(broadcastIntent);
        Log.e("TAG", "locationServiceOnDestroyCalled");

        try {
            startService(new Intent(getApplicationContext(), ServiceLocation.class));
        } catch (Exception E) {
        }

//        Intent myIntent = new Intent(getApplicationContext(), ServiceLocation.class);
//        startService(myIntent);
    }


    public void LocationOperation(Map locationInfo, final Context ctx, final String sColumnId, boolean isOffline) {

        Map<String, String> mMobileDataInfo = new HashMap<>();
        if (!isOffline) {
            InternetConnector icDataSyncing = new InternetConnector();
            if (!nh_userid.equals("0"))
                if (MyApp.isConnectingToInternet(ctx)) {
                    try {
                        icDataSyncing.offlineSyncing(ctx, 1);

                        mMobileDataInfo.put("UserId", nh_userid);
                        mMobileDataInfo.put("DeviceId", sDeviceId);
                        mMobileDataInfo.put("Enabled", "true");
                        mMobileDataInfo.put("ActionDate", currentDateTimeString);
                        mMobileDataInfo.put("RealTimeUpdate", "true");

                    } catch (Exception e) {
                    }
                } else {
                    mMobileDataInfo.put("UserId", nh_userid);
                    mMobileDataInfo.put("DeviceId", sDeviceId);
                    mMobileDataInfo.put("Enabled", "false");
                    mMobileDataInfo.put("ActionDate", currentDateTimeString);
                    mMobileDataInfo.put("RealTimeUpdate", "true");

                }

        }


        String sCheckInStatus = pref.getString("CheckedInStatus", "0");
        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
            long lastLocTime = MyApp.getSharedPrefLong("LOC");
            if (lastLocTime == 0) {
                MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
            }
            long differLoc = System.currentTimeMillis() - lastLocTime;
            if (differLoc < (2 * 58 * 1000)) {
                return;
            }


            MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
            try {
                if (locationInfo.get("UserId").toString().isEmpty() || locationInfo.get("UserId").toString().equals("0")
                        || locationInfo.get("DeviceId").toString().isEmpty() || locationInfo.get("DeviceId").toString().equals("0")) {
                    Log.e("LocationOperation: ", "Not executed dut to wrong user id");
                    return;
                }
            } catch (Exception e) {
                Log.e("LocationOperation: ", "Not executed dut to wrong user id");
                return;
            }
            Log.e("LocationOperation: ", "Method called LocationOperation");
//            if (isLocationCalled) {
//                isLocationCalled = false;
//                return;
//            }
//            isLocationCalled = false;
            apiInterface = ApiClient.getClient().create(ApiInterface.class);

            try {
                sql = ctx.openOrCreateDatabase("MZI.sqlite", ctx.MODE_PRIVATE, null);
            } catch (Exception e) {
            }
            final ApiResult apiResult = new ApiResult();
            try {
                Log.e("LocationOperation: ", locationInfo.toString());
                String sublocalityString = "";
                try {
                    sublocalityString = locationInfo.get("SubLocality").toString();
                    if (sublocalityString.length() == 0 || sublocalityString.isEmpty()) {
                        sublocalityString = "NA";
                    }
                } catch (Exception eee) {
                    sublocalityString = "NA";
                }

                final ApiResult.User_Location user_location;

                Log.e("postcoordinat", "ServiceLocation at 502");


                Call<ApiResult.User_Location> call1;
                if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
                    user_location = apiResult.new User_Location("true",
                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());

                    call1 = apiInterface.PostCoordinatesShorten(user_location);
                } else {
                    user_location = apiResult.new User_Location("true",
                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(),
                            locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(),
                            locationInfo.get("City").toString(), locationInfo.get("State").toString(),
                            locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
                    call1 = apiInterface.PostCoordinates(user_location);
                }

                final String finalColumnId = sColumnId;
                call1.enqueue(new Callback<ApiResult.User_Location>() {
                    @Override
                    public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
                        try {
                            ApiResult.User_Location iData = response.body();
                            if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "false");
                                if (!finalColumnId.isEmpty())
                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
                            } else {
                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "true");
                                if (!finalColumnId.isEmpty())
                                    sql.delete("User_Location", "Id" + "=" + finalColumnId, null);
//                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);


                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult.User_Location> call, Throwable t) {
                        call.cancel();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            long lastBatteryTime = MyApp.getSharedPrefLong("BAT");
            if (lastBatteryTime == 0) {
                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
            }
            long differ = System.currentTimeMillis() - lastBatteryTime;
            if (differ >= (15 * 57 * 1000)) {
                if (MyApp.isConnectingToInternet(ctx)) {
                    PushMobileData(mMobileDataInfo);
                } else {
                    try {
                        sql.execSQL("INSERT INTO User_MobileData(UserId,Enabled,ActionDate,SyncStatus)VALUES" +
                                "('" + mMobileDataInfo.get("UserId") + "','" + mMobileDataInfo.get("Enabled") + "','" + mMobileDataInfo.get("ActionDate") + "','-1')");
                    } catch (Exception e) {
                    }
                }
                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());

                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                int batLevel = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                }

                try {
                    Log.d("battery", batLevel + "%");
                    Log.d("battery2", getBatteryPercentage(getApplicationContext()) + "%");
                    if (batLevel > 0) {
                        sendBatteryCheckinLevel(batLevel);
                    } else {
                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
                    }
                } catch (Exception e) {
                    try {
                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
                    } catch (Exception eee) {
                    }
                }
            }
        }
    }

    public void PushMobileData(Map mMobileDataInfo) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Log.e("PushMobileData: ", mMobileDataInfo.toString());
        final ApiResult apiResult = new ApiResult();
        final ApiResult.User_MobileData user_MobileData = apiResult.new User_MobileData("false", mMobileDataInfo.get("UserId").toString(), mMobileDataInfo.get("DeviceId").toString(), mMobileDataInfo.get("Enabled").toString(), mMobileDataInfo.get("ActionDate").toString());
        Call<ApiResult.User_MobileData> call1 = apiInterface.PostMobileData(user_MobileData);
        call1.enqueue(new Callback<ApiResult.User_MobileData>() {
            @Override
            public void onResponse(Call<ApiResult.User_MobileData> call, Response<ApiResult.User_MobileData> response) {
                try {
                    ApiResult.User_MobileData iData = response.body();
                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                    } else {
//                        MyApp.showMassage(getApplication(), "Location sent successfully!!!");
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(Call<ApiResult.User_MobileData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void LocationOperationOffline(Map locationInfo, final Context ctx, final String sColumnId, final boolean showToast) {

        pref = ctx.getSharedPreferences("login", 0);
        editor = pref.edit();
        String sCheckInStatus = pref.getString("CheckedInStatus", "0");
        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
            try {
                if (locationInfo.get("UserId").toString().isEmpty() || locationInfo.get("UserId").toString().equals("0")
                        || locationInfo.get("DeviceId").toString().isEmpty() || locationInfo.get("DeviceId").toString().equals("0")) {
                    Log.e("LocationOperation: ", "Not executed dut to wrong user id");
                    return;
                }
            } catch (Exception e) {
                Log.e("LocationOperation: ", "Not executed dut to wrong user id");
                return;
            }
            Log.e("LocationOperation: ", "Method called LocationOperation");

            apiInterface = ApiClient.getClient().create(ApiInterface.class);

            sql = ctx.openOrCreateDatabase("MZI.sqlite", ctx.MODE_PRIVATE, null);
            final ApiResult apiResult = new ApiResult();
            try {
                Log.e("LocationOperation: ", locationInfo.toString());
                String sublocalityString = "";
                try {
                    sublocalityString = locationInfo.get("SubLocality").toString();
                    if (sublocalityString.length() == 0 || sublocalityString.isEmpty()) {
                        sublocalityString = "NA";
                    }
                } catch (Exception eee) {
                    sublocalityString = "NA";
                }

                final ApiResult.User_Location user_location;
                Log.e("postcoordinat", "ServiceLocation at 602");
                Call<ApiResult.User_Location> call1;
                if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
                    user_location = apiResult.new User_Location("true",
                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());

                    call1 = apiInterface.PostCoordinatesShorten(user_location);
                } else {
                    user_location = apiResult.new User_Location("true",
                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(),
                            locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(),
                            locationInfo.get("City").toString(), locationInfo.get("State").toString(),
                            locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
                    call1 = apiInterface.PostCoordinates(user_location);
                }

                final String finalColumnId = sColumnId;
                call1.enqueue(new Callback<ApiResult.User_Location>() {
                    @Override
                    public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
                        try {
                            ApiResult.User_Location iData = response.body();
                            if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "true");
                                if (!finalColumnId.isEmpty())
                                    sql.delete("User_Location", "Id" + "=" + finalColumnId, null);
//                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
                            } else {
                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "true");
                                if (!finalColumnId.isEmpty())
                                    sql.delete("User_Location", "Id" + "=" + finalColumnId, null);
//                                    sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
                                int locationsCount = sql.rawQuery("select * from User_Location", null).getCount();
                                ((MainActivity) ctx).txt_locations_count.setText(locationsCount + "");

                            }
                            if (showToast)
                                MyApp.showMassage(getApplicationContext(), "Location sent successfully!!!");
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult.User_Location> call, Throwable t) {
                        call.cancel();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            long lastBatteryTime = MyApp.getSharedPrefLong("BAT");
            if (lastBatteryTime == 0) {
                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
            }
            long differ = System.currentTimeMillis() - lastBatteryTime;
            if (differ >= (15 * 60 * 1000)) {
                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
                try {
                    BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                    int batLevel = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    }

                    Log.d("battery", batLevel + "%");
                    Log.d("battery2", getBatteryPercentage(getApplicationContext()) + "%");
                    if (batLevel > 0) {
                        sendBatteryCheckinLevel(batLevel);
                    } else {
                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
                    }
                } catch (Exception e) {
                    try {
                        sendBatteryCheckinLevel(getBatteryPercentage(getApplicationContext()));
                    } catch (Exception eee) {
                    }
                }
            }
        }
    }

    private void sendBatteryCheckinLevel(int BatteryLevel) {
        Date cDate = new Date();
        try {
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
//            sql.execSQL("INSERT INTO User_BatteryLevel(UserId,BatteryLevel,AutoCaptured,ActionDate,SyncStatus)VALUES('" + nh_userid + "','" + BatteryLevel + "','true','" + currentDateTimeString + "','-1')");
//            Cursor cquery = sql.rawQuery("select * from User_BatteryLevel ", null);
//            String sColumnId = null;
//            if (cquery.getCount() > 0) {
//                cquery.moveToLast();
//                sColumnId = cquery.getString(0).toString();
//            }
//            cquery.close();
            BatteryOperation(batteryInfo, getApplicationContext());
            cDate = new Date();
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            editor.putString("CheckedInDuration", currentDateTimeString);
            editor.commit();
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showSettingsAlert() {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MyApp.showMassage(this, "GPS is off");
//        Toast.makeText(this, "GPS is off", Toast.LENGTH_SHORT).show();
        Intent callGPSSettingIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //ctx.startActivity(callGPSSettingIntent);
        //Intent inte = new Intent(ctx, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), callGPSSettingIntent, 0);
        Notification noti = new Notification.Builder(this).setContentTitle("GPS is not Enable!! Enable Now?")
                .setContentText("Click Setting!!").setSmallIcon(R.drawable.som)
                .setContentIntent(pIntent)
                .setSound(soundUri)
                .addAction(R.drawable.som, "Setting", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);
    }

    public void BatteryOperation(final Map<String, String> batteryInfo, final Context ctx) {
        final Map<String, Map<String, String>> batMap = MyApp.getApplication().readBatteryHistory();
        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", getApplicationContext().MODE_PRIVATE, null);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Log.e("BatteryOperation: ", batteryInfo.toString());
        final ApiResult apiResult = new ApiResult();

        final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel("false", batteryInfo.get("UserId").toString(), batteryInfo.get("DeviceId").toString(), batteryInfo.get("Battery").toString(), batteryInfo.get("ActivityDate").toString(), batteryInfo.get("AutoCaptured").toString());
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
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "true");
//                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
                    }
                } catch (Exception e) {
//                   MyApp.showMassage();
                }
            }

            @Override
            public void onFailure(Call<ApiResult.User_BatteryLevel> call, Throwable t) {
                call.cancel();

            }
        });

        try {
            startService(new Intent(getApplicationContext(), ServiceLocation.class));
        } catch (Exception e) {
        }
    }
}
