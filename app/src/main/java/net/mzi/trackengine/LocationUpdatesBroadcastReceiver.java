package net.mzi.trackengine;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.google.android.gms.location.LocationResult;
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
 * Receiver for handling location updates.
 * <p>
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 * <p>
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 * foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";
    Map<String, String> locationInfo = new HashMap<String, String>();
    ApiInterface apiInterface;
    Cursor cquery;
    String sDeviceId, sDuration;
    String nh_userid;
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    SharedPreferences.Editor editor;
    String currentDateTimeString;
    SQLiteDatabase sql;
    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    Utils.setLocationUpdatesResult(context, locations);
                    Utils.sendNotification(context, Utils.getLocationResultTitle(context, locations));
                    Log.i(TAG, Utils.getLocationUpdatesResult(context));

                    apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    FirebaseApp.initializeApp(context);
                    Firebase.setAndroidContext(context);
                    pref = context.getSharedPreferences("login", 0);
                    editor = pref.edit();
                    sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
                    Date cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    //editor = pref.edit();
                    nh_userid = pref.getString("userid", "0");
                    sDeviceId = pref.getString("DeviceId", "0");
                    sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
                    SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date localDate = null;
                    Date liveDate = cDate;
                    String strUpdatedDate = sDuration;
                    try {
                        long secondsInMilli = 1000;
                        long minutesInMilli = secondsInMilli * 60;
                        localDate = Updatedate.parse(strUpdatedDate);
                        long different = liveDate.getTime() - localDate.getTime();
                        long m = different;
                        long elapsedMinutes = different / minutesInMilli;
                        {
                            cDate = new Date();
                            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                            User_Location user_location = new User_Location();
               /*user_location.ActionDate=currentDateTimeString;
                user_location.AutoCaptured="true";
                user_location.DeviceId=SplashActivity.deviceId;*/
                            user_location.Latitude = locations.get(0).getLatitude();
                            user_location.Longitude = locations.get(0).getLongitude();

                            if (user_location.Latitude == 0.0 || user_location.Latitude == 0) {
                                if (SOMTracker.getSharedPrefString("lat").isEmpty()) {

                                } else {
                                    user_location.Longitude = Double.parseDouble(SOMTracker.getSharedPrefString("lng"));
                                    user_location.Latitude = Double.parseDouble(SOMTracker.getSharedPrefString("lat"));
                                    Geocoder geocoder;
                                    List<Address> addresses;
                                    geocoder = new Geocoder(context, Locale.getDefault());

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
                                                "('" + nh_userid + "','" + locations.get(0).getLatitude()
                                                + "','" + locations.get(0).getLongitude() + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                                        //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                                        Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                                        String sColumnId = null;
                                        if (cquery.getCount() > 0) {
                                            cquery.moveToLast();
                                            sColumnId = cquery.getString(0).toString();
                                        }

                                        LocationOperation(locationInfo, context, sColumnId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(context, Locale.getDefault());

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
                                            "('" + nh_userid + "','" + locations.get(0).getLatitude() + "','"
                                            + locations.get(0).getLongitude() + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                                    //sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActionDate,SyncStatus)VALUES("+nh_userid+",'"+latitude+"','"+longitude+"',0,'"+currentDateTimeString+"','-1')");
                                    Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                                    String sColumnId = null;
                                    if (cquery.getCount() > 0) {
                                        cquery.moveToLast();
                                        sColumnId = cquery.getString(0).toString();
                                    }

                                    LocationOperation(locationInfo, context, sColumnId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            cDate = new Date();
                            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                            editor.putString("CheckedInDuration", currentDateTimeString);
                            editor.commit();
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
        Log.e("LocationOperation: ", "Method called LocationUpdatesBroadCastReceiver");

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

            final ApiResult.User_Location user_location = apiResult.new User_Location("true", locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(), locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(), locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(), locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(), locationInfo.get("City").toString(), locationInfo.get("State").toString(), locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
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
                        e.printStackTrace();
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
