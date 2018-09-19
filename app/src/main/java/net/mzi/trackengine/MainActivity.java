package net.mzi.trackengine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import net.mzi.trackengine.adapter.MainActivityAdapter;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.User_Location;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mzi.trackengine.SessionManager.KEY_USERID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    BroadcastReceiver brOfflineData;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    ApiInterface apiInterface;
    Map<String, String> mMobileDataInfo = new HashMap<String, String>();
    User_Location user_location = new User_Location();
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    Map<String, String> appCheckInInfo = new HashMap<String, String>();
    static Intent batteryIntent, locationIntent;
    static PendingIntent batteryPendingIntent, locationPendingIntent;
    static AlarmManager batteryAlarmManager, locationAlarmManager;
    RelativeLayout timerlayout;
    LinearLayout lTimerLayout;
    static RelativeLayout RLay;
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView timer, currTime;
    static RelativeLayout newtkt;
    String currentDateTimeString;
    static String currentDateTimeStringCheckIN = "";
    TextView viewAll, h_uname, tCheckIntTime, tCheckInStatus;
    static TextView showAlert;
    LocationManager locationManager;
    boolean isGPSEnabled = false;
    Location location;
    Double latitude, longitude;
    boolean isNetworkEnabled = false;
    FirebaseDatabase databaseFirebase = FirebaseDatabase.getInstance();
    DatabaseReference drRef;

    static SQLiteDatabase sql;
    Cursor cquery;
    String sDeviceId, sDepartment;
    static String LOGINID;
    private RecyclerView.LayoutManager mLayoutManager;
    static MainActivityAdapter mAdapter;
    public static final int TASK = 0;
    static RecyclerView mRecyclerView;
    String sCompanyId, nh_uname, sParentCompanyId;
    public List<String> mDataset = new ArrayList<String>();
    public List<Integer> mCardColor = new ArrayList<Integer>();
    public List<Integer> mDatasetTypes = new ArrayList<Integer>();
    public List<String> mDatasetCount = new ArrayList<String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    //SharedPreferences timerSharedPreference;
    SharedPreferences.Editor editorForTimer;
    Gps gps;
    // Editor for Shared preferences
    //SharedPreferences.Editor editorSharedPreferences;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    // A request object to store parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;
    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 120000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent
    // than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(0.0, 0.0);
    private static final int DEFAULT_ZOOM = 19;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located.
    private Location mCurrentLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    static String isCheckIn = "false";
    String sCheckInTime, sCheckInStatus;

    //static Context mainAtctivityctx;
    private SessionManager session;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        pref = getSharedPreferences("login", 0);
        editor = pref.edit();

        LOGINID = pref.getString("userid", "0");
        sDepartment = pref.getString("DepartmentId", "0");
        nh_uname = pref.getString("name", "0");
        sCompanyId = pref.getString("ParentCompanyId", "0");
        sDeviceId = pref.getString("DeviceId", "0");
        databaseFirebase.getInstance();
        drRef = databaseFirebase.getReference().child("UserLoginInfo").child(LOGINID);
        drRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    String DeviceId = (String) postSnapshot.getValue();
                    if (DeviceId.equals(sDeviceId)) ;
                    else {
                        Map<String, String> postLogin = new HashMap<String, String>();
                        Date cDate = new Date();
                        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                        //LOGINID=map.get("userid");
                        postLogin.put("UserId", LOGINID);
                        postLogin.put("IsLogin", "false");
                        postLogin.put("ActionDate", currentDateTimeString);
                        postLogin.put("DeviceId", sDeviceId);
                        postLogin.put("RealTimeUpdate", "true");
                        String sPostLogin = new Gson().toJson(postLogin);
                        if (isCheckIn.equals("true")) {
                            appCheckInInfo.put("UserId", LOGINID);
                            appCheckInInfo.put("DeviceId", sDeviceId);
                            appCheckInInfo.put("IsCheckedIn", "false");
                            appCheckInInfo.put("ActivityDate", currentDateTimeString);
                            appCheckInInfo.put("RealTimeUpdate", "true");
                            String sPostCheckIn = new Gson().toJson(appCheckInInfo);
//                            appCheckINOpemmmration(appCheckInInfo, cquery.getString(0).toString());
                            // new appCheckINOmmmperation(sPostCheckIn).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nMgr.cancelAll();
                        Log.e("logout", "firbase");
                        sql.delete("Issue_Detail", null, null);
                        sql.delete("FirebaseIssueData", null, null);
                        //sql.delete("Issue_Status", null, null);
                        session.logoutUser();
                        //clearPreferences();


                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        // Add new Flag to start new Activity
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(i);
                        overridePendingTransition(0, 0);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (brOfflineData == null) {
            try {
                brOfflineData = new BroadcastReceiver() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extras = intent.getExtras();

                        ConnectivityManager cm =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                        if (networkInfo != null) {
                            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                // do something
                                Log.e("TEST Internet", "WIFI");
                            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                                // check NetworkInfo subtype
                                Log.e("TEST Internet", "Mobile");
                                if (networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_HSPAP) {
                                    // Bandwidth between 100 kbps and below
                                    Log.e("TEST Internet", "LOW");
                                } else if (networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS) {
                                    // Bandwidth between 100 kbps and below
                                    Log.e("TEST Internet", "LOW");
                                } else if (networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE) {
                                    // Bandwidth between 50-100 kbps
                                    Log.e("TEST Internet", "MEDIUM");
                                } else if (networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_0) {
                                    // Bandwidth between 400-1000 kbps
                                    Log.e("TEST Internet", "NORMAL");
                                } else if (networkInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EVDO_A) {
                                    // Bandwidth between 600-1400 kbps
                                    Log.e("TEST Internet", "HIGH");
                                }
                            }


                            Date cDate = new Date();
                            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                                InternetConnector icDataSyncing = new InternetConnector();
                                try {
                                    icDataSyncing.offlineSyncing(MainActivity.this, 1);
                                    mMobileDataInfo.put("UserId", LOGINID);
                                    mMobileDataInfo.put("DeviceId", sDeviceId);
                                    mMobileDataInfo.put("Enabled", "true");
                                    mMobileDataInfo.put("ActionDate", currentDateTimeString);
                                    mMobileDataInfo.put("RealTimeUpdate", "true");
                                    String jsonString = new Gson().toJson(mMobileDataInfo);
                                    sql.execSQL("INSERT INTO User_MobileData(UserId,Enabled,ActionDate,SyncStatus)VALUES" +
                                            "('" + mMobileDataInfo.get("UserId") + "','" + mMobileDataInfo.get("Enabled") + "','" + mMobileDataInfo.get("ActionDate") + "','-1')");
                                    Cursor cquery = sql.rawQuery("select * from User_MobileData ", null);
                                    String sColumnId = null;
                                    if (cquery.getCount() > 0) {
                                        cquery.moveToLast();
                                        sColumnId = cquery.getString(0).toString();
                                    }
                                    cquery.close();
                                    PushMobileData(mMobileDataInfo, getApplicationContext(), sColumnId);
                                } catch (Exception e) {
                                }


                            } else {
                                SOMTracker.showMassage(MainActivity.this, "Internet connection is Off");
//                                Toast.makeText(getApplicationContext(), "Internet connection is Off", Toast.LENGTH_LONG).show();
                                mMobileDataInfo.put("UserId", LOGINID);
                                mMobileDataInfo.put("DeviceId", sDeviceId);
                                mMobileDataInfo.put("Enabled", "false");
                                mMobileDataInfo.put("ActionDate", currentDateTimeString);
                                mMobileDataInfo.put("RealTimeUpdate", "true");
                                String jsonString = new Gson().toJson(mMobileDataInfo);
                                sql.execSQL("INSERT INTO User_MobileData(UserId,Enabled,ActionDate,SyncStatus)VALUES" +
                                        "('" + mMobileDataInfo.get("UserId") + "','" + mMobileDataInfo.get("Enabled") + "','" + mMobileDataInfo.get("ActionDate") + "','-1')");
                                Cursor cquery = sql.rawQuery("select * from User_MobileData ", null);
                                String sColumnId = null;
                                if (cquery.getCount() > 0) {
                                    cquery.moveToLast();
                                    sColumnId = cquery.getString(0).toString();
                                }
                                cquery.close();
                                PushMobileData(mMobileDataInfo, getApplicationContext(), sColumnId);
                            }
                            showAlert.setText("Please Wait, fetching ticket info!!!");
                        } else
                            showAlert.setText("Internet is not Working");

                    }
                };
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(brOfflineData, intentFilter);
            } catch (Exception e) {

            }
        }
        Firebase.setAndroidContext(this);
        session = new SessionManager(getApplicationContext());


        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        SessionManager s = new SessionManager(getApplicationContext());

        HashMap<String, String> map = new HashMap<>();
        map = s.getUserDetails();
        LOGINID = map.get("userid");
//      Log.e("USERID",LOGINID);
        lTimerLayout = findViewById(R.id.timerL);
        tCheckInStatus = findViewById(R.id.checkInStatus);
        tCheckIntTime = findViewById(R.id.checkInTime);
        //tAt =  findViewById(R.id.atID);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        timerlayout = (RelativeLayout) findViewById(R.id.timerLayout);
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        //SQLiteDatabase.openOrCreateDatabase()
        timer = findViewById(R.id.textView11);
        currTime = findViewById(R.id.textView4);
        viewAll = findViewById(R.id.viewAll);
        showAlert = findViewById(R.id.viewAlert);
        RLay = (RelativeLayout) findViewById(R.id.remaininglayout);
        newtkt = (RelativeLayout) findViewById(R.id.newtkt);
        showAlert.setVisibility(View.VISIBLE);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sCheckInTime = pref.getString("CheckedInTime", "0");
        sCheckInStatus = pref.getString("CheckedInStatus", "0");

     /*   if(MainActivity.isCheckIn.equals("true")) {


        }*/
        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
            tCheckInStatus.setText("Checked-IN");
            lTimerLayout.setBackgroundResource(R.drawable.cardbk_green);
            //tCheckInStatus.setTextColor(getResources().getColor(R.color.green));
            tCheckInStatus.setBackgroundResource(R.drawable.cardbk_green_solid);
            tCheckIntTime.setTextColor(getResources().getColor(R.color.green));
            /*********************************Alarm : 6 o clock********************************************************/
            Calendar alarmStartTime = Calendar.getInstance();
            alarmStartTime.set(Calendar.HOUR_OF_DAY, 18);
            alarmStartTime.set(Calendar.MINUTE, 00);
            alarmStartTime.set(Calendar.SECOND, 0);

            Intent intent = new Intent(this, Notificationmessage.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am =
                    (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, alarmStartTime.getTimeInMillis(),
                    pendingIntent);
            /*********************************Alarm : 6 o clock********************************************************/
            Date cDate = new Date();
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            editor.putString("CheckedInDuration", currentDateTimeString);
            editor.commit();
            setData();

            //tAt.setTextColor(getResources().getColor(R.color.green));
        } else if (sCheckInStatus.equals("False") || sCheckInStatus.equals("false")) {
            tCheckInStatus.setText("Checked-OUT");
            lTimerLayout.setBackgroundResource(R.drawable.cardbk_red);
            tCheckInStatus.setBackgroundResource(R.drawable.cardbk_red_solid);
            //tCheckInStatus.setTextColor(getResources().getColor(R.color.red));
            tCheckIntTime.setTextColor(getResources().getColor(R.color.red));
            //tAt.setTextColor(getResources().getColor(R.color.red));
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(12345);

        } else {
            lTimerLayout.setVisibility(View.GONE);
        }
        tCheckIntTime.setText(sCheckInTime);


        Log.e(TAG, "onCreate:sdsdsd " + sCheckInTime);
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TaskActivity.class);
                i.putExtra("cardpos", "-1");
                startActivity(i);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDataset.add("New");
        mDataset.add("Accepted");
        mDataset.add("Attended");
        mDataset.add("Resolved");

        mCardColor.add(R.drawable.cardbk_purple);
        mCardColor.add(R.drawable.cardbk_orange);
        mCardColor.add(R.drawable.cardbk_blue);
        mCardColor.add(R.drawable.cardbk_green);

        mDatasetTypes.add(TASK);
        mDatasetTypes.add(TASK);
        mDatasetTypes.add(TASK);
        mDatasetTypes.add(TASK);


        Cursor cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));
        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 1", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));
        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 2", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));

        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 3", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));
        cquery.close();
        mRecyclerView = (RecyclerView) findViewById(R.id.task_view);
        // mLayoutManager = new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false);
        StaggeredGridLayoutManager gaggeredGridLayoutManager;
        gaggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(gaggeredGridLayoutManager);
        mAdapter = new MainActivityAdapter(mDataset, mDatasetCount, mCardColor, mDatasetTypes, MainActivity.this);
        mRecyclerView.setAdapter(mAdapter);


        tCheckInStatus.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: ");
                if (tCheckInStatus.getText().toString().equals("Checked-OUT")) {
                    SOMTracker.setStatus("isCheckin", true);
                    Log.e(TAG, "onClick: ");
                    Date cDate = new Date();
                    currentDateTimeStringCheckIN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    currTime.setText("CheckedIN: " + currentDateTimeStringCheckIN);
                    appCheckInInfo.put("ActivityDate", currentDateTimeStringCheckIN);
                    editor.putString("CheckedInTime", currentDateTimeStringCheckIN);
                    editor.putString("CheckedInStatus", "True");
                    editor.putString("CheckedInDuration", currentDateTimeStringCheckIN);
                    editor.commit();
                    tCheckIntTime.setText(currentDateTimeStringCheckIN);
                    tCheckInStatus.setText("Checked-IN");
                    Log.e(TAG, "onCheckedChanged: " + tCheckInStatus.getText().toString());
                    lTimerLayout.setBackgroundResource(R.drawable.cardbk_green);
                    tCheckIntTime.setTextColor(getResources().getColor(R.color.green));
                    tCheckInStatus.setBackgroundResource(R.drawable.cardbk_green_solid);
                    //tCheckInStatus.setTextColor(getResources().getColor(R.color.green));
                    //tAt.setTextColor(getResources().getColor(R.color.green));
                    Cursor cquery = sql.rawQuery("select * from User_Location", null);
                    if (cquery.getCount() > 0) {

                        Log.e("InternetConnector: ", "I am in User_location" + cquery.getCount());

                        for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                            String id = cquery.getString(0).toString();
                            sql.delete("User_Location", "Id" + "=" + id, null);
                        }
                    }
                    cquery.close();
                    setData();
                } else {
                    SOMTracker.setStatus("isCheckin", false);
                    Date cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    batteryAlarmManager.cancel(batteryPendingIntent);
                    try {
                        locationAlarmManager.cancel(locationPendingIntent);
//                        stopService(locationIntent);
                    } catch (Exception e) {
                    }
//                    stopService(intent);
//                    stopService(batteryIntent);

                    Log.e(TAG, "setData:Checked-IN " + currentDateTimeString);
                    isCheckIn = "false";
                    appCheckInInfo.put("ActivityDate", currentDateTimeString);
                    timer.setText("00:00:00");
                    editor.putString("CheckedInTime", currentDateTimeString);
                    editor.putString("CheckedInStatus", "False");
                    editor.commit();
                    tCheckIntTime.setText(currentDateTimeString);
                    tCheckInStatus.setText("Checked-OUT");
                    lTimerLayout.setBackgroundResource(R.drawable.cardbk_red);
                    tCheckInStatus.setBackgroundResource(R.drawable.cardbk_red_solid);
                    //tCheckInStatus.setTextColor(getResources().getColor(R.color.red));
                    tCheckIntTime.setTextColor(getResources().getColor(R.color.red));

                    NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nMgr.cancel(12345);
                    try {
                        Cursor cquery = sql.rawQuery("select * from User_Location", null);
                        if (cquery.getCount() > 0) {

                            Log.e("InternetConnector: ", "I am in User_location" + cquery.getCount());

                            for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                                String id = cquery.getString(0).toString();
                                sql.delete("User_Location", "Id" + "=" + id, null);
                            }
                        }
                        cquery.close();
                    } catch (Exception e) {
                    }
                    //tAt.setTextColor(getResources().getColor(R.color.red));
                }
                Map<String, String> locationInfo = new HashMap<String, String>();
                gps = new Gps(getApplicationContext());
                if (gps.canGetLocation()) {

                    Date cDate = new Date();
                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                    if (gps.canGetLocation) {
                        user_location = getLocation();
                    } else {
                        if (SOMTracker.getSharedPrefString("lat").isEmpty()) {
                            user_location.Latitude = 0.0;
                            user_location.Longitude = 0.0;
                        } else {
                            try {
                                user_location.Latitude = Double.parseDouble(SOMTracker.getSharedPrefString("lat"));
                                user_location.Longitude = Double.parseDouble(SOMTracker.getSharedPrefString("lng"));
                            } catch (Exception e) {
                            }
                        }

                    }
                    Geocoder geocoder = null;
                    List<Address> addresses;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {
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
                            sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            sCity = "NA";
                            sState = "NA";
                            sCountry = "NA";
                            sPostalCode = "NA";
                            sKnownName = "NA";
                            sPremises = "NA";
                            sSubLocality = "NA";
                            sSubAdminArea = "NA";
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        sCity = "NA";
                        sState = "NA";
                        sCountry = "NA";
                        sPostalCode = "NA";
                        sKnownName = "NA";
                        sPremises = "NA";
                        sSubLocality = "NA";
                        sSubAdminArea = "NA";
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        sCity = "NA";
                        sState = "NA";
                        sCountry = "NA";
                        sPostalCode = "NA";
                        sKnownName = "NA";
                        sPremises = "NA";
                        sSubLocality = "NA";
                        sSubAdminArea = "NA";
                    }

                    locationInfo.put("UserId", LOGINID);
                    locationInfo.put("DeviceId", sDeviceId);
                    locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
                    locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
                    locationInfo.put("ActivityDate", currentDateTimeString);
                    locationInfo.put("AutoCaptured", "false");
                    locationInfo.put("AddressLine", sAddressLine);
                    locationInfo.put("Premises", sPremises);
                    locationInfo.put("SubLocality", sSubLocality);
                    locationInfo.put("SubAdminArea", sSubAdminArea);
                    locationInfo.put("PostalCode", sPostalCode);
                    locationInfo.put("City", sCity);
                    locationInfo.put("State", sState);
                    locationInfo.put("Country", sCountry);
                    locationInfo.put("KnownName", sKnownName);
                    locationInfo.put("RealTimeUpdate", "true");
                    locationInfo.put("Provider", "NA");
                    String jsonString = new Gson().toJson(locationInfo);
                    Log.e(TAG, "run:" + jsonString);

                    try {
                        sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
                                "('" + LOGINID + "','" + user_location.Latitude + "','" + user_location.Longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                        Log.e("Location insertion", "Inserted by MainActivity at 682");
                        Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                        String sColumnId = null;
                        if (cquery.getCount() > 0) {
                            cquery.moveToLast();
                            sColumnId = cquery.getString(0).toString();
                        }
                        cquery.close();
                        ServiceLocation m = new ServiceLocation();
                        Log.d("postcoordinat", "from main activity at 663");
                        m.LocationOperationOffline(locationInfo, getApplicationContext(), sColumnId);
                    } catch (Exception e) {
                        ServiceLocation m = new ServiceLocation();
                        Log.d("postcoordinat", "from main activity at 663");
                        m.LocationOperationOffline(locationInfo, getApplicationContext(), "");
                    }
//                    Toast.makeText(getApplicationContext(), "Location sent successfully!!!", Toast.LENGTH_LONG).show();
                    SOMTracker.showMassage(MainActivity.this, "Location sent successfully!!!");
                }

                String jsonString = new Gson().toJson(locationInfo);
                Log.e(TAG, "run: " + jsonString);
                SOMTracker.showMassage(MainActivity.this, "CheckIn-Out Info sent successfully!!!");
//                Toast.makeText(getApplicationContext(), "CheckIn-Out Info sent successfully!!!", Toast.LENGTH_LONG).show();
                Date cDate = new Date();
                currentDateTimeStringCheckIN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                appCheckInInfo.put("UserId", MainActivity.LOGINID);
                appCheckInInfo.put("DeviceId", sDeviceId);
                appCheckInInfo.put("IsCheckedIn", isCheckIn);
                appCheckInInfo.put("RealTimeUpdate", "true");
                appCheckInInfo.put("ActivityDate", currentDateTimeStringCheckIN);

                String sAppCheckInInfo = new Gson().toJson(appCheckInInfo);
                Log.e("onCheckedChanged: ", sAppCheckInInfo);
                sql.execSQL("INSERT INTO User_AppCheckIn(UserId,IsCheckIn,ActionDate,SyncStatus)VALUES" +
                        "('" + appCheckInInfo.get("UserId") + "','" + appCheckInInfo.get("IsCheckedIn") + "','" + appCheckInInfo.get("ActivityDate") + "','-1')");
                Cursor cquery = sql.rawQuery("select * from User_AppCheckIn ", null);
                String sColumnId = null;
                if (cquery.getCount() > 0) {
                    cquery.moveToLast();
                    sColumnId = cquery.getString(0).toString();
                }
                cquery.close();
                appCheckINOperation(appCheckInInfo, sColumnId);

                BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);

                try {
                    int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    Log.d(">>>>>>>>>>", "sending battery");
                    Log.d("battery", batLevel + "%");
                    Log.d("battery2", getBatteryPercentage(MainActivity.this) + "%");
                    if (batLevel > 0) {
                        sendBatteryCheckinLevel(batLevel);
                    } else {
                        sendBatteryCheckinLevel(getBatteryPercentage(MainActivity.this));
                    }
                } catch (Exception e) {
                    try {
                        sendBatteryCheckinLevel(getBatteryPercentage(MainActivity.this));
                    } catch (Exception eee) {
                    }
                }

            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)

        {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, 200);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

        {
            @Override
            public void onRefresh() {
                //handling swipe refresh
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);

                        double latitude, longitude;
                        gps = new Gps(getApplicationContext());
                        if (isCheckIn.equals("true")) {
                            if (gps.canGetLocation()) {

                                Date cDate = new Date();
                                currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                                if (gps.canGetLocation) {

                                    user_location = getLocation();

                                } else {
                                    if (SOMTracker.getSharedPrefString("lat").isEmpty()) {
                                        user_location.Latitude = 0.0;
                                        user_location.Longitude = 0.0;
                                    } else {
                                        try {
                                            user_location.Latitude = Double.parseDouble(SOMTracker.getSharedPrefString("lat"));
                                            user_location.Longitude = Double.parseDouble(SOMTracker.getSharedPrefString("lng"));
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                Geocoder geocoder = null;
                                List<Address> addresses;
                                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                                try {
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
                                        sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                        sCity = "NA";
                                        sState = "NA";
                                        sCountry = "NA";
                                        sPostalCode = "NA";
                                        sKnownName = "NA";
                                        sPremises = "NA";
                                        sSubLocality = "NA";
                                        sSubAdminArea = "NA";
                                    }
                                    //String s= address+" "+city+""+state+" "+country+" "+postalCode+" "+knownName+" "+premises+" "+subLocality+" "+subAdminArea;
                                    //sLog.e(TAG, "onLocationChanged: "+s);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                    sCity = "NA";
                                    sState = "NA";
                                    sCountry = "NA";
                                    sPostalCode = "NA";
                                    sKnownName = "NA";
                                    sPremises = "NA";
                                    sSubLocality = "NA";
                                    sSubAdminArea = "NA";
                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                    sAddressLine = "NA"; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                    sCity = "NA";
                                    sState = "NA";
                                    sCountry = "NA";
                                    sPostalCode = "NA";
                                    sKnownName = "NA";
                                    sPremises = "NA";
                                    sSubLocality = "NA";
                                    sSubAdminArea = "NA";
                                }
                                Map<String, String> locationInfo = new HashMap<String, String>();
                                locationInfo.put("UserId", LOGINID);
                                locationInfo.put("DeviceId", sDeviceId);
                                locationInfo.put("Latitude", String.valueOf(user_location.Latitude));
                                locationInfo.put("Longitude", String.valueOf(user_location.Longitude));
                                locationInfo.put("ActivityDate", currentDateTimeString);
                                locationInfo.put("AutoCaptured", "false");
                                locationInfo.put("AddressLine", sAddressLine);
                                locationInfo.put("Premises", sPremises);
                                locationInfo.put("SubLocality", sSubLocality);
                                locationInfo.put("SubAdminArea", sSubAdminArea);
                                locationInfo.put("PostalCode", sPostalCode);
                                locationInfo.put("City", sCity);
                                locationInfo.put("State", sState);
                                locationInfo.put("Country", sCountry);
                                locationInfo.put("KnownName", sKnownName);
                                locationInfo.put("RealTimeUpdate", "true");
                                locationInfo.put("Provider", "NA");
                                String jsonString = new Gson().toJson(locationInfo);
                                Log.e(TAG, "run: " + jsonString);
                                try {
                                    sql.execSQL("INSERT INTO User_Location(UserId,Latitude,Longitude,AutoCaptured,ActivityDate,AddressLine,City,State,Country,PostalCode,KnownName,Premises,SubLocality,SubAdminArea,SyncStatus)VALUES" +
                                            "('" + LOGINID + "','" + user_location.Latitude + "','" + user_location.Longitude + "','true','" + currentDateTimeString + "','" + sAddressLine + "','" + sCity + "','" + sState + "','" + sCountry + "','" + sPostalCode + "','" + sKnownName + "','" + sPremises + "','" + sSubLocality + "','" + sSubAdminArea + "','-1')");
                                } catch (Exception e) {
                                }
                                Log.e("Location insertion", "Inserted by MainActivity at 863");
                                Cursor cquery = sql.rawQuery("select * from User_Location ", null);
                                String sColumnId = null;
                                if (cquery.getCount() > 0) {
                                    cquery.moveToLast();
                                    sColumnId = cquery.getString(0).toString();
                                }
                                cquery.close();
                                if (user_location.Latitude == 0.0 || user_location.Longitude == 0) {
//                                    Toast.makeText(getApplicationContext(), "Location could not captured, check your GPS!!!", Toast.LENGTH_LONG).show();
                                    SOMTracker.showMassage(MainActivity.this, "Location could not captured, check your GPS!!!");
                                    user_location.Latitude = Double.parseDouble(SOMTracker.getSharedPrefString("lat"));
                                    user_location.Longitude = Double.parseDouble(SOMTracker.getSharedPrefString("lng"));
                                    ServiceLocation m = new ServiceLocation();
                                    Log.d("postcoordinat", "from main activity at 836");
                                    m.LocationOperationOffline(locationInfo, getApplicationContext(), sColumnId);
                                } else {
                                    SOMTracker.showMassage(MainActivity.this, "Location sent successfully!!!");
//                                    Toast.makeText(getApplicationContext(), "Location sent successfully!!!", Toast.LENGTH_LONG).show();
                                    ServiceLocation m = new ServiceLocation();
                                    Log.d("postcoordinat", "from main activity at 841");
                                    m.LocationOperationOffline(locationInfo, getApplicationContext(), sColumnId);
                                }
                                //MainActivity.this.registerReceiver(broadcastreceiver,intentfilter);
                            }
                        }

                    }
                }, 4000);


            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TicketCreation.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        h_uname = headerView.findViewById(R.id.header_username);
        h_uname.setText(nh_uname);

    }

    private void sendBatteryCheckinLevel(int batLevel) {
        Map<String, String> batteryInfo = new HashMap<>();

        batteryInfo.put("UserId", LOGINID);
        batteryInfo.put("DeviceId", sDeviceId);
        batteryInfo.put("Battery", String.valueOf(batLevel));
        batteryInfo.put("ActivityDate", currentDateTimeString);
        batteryInfo.put("AutoCaptured", "false");
        batteryInfo.put("RealTimeUpdate", "true");
        String jsonBatteryString = new Gson().toJson(batteryInfo);
        ServiceBattery serviceBattery = new ServiceBattery();
//              serviceBattery.registerReceiver(serviceBattery.mBatInfoReceiver)
        sql.execSQL("INSERT INTO User_BatteryLevel(UserId,BatteryLevel,AutoCaptured,ActionDate,SyncStatus)VALUES('" + LOGINID + "','" + batLevel + "','true','" + currentDateTimeString + "','-1')");
        Cursor cquery = sql.rawQuery("select * from User_Login ", null);
        String sColumnId = null;
        if (cquery.getCount() > 0) {
            cquery.moveToLast();
            sColumnId = cquery.getString(0).toString();
        }
        cquery.close();
        SOMTracker.setSharedPrefLong("BAT", 0);
        Log.d(">>>>>>>>>>", "send battery method called");
        serviceBattery.BatteryOperation(batteryInfo, getApplicationContext(), sColumnId);
    }

    public static void removeTkt() {
        try {
            MainActivity.newtkt.setVisibility(View.GONE);
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonLayoutParams.setMargins(0, 130, 0, 0);
            RLay.setLayoutParams(buttonLayoutParams);
        } catch (Exception e) {
        }
    }

    public static void showTkt() {
        MainActivity.newtkt.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            MainActivity.this.finish();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting and binding service");
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        sCheckInStatus = pref.getString("CheckedInStatus", "0");

        if (sCheckInStatus.equals("True") || sCheckInStatus.equals("true")) {
            Date cDate = new Date();
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            editor.putString("CheckedInDuration", currentDateTimeString);
            editor.commit();
            setData();
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setData() {
        isCheckIn = "true";
        Log.e(TAG, "setData:Checked-IN " + currentDateTimeStringCheckIN);
        currTime.setText("Checked-In:" + currentDateTimeStringCheckIN);


        long locationTime = System.currentTimeMillis() + (120 * 1000);
        long batteryTime = System.currentTimeMillis() + (900 * 1000);

//        int sIntervalLocation = pref.getInt("AppLocationSendingFrequency", 120);
//        int sIntervalBattery = pref.getInt("AppBatterySendingFrequency", 900);

        batteryIntent = new Intent(MainActivity.this, ServiceBattery.class);
        batteryPendingIntent = PendingIntent.getService(MainActivity.this, 5, batteryIntent, 0);
        batteryAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        batteryAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, batteryTime, 15 * 1000, batteryPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setRequestingLocationUpdates(this, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
//           if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            locationIntent = new Intent(MainActivity.this, ServiceLocation.class);
            locationPendingIntent = PendingIntent.getService(MainActivity.this, 1, locationIntent, 0);
            locationAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            locationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, locationTime, 630 * 1000, locationPendingIntent);
//           }
        } else {
            locationIntent = new Intent(MainActivity.this, ServiceLocation.class);
            locationPendingIntent = PendingIntent.getService(MainActivity.this, 1, locationIntent, 0);
            locationAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            locationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, locationTime, 60 * 1000, locationPendingIntent);
        }

    }

    private PendingIntent getPendingIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
            intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent intent = new Intent(this, LocationUpdatesIntentService.class);
            intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
            return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SOMTracker.showMassage(MainActivity.this, "Coming Soon");
//            Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).
//                    show();
            return true;
        /*} else if (id == R.id.myswitch) {
            return true;*/
        } else if (id == R.id.action_helpme) {
            Intent i = new Intent(MainActivity.this, RaiseTicket.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_chPwd) {
            Intent i = new Intent(MainActivity.this, ChangePasswordActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_appinfo) {
            //Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Version Code: " + String.valueOf(BuildConfig.VERSION_CODE + "\n\nVersion Name: " + BuildConfig.VERSION_NAME)).setTitle("App Info")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
            return true;
        } else if (id == R.id.action_logout) {

            Map<String, String> postLogin = new HashMap<String, String>();
            Date cDate = new Date();
            //LOGINID=map.get("userid");
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

            postLogin.put("UserId", LOGINID);
            postLogin.put("IsLogin", "false");
            postLogin.put("ActionDate", currentDateTimeString);
            postLogin.put("DeviceId", sDeviceId);
            postLogin.put("RealTimeUpdate", "true");

            String sPostLogin = new Gson().toJson(postLogin);
            if (isCheckIn.equals("true")) {
                try {
                    batteryAlarmManager.cancel(batteryPendingIntent);
                    stopService(batteryIntent);
                    locationAlarmManager.cancel(locationPendingIntent);
                    stopService(locationIntent);
                } catch (Exception e) {
                }
                appCheckInInfo.put("UserId", LOGINID);
                appCheckInInfo.put("DeviceId", sDeviceId);
                appCheckInInfo.put("IsCheckedIn", "false");
                appCheckInInfo.put("ActivityDate", currentDateTimeString);
                appCheckInInfo.put("RealTimeUpdate", "true");
            }
            new IsLogin(sPostLogin, 0, "0").execute();
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
            session.logoutUser();
            sql.delete("Issue_Detail", null, null);
            sql.delete("FirebaseIssueData", null, null);
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            editor.putString(KEY_USERID, "0");
            try {
                ServiceLocation serviceLocation = new ServiceLocation(this);
                Intent intent = new Intent(this, serviceLocation.getClass());
                if (!isMyServiceRunning(serviceLocation.getClass())) {
                    stopService(intent);
                }
            } catch (Exception e) {
            }
            try {
                ServiceDataUpdateFirstFragment serviceDataUpdateFirstFragment= new ServiceDataUpdateFirstFragment(this);
                Intent intent = new Intent(this, serviceDataUpdateFirstFragment.getClass());
                if (!isMyServiceRunning(serviceDataUpdateFirstFragment.getClass())) {
                    stopService(intent);
                }
            } catch (Exception e) {
            }
            finish();
            overridePendingTransition(0, 0);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_performance) {
            // Handle the camera action
            Intent i = new Intent(MainActivity.this, AgentPerformance.class);
            startActivity(i);
        } else if (id == R.id.nav_history) {
            Intent i = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_myvouchers) {
            Intent i = new Intent(MainActivity.this, VouchersActivity.class);
            startActivity(i);

        } else if (id == R.id.nav_mytkts) {
            // Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, InternalIssueListing.class);
            startActivity(i);
        } else if (id == R.id.nav_resetdata) {
            // Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setMessage("All of this apps data will be deleted permanently. This includes all files, settings, accounts, databases, etc").setTitle("Alert!!!")
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do nothing
                            dialog.dismiss();
                            clearPreferences();
                        }
                    });
            android.app.AlertDialog alert = builder.create();
            alert.show();
        } else if (id == R.id.nav_updatedata) {
            new FetchStatus();
            Intent i = getIntent();
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(i);
            overridePendingTransition(0, 0);
        } else if (id == R.id.nav_offlinesync) {
            // Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, OfflineSyncInfo.class);
            startActivity(i);
        } else if (id == R.id.nav_sendLog) {
            SOMTracker.showMassage(MainActivity.this, "Coming Soon");
//            Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            /*Intent i=new Intent(MainActivity.this,OfflineSyncInfo.class);
            startActivity(i);*/
        } else if (id == R.id.nav_aboutus) {
            Intent i = new Intent(MainActivity.this, AboutUs.class);
            startActivity(i);

        } else if (id == R.id.nav_contactus) {
            SOMTracker.showMassage(MainActivity.this, "Coming Soon");
//            Toast.makeText(getApplicationContext(), "Coming Soon!!!", Toast.LENGTH_LONG).show();
        } else if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("tsext/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Track Engine");
                String sShareText = "\nLet me recommend you this application\n\n";
                sShareText = sShareText + "https://play.google.com/store/apps/details?id=net.mzi.trackengine\n\n";
                i.putExtra(Intent.EXTRA_TEXT, sShareText);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch (Exception e) {

            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SOMTracker.activityResumed();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        updateMarkers();
        Utils.getLocationUpdatesResult(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
    }

    // AddonVisoin Infotech integrated to call api in every 10 minutes


    @Override
    protected void onPause() {
        super.onPause();
        SOMTracker.activityPaused();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brOfflineData);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("TAG", "Play services connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateMarkers();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Add markers for nearby places.
        updateMarkers();
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = (infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = (infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
            mMap.getUiSettings().setZoomControlsEnabled(true);
        } else {
            Log.d("TAG", "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                user_location.Latitude = location.getLatitude();
                user_location.Longitude = location.getLongitude();
                SOMTracker.setSharedPrefString("lat", user_location.Latitude + "");
                SOMTracker.setSharedPrefString("lng", user_location.Longitude + "");
//                mCameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
//                        location.getLongitude())).build();
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            }
        });
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        /*
         * Sets the desired interval for active location updates. This interval is
         * inexact. You may not receive updates at all if no location sources are available, or
         * you may receive them slower than requested. You may also receive updates faster than
         * requested if other applications are requesting location at a faster interval.
         */
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        /*
         * Sets the fastest rate for active location updates. This interval is exact, and your
         * application will never receive updates faster than this value.
         */
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        else
            mLocationPermissionGranted = true;
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         * Also request regular updates about the device location.
         */
        if (mLocationPermissionGranted) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //sDeviceId = telephonyManager.getDeviceId().toString();
                    // permissions granted.
                }
            }
        }
        updateLocationUI();
    }

    private void updateMarkers() {
        if (mMap == null) {
            return;
        }
        if (mLocationPermissionGranted) {
            // Get the businesses and other points of interest located
            // nearest to the device's current location.
            @SuppressWarnings("MissingPermission")
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        // Add a marker for each place near the device's current location, with an
                        // info window showing place information.
                        String attributions = (String) placeLikelihood.getPlace().getAttributions();
                        String snippet = (String) placeLikelihood.getPlace().getAddress();
                        if (attributions != null) {
                            snippet = snippet + "\n" + attributions;
                        }

                    }
                    // Release the place likelihood buffer.
                    likelyPlaces.release();
                }
            });
        } else {
        }
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mCurrentLocation = null;
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent i = new Intent(MainActivity.this, FullScreenMap.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_LOCATION_UPDATES_RESULT)) {
            Utils.getLocationUpdatesResult(this);
        } else if (s.equals(Utils.KEY_LOCATION_UPDATES_REQUESTED)) {
            Utils.getLocationUpdatesResult(this);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
    }

    private class FetchStatus extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "GetParentStatus?iDepartmentId=" + sDepartment);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                //Toast.makeText(getActivity(),R.string.internet_error, Toast.LENGTH_LONG).show();
            } else {
                Log.i("INFO", s);
                sql.delete("Issue_Status", null, null);
                sql.delete("Issue_StatusHiererchy", null, null);
                try {
                    Cursor cqueryFetchingStatus = sql.rawQuery("select * from Issue_Status where DepartmentId='" + sDepartment + "'", null);
                    if (cqueryFetchingStatus.getCount() > 0) {

                    } else {
                        JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                        JSONArray jdata = jsonObject.getJSONArray("lstIssue_Status");
                        for (int i = 0; i < jdata.length(); i++) {
                            JSONObject object = jdata.getJSONObject(i);
                            sql.execSQL("INSERT INTO Issue_Status(StatusId,StatusName,CommentRequired,MainStatusId,IsMobileStatus,CompanyId,DepartmentId,ParentStatus)VALUES" +
                                    "('" + object.getString("Id") + "','" + object.getString("StatusName") + "','" + object.getString("CommentRequired") + "','" + object.getString("MainStatusId") + "','" + object.getString("IsMobileStatus") + "','" + sCompanyId + "','" + sDepartment + "','" + object.getString("ParentStatuses") + "')");
                            Log.e("loginActivity", object.getString("StatusName") + ":" + object.getString("Id"));
                            String sAllParentStatuses = object.getString("ParentStatuses");
                            List<String> listsAllParentStatuses = Arrays.asList(sAllParentStatuses.split(","));
                            for (int j = 0; j < listsAllParentStatuses.size(); j++) {
                                sql.execSQL("INSERT INTO Issue_StatusHiererchy(StatusId,ParentStatus,WaitForEntry,ActionDate,RedirectToStatus)VALUES" +
                                        "('" + object.getString("Id") + "','" + listsAllParentStatuses.get(j).trim() + "','0','0','0')");
                                Log.e("loginActivity ", object.getString("StatusName") + ":" + listsAllParentStatuses.get(j).trim());
                            }
                        }
                    }
                    cqueryFetchingStatus.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class IsLogin extends AsyncTask<String, String, String> {
        String jsonString, sColumnId;
        int flag = 0;

        public IsLogin(String jsonString, int flag, String sColumnId) {
            this.jsonString = jsonString;
            this.flag = flag;
            this.sColumnId = sColumnId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            sql = getActivity().getApplicationContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        }

        @Override
        protected String doInBackground(String... params) {
            String uri = PostUrl.sUrl + "PostLogin";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                urlConnection.connect();

                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                //Call parserUsuarioJson() inside write(),Make sure it is returning proper json string .
                writer.write(jsonString);
                writer.close();
                outputStream.close();

                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("Post Login!!!", s);
        }
    }

    public void appCheckINOperation(Map appCheckInInfo, final String sColumnId) {
        //sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        final ApiResult apiResult = new ApiResult();
        final ApiResult.UserCheckInOut userCheckInOut = apiResult.new UserCheckInOut("true", appCheckInInfo.get("UserId").toString(), appCheckInInfo.get("DeviceId").toString(), appCheckInInfo.get("IsCheckedIn").toString(), appCheckInInfo.get("ActivityDate").toString());
        Call<ApiResult.UserCheckInOut> call1 = apiInterface.PostCheckIn(userCheckInOut);
        final String finalColumnId = sColumnId;
        call1.enqueue(new Callback<ApiResult.UserCheckInOut>() {
            @Override
            public void onResponse(Call<ApiResult.UserCheckInOut> call, Response<ApiResult.UserCheckInOut> response) {
                ApiResult.UserCheckInOut iData = response.body();
                if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Data is currently saved offline, will sync once it gets internet connection!!!").setTitle("Response from Server")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // do nothing

                                    dialog.dismiss();
                                }
                            });
                    android.app.AlertDialog alert = builder.create();
                    alert.show();
                    ContentValues newValues = new ContentValues();
                    newValues.put("SyncStatus", "false");
                    sql.update("User_AppCheckIn", newValues, "Id=" + sColumnId, null);
                } else {
                    ContentValues newValues = new ContentValues();
                    newValues.put("SyncStatus", "true");
                    sql.update("User_AppCheckIn", newValues, "Id=" + sColumnId, null);
                }
            }

            @Override
            public void onFailure(Call<ApiResult.UserCheckInOut> call, Throwable t) {
                call.cancel();

            }
        });
    }

    public void updateCounter(Context mContext) {
        MainActivityAdapter maCounter = new MainActivityAdapter();
        int max = mDatasetCount.size();
        for (int i = 0; i < max; i++) {
            mDatasetCount.remove(i);
            mDataset.remove(i);
            mCardColor.remove(i);
            mDatasetCount.remove(i);
        }
        try {
            Cursor cquery1 = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);//new
            mDatasetCount.add(String.valueOf(cquery1.getCount()));
            mCardColor.add(R.drawable.cardbk_purple);
            mDataset.add("New");
            mDatasetTypes.add(0);
            cquery1.close();
            Cursor cquery2 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 1", null);
            mDatasetCount.add(String.valueOf(cquery2.getCount()));
            mCardColor.add(R.drawable.cardbk_orange);
            mDataset.add("Accepted");
            mDatasetTypes.add(0);
            cquery2.close();
            Cursor cquery3 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 2", null);
            mDatasetCount.add(String.valueOf(cquery3.getCount()));
            mCardColor.add(R.drawable.cardbk_blue);
            mDataset.add("Attended");
            mDatasetTypes.add(0);
            cquery3.close();
            Cursor cquery4 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 3", null);
            mDatasetCount.add(String.valueOf(cquery4.getCount()));
            mCardColor.add(R.drawable.cardbk_green);
            mDataset.add("Resolved");
            mDatasetTypes.add(0);
            cquery4.close();
            maCounter = new MainActivityAdapter(mDataset, mDatasetCount, mCardColor, mDatasetTypes, mContext);
            mRecyclerView.setAdapter(maCounter);
        } catch (Exception e) {
        }

        //maCounter.updateCounter();

    }

    public void PushMobileData(Map mMobileDataInfo, final Context ctx, final String sColumnId) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        Log.e("PushMobileData: ", mMobileDataInfo.toString());
        final ApiResult apiResult = new ApiResult();
        final ApiResult.User_MobileData user_MobileData = apiResult.new User_MobileData("true", mMobileDataInfo.get("UserId").toString(), mMobileDataInfo.get("DeviceId").toString(), mMobileDataInfo.get("Enabled").toString(), mMobileDataInfo.get("ActionDate").toString());
        Call<ApiResult.User_MobileData> call1 = apiInterface.PostMobileData(user_MobileData);
        final String finalColumnId = sColumnId;
        call1.enqueue(new Callback<ApiResult.User_MobileData>() {
            @Override
            public void onResponse(Call<ApiResult.User_MobileData> call, Response<ApiResult.User_MobileData> response) {
                try {
                    ApiResult.User_MobileData iData = response.body();
                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                        ContentValues newValues = new ContentValues();
                        newValues.put("SyncStatus", "false");
                        sql.update("User_MobileData", newValues, "Id=" + sColumnId, null);

                    } else {
                        ContentValues newValues = new ContentValues();
                        newValues.put("SyncStatus", "true");
                        sql.update("User_MobileData", newValues, "Id=" + sColumnId, null);
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

    private void clearPreferences() {
        try {
            // clearing app data
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear net.mzi.trackengine");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    User_Location getLocation() {
        User_Location user_location = new User_Location();
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

                // no network provider is enabled
            } else {

                // First get location from Network Provider
                if (isNetworkEnabled) {
                    Log.e("Network", "Network");
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return null;
                        }
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            user_location.Latitude = location.getLatitude();
                            user_location.Longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {

                        Log.e("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                user_location.Latitude = location.getLatitude();
                                user_location.Longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user_location;
    }

    public void setShowAlert() {
        Log.e(TAG, "setShowAlert: setShowAlert");
        showAlert.setVisibility(View.VISIBLE);
    }

    public void setHideAlert() {
        try {
            showAlert.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
        }
    }

    public static int getBatteryPercentage(Context context) {

        try {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            float batteryPct = level / (float) scale;

            return (int) (batteryPct * 100);
        } catch (Exception e) {
            return 50;
        }
    }
}
