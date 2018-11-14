package net.mzi.trackengine;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
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
 * Created by root on 12/2/18.
 */
@SuppressLint("NewApi")
public class MyService extends JobService {
    Map<String, String> locationInfo = new HashMap<String, String>();
    ApiInterface apiInterface;
    Gps gps;
    Cursor cquery;
    String sDeviceId, sDuration;
    String nh_userid;
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    String TAG = "MyService";
    SharedPreferences.Editor editor;
    String currentDateTimeString;
    private static int LOCATION_INTERVAL = 1 * 60 * 1000;
    private static final float LOCATION_DISTANCE = 100f;// 100 meters
    // private DatabaseReference mDatabase;
    static double latitude, longitude;
    SharedPreferences pref;
    SQLiteDatabase sql;
    private LocationManager mLocationManager = null;
    /**
     * The Job scheduler.
     */
    JobScheduler jobScheduler;

    /**
     * The Tag.
     */
//    String TAG = "MyService";

    /**
     * LocationRequest instance
     */
    /**
     * Location instance
     */
    private Location lastLocation;

    /**
     * this method writes location to text view or shared preferences
     *
     * @param location - location from fused location provider
     */
    @SuppressLint("SetTextI18n")
    private void writeActualLocation(Location location) {
        Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());
        //here in this method you can use web service or any other thing
    }

    /**
     * this method only provokes writeActualLocation().
     */
    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }


    /**
     * Default method of service
     *
     * @param params - JobParameters params
     * @return boolean
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        startJobAgain();

//        createGoogleApi();
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
//        initializeLocationManager();
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
                                Log.e("Location insertion","Inserted by MyService at 283");
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
                            Log.e("Location insertion","Inserted by MyService at 371");
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
        return false;
    }

    /**
     * Create google api instance
     */
//    private void createGoogleApi() {
//        //Log.d(TAG, "createGoogleApi()");
//        if (googleApiClient == null) {
//            googleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }
//
//        //connect google api
//        googleApiClient.connect();
//
//    }

    /**
     * disconnect google api
     *
     * @param params - JobParameters params
     * @return result
     */
    @Override
    public boolean onStopJob(JobParameters params) {
//        googleApiClient.disconnect();
        return false;
    }

    /**
     * starting job again
     */
    private void startJobAgain() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Job Started");
            ComponentName componentName = new ComponentName(getApplicationContext(),
                    MyService.class);
            jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                    .setMinimumLatency(10000) //10 sec interval
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).
                            setPeriodic(30000).setRequiresCharging(false).build();
            jobScheduler.schedule(jobInfo);
        }
    }


    public void LocationOperation(Map locationInfo, final Context ctx, final String sColumnId) {
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
            Log.e("LocationOperation: ", "Method called LocationOperation");
            MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
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
                Log.e("postcoordinat", "MyService at 482");
                Call<ApiResult.User_Location> call1;
                if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
                    user_location = apiResult.new User_Location("true",
                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());

                    call1 = apiInterface.PostCoordinatesShorten(user_location);
                } else {
                    user_location = apiResult.new User_Location("true", locationInfo.get("UserId").toString(),
                            locationInfo.get("DeviceId").toString(), locationInfo.get("Latitude").toString(),
                            locationInfo.get("Longitude").toString(), locationInfo.get("ActivityDate").toString(),
                            locationInfo.get("AutoCaptured").toString(), locationInfo.get("AddressLine").toString(),
                            sublocalityString, locationInfo.get("PostalCode").toString(), locationInfo.get("City").toString(),
                            locationInfo.get("State").toString(), locationInfo.get("Country").toString(),
                            locationInfo.get("KnownName").toString(), "NA");
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
    }

    public void showSettingsAlert() {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MyApp.showMassage(this,"GPS is off");
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

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private boolean isFirstTime = true;

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

            if (isFirstTime && false) {

                {
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
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
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
                    locationInfo.put("Latitude", String.valueOf(location.getLatitude()));
                    locationInfo.put("Longitude", String.valueOf(location.getLongitude()));
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
                        Log.e("Location insertion","Inserted by MyService at 670");
                        //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                        Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                        String sColumnId = null;
                        if (cquery.getCount() > 0) {
                            cquery.moveToLast();
                            sColumnId = cquery.getString(0).toString();
                        }
                        ServiceLocation m = new ServiceLocation();
                        m.LocationOperationOffline(locationInfo, getApplicationContext(), sColumnId,false);
                    } catch (Exception e) {
                        ServiceLocation m = new ServiceLocation();
                        m.LocationOperationOffline(locationInfo, getApplicationContext(), "",false);
                    }
                }
            } else {
                isFirstTime = true;
            }

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
}
