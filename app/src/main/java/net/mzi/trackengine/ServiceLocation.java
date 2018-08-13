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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
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

/**
 * Created by Poonam on 4/19/2017.
 */
public class ServiceLocation extends Service {

    public ServiceLocation(Context c) {
        super();
    }

    public ServiceLocation() {
    }

    Map<String, String> locationInfo = new HashMap<String, String>();
    ApiInterface apiInterface;
    Gps gps;
    Cursor cquery;
    String sDeviceId, sDuration;
    String nh_userid;
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    private static int LOCATION_INTERVAL = 10 * 2 * 1000;
    private static final float LOCATION_DISTANCE = 0f;
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
            SOMTracker.setSharedPrefString("lat", latitude + "");
            SOMTracker.setSharedPrefString("lng", longitude + "");
        }

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
        isLocationCalled = false;
        h.postDelayed(check2MinTask, 60 * 1000);

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
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
//        h.postDelayed(check2MinTask, 2 * 60 * 1000);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        Firebase.setAndroidContext(getApplicationContext());
        pref = getSharedPreferences("login", 0);
        editor = pref.edit();
        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        gps = new Gps(getApplicationContext());
        Date cDate = new Date();
        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
        //editor = pref.edit();
        nh_userid = pref.getString("userid", "0");
        sDeviceId = pref.getString("DeviceId", "0");
        sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
        gps = new Gps(getApplicationContext());
        initializeLocationManager();
        SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date localDate = null;
        Date liveDate = cDate;
        String strUpdatedDate = sDuration;
        //SimpleDateFormat liveUpdatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            localDate = Updatedate.parse(strUpdatedDate);
            long different = liveDate.getTime() - localDate.getTime();
            long m = different;
            long elapsedMinutes = different / minutesInMilli;
            if (elapsedMinutes >= 2) {
                try {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                            mLocationListeners[1]);
                } catch (SecurityException ex) {
                    Log.i(TAG, "fail to request location update, ignore", ex);
                    showSettingsAlert();
                } catch (IllegalArgumentException ex) {
                    showSettingsAlert();
                    Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                }
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
                        if (SOMTracker.getSharedPrefString("lat").isEmpty()) {

                        } else {
                            user_location.Longitude = Double.parseDouble(SOMTracker.getSharedPrefString("lng"));
                            user_location.Latitude = Double.parseDouble(SOMTracker.getSharedPrefString("lat"));
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

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
                                //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                                Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                                String sColumnId = null;
                                if (cquery.getCount() > 0) {
                                    cquery.moveToLast();
                                    sColumnId = cquery.getString(0).toString();
                                }

                                LocationOperation(locationInfo, getApplicationContext(), sColumnId);
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

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
                            //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                            Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                            String sColumnId = null;
                            if (cquery.getCount() > 0) {
                                cquery.moveToLast();
                                sColumnId = cquery.getString(0).toString();
                            }

                            LocationOperation(locationInfo, getApplicationContext(), sColumnId);
                        } catch (Exception e) {
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
//        return Service.START_STICKY;
    }

    private Handler h = new Handler();
    private boolean isLocationCalled = false;
    private Runnable check2MinTask = new Runnable() {
        @Override
        public void run() {
            if (isLocationCalled) {
                isLocationCalled = false;
            }
            h.postDelayed(check2MinTask, 30 * 1000);
        }
    };

    @Override
    public void onDestroy() {
//        super.onDestroy();
        h.removeCallbacks(check2MinTask);
//        Intent broadcastIntent = new Intent("net.mzi.trackengine.BootReceiver");
//        sendBroadcast(broadcastIntent);
        Log.e("TAG", "locationServiceOnDestroyCalled");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplicationContext(), ServiceLocation.class));
        } else {
            startService(new Intent(getApplicationContext(), ServiceLocation.class));
        }
//        Intent myIntent = new Intent(getApplicationContext(), ServiceLocation.class);
//        startService(myIntent);
    }


    public void LocationOperation(Map locationInfo, final Context ctx, final String sColumnId) {
//        if (!SOMTracker.getStatus("isCheckin")) {
//            return;
//        }
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
        if (isLocationCalled) {
            isLocationCalled = false;
            return;
        }
        isLocationCalled = false;
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

            final ApiResult.User_Location user_location = apiResult.new User_Location("true",
                    locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(), locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(), locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(), locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(), locationInfo.get("City").toString(), locationInfo.get("State").toString(), locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
            Call<ApiResult.User_Location> call1 = apiInterface.PostCoordinates(user_location);
            final String finalColumnId = sColumnId;
            call1.enqueue(new Callback<ApiResult.User_Location>() {
                @Override
                public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
                    try {
                        ApiResult.User_Location iData = response.body();
                        if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                            ContentValues newValues = new ContentValues();
                            newValues.put("SyncStatus", "false");
                            sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
                        } else {
                            ContentValues newValues = new ContentValues();
                            newValues.put("SyncStatus", "true");
                            sql.update("User_Location", newValues, "Id=" + finalColumnId, null);
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
        Toast.makeText(this, "GPS is off", Toast.LENGTH_SHORT).show();
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

}
