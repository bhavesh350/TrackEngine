package net.mzi.trackengine;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.mzi.trackengine.MyApp.SHARED_PREF_NAME;

/**
 * Created by Poonam on 5/1/2017.
 */

@ReportsCrashes(
        formUri = "http://trackengine.mzservices.net/api/Post/PostMobileLog",
        //mailTo="vaishaligoel89@gmail.com",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,/*
        formUriBasicAuthLogin = "GENERATED_USERNAME_WITH_WRITE_PERMISSIONS",
        formUriBasicAuthPassword = "GENERATED_PASSWORD",*/
        //formKey = "", // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)
public class SOMTracker extends MultiDexApplication {
    SharedPreferences pref;
    private static SOMTracker myApplication = null;
    public static boolean activityVisible; // Variable that will check the
    // current activity state

    public static boolean isActivityVisible() {
        return activityVisible; // return true or false
    }

    public static void activityResumed() {
        activityVisible = true;// this will set true when activity resumed
    }

    public static void activityPaused() {
        activityVisible = false;// this will set false when activity paused
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {


            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setStatus("TIME", true);
            }
        }, 1000 * 60 * 2);

    }

    private Handler h = new Handler();

    public static boolean getStatus(String name) {
        boolean status;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        status = sp.getBoolean(name, false);
        return status;
    }


    public static void setStatus(String name, boolean istrue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(name, istrue);
        editor.commit();
    }

    private static ProgressDialog dialog;

    public static void spinnerStop() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                }

            }
        }

    }

    public static void spinnerStart(Context context, String text) {
        String pleaseWait = text;
        try {
            dialog = ProgressDialog.show(context, pleaseWait, "", true);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        String nh_userid = "0";
        ACRA.init(this);
        String sIsDataOn, sIsLocationOn;
        pref = getSharedPreferences("login", 0);
        nh_userid = pref.getString("userid", "userid");
        Date cDate = new Date();
        String currentDateTimeString = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss").format(cDate);
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetworkWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        boolean isConnectedWifi = activeNetworkWifi != null && activeNetworkWifi.isConnected();

        if ((!isConnected) && (!isConnectedWifi)) {
            sIsDataOn = "false";
        } else {
            sIsDataOn = "true";
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            sIsLocationOn = "false";
        } else {
            sIsLocationOn = "true";
        }
        String sDeviceId = "0";

        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String sBatteryLevel = String.valueOf(battery.getIntExtra("level", 0));
        ACRA.getErrorReporter().putCustomData("UserId", nh_userid);
        ACRA.getErrorReporter().putCustomData("IsDataOn", sIsDataOn);
        ACRA.getErrorReporter().putCustomData("IsLocationOn", sIsLocationOn);
        ACRA.getErrorReporter().putCustomData("DeviceId", sDeviceId);
        ACRA.getErrorReporter().putCustomData("CreatedOn", currentDateTimeString);
        ACRA.getErrorReporter().putCustomData("BatteryLevel", sBatteryLevel);

    }

}
