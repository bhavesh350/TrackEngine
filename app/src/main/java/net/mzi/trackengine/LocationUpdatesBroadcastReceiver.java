//package net.mzi.trackengine;
//
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.BatteryManager;
//import android.util.Log;
//
//import com.firebase.client.Firebase;
//import com.google.android.gms.location.LocationResult;
//import com.google.firebase.FirebaseApp;
//
//import net.mzi.trackengine.model.User_Location;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import static android.content.Context.BATTERY_SERVICE;
//import static net.mzi.trackengine.MainActivity.getBatteryPercentage;
//
///**
// * Receiver for handling location updates.
// * <p>
// * For apps targeting API level O
// * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
// * requesting location updates. Due to limits on background services,
// * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
// * <p>
// * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
// * less frequently than the interval specified in the
// * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
// * foreground.
// */
//public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
//    private static final String TAG = "LUBroadcastReceiver";
//
//    static final String ACTION_PROCESS_UPDATES =
//            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
//                    ".PROCESS_UPDATES";
//    Map<String, String> locationInfo = new HashMap<String, String>();
//    ApiInterface apiInterface;
//    Cursor cquery;
//    String sDeviceId, sDuration;
//    String nh_userid;
//    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
//    SharedPreferences.Editor editor;
//    String currentDateTimeString;
//    SQLiteDatabase sql;
//    SharedPreferences pref;
//    Map<String, String> batteryInfo = new HashMap<String, String>();
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_PROCESS_UPDATES.equals(action)) {
//                LocationResult result = LocationResult.extractResult(intent);
//                if (result != null) {
//                    List<Location> locations = result.getLocations();
//                    Utils.setLocationUpdatesResult(context, locations);
//                    Utils.sendNotification(context, Utils.getLocationResultTitle(context, locations));
//                    Log.i(TAG, Utils.getLocationUpdatesResult(context));
//
//                    apiInterface = ApiClient.getClient().create(ApiInterface.class);
//                    FirebaseApp.initializeApp(context);
//                    Firebase.setAndroidContext(context);
//                    pref = context.getSharedPreferences("login", 0);
//                    editor = pref.edit();
//                    sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
//                    Date cDate = new Date();
//                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//                    //editor = pref.edit();
//                    nh_userid = pref.getString("userid", "0");
//                    sDeviceId = pref.getString("DeviceId", "0");
//                    sDuration = pref.getString("CheckedInDuration", currentDateTimeString);
//                    SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    Date localDate = null;
//                    Date liveDate = cDate;
//                    String strUpdatedDate = sDuration;
//                    try {
//                        long secondsInMilli = 1000;
//                        long minutesInMilli = secondsInMilli * 60;
//                        localDate = Updatedate.parse(strUpdatedDate);
//                        long different = liveDate.getTime() - localDate.getTime();
//                        long m = different;
//                        long elapsedMinutes = different / minutesInMilli;
//                        long lastLocTimee = MyApp.getSharedPrefLong("LOC");
//                        if (lastLocTimee == 0) {
//                            MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
//                        }
//                        long differLocc = System.currentTimeMillis() - lastLocTimee;
//                        if (differLocc < (2 * 58 * 1000)) {
//                            return;
//                        }
//                        {
//                            cDate = new Date();
//                            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//                            User_Location user_location = new User_Location();
//               /*user_location.ActionDate=currentDateTimeString;
//                user_location.AutoCaptured="true";
//                user_location.DeviceId=SplashActivity.deviceId;*/
//                            user_location.Latitude = locations.get(0).getLatitude();
//                            user_location.Longitude = locations.get(0).getLongitude();
//
//                            if (user_location.Latitude == 0.0 || user_location.Latitude == 0) {
//                                if (MyApp.getSharedPrefString("lat").isEmpty()) {
//
//                                } else {
//                                    user_location.Longitude = Double.parseDouble(MyApp.getSharedPrefString("lng"));
//                                    user_location.Latitude = Double.parseDouble(MyApp.getSharedPrefString("lat"));
//                                    Geocoder geocoder = null;
//                                    List<Address> addresses;
//                                    long lastLocTime = MyApp.getSharedPrefLong("GEO");
//                                    if (lastLocTime == 0) {
//                                        MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                                    }
//                                    long differLoc = System.currentTimeMillis() - lastLocTime;
//                                    if (differLoc > (10 * 60 * 1000)) {
//                                        geocoder = new Geocoder(context, Locale.getDefault());
//                                        MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                                    }
//
//                                    try {
//                                        sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
//                                        addresses = geocoder.getFromLocation(user_location.Latitude, user_location.Longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                                        if (addresses.size() > 0) {
//                                            sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                                            sCity = addresses.get(0).getLocality();
//                                            sState = addresses.get(0).getAdminArea();
//                                            sCountry = addresses.get(0).getCountryName();
//                                            sPostalCode = addresses.get(0).getPostalCode();
//                                            sKnownName = addresses.get(0).getFeatureName();
//                                            sPremises = addresses.get(0).getPremises();
//                                            sSubLocality = addresses.get(0).getSubLocality();
//                                            sSubAdminArea = addresses.get(0).getSubAdminArea();
//                                        } else {
//                                            sAddressLine = "NA";
//                                            sCity = "NA";
//                                            sState = "NA";
//                                            sCountry = "NA";
//                                            sPostalCode = "NA";
//                                            sKnownName = "NA";
//                                            sPremises = "NA";
//                                            sSubLocality = "NA";
//                                            sSubAdminArea = "NA";
//                                        }
//
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                        sAddressLine = "NA";
//                                        sCity = "NA";
//                                        sState = "NA";
//                                        sCountry = "NA";
//                                        sPostalCode = "NA";
//                                        sKnownName = "NA";
//                                        sPremises = "NA";
//                                        sSubLocality = "NA";
//                                        sSubAdminArea = "NA";
//                                    }
//                                    locationInfo.put("RealTimeUpdate", "true");
//                                    locationInfo.put("UserId", nh_userid);
//                                    locationInfo.put("DeviceId", sDeviceId);
//                                    locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
//                                    locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
//                                    locationInfo.put("ActivityDate", currentDateTimeString);
//                                    locationInfo.put("AutoCaptured", "true");
//                                    locationInfo.put("AddressLine", sAddressLine);
//                                    locationInfo.put("Premises", sPremises);
//                                    locationInfo.put("SubLocality", sSubLocality);
//                                    locationInfo.put("SubAdminArea", sSubAdminArea);
//                                    locationInfo.put("PostalCode", sPostalCode);
//                                    locationInfo.put("City", sCity);
//                                    locationInfo.put("State", sState);
//                                    locationInfo.put("Country", sCountry);
//                                    locationInfo.put("KnownName", sKnownName);
//                                    locationInfo.put("Provider", "NA");
//
//                                    try {
//                                        Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//                                        locMap.put(currentDateTimeString, locationInfo);
//                                        MyApp.getApplication().writeLocationData(locMap);
//
//                                        LocationOperation(locationInfo, context, currentDateTimeString);
//                                    } catch (Exception e) {
//                                        LocationOperation(locationInfo, context, "");
//                                        e.printStackTrace();
//                                    }
//                                }
//                            } else {
//                                Geocoder geocoder = null;
//                                List<Address> addresses;
//                                long lastLocTime = MyApp.getSharedPrefLong("GEO");
//                                if (lastLocTime == 0) {
//                                    MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                                }
//                                long differLoc = System.currentTimeMillis() - lastLocTime;
//                                if (differLoc > (10 * 60 * 1000)) {
//                                    geocoder = new Geocoder(context, Locale.getDefault());
//                                    MyApp.setSharedPrefLong("GEO", System.currentTimeMillis());
//                                }
//
//
//                                try {
//                                    sAddressLine = sCity = sState = sCountry = sPostalCode = sKnownName = sPremises = sSubLocality = sSubAdminArea = "NA";
//                                    addresses = geocoder.getFromLocation(user_location.Latitude, user_location.Longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//                                    if (addresses.size() > 0) {
//                                        sAddressLine = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                                        sCity = addresses.get(0).getLocality();
//                                        sState = addresses.get(0).getAdminArea();
//                                        sCountry = addresses.get(0).getCountryName();
//                                        sPostalCode = addresses.get(0).getPostalCode();
//                                        sKnownName = addresses.get(0).getFeatureName();
//                                        sPremises = addresses.get(0).getPremises();
//                                        sSubLocality = addresses.get(0).getSubLocality();
//                                        sSubAdminArea = addresses.get(0).getSubAdminArea();
//                                    } else {
//                                        sAddressLine = "NA";
//                                        sCity = "NA";
//                                        sState = "NA";
//                                        sCountry = "NA";
//                                        sPostalCode = "NA";
//                                        sKnownName = "NA";
//                                        sPremises = "NA";
//                                        sSubLocality = "NA";
//                                        sSubAdminArea = "NA";
//                                    }
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    sAddressLine = "NA";
//                                    sCity = "NA";
//                                    sState = "NA";
//                                    sCountry = "NA";
//                                    sPostalCode = "NA";
//                                    sKnownName = "NA";
//                                    sPremises = "NA";
//                                    sSubLocality = "NA";
//                                    sSubAdminArea = "NA";
//                                }
//                                locationInfo.put("RealTimeUpdate", "true");
//                                locationInfo.put("UserId", nh_userid);
//                                locationInfo.put("DeviceId", sDeviceId);
//                                locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
//                                locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
//                                locationInfo.put("ActivityDate", currentDateTimeString);
//                                locationInfo.put("AutoCaptured", "true");
//                                locationInfo.put("AddressLine", sAddressLine);
//                                locationInfo.put("Premises", sPremises);
//                                locationInfo.put("SubLocality", sSubLocality);
//                                locationInfo.put("SubAdminArea", sSubAdminArea);
//                                locationInfo.put("PostalCode", sPostalCode);
//                                locationInfo.put("City", sCity);
//                                locationInfo.put("State", sState);
//                                locationInfo.put("Country", sCountry);
//                                locationInfo.put("KnownName", sKnownName);
//                                locationInfo.put("Provider", "NA");
//
//                                try {
//                                    Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//                                    locMap.put(currentDateTimeString, locationInfo);
//                                    MyApp.getApplication().writeLocationData(locMap);
//
//                                    LocationOperation(locationInfo, context, currentDateTimeString);
//                                } catch (Exception e) {
//                                    LocationOperation(locationInfo, context, "");
//                                    e.printStackTrace();
//                                }
//                            }
//                            cDate = new Date();
//                            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//                            editor.putString("CheckedInDuration", currentDateTimeString);
//                            editor.commit();
//                        }
//
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    public void LocationOperation(Map locationInfo, final Context ctx, final String key) {
//        boolean sCheckInStatus = MyApp.getStatus("CheckedInStatus");
//        if (sCheckInStatus) {
//
//
//            MyApp.setSharedPrefLong("LOC", System.currentTimeMillis());
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
//            Log.e("LocationOperation: ", "Method called LocationUpdatesBroadCastReceiver");
//            if (locationInfo.get("Latitude").toString().length() <= 3) {
//                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//                locMap.remove(key);
//                MyApp.getApplication().writeLocationData(locMap);
//                return;
//            }
//
//            apiInterface = ApiClient.getClient().create(ApiInterface.class);
//
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
//                Log.e("postcoordinat", "LocationUpdateBroadcastReceiver at 364");
//                Call<ApiResult.User_Location> call1;
//                if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
//                    user_location = apiResult.new User_Location("true",
//                            locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
//                            locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
//                            locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());
//
//                    call1 = apiInterface.PostCoordinatesShorten(user_location);
//                } else {
//                    user_location = apiResult.new User_Location("true", locationInfo.get("UserId").toString(),
//                            locationInfo.get("DeviceId").toString(), locationInfo.get("Latitude").toString(),
//                            locationInfo.get("Longitude").toString(), locationInfo.get("ActivityDate").toString(),
//                            locationInfo.get("AutoCaptured").toString(), locationInfo.get("AddressLine").toString(),
//                            sublocalityString, locationInfo.get("PostalCode").toString(), locationInfo.get("City").toString(),
//                            locationInfo.get("State").toString(), locationInfo.get("Country").toString(),
//                            locationInfo.get("KnownName").toString(), "NA");
//                    call1 = apiInterface.PostCoordinates(user_location);
//                }
//                call1.enqueue(new Callback<ApiResult.User_Location>() {
//                    @Override
//                    public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
//                        try {
//                            ApiResult.User_Location iData = response.body();
//                            if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
//                                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//                                locMap.remove(key);
//                                MyApp.getApplication().writeLocationData(locMap);
//                            } else {
//                                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//                                locMap.remove(key);
//                                MyApp.getApplication().writeLocationData(locMap);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
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
//            if (differ >= (15 * 58 * 1000)) {
//                MyApp.setSharedPrefLong("BAT", System.currentTimeMillis());
//
//                BatteryManager bm = (BatteryManager) ctx.getSystemService(BATTERY_SERVICE);
//                int batLevel = 0;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//                }
//
//                try {
//                    Log.d("battery", batLevel + "%");
//                    Log.d("battery2", getBatteryPercentage(ctx) + "%");
//                    if (batLevel > 0) {
//                        sendBatteryCheckinLevel(batLevel, ctx);
//                    } else {
//                        sendBatteryCheckinLevel(getBatteryPercentage(ctx), ctx);
//                    }
//                } catch (Exception e) {
//                    try {
//                        sendBatteryCheckinLevel(getBatteryPercentage(ctx), ctx);
//                    } catch (Exception eee) {
//                    }
//                }
//            }
//        }else{
//            Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
//            locMap.remove(key);
//            MyApp.getApplication().writeLocationData(locMap);
//        }
//    }
//
//    private void sendBatteryCheckinLevel(int BatteryLevel, Context c) {
//        Date cDate = new Date();
//        try {
//            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//            batteryInfo.put("UserId", nh_userid);
//            batteryInfo.put("DeviceId", sDeviceId);
//            batteryInfo.put("Battery", String.valueOf(BatteryLevel));
//            batteryInfo.put("ActivityDate", currentDateTimeString);
//            batteryInfo.put("AutoCaptured", "true");
//            batteryInfo.put("RealTimeUpdate", "true");
//            batteryInfo.put("syncStatus", "false");
//            Map<String, Map<String, String>> bMap = MyApp.getApplication().readBatteryHistory();
//            bMap.put(currentDateTimeString, batteryInfo);
//            MyApp.getApplication().writeBatteryHistory(bMap);
////            sql.execSQL("INSERT INTO User_BatteryLevel(UserId,BatteryLevel,AutoCaptured,ActionDate,SyncStatus)VALUES('" + nh_userid + "','" + BatteryLevel + "','true','" + currentDateTimeString + "','-1')");
////            Cursor cquery = sql.rawQuery("select * from User_BatteryLevel ", null);
////            String sColumnId = null;
////            if (cquery.getCount() > 0) {
////                cquery.moveToLast();
////                sColumnId = cquery.getString(0).toString();
////            }
//            BatteryOperation(batteryInfo, c);
//            cDate = new Date();
//            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//            editor.putString("CheckedInDuration", currentDateTimeString);
//            editor.commit();
//        } catch (Exception e) {
//        }
//    }
//
//    public void BatteryOperation(final Map<String, String> batteryInfo, final Context ctx) {
//        final Map<String, Map<String, String>> batMap = MyApp.getApplication().readBatteryHistory();
//        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
//        apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Log.e("BatteryOperation: ", batteryInfo.toString());
//        final ApiResult apiResult = new ApiResult();
//
//        final ApiResult.User_BatteryLevel userBatteryLevel = apiResult.new User_BatteryLevel("false", batteryInfo.get("UserId"), batteryInfo.get("DeviceId"), batteryInfo.get("Battery"), batteryInfo.get("ActivityDate"), batteryInfo.get("AutoCaptured"));
//        Call<ApiResult.User_BatteryLevel> call1 = apiInterface.PostBatteryLevel(userBatteryLevel);
//        call1.enqueue(new Callback<ApiResult.User_BatteryLevel>() {
//            @Override
//            public void onResponse(Call<ApiResult.User_BatteryLevel> call, Response<ApiResult.User_BatteryLevel> response) {
//                try {
//                    ApiResult.User_BatteryLevel iData = response.body();
//                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
//                        Map<String, String> bMap = batMap.get(batteryInfo.get("ActivityDate"));
//                        bMap.put("syncStatus", "false");
//                        batMap.put(batteryInfo.get("ActivityDate"), bMap);
//                        MyApp.getApplication().writeBatteryHistory(batMap);
////                        ContentValues newValues = new ContentValues();
////                        newValues.put("SyncStatus", "false");
////                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
//                    } else {
//                        batMap.remove(batteryInfo.get("ActivityDate"));
//                        MyApp.getApplication().writeBatteryHistory(batMap);
////                        ContentValues newValues = new ContentValues();
////                        newValues.put("SyncStatus", "true");
////                        sql.update("User_BatteryLevel", newValues, "Id=" + sColumnId, null);
//                    }
//                } catch (Exception e) {
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
//}
