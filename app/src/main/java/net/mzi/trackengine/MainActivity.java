package net.mzi.trackengine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;

import net.mzi.trackengine.adapter.MainActivityAdapter;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;
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
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mzi.trackengine.SessionManager.KEY_USERID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    GpsLocationReceiver gpsLocationReceiver;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    ApiInterface apiInterface;
    Map<String, String> mMobileDataInfo = new HashMap<String, String>();
    User_Location user_location = new User_Location();
    String sAddressLine, sCity, sState, sCountry, sPostalCode, sKnownName, sPremises, sSubLocality, sSubAdminArea;
    Map<String, String> appCheckInInfo = new HashMap<String, String>();
    static Intent batteryIntent, locationIntent;
    static PendingIntent batteryPendingIntent, locationPendingIntent;
    public static AlarmManager batteryAlarmManager, locationAlarmManager;
    RelativeLayout timerlayout;
    //    LinearLayout lTimerLayout;
    static RelativeLayout RLay;
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView timer, currTime;
    static RelativeLayout newtkt;
    String currentDateTimeString;
    static String currentDateTimeStringCheckIN = "";
    TextView viewAll, h_uname;
    private Button btn_check_in_out;
    static TextView showAlert;
    LocationManager locationManager;
    boolean isGPSEnabled = false;
    Location location;
    Double latitude, longitude;
    //    boolean isNetworkEnabled = false;
    FirebaseDatabase databaseFirebase = FirebaseDatabase.getInstance();
    DatabaseReference drRef;

    static SQLiteDatabase sql;
    Cursor cquery;
    String sDeviceId, sDepartment;
    static String LOGINID = "";
    //    private RecyclerView.LayoutManager mLayoutManager;
    static MainActivityAdapter mAdapter = null;
    public static final int TASK = 0;
    static RecyclerView mRecyclerView;
    String sCompanyId, nh_uname, sParentCompanyId;
    public List<String> mDataset = new ArrayList<String>();
    public List<Integer> mCardColor = new ArrayList<Integer>();
    public List<String> mCardBgClr = new ArrayList<>();
    public List<Integer> mDatasetTypes = new ArrayList<Integer>();
    public List<String> mDatasetCount = new ArrayList<String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //SharedPreferences timerSharedPreference;
//    SharedPreferences.Editor editorForTimer;
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
    String checkInOutTime = "";
    private boolean sCheckInStatus;
    private SessionManager session;
    public TextView tv_location;
    public TextView txt_sync_all, txt_check_in_out_count, txt_locations_count, txt_issues_count, txt_battery_count;
    public RelativeLayout rl_sync_check_in_out, rl_sync_locations, rl_sync_issue_status, rl_sync_battery;
    public ProgressBar progress_sync_progress_in_out, progress_sync_locations, progress_sync_status, progress_sync_battery;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        new FetchStatus().execute();
        setContentView(R.layout.activity_main);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

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
                        MyApp.setSharedPrefString("lastUserId", LOGINID);
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
//                        sql.delete("Issue_Detail", null, null);
//                        sql.delete("FirebaseIssueData", null, null);
                        //sql.delete("Issue_Status", null, null);
                        session.logoutUser();
                        MyApp.getApplication().writeMessage(new ArrayList<TicketInfoClass>());
                        //clearPreferences();
                        MyApp.getApplication().writeUser(null);

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


        Firebase.setAndroidContext(this);
        session = new SessionManager(getApplicationContext());
        tv_location = findViewById(R.id.tv_location);
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            if (mCurrentLocation != null) {
                String add = getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                if (!add.isEmpty())
                    tv_location.setText(add);
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SessionManager s = new SessionManager(getApplicationContext());

        HashMap<String, String> map = new HashMap<>();
        map = s.getUserDetails();
        LOGINID = map.get("userid");
        btn_check_in_out = findViewById(R.id.btn_check_in_out);
        mSwipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        timerlayout = findViewById(R.id.timerLayout);
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        timer = findViewById(R.id.textView11);
        currTime = findViewById(R.id.textView4);
        viewAll = findViewById(R.id.viewAll);
        showAlert = findViewById(R.id.viewAlert);
        RLay = findViewById(R.id.remaininglayout);
        newtkt = findViewById(R.id.newtkt);
        showAlert.setVisibility(View.VISIBLE);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkInOutTime = MyApp.getSharedPrefString("CheckedInTime");
        sCheckInStatus = MyApp.getStatus("CheckedInStatus");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        h_uname = headerView.findViewById(R.id.header_username);
        h_uname.setText(nh_uname);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        mRecyclerView = findViewById(R.id.task_view);
        GridLayoutManager gaggeredGridLayoutManager;
        gaggeredGridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
        mRecyclerView.setLayoutManager(gaggeredGridLayoutManager);

        refreshData(false);

        try {
            ApiResult.IssueStatus.lstDetails data[] = MyApp.getApplication().readIssuesStatusList();
            Log.e("issuesStatusListSize", data.length + "");
//            for (int i = 0; i <MyApp.getApplication().readIssuesStatusList().length ; i++) {
//                Log.d("issuesStatusListSize",data[i].Id);
//                Log.d("issuesStatusListSize",data[i].StatusName);
//            }
        } catch (Exception e) {
        }
        rl_address = findViewById(R.id.rl_address);
        rl_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FullScreenMap.class));
            }
        });

        txt_sync_all = findViewById(R.id.txt_sync_all);
        rl_sync_check_in_out = findViewById(R.id.rl_sync_check_in_out);
        rl_sync_locations = findViewById(R.id.rl_sync_locations);
        rl_sync_issue_status = findViewById(R.id.rl_sync_issue_status);
        rl_sync_battery = findViewById(R.id.rl_sync_battery);
        progress_sync_progress_in_out = findViewById(R.id.progress_sync_progress_in_out);
        progress_sync_locations = findViewById(R.id.progress_sync_locations);
        progress_sync_status = findViewById(R.id.progress_sync_status);
        progress_sync_battery = findViewById(R.id.progress_sync_battery);
        txt_check_in_out_count = findViewById(R.id.txt_check_in_out_count);
        txt_locations_count = findViewById(R.id.txt_locations_count);
        txt_issues_count = findViewById(R.id.txt_issues_count);
        txt_battery_count = findViewById(R.id.txt_battery_count);


        txt_sync_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callable) {
                    callable = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            callable = true;
                        }
                    }, 10000);
                    syncCheckInOut();
                    syncLocations();
                    if (!MyApp.getStatus("isTicketUpdating"))
                        try {
                            if (!stringIterator.hasNext())
                                syncIssues();
                        } catch (Exception ee) {
                            syncIssues();
                        }
                    syncBattery();
                }

            }
        });
        rl_sync_check_in_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncCheckInOut();
            }
        });
        rl_sync_locations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncLocations();
            }
        });
        rl_sync_issue_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MyApp.getStatus("isTicketUpdating"))
                    try {
                        if (!stringIterator.hasNext())
                            syncIssues();
                    } catch (Exception ee) {
                        syncIssues();
                    }
            }
        });
        rl_sync_battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                syncBattery();
            }
        });

        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:net.mzi.trackengine"));
            startActivity(intent);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.RECEIVE_SMS)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, 1);
            }
        }

        startService(new Intent(this, ReadMessageService.class));

        db.collection("SOMapp").document("appInfo").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {

                    if (!snapshot.getBoolean("alternative"))
                        if (snapshot.getBoolean("forceUpdate")) {
                            try {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                int versionCode = pInfo.versionCode;
                                int serverVersionCode = snapshot.getLong("appVersionCode").intValue();
                                if (versionCode >= serverVersionCode) {
                                    try {
                                        if (updateDialog != null) {
                                            updateDialog.dismiss();
                                        }
                                    } catch (Exception ee) {
                                    }
                                } else {
                                    try {
                                        if (updateDialog != null) {
                                            updateDialog.dismiss();
                                        }
                                    } catch (Exception ee) {
                                    }
                                    showUpdateAppDialog(true, serverVersionCode);
                                }
                            } catch (PackageManager.NameNotFoundException ee) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                int versionCode = pInfo.versionCode;
                                int serverVersionCode = snapshot.getLong("appVersionCode").intValue();
                                if (versionCode >= serverVersionCode) {
                                    try {
                                        if (updateDialog != null) {
                                            updateDialog.dismiss();
                                        }
                                    } catch (Exception ee) {
                                    }
                                } else {
                                    try {
                                        if (updateDialog != null) {
                                            updateDialog.dismiss();
                                        }
                                    } catch (Exception ee) {
                                    }
                                    showUpdateAppDialog(false, serverVersionCode);
                                }
                            } catch (PackageManager.NameNotFoundException ee) {
                                e.printStackTrace();
                            }
                        }
                    else {
                        try {
                            if (updateDialog != null) {
                                updateDialog.dismiss();
                            }
                        } catch (Exception ee) {
                        }
                    }
                }
            }
        });
        updateCounter(this, false);
        gpsLocationReceiver = new GpsLocationReceiver();
        registerReceiver(new NetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(gpsLocationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

//        new FetchStatus().execute();
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Dialog updateDialog = null;

    private void showUpdateAppDialog(boolean isForce, final int version) {
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setTitle("Update App").setMessage("New update is available to download on playstore, please updated this app for better " +
                "experience.\nThank you").setCancelable(isForce).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                MyApp.setStatus("resetAppData", true);
                MyApp.setSharedPrefInteger("updatingVersion", version);
//                clearPreferences();
                finish();
            }
        });
        if (!isForce) {
            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        updateDialog = b.create();
        try {
            updateDialog.show();
        } catch (Exception e) {
        }
    }

    private RelativeLayout rl_address;

    private void sendBatteryCheckinLevel(int batLevel) {
        Map<String, String> batteryInfo = new HashMap<>();
        batteryInfo.put("UserId", LOGINID);
        batteryInfo.put("DeviceId", sDeviceId);
        batteryInfo.put("Battery", String.valueOf(batLevel));
        batteryInfo.put("ActivityDate", currentDateTimeString);
        batteryInfo.put("AutoCaptured", "false");
        batteryInfo.put("RealTimeUpdate", "true");
        batteryInfo.put("syncStatus", "false");
        ServiceBattery serviceBattery = new ServiceBattery();
        Map<String, Map<String, String>> batteryMap = MyApp.getApplication().readBatteryHistory();
        batteryMap.put(currentDateTimeString, batteryInfo);
        MyApp.getApplication().writeBatteryHistory(batteryMap);

        MyApp.setSharedPrefLong("BAT", 0);
        serviceBattery.BatteryOperation(batteryInfo, getApplicationContext(), true);
    }

    public static void removeTkt() {
        try {
            MainActivity.newtkt.setVisibility(View.GONE);
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonLayoutParams.setMargins(0, 130, 0, 0);
//            RLay.setLayoutParams(buttonLayoutParams);
        } catch (Exception e) {
        }
    }

//    public void showTkt() {
////        MainActivity.newtkt.setVisibility(View.VISIBLE);
//        startActivity(new Intent(MainActivity.this, NewTaskActivity.class));
//    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        sCheckInStatus = MyApp.getStatus("CheckedInStatus");

        if (sCheckInStatus) {
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
            locationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, locationTime, 60 * 1000, locationPendingIntent);
//           }
        } else {
            locationIntent = new Intent(MainActivity.this, ServiceLocation.class);
            locationPendingIntent = PendingIntent.getService(MainActivity.this, 1, locationIntent, 0);
            locationAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            locationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, locationTime, 60 * 1000, locationPendingIntent);
        }

        try {
            startService(new Intent(getApplicationContext(), ServiceLocation.class));
        } catch (Exception e) {
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
            MyApp.showMassage(MainActivity.this, "Coming Soon");
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
            MyApp.getApplication().writeMessage(new ArrayList<TicketInfoClass>());
            if (!MyApp.isConnectingToInternet(MainActivity.this)) {
                android.support.v7.app.AlertDialog.Builder b = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                b.setMessage("Please connect to a working internet connection");
                b.setTitle("Alert!");
                b.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableLocationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivityForResult(enableLocationIntent, 10);
                    }
                }).create().show();
                return false;
            }

            String versionName = "";
            String versionCode = "";
            String AndroidVersion = "";
            StringBuilder builder = new StringBuilder();
            builder.append("android : ").append(Build.VERSION.RELEASE);

            Field[] fields = Build.VERSION_CODES.class.getFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                int fieldValue = -1;

                try {
                    fieldValue = field.getInt(new Object());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (fieldValue == Build.VERSION.SDK_INT) {
                    builder.append(" : ").append(fieldName).append(" : ");
                    builder.append("sdk=").append(fieldValue);
                }
            }
            AndroidVersion = "OS: " + builder.toString();
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = pInfo.versionName;
                versionCode = pInfo.versionCode + "";
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            Map<String, String> postLogin = new HashMap<String, String>();
            Date cDate = new Date();
            //LOGINID=map.get("userid");
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            MyApp.setSharedPrefString("lastUserId", LOGINID);
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
                appCheckInInfo.put("AndroidVersion", AndroidVersion);
                appCheckInInfo.put("AppVersionCode", versionCode);
                appCheckInInfo.put("AppVersionName", versionName);
            }
            new IsLogin(sPostLogin, 0, "0").execute();
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
            session.logoutUser();
            MyApp.getApplication().writeUser(null);
//            sql.delete("Issue_Detail", null, null);
//            sql.delete("FirebaseIssueData", null, null);

            MyApp.setSharedPrefString("CheckedInTime", "");
            MyApp.setStatus("CheckedInStatus", false);
            editor.commit();

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
                ServiceDataUpdateFirstFragment serviceDataUpdateFirstFragment = new ServiceDataUpdateFirstFragment(this);
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
        } else if (id == R.id.action_refresh) {

            if (txt_issues_count.getText().toString().equals("0")) {

                MyApp.showMassage(this, "Refreshing...");
                refreshData(true);
//                new FetchStatus().execute();
                refreshResume();

            } else {
                MyApp.popMessage("Alert!", "Please sync your offline tickets status data first.", MainActivity.this);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshResume() {
        MyApp.setStatus("isNewTaskOpen", false);

        int checkInOutCount = MyApp.getApplication().readCheckInOutData().keySet().size();
        int locationsCount = MyApp.getApplication().readLocationData().keySet().size();
        int issuesCount = MyApp.getApplication().readTicketsIssueHistory().keySet().size();
        int batteryCount = MyApp.getApplication().readBatteryHistory().keySet().size();

        txt_battery_count.setText(batteryCount + "");
        if (!MyApp.getStatus("isTicketUpdating"))
            txt_issues_count.setText(issuesCount + "");
        txt_locations_count.setText(locationsCount + "");
        txt_check_in_out_count.setText(checkInOutCount + "");

        progress_sync_locations.setVisibility(View.GONE);
        progress_sync_status.setVisibility(View.GONE);
        progress_sync_battery.setVisibility(View.GONE);
        progress_sync_progress_in_out.setVisibility(View.GONE);

        try {
            txt_locations_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_check_in_out_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_issues_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_battery_count.setBackground(getDrawable(R.drawable.sync_orange));
        } catch (NoSuchMethodError e) {
            txt_locations_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            txt_check_in_out_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            if (!MyApp.getStatus("isTicketUpdating"))
                txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            txt_battery_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
        } catch (Exception e) {
        }

        if (callable) {

            syncLocations();
            syncCheckInOut();
            syncBattery();
            if (!MyApp.getStatus("isTicketUpdating"))
                try {
                    if (!stringIterator.hasNext())
                        syncIssues();
                } catch (Exception ee) {
                    syncIssues();
                }
            callable = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callable = true;
                }
            }, 10000);
        }

//        if (checkInOutCount == 0)
        btn_check_in_out.setText("Refreshing...");
        btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));
        new CheckInInfo().execute();

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

//        } else if (id == R.id.nav_mytkts) {
//            // Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
//            Intent i = new Intent(MainActivity.this, InternalIssueListing.class);
//            startActivity(i);
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
//            new FetchStatus().execute();
            Intent i = getIntent();
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);
            startActivity(i);
            overridePendingTransition(0, 0);
        } else if (id == R.id.nav_offlinesync) {
            callable = true;
            // Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, OfflineSyncInfo.class);
            startActivity(i);
        } else if (id == R.id.nav_sendLog) {
            MyApp.showMassage(MainActivity.this, "Coming Soon");
//            Toast.makeText(getApplicationContext(), "Coming Soon", Toast.LENGTH_LONG).show();
            /*Intent i=new Intent(MainActivity.this,OfflineSyncInfo.class);
            startActivity(i);*/
        } else if (id == R.id.nav_aboutus) {
            Intent i = new Intent(MainActivity.this, AboutUs.class);
            startActivity(i);

        } else if (id == R.id.nav_contactus) {
            MyApp.showMassage(MainActivity.this, "Coming Soon");
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
    protected void onPause() {
        super.onPause();
        MyApp.activityPaused();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
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
//        updateMarkers();
        String add = getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (!add.isEmpty())
            tv_location.setText(add);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
//                Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
//                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // Turn on the My Location layer and the related control on the map.
//        updateLocationUI();
//        // Add markers for nearby places.
//        updateMarkers();
//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//            public View getInfoWindow(Marker marker) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
//
//                TextView title = (infoWindow.findViewById(R.id.title));
//                title.setText(marker.getTitle());
//
//                TextView snippet = (infoWindow.findViewById(R.id.snippet));
//                snippet.setText(marker.getSnippet());
//
//                return infoWindow;
//            }
//        });
//
//        if (mCameraPosition != null) {
//            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//        } else if (mCurrentLocation != null) {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                    new LatLng(mCurrentLocation.getLatitude(),
//                            mCurrentLocation.getLongitude()), DEFAULT_ZOOM));
//            mMap.getUiSettings().setZoomControlsEnabled(true);
//        } else {
//            Log.d("TAG", "Current location is null. Using defaults.");
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//            mMap.getUiSettings().setMyLocationButtonEnabled(false);
//            mMap.getUiSettings().setZoomControlsEnabled(true);
//        }
//
//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//                user_location.Latitude = location.getLatitude();
//                user_location.Longitude = location.getLongitude();
//                MyApp.setSharedPrefString("lat", user_location.Latitude + "");
//                MyApp.setSharedPrefString("lng", user_location.Longitude + "");
////                mCameraPosition = new CameraPosition.Builder().target(new LatLng(location.getLatitude(),
////                        location.getLongitude())).build();
////                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//            }
//        });
//    }

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
                return;
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
        if (mCurrentLocation != null) {
            String add = getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            if (!add.isEmpty())
                tv_location.setText(add);
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
                } catch (Exception e) {
                    e.printStackTrace();
                    urlConnection.disconnect();
                    return null;
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
                            Log.e("MainActivity", object.getString("StatusName") + ":" + object.getString("Id"));
                            String sAllParentStatuses = object.getString("ParentStatuses");
                            List<String> listsAllParentStatuses = Arrays.asList(sAllParentStatuses.split(","));
                            for (int j = 0; j < listsAllParentStatuses.size(); j++) {
                                sql.execSQL("INSERT INTO Issue_StatusHiererchy(StatusId,ParentStatus,WaitForEntry,ActionDate,RedirectToStatus)VALUES" +
                                        "('" + object.getString("Id") + "','" + listsAllParentStatuses.get(j).trim() + "','0','0','0')");
                                Log.e("MainActivity ", object.getString("StatusName") + ":" + listsAllParentStatuses.get(j).trim());
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
                Log.e("postLoginData", result);
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

    public void appCheckINOperation(final Map<String, String> appCheckInInfo, final String realtimeUpdate) throws Exception {
        Map<String, Map<String, String>> map = MyApp.getApplication().readCheckInOutData();
        if (!map.containsKey("ActivityDate")) {
            map.put(appCheckInInfo.get("ActivityDate"), appCheckInInfo);
            MyApp.getApplication().writeCheckInOutData(map);
        }

        //sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        final ApiResult apiResult = new ApiResult();
        final ApiResult.UserCheckInOut userCheckInOut = apiResult.new
                UserCheckInOut(realtimeUpdate, appCheckInInfo.get("UserId").toString(),
                appCheckInInfo.get("DeviceId").toString(), appCheckInInfo.get("IsCheckedIn").toString(),
                appCheckInInfo.get("ActivityDate").toString());
        Call<ApiResult.UserCheckInOut> call1 = apiInterface.PostCheckIn(userCheckInOut);
        call1.enqueue(new Callback<ApiResult.UserCheckInOut>() {
            @Override
            public void onResponse(Call<ApiResult.UserCheckInOut> call, Response<ApiResult.UserCheckInOut> response) {
                ApiResult.UserCheckInOut iData = response.body();
                try {
                    if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Server response error. Data has been saved to offline, it will be synced after some time.").setTitle("Response from Server")
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
//                    MyApp.getApplication().writeCheckInOutData(map);
//                    sql.update("User_AppCheckIn", newValues, "Id=" + sColumnId, null);
                    } else {
                        if (realtimeUpdate.equals("true"))
                            MyApp.showMassage(MainActivity.this, "CheckIn-Out Info sent successfully!!!");
                        ContentValues newValues = new ContentValues();
                        newValues.put("SyncStatus", "true");
                        if (iData.resData.Status.equals("false")) {
                            MyApp.popMessage("Error", iData.resData.Message, MainActivity.this);
                            btn_check_in_out.setText("Refreshing...");
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                            new CheckInInfo().execute();
                        }
                        Map<String, Map<String, String>> map = MyApp.getApplication().readCheckInOutData();
                        map.remove(appCheckInInfo.get("ActivityDate"));
                        MyApp.getApplication().writeCheckInOutData(map);
                        txt_check_in_out_count.setText(map.keySet().size() + "");
//                    sql.update("User_AppCheckIn", newValues, "Id=" + sColumnId, null);
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(Call<ApiResult.UserCheckInOut> call, Throwable t) {
                try {
                    call.cancel();
                    ContentValues newValues = new ContentValues();
                    newValues.put("SyncStatus", "false");
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
                } catch (Exception e) {
                }
            }
        });
    }

    public void updateCounter(Context mContext, boolean isSwipe) {
        mDatasetCount.clear();
        mCardBgClr.clear();
        mCardColor.clear();
        mDataset.clear();
        mDatasetTypes.clear();
        try {
            Cursor cquery1 = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);//new
            mDatasetCount.add(String.valueOf(cquery1.getCount()));
            try {
                if (cquery1.getCount() == 0) {
                    Map<String, TicketInfoClass> map = MyApp.getApplication().readTicketCapture();
                    for (String s : map.keySet()) {
                        if (map.get(s).isCaptured()) {
                            map.remove(s);
                        }
                    }
                    MyApp.getApplication().writeTicketCapture(map);
                }
            } catch (Exception eee) {
            }
            mCardColor.add(R.drawable.cardbk_purple);
            mCardBgClr.add("Purple");
            mDataset.add("New");
            mDatasetTypes.add(0);
            cquery1.close();
            Cursor cquery2 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 1", null);
            mDatasetCount.add(String.valueOf(cquery2.getCount()));
            mCardColor.add(R.drawable.cardbk_orange);
            mCardBgClr.add("Orange");
            mDataset.add("Accepted");
            mDatasetTypes.add(0);
            cquery2.close();
            Cursor cquery3 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 2", null);
            mDatasetCount.add(String.valueOf(cquery3.getCount()));
            mCardColor.add(R.drawable.cardbk_blue);
            mCardBgClr.add("Blue");
            mDataset.add("Attended");
            mDatasetTypes.add(0);
            cquery3.close();


            Cursor cquery5 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 4", null);
            mDatasetCount.add(String.valueOf(cquery5.getCount()));
            mCardColor.add(R.drawable.cardbk_green);
            mCardBgClr.add("Red");
            mDataset.add("Scheduled");
            mDatasetTypes.add(0);
            cquery5.close();

            Cursor cquery4 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 3", null);
            mDatasetCount.add(String.valueOf(cquery4.getCount()));
            mCardColor.add(R.drawable.cardbk_green);
            mCardBgClr.add("Green");
            mDataset.add("Resolved");
            mDatasetTypes.add(0);
            cquery4.close();

            Cursor cquery6 = sql.rawQuery("select * from Issue_Detail where IsAccepted = 5", null);
            mDatasetCount.add(String.valueOf(cquery6.getCount()));
            mCardColor.add(R.drawable.cardbk_green);
            mCardBgClr.add("Primary dark");
            mDataset.add("Pending Rescheduled");
            mDatasetTypes.add(0);
            cquery6.close();

            if (isSwipe && mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            } else {
                mRecyclerView.removeAllViews();
                MainActivityAdapter maCounter = new MainActivityAdapter(mDataset, mDatasetCount, mCardColor, mDatasetTypes, mContext, mCardBgClr);
                mRecyclerView.setAdapter(maCounter);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //maCounter.updateCounter();

    }

    public void PushMobileData(Map mMobileDataInfo, final Context ctx, final long key) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        final ApiResult apiResult = new ApiResult();
        final ApiResult.User_MobileData user_MobileData = apiResult.new User_MobileData("true", mMobileDataInfo.get("UserId").toString(), mMobileDataInfo.get("DeviceId").toString(), mMobileDataInfo.get("Enabled").toString(), mMobileDataInfo.get("ActionDate").toString());
        Call<ApiResult.User_MobileData> call1 = apiInterface.PostMobileData(user_MobileData);
        call1.enqueue(new Callback<ApiResult.User_MobileData>() {
            @Override
            public void onResponse(Call<ApiResult.User_MobileData> call, Response<ApiResult.User_MobileData> response) {
                try {
                    ApiResult.User_MobileData iData = response.body();
                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {

                    } else {
                        Map<Long, Map<String, String>> mobileData = MyApp.getApplication().readMobileData();
                        try {
                            mobileData.remove(key);
                            MyApp.getApplication().writeMobileData(mobileData);
                        } catch (Exception e) {
                        }
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
//            isNetworkEnabled = locationManager
//                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled) {

                // no network provider is enabled
            } else {

                // First get location from Network Provider
//                if (isNetworkEnabled) {
//                    Log.e("Network", "Network");
//                    if (locationManager != null) {
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return null;
//                        }
//                        location = locationManager
//                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if (location != null) {
//                            user_location.Latitude = location.getLatitude();
//                            user_location.Longitude = location.getLongitude();
//                        }
//                    }
//                }
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
            showAlert.setVisibility(View.GONE);
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

    public void refreshData(boolean isRefresh) {
        if (isRefresh) {
            setShowAlert();
            Firstfrag fragment = (Firstfrag) getSupportFragmentManager().findFragmentById(R.id.fragment);
            fragment.NewTicketsInfo(fragment.mTicketIdList);
        }

        checkInOutTime = MyApp.getSharedPrefString("CheckedInTime");
        sCheckInStatus = MyApp.getStatus("CheckedInStatus");

        Map<Long, Boolean> map = MyApp.getApplication().readCheckInOutDataHistory();
        List<Long> historyData = new ArrayList<>();
        for (Long l : map.keySet()) {
            historyData.add(l);
        }

        if (historyData.size() > 0) {
            Collections.sort(historyData, Collections.<Long>reverseOrder());
            try {

                sCheckInStatus = map.get(historyData.get(0));
                checkInOutTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(historyData.get(0)));

                MyApp.setStatus("CheckedInStatus", sCheckInStatus);
                MyApp.setSharedPrefString("CheckedInTime", checkInOutTime);
            } catch (Exception e) {
            }
        }

        if (sCheckInStatus) {
            btn_check_in_out.setText("Checked-IN\n" + checkInOutTime);
            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.green));

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
        } else {
            btn_check_in_out.setText("Checked-OUT\n" + checkInOutTime);
            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.red));
            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(12345);

        }

        try {
            if (!isRefresh && isOnline(MainActivity.this)) {
                btn_check_in_out.setText("Refreshing...");
                btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));
            }
        } catch (Exception e) {
        }
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TaskActivity.class);
                i.putExtra("cardpos", "-1");
                startActivity(i);
            }
        });

        mDataset = new ArrayList<>();
        mDatasetTypes = new ArrayList<>();
        mDatasetCount = new ArrayList<>();
        mCardColor = new ArrayList<>();
        mCardBgClr = new ArrayList<>();

        mDataset.add("New");
        mDataset.add("Accepted");
        mDataset.add("Attended");
        mDataset.add("Scheduled");
        mDataset.add("Resolved");
        mDataset.add("Pending Scheduled");

        mCardColor.add(R.drawable.cardbk_purple);
        mCardColor.add(R.drawable.cardbk_orange);
        mCardColor.add(R.drawable.cardbk_blue);
        mCardColor.add(R.drawable.cardbk_green);
        mCardColor.add(R.drawable.cardbk_green);
        mCardColor.add(R.drawable.cardbk_green);

        mCardBgClr.add("Purple");
        mCardBgClr.add("Orange");
        mCardBgClr.add("Blue");
        mCardBgClr.add("Red");
        mCardBgClr.add("Green");
        mCardBgClr.add("Primary Dark");

        mDatasetTypes.add(TASK);
        mDatasetTypes.add(TASK);
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
        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 4", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));
        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 5", null);
        mDatasetCount.add(String.valueOf(cquery.getCount()));
        cquery.close();

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter = new MainActivityAdapter(mDataset, mDatasetCount, mCardColor, mDatasetTypes, MainActivity.this, mCardBgClr);
            mRecyclerView.setAdapter(mAdapter);
        }


        btn_check_in_out.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: ");


                if (!MyApp.isConnectingToInternet(MainActivity.this)) {
                    android.support.v7.app.AlertDialog.Builder b = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                    b.setMessage("Please connect to a working internet connection");
                    b.setTitle("Alert!");
                    b.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableLocationIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivityForResult(enableLocationIntent, 10);
                        }
                    }).create().show();
                    return;
                }

                Map<String, Map<String, String>> mapl = MyApp.getApplication().readCheckInOutData();
                for (String key : mapl.keySet()) {
                    mapl.get(key).put("RealTimeUpdate", "false");
                    try {
                        appCheckINOperation(mapl.get(key), "false");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!MyApp.isLocationEnabled(MainActivity.this)) {
                    android.support.v7.app.AlertDialog.Builder b = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                    b.setMessage("GPS is not enabled");
                    b.setTitle("Alert!");
                    b.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableLocationIntent, 10);
                        }
                    }).create().show();
                    return;
                }
                if (!sCheckInStatus) {
                    Map<Long, Boolean> historyMap = new HashMap<>();
                    historyMap.put(System.currentTimeMillis(), true);
                    MyApp.getApplication().writeCheckInOutDataHistory(historyMap);
                    btn_check_in_out.setEnabled(false);
                    btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            btn_check_in_out.setEnabled(true);
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.green));
                        }
                    }, 5000);
                    Date cDate = new Date();
                    currentDateTimeStringCheckIN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    MyApp.setSharedPrefString("CheckedInTime", currentDateTimeStringCheckIN);

                    btn_check_in_out.setText("Checked-IN\n" + currentDateTimeStringCheckIN);
                    sCheckInStatus = true;
                    MyApp.setStatus("CheckedInStatus", true);
                    editor.putString("CheckedInDuration", currentDateTimeStringCheckIN);
                    editor.commit();
                    Log.e(TAG, "onClick: ");

                    currTime.setText("CheckedIN: " + currentDateTimeStringCheckIN);
                    appCheckInInfo.put("ActivityDate", currentDateTimeStringCheckIN);
                    isCheckIn = "true";
//                    MyApp.getApplication().writeLocationData(new HashMap<String, Map<String, String>>());
                    setData();
                    checkInOutClickEvent();
                } else {

                    AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                    b.setTitle("Alert").setMessage("Do you want to check-out?")
                            .setPositiveButton("Check-out", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    btn_check_in_out.setEnabled(false);
                                    Map<Long, Boolean> historyMap = new HashMap<>();
                                    historyMap.put(System.currentTimeMillis(), false);
                                    MyApp.getApplication().writeCheckInOutDataHistory(historyMap);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            btn_check_in_out.setEnabled(true);
                                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.red));
                                        }
                                    }, 5000);
//                                    btn_check_in_out.setText("Checked-IN\n" + currentDateTimeStringCheckIN);
                                    btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));


                                    Date cDate = new Date();
                                    currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                                    MyApp.setSharedPrefString("CheckedInTime", currentDateTimeString);
                                    MyApp.setStatus("CheckedInStatus", false);
                                    sCheckInStatus = false;
                                    editor.commit();
                                    btn_check_in_out.setText("Checked-OUT\n" + currentDateTimeString);
                                    try {
                                        batteryAlarmManager.cancel(batteryPendingIntent);
                                        locationAlarmManager.cancel(locationPendingIntent);
                                    } catch (Exception e) {
                                    }

                                    isCheckIn = "false";
                                    appCheckInInfo.put("ActivityDate", currentDateTimeString);
                                    timer.setText("00:00:00");


                                    NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    nMgr.cancel(12345);


                                    try {
                                        stopService(new Intent(getApplicationContext(), ServiceLocation.class));
                                    } catch (Exception e) {
                                    }

                                    //tAt.setTextColor(getResources().getColor(R.color.red));
                                    checkInOutClickEvent();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }

            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mSwipeRefreshLayout.setProgressViewOffset(false, 0, 200);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
                                    if (MyApp.getSharedPrefString("lat").isEmpty()) {
                                        user_location.Latitude = 0.0;
                                        user_location.Longitude = 0.0;
                                    } else {
                                        try {
                                            user_location.Latitude = Double.parseDouble(MyApp.getSharedPrefString("lat"));
                                            user_location.Longitude = Double.parseDouble(MyApp.getSharedPrefString("lng"));
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
                                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
                                locMap.put(currentDateTimeString, locationInfo);
                                MyApp.getApplication().writeLocationData(locMap);
                                Log.e("Location insertion", "Inserted by MainActivity at 863");

                                if (user_location.Latitude == 0.0 || user_location.Longitude == 0) {
//                                    Toast.makeText(getApplicationContext(), "Location could not captured, check your GPS!!!", Toast.LENGTH_LONG).show();
                                    MyApp.showMassage(MainActivity.this, "Location could not captured, check your GPS!!!");
                                    try {
                                        user_location.Latitude = Double.parseDouble(MyApp.getSharedPrefString("lat"));
                                        user_location.Longitude = Double.parseDouble(MyApp.getSharedPrefString("lng"));
                                    } catch (Exception e) {
                                    }
                                    LocationOperationOffline(locationInfo, getApplicationContext(), currentDateTimeString, false, true);
                                } else {
                                    MyApp.showMassage(MainActivity.this, "Location sent successfully!!!");
                                    LocationOperationOffline(locationInfo, getApplicationContext(), currentDateTimeString, false, true);
                                }
                                //MainActivity.this.registerReceiver(broadcastreceiver,intentfilter);
                            }
                        }

                    }
                }, 4000);


            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TicketCreation.class);
                startActivity(i);
            }
        });
    }

    private void checkInOutClickEvent() {
        Map<String, String> locationInfo = new HashMap<String, String>();
        gps = new Gps(getApplicationContext());
        if (gps.canGetLocation()) {
            Date cDate = new Date();
            currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
            if (gps.canGetLocation) {
                user_location = getLocation();
            } else {
                if (MyApp.getSharedPrefString("lat").isEmpty()) {
                    user_location.Latitude = 0.0;
                    user_location.Longitude = 0.0;
                } else {
                    try {
                        user_location.Latitude = Double.parseDouble(MyApp.getSharedPrefString("lat"));
                        user_location.Longitude = Double.parseDouble(MyApp.getSharedPrefString("lng"));
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
                Map<String, Map<String, String>> map = MyApp.getApplication().readCheckInOutData();
                map.put(currentDateTimeString, locationInfo);
                MyApp.getApplication().writeLocationData(map);
                LocationOperationOffline(locationInfo, getApplicationContext(), currentDateTimeString, true, true);
            } catch (Exception e) {
                LocationOperationOffline(locationInfo, getApplicationContext(), currentDateTimeString, true, true);
            }
//                    Toast.makeText(getApplicationContext(), "Location sent successfully!!!", Toast.LENGTH_LONG).show();

        } else {
            MyApp.popMessage("Alert!", "Location not captured, make sure your phone has GPS enabled.", this);
        }

        String jsonString = new Gson().toJson(locationInfo);
        Log.e(TAG, "run: " + jsonString);
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
//                sql.execSQL("INSERT INTO User_AppCheckIn(UserId,IsCheckIn,ActionDate,SyncStatus)VALUES" +
//                        "('" + appCheckInInfo.get("UserId") + "','" + appCheckInInfo.get("IsCheckedIn") + "','" + appCheckInInfo.get("ActivityDate") + "','-1')");
//                Cursor cquery = sql.rawQuery("select * from User_AppCheckIn ", null);
        String sColumnId = null;
//                if (cquery.getCount() > 0) {
//                    cquery.moveToLast();
//                    sColumnId = cquery.getString(0).toString();
//                }
//                cquery.close();
        try {
            appCheckINOperation(appCheckInInfo, "true");
//                    appCheckINOperation(appCheckInInfo, sColumnId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);

        try {
            int batLevel = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
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
        } catch (NoSuchMethodError ee) {
            try {
                sendBatteryCheckinLevel(getBatteryPercentage(MainActivity.this));
            } catch (Exception eee) {
            }
        }
    }


//    private void callPostLogin() {
//        Map<String, String> postLogin = new HashMap<String, String>();
//        Date cDate = new Date();
//        //LOGINID=map.get("userid");
//        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//
//        postLogin.put("UserId", LOGINID);
//        postLogin.put("IsLogin", "true");
//        postLogin.put("ActionDate", currentDateTimeString);
//        postLogin.put("DeviceId", sDeviceId);
//        postLogin.put("RealTimeUpdate", "true");
//        String sPostLogin = new Gson().toJson(postLogin);
//
//        new IsLogin(sPostLogin, 0, "0").execute();
//    }

    private class CheckInInfo extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Date cDate = new Date();
                currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
//                http://trackengine.mzservices.net/api/Post/GetUserCheckIn?iUserId=7125
                String API_URL = PostUrl.sUrl + "GetUserCheckIn?iUserId=" + LOGINID;
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (s == null) {
                    if (sCheckInStatus) {
                        btn_check_in_out.setText("Checked-IN\n" + checkInOutTime);
                        btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.green));

                        /*********************************Alarm : 6 o clock********************************************************/
                        Calendar alarmStartTime = Calendar.getInstance();
                        alarmStartTime.set(Calendar.HOUR_OF_DAY, 18);
                        alarmStartTime.set(Calendar.MINUTE, 00);
                        alarmStartTime.set(Calendar.SECOND, 0);

                        Intent intent = new Intent(MainActivity.this, Notificationmessage.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
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
                    } else {
                        btn_check_in_out.setText("Checked-OUT\n" + checkInOutTime);
                        btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.red));
                        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nMgr.cancel(12345);

                    }
                } else {
                    Log.i("INFO", s);
                    try {
                        JSONObject o = new JSONObject(s);
                        JSONArray arr = o.optJSONArray("UserCheckIn");
                        JSONObject oo = arr.getJSONObject(0);
                        String userId = oo.optString("UserId");
                        boolean IsCheckIn = oo.optBoolean("IsCheckIn");
                        String ActionDate = oo.optString("ActionDate");
                        ActionDate = ActionDate.replace("T", " ");

                        MyApp.setSharedPrefString("CheckedInTime", ActionDate);
                        MyApp.setStatus("CheckedInStatus", IsCheckIn);

                        checkInOutTime = MyApp.getSharedPrefString("CheckedInTime");
                        sCheckInStatus = MyApp.getStatus("CheckedInStatus");


                        if (sCheckInStatus) {
                            btn_check_in_out.setText("Checked-IN\n" + checkInOutTime);
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.green));
                        } else {
                            btn_check_in_out.setText("Checked-OUT\n" + checkInOutTime);
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.red));
                            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nMgr.cancel(12345);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (sCheckInStatus) {
                            btn_check_in_out.setText("Checked-IN\n" + checkInOutTime);
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.green));

                            /*********************************Alarm : 6 o clock********************************************************/
                            Calendar alarmStartTime = Calendar.getInstance();
                            alarmStartTime.set(Calendar.HOUR_OF_DAY, 18);
                            alarmStartTime.set(Calendar.MINUTE, 00);
                            alarmStartTime.set(Calendar.SECOND, 0);

                            Intent intent = new Intent(MainActivity.this, Notificationmessage.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
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
                        } else {
                            btn_check_in_out.setText("Checked-OUT\n" + checkInOutTime);
                            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.red));
                            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nMgr.cancel(12345);

                        }
                    }
                }
            } catch (Exception e) {

            }

        }
    }


    public static String parseDateToddMMyyyy(String time) {
//                    2018-10-10 18:49:50
//                    Oct 10 2018  6:49:51 PM
        String inputPattern = "MMM dd yyyy";
        String outputPattern = "yyyy-MM-dd";
        Log.d(">>>>>>>>>>>>>>>", "time " + time);


        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = "null";

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
            return str;
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }

    }

    public static String parseDateTime(String time) {
        String inputPattern = "h:mm:ss a";
        String outputPattern = "H:mm:ss";
        Log.d(">>>>>>>>>>>>>>>", "time " + time);


        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = "null";

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
            return str;
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setStatus("isNewTaskOpen", false);
        MyApp.activityResumed();
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        updateMarkers();
        Utils.getLocationUpdatesResult(this);

        int checkInOutCount = MyApp.getApplication().readCheckInOutData().keySet().size();
        int locationsCount = 0;
        try {
            locationsCount = MyApp.getApplication().readLocationData().keySet().size();
        } catch (Exception e) {
        }
        int issuesCount = MyApp.getApplication().readTicketsIssueHistory().keySet().size();
        int batteryCount = MyApp.getApplication().readBatteryHistory().keySet().size();

        txt_battery_count.setText(batteryCount + "");
        if (!MyApp.getStatus("isTicketUpdating"))
            txt_issues_count.setText(issuesCount + "");
        txt_locations_count.setText(locationsCount + "");
        txt_check_in_out_count.setText(checkInOutCount + "");

        progress_sync_locations.setVisibility(View.GONE);
        progress_sync_status.setVisibility(View.GONE);
        progress_sync_battery.setVisibility(View.GONE);
        progress_sync_progress_in_out.setVisibility(View.GONE);

        try {
            txt_locations_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_check_in_out_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_issues_count.setBackground(getDrawable(R.drawable.sync_orange));
            txt_battery_count.setBackground(getDrawable(R.drawable.sync_orange));
        } catch (NoSuchMethodError e) {
            txt_locations_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            txt_check_in_out_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            if (!MyApp.getStatus("isTicketUpdating"))
                txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
            txt_battery_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
        } catch (Exception e) {
        }

        if (callable) {

            syncLocations();
            syncCheckInOut();
            syncBattery();
            if (!MyApp.getStatus("isTicketUpdating"))
                try {
                    if (!stringIterator.hasNext())
                        syncIssues();
                } catch (Exception ee) {
                    syncIssues();
                }
            callable = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callable = true;
                }
            }, 10000);
        }

        if (checkInOutCount == 0) {
            btn_check_in_out.setText("Refreshing...");
            btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));
            new CheckInInfo().execute();
        }

    }

    private boolean callable = true;
    private boolean callableLocation = true;
    private boolean callableIssues = true;
    private boolean callableBattery = true;
    private boolean callableCheckInOut = true;

    private void syncCheckInOut() {
        if (MyApp.isConnectingToInternet(MainActivity.this) && callableCheckInOut) {
            callableCheckInOut = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callableCheckInOut = true;
                }
            }, 5000);
            Map<String, Map<String, String>> map = MyApp.getApplication().readCheckInOutData();
            if (map.keySet().size() == 0) return;
            progress_sync_progress_in_out.setVisibility(View.VISIBLE);
            txt_check_in_out_count.setBackground(null);
            for (String key : map.keySet()) {
                map.get(key).put("RealTimeUpdate", "false");
                try {
                    appCheckINOperation(map.get(key), "false");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void syncLocations() {
        if (MyApp.isConnectingToInternet(MainActivity.this) && callableLocation) {
            callableLocation = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callableLocation = true;
                    progress_sync_locations.setVisibility(View.GONE);
                }
            }, 5000);
            Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
            int count = locMap.keySet().size();
            if (count <= 0) {
                txt_locations_count.setText("0");
                return;
            }
            progress_sync_locations.setVisibility(View.VISIBLE);
            txt_locations_count.setBackground(null);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress_sync_locations.setVisibility(View.GONE);
                    txt_locations_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                }
            }, 4000);

            List<String> keysList = new ArrayList<>();

            if (locMap.keySet().size() > 0) {

                for (String key : locMap.keySet()) {
                    keysList.add(key);
                }

                locIterator = keysList.iterator();
                startLocationSync();

            }

        }
    }

    private void startLocationSync() {
        try {
            if (locIterator.hasNext()) {
                txt_locations_count.setBackground(null);
                progress_sync_locations.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callableLocation = true;
                        progress_sync_locations.setVisibility(View.GONE);
                        txt_locations_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                    }
                }, 5000);

                String key = locIterator.next();
                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
                HashMap<String, String> locationInfo = new HashMap<>();
                if (locMap.get(key).get("UserId").equals("0")) {
                    locMap.remove(key);
                    MyApp.getApplication().writeLocationData(locMap);
                    startLocationSync();

                }
                locationInfo.put("RealTimeUpdate", "false");
                locationInfo.put("UserId", locMap.get(key).get("UserId"));
                locationInfo.put("DeviceId", sDeviceId);
                locationInfo.put("Latitude", locMap.get(key).get("Latitude"));
                locationInfo.put("Longitude", locMap.get(key).get("Longitude"));
                locationInfo.put("AutoCaptured", locMap.get(key).get("AutoCaptured"));
                locationInfo.put("ActivityDate", key);
                locationInfo.put("AddressLine", locMap.get(key).get("AddressLine"));
                locationInfo.put("Premises", locMap.get(key).get("Premises"));
                locationInfo.put("SubLocality", locMap.get(key).get("SubLocality"));
                locationInfo.put("SubAdminArea", locMap.get(key).get("SubAdminArea"));
                locationInfo.put("PostalCode", locMap.get(key).get("PostalCode"));
                locationInfo.put("City", locMap.get(key).get("City"));
                locationInfo.put("State", locMap.get(key).get("State"));
                locationInfo.put("Country", locMap.get(key).get("Country"));
                locationInfo.put("KnownName", locMap.get(key).get("KnownName"));
                locationInfo.put("Provider", locMap.get(key).get("Provider"));
                LocationOperationOffline(locationInfo, MainActivity.this, key, false, false);
            }
        } catch (Exception e) {
        }
    }

    private Iterator<String> locIterator = null;

    private void syncIssues() {

        if (MyApp.isConnectingToInternet(MainActivity.this) && callableIssues) {
            callableIssues = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callableIssues = true;
                }
            }, 5000);
            final Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
            if (savedMap.keySet().size() == 0) return;
            progress_sync_status.setVisibility(View.VISIBLE);
            txt_issues_count.setBackground(null);


            if (savedMap.keySet().size() > 0) {
                ArrayList<String> keys = new ArrayList<>(savedMap.keySet());
                List<String> arrayList = new ArrayList<>();
                for (int i = 0; i < keys.size(); i++) {
                    arrayList.add(savedMap.get(keys.get(i)).get("ActivityDate") + "@@@" + keys.get(i));
                }

                class StringDateComparator implements Comparator<String> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    public int compare(String lhs, String rhs) {
                        try {
                            return dateFormat.parse(lhs.split("@@@")[0]).compareTo(dateFormat.parse(rhs.split("@@@")[0]));
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                }

                Collections.sort(arrayList, new StringDateComparator());
                List<String> seqKeys = new ArrayList<>();
                for (String key : arrayList) {
                    if (savedMap.get(key.split("@@@")[1]).get("SyncStatus").equals("true")) {
                        savedMap.remove("TicketId");
                    } else {
                        seqKeys.add(key.split("@@@")[1]);
                    }
                }

                if (seqKeys.size() > 0) {
                    stringIterator = seqKeys.iterator();
                    String keySent = stringIterator.next();
                    Log.e("AssignedUserId", keySent);
                    UpdateTask(savedMap.get(keySent), "false", keySent);
                }

            } else {
                Log.e("TicketStatusTable", "offline sync count is 0");
            }
        }
    }

    Iterator<String> stringIterator = null;

    private void syncBattery() {
        if (MyApp.isConnectingToInternet(MainActivity.this) && callableBattery) {
            callableBattery = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callableBattery = true;
                }
            }, 5000);
            final Map<String, Map<String, String>> batMap = MyApp.getApplication().readBatteryHistory();
            if (batMap.keySet().size() == 0) return;
            progress_sync_battery.setVisibility(View.VISIBLE);
            txt_battery_count.setBackground(null);
            if (batMap.keySet().size() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (String key : batMap.keySet()) {
                            if (batMap.get(key).get("syncStatus").equals("false")) {
                                ServiceBattery m = new ServiceBattery();
                                m.BatteryOffline(batMap.get(key), MainActivity.this, false);
                            }
                        }
                    }
                }).start();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress_sync_battery.setVisibility(View.GONE);
                    txt_battery_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                }
            }, 4000);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("refresh", false)) {
            refreshData(true);
        }
    }


    public void UpdateTask(final Map<String, String> postTktStatus, String realTimeUpdate, final String keyToDelete) {
        if (postTktStatus == null) {
            try {
                if (stringIterator.hasNext()) {
                    String keySent = stringIterator.next();
                    Log.e("AssignedUserId", keySent);

                    UpdateTask(MyApp.getApplication().readTicketsIssueHistory().get(keySent), "false", keySent);
                } else {
                    stringIterator = null;
                    progress_sync_status.setVisibility(View.GONE);
                    txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                }
            } catch (Exception e) {
            }
            return;
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        final ApiResult apiResult = new ApiResult();
        try {
            ApiResult.IssueDetail issueDetail;
            try {
                if (postTktStatus.get("TicketId").equals("0")) {
                    issueDetail = apiResult.new IssueDetail(postTktStatus.get("UserId"),
                            postTktStatus.get("ParentCompanyId"),
                            postTktStatus.get("TaskId").contains("@@") ? postTktStatus.get("TaskId").split("@@")[0] : postTktStatus.get("TaskId"),
                            postTktStatus.get("StatusId"),
                            postTktStatus.get("Comment"),
                            postTktStatus.get("ActivityDate"),
                            postTktStatus.get("DepartmentId"),
                            postTktStatus.get("Latitude"),
                            postTktStatus.get("Longitude"),
                            postTktStatus.get("AssetSerialNo"),
                            postTktStatus.get("DeviceId"),
                            realTimeUpdate,
                            postTktStatus.get("ModeOfTransport"),
                            postTktStatus.get("Expense"),
                            postTktStatus.get("AssignedUserId"),
                            postTktStatus.get("CustomDestination"),
                            postTktStatus.get("StartingForSite")
                    );

                } else {
                    issueDetail = apiResult.new IssueDetail(postTktStatus.get("UserId"),
                            postTktStatus.get("ParentCompanyId"),
                            postTktStatus.get("TicketId").contains("@@") ? postTktStatus.get("TicketId").split("@@")[0] : postTktStatus.get("TicketId"),
                            postTktStatus.get("StatusId"),
                            postTktStatus.get("Comment"),
                            postTktStatus.get("ActivityDate"),
                            postTktStatus.get("DepartmentId"),
                            postTktStatus.get("Latitude"),
                            postTktStatus.get("Longitude"),
                            postTktStatus.get("AssetSerialNo"),
                            postTktStatus.get("DeviceId"),
                            realTimeUpdate,
                            postTktStatus.get("ModeOfTransport"),
                            postTktStatus.get("Expense"),
                            postTktStatus.get("AssignedUserId"),
                            postTktStatus.get("CustomDestination"),
                            postTktStatus.get("StartingForSite")
                    );

                }
            } catch (Exception e) {
                try {
                    if (stringIterator.hasNext()) {
                        String keySent = stringIterator.next();
                        Log.e("AssignedUserId", keySent);

                        UpdateTask(MyApp.getApplication().readTicketsIssueHistory().get(keySent), "false", keySent);
                    } else {
                        stringIterator = null;
                        progress_sync_status.setVisibility(View.GONE);
                        txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                    }
                } catch (Exception re) {
                }
                return;
            }

            if (issueDetail.taskId.equals("0") && issueDetail.IssueId.equals("0") &&
                    !(issueDetail.Status.equals("2") || issueDetail.Status.equals("3"))) {
                Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();

                savedMap.remove(keyToDelete);

                try {
                    savedMap.remove(postTktStatus.get("TicketId"));
                    savedMap.remove(postTktStatus.get("ActivityDate"));
                    savedMap.remove(postTktStatus.get("TaskId"));
                } catch (Exception e) {
                }
                MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                int issuesCount = savedMap.keySet().size();
                try {
                    txt_issues_count.setText(issuesCount + "");
                } catch (Exception e) {
                    MyApp.spinnerStop();
                }

                try {

                    if (stringIterator.hasNext()) {
                        String keySent = stringIterator.next();
                        Log.e("AssignedUserId", keySent);

                        UpdateTask(MyApp.getApplication().readTicketsIssueHistory().get(keySent), "false", keySent);
                    } else {
                        stringIterator = null;
                        progress_sync_status.setVisibility(View.GONE);
                        txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                    }
                } catch (Exception e) {
                }

                return;
            }

            Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);
            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                @Override
                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                    MyApp.setStatus("isTicketUpdating", false);
                    MyApp.spinnerStop();
                    try {
                        ApiResult.IssueDetail iData = response.body();
                        if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                            try {
                                MyApp.showMassage(MainActivity.this, "Server response error. Data has been saved to offline, it will be synced after some time.");
                            } catch (Exception e) {
                                e.getMessage();
                            }
                            Log.e("TicketStatusTable", "Success but not true");
                            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                            Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                            map.put("SyncStatus", "false");
                            savedMap.put(postTktStatus.get("TicketId"), map);
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                        } else {
                            Log.e("TicketStatusTable", "Success" + " response " + iData.toString());
                            if (iData.resData.TicketRedirection) {
                                MyApp.setStatus(postTktStatus.get("TicketId") + "RED", true);
                            }
                            if (iData.resData.Status.equals("false")) {
                                try {
                                    Map<String, String[]> statusArr = MyApp.getApplication().readSavedStatusValue();
                                    String[] previousStatus = statusArr.get(postTktStatus.get("TicketId"));
                                    sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);

                                    if (statusArr.containsKey(postTktStatus.get("TicketId"))) {
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("StatusId", previousStatus[0]);
                                        newValues.put("IsAccepted", previousStatus[1]);
                                        newValues.put("UpdatedDate", previousStatus[2]);
                                        newValues.put("PreviousStatus", previousStatus[3]);
                                        sql.update("Issue_Detail", newValues, "IssueId=" + postTktStatus.get("TicketId"), null);
                                    } else if (statusArr.containsKey(postTktStatus.get("TaskId"))) {
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("StatusId", previousStatus[0]);
                                        newValues.put("IsAccepted", previousStatus[1]);
                                        newValues.put("UpdatedDate", previousStatus[2]);
                                        newValues.put("PreviousStatus", previousStatus[3]);
                                        sql.update("Issue_Detail", newValues, "IssueId=" + postTktStatus.get("TaskId"), null);
                                    }
                                } catch (Exception e) {
                                }
                                MyApp.popMessage("Error", iData.resData.Message, MainActivity.this);
                            }
                            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
//                            String removableKey = "";
//                            for (String keyValue : savedMap.keySet()) {
//                                if (keyValue.contains(postTktStatus.get("TicketId"))) {
//                                    removableKey = keyValue;
//                                } else if (keyValue.contains(postTktStatus.get("TaskId"))) {
//                                    removableKey = keyValue;
//                                }
//                            }
                            savedMap.remove(keyToDelete);
//                            savedMap.remove(postTktStatus.get("ActivityDate"));
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                            int issuesCount = savedMap.keySet().size();
                            try {
                                txt_issues_count.setText(issuesCount + "");
                                if (issuesCount == 0) {
                                    refreshData(true);
                                }
                            } catch (Exception e) {
                            }
//                            if (iData.resData.Status.equals("false")) {
//                                MyApp.popMessage("Error", iData.resData.Message, MainActivity.this);
//                            }
                        }
                    } catch (Exception e) {
                        MyApp.spinnerStop();
                        MyApp.setStatus("isTicketUpdating", false);
                        Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                        Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                        try {
                            map.put("SyncStatus", "false");
                            savedMap.put(postTktStatus.get("TicketId"), map);
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                        } catch (Exception ee) {

                        }
                    }

                    try {
                        if (stringIterator.hasNext()) {
                            String keySent = stringIterator.next();
                            Log.e("AssignedUserId", keySent);

                            UpdateTask(MyApp.getApplication().readTicketsIssueHistory().get(keySent), "false", keySent);
                        } else {
                            stringIterator = null;
                            progress_sync_status.setVisibility(View.GONE);
                            txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
                        }
                    } catch (Exception e) {
                    }
//                    progress_sync_status.setVisibility(View.GONE);
//                    txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));
//                    stringIterator = null;
                }

                @Override
                public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                    MyApp.spinnerStop();
                    MyApp.setStatus("isTicketUpdating", false);
                    try {
                        Log.e("TicketStatusTable", "On failure");
                        call.cancel();
                        Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                        Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                        map.put("SyncStatus", "false");
                        savedMap.put(postTktStatus.get("TicketId"), map);
                        MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                    } catch (Exception e) {
                    }

                    stringIterator = null;
                    progress_sync_status.setVisibility(View.GONE);
                    txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));

                }
            });
        } catch (Exception e) {
            MyApp.setStatus("isTicketUpdating", false);
            e.printStackTrace();
            MyApp.spinnerStop();
            try {
                Log.e("TicketStatusTable", "came to exception " + e.toString());
                Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                map.put("SyncStatus", "false");
                savedMap.put(postTktStatus.get("TicketId"), map);
                MyApp.getApplication().writeTicketsIssueHistory(savedMap);
            } catch (Exception ee) {
            }
            stringIterator = null;
            progress_sync_status.setVisibility(View.GONE);
            txt_issues_count.setBackground(getResources().getDrawable(R.drawable.sync_orange));

        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                if (isOnline(context)) {

                    InternetConnector icDataSyncing = new InternetConnector();
                    try {
                        btn_check_in_out.setText("Refreshing...");
                        btn_check_in_out.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                        new CheckInInfo().execute();
                        icDataSyncing.offlineSyncing(MainActivity.this, 1);
                        mMobileDataInfo.put("UserId", LOGINID);
                        mMobileDataInfo.put("DeviceId", sDeviceId);
                        mMobileDataInfo.put("Enabled", "true");
                        mMobileDataInfo.put("ActionDate", currentDateTimeString);
                        mMobileDataInfo.put("RealTimeUpdate", "true");

                        Map<Long, Map<String, String>> mobileData = MyApp.getApplication().readMobileData();
                        long keyDelete = System.currentTimeMillis();
                        mobileData.put(keyDelete, mMobileDataInfo);
                        MyApp.getApplication().writeMobileData(mobileData);
                        PushMobileData(mMobileDataInfo, getApplicationContext(), keyDelete);
                        int checkInOutCount = MyApp.getApplication().readCheckInOutData().keySet().size();
                        int locationsCount = MyApp.getApplication().readLocationData().keySet().size();
                        int issuesCount = MyApp.getApplication().readTicketsIssueHistory().keySet().size();
                        int batteryCount = MyApp.getApplication().readBatteryHistory().keySet().size();
                        txt_battery_count.setText(batteryCount + "");
                        if (!MyApp.getStatus("isTicketUpdating"))
                            txt_issues_count.setText(issuesCount + "");
                        txt_locations_count.setText(locationsCount + "");
                        txt_check_in_out_count.setText(checkInOutCount + "");
                        syncLocations();
                        syncCheckInOut();
                        syncBattery();
                        try {
                            if (!stringIterator.hasNext())
                                syncIssues();
                        } catch (Exception ee) {
                            syncIssues();
                        }
                    } catch (Exception e) {
                    }


                } else {
                    showAlert.setText("Internet is not Working");
                    MyApp.showMassage(MainActivity.this, "Internet connection is Off");
                    mMobileDataInfo.put("UserId", LOGINID);
                    mMobileDataInfo.put("DeviceId", sDeviceId);
                    mMobileDataInfo.put("Enabled", "false");
                    mMobileDataInfo.put("ActionDate", currentDateTimeString);
                    mMobileDataInfo.put("RealTimeUpdate", "true");
                    Map<Long, Map<String, String>> mobileData = MyApp.getApplication().readMobileData();
                    long key = System.currentTimeMillis();
                    mobileData.put(key, mMobileDataInfo);
                    MyApp.getApplication().writeMobileData(mobileData);
                    PushMobileData(mMobileDataInfo, getApplicationContext(), key);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        private boolean isOnline(Context context) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                //should check null because in airplane mode it will be null
                return (netInfo != null && netInfo.isConnected());
            } catch (NullPointerException e) {
                e.printStackTrace();
                showAlert.setText("Internet is not Working");
                return false;
            }
        }
    }

    public void LocationOperationOffline(Map locationInfo, final Context ctx, final String key, final boolean showToast, boolean realtimeupdate) {

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

            if (locationInfo.get("Latitude").toString().length() <= 3) {
                Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
                locMap.remove(key);
                MyApp.getApplication().writeLocationData(locMap);
                int locationsCount = MyApp.getApplication().readLocationData().keySet().size();
                txt_locations_count.setText(locationsCount + "");
                return;
            }

            final ApiResult.User_Location user_location;
            Call<ApiResult.User_Location> call1;
            if (locationInfo.get("City").equals("NA") || locationInfo.get("State").equals("NA")) {
                user_location = apiResult.new User_Location("true",
                        locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                        locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                        locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString());

                call1 = apiInterface.PostCoordinatesShorten(user_location);
            } else {
                user_location = apiResult.new User_Location(realtimeupdate ? "true" : "false",
                        locationInfo.get("UserId").toString(), locationInfo.get("DeviceId").toString(),
                        locationInfo.get("Latitude").toString(), locationInfo.get("Longitude").toString(),
                        locationInfo.get("ActivityDate").toString(), locationInfo.get("AutoCaptured").toString(),
                        locationInfo.get("AddressLine").toString(), sublocalityString, locationInfo.get("PostalCode").toString(),
                        locationInfo.get("City").toString(), locationInfo.get("State").toString(),
                        locationInfo.get("Country").toString(), locationInfo.get("KnownName").toString(), "NA");
                call1 = apiInterface.PostCoordinates(user_location);
            }

            call1.enqueue(new Callback<ApiResult.User_Location>() {
                @Override
                public void onResponse(Call<ApiResult.User_Location> call, Response<ApiResult.User_Location> response) {
                    try {
                        ApiResult.User_Location iData = response.body();
                        if (iData.resData == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                            Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
                            locMap.remove(key);
                            MyApp.getApplication().writeLocationData(locMap);
                        } else {
                            Map<String, Map<String, String>> locMap = MyApp.getApplication().readLocationData();
                            locMap.remove(key);
                            MyApp.getApplication().writeLocationData(locMap);
                            int locationsCount = MyApp.getApplication().readLocationData().keySet().size();
                            txt_locations_count.setText(locationsCount + "");

                        }
                        if (showToast)
                            MyApp.showMassage(getApplicationContext(), "Location sent successfully!!!");
                    } catch (Exception e) {
                    }
                    startLocationSync();
                }

                @Override
                public void onFailure(Call<ApiResult.User_Location> call, Throwable t) {
                    call.cancel();
                    startLocationSync();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            startLocationSync();
        }
    }
}
