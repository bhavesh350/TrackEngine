package net.mzi.trackengine;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import net.mzi.trackengine.model.Message;
import net.mzi.trackengine.model.TicketInfoClass;

import io.fabric.sdk.android.Fabric;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MyApp extends MultiDexApplication {
    SharedPreferences pref;
    public static String SHARED_PREF_NAME = "RS_PREF";
    private static MyApp myApplication = null;
    public static boolean activityVisible; // Variable that will check the
    private Context c;
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

    public static MyApp getApplication() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        myApplication = this;
        c = this;
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

//
//        try {
//            mSensorService = new ServiceLocation(c);
//            mServiceIntentLocation = new Intent(c, mSensorService.getClass());
//            if (!isMyServiceRunning(mSensorService.getClass())) {
////                startService(mServiceIntent);
//                startService(mServiceIntentLocation);
//            }
//        } catch (Exception e) {
//        }
//
//        try {
//            betteryService = new ServiceBattery(c);
//            mServiceIntentBattery = new Intent(c, betteryService.getClass());
//            if (!isMyServiceRunning(betteryService.getClass()))
//
//                startService(mServiceIntentBattery);
//
//        } catch (Exception e) {
//        }
    }


    Intent mServiceIntentBattery;
    Intent mServiceIntentLocation;

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


    //    ServiceLocation mSensorService;
//    ServiceBattery betteryService;
    private Handler h = new Handler();

    public static void showMassage(Context ctx, String msg) {
        try {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception w) {
        } catch (StackOverflowError ee) {
        }
    }

    public static long getSharedPrefLong(String preffConstant) {
        long longValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        longValue = sp.getLong(preffConstant, 0);
        return longValue;
    }

    public static void setSharedPrefLong(String preffConstant, long longValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(preffConstant, longValue);
        editor.commit();
    }

    public static String getSharedPrefString(String preffConstant) {
        String stringValue = "";
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        stringValue = sp.getString(preffConstant, "");
        return stringValue;
    }

    public static void setSharedPrefString(String preffConstant,
                                           String stringValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(preffConstant, stringValue);
        editor.commit();
    }

    public static int getSharedPrefInteger(String preffConstant) {
        int intValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        intValue = sp.getInt(preffConstant, 0);
        return intValue;
    }

    public static void setSharedPrefInteger(String preffConstant, int value) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(preffConstant, value);
        editor.commit();
    }

    public static float getSharedPrefFloat(String preffConstant) {
        float floatValue = 0;
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        floatValue = sp.getFloat(preffConstant, 0);
        return floatValue;
    }

    public static void setSharedPrefFloat(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

    public static void setSharedPrefArray(String preffConstant, float floatValue) {
        SharedPreferences sp = myApplication.getSharedPreferences(
                preffConstant, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(preffConstant, floatValue);
        editor.commit();
    }

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
            dialog.setCancelable(false);
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

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static void popMessage(String titleMsg, String errorMsg,
                                  Context context) {
        // pop error message
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleMsg).setMessage(errorMsg)
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        try {
            alert.show();
        } catch (Exception e) {
        }

    }

    public static String parseDateTime(String time) {
//        2018-10-14 13:59:39

        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd MMM yyyy h:mm a";

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

    public void writeIssuesStatusList(ApiResult.IssueStatus.lstDetails[] issuesStatusList) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/issuesStatusList.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
                System.out.println("old file deleted>>>>>>>>> ");
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(issuesStatusList);
            out.close();
            fileOut.close();
            System.out.println("my file replaced>>>>>>>>> ");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SdCardPath")
    public ApiResult.IssueStatus.lstDetails[] readIssuesStatusList() {
        String path = "/data/data/" + c.getPackageName()
                + "/issuesStatusList.ser";
        File f = new File(path);
        ApiResult.IssueStatus.lstDetails[] device = null;
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                device = (ApiResult.IssueStatus.lstDetails[]) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return device;
    }


    public void writeTicketCapture(Map<String, TicketInfoClass> map) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/ticketCapture.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, TicketInfoClass> readTicketCapture() {
        String path = "/data/data/" + c.getPackageName()
                + "/ticketCapture.ser";
        File f = new File(path);
        HashMap<String, TicketInfoClass> map = new HashMap<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = (HashMap<String, TicketInfoClass>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public void writeTicketCaptureSchedule(Map<String, String> map) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/ticketCaptureSchedule.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> readTicketCaptureSchedule() {
        String path = "/data/data/" + c.getPackageName()
                + "/ticketCaptureSchedule.ser";
        File f = new File(path);
        HashMap<String, String> map = new HashMap<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = (HashMap<String, String>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public void writeUser(ApiResult.User user) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/user.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(user);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ApiResult.User readUser() {
        String path = "/data/data/" + c.getPackageName()
                + "/user.ser";
        File f = new File(path);
        ApiResult.User user = null;
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                user = (ApiResult.User) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return user;
    }


    public void writeCheckInOutData(Map<String, Map<String, String>> map) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/checkInOut.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, String>> readCheckInOutData() {
        String path = "/data/data/" + c.getPackageName()
                + "/checkInOut.ser";
        File f = new File(path);
        Map<String, Map<String, String>> map = new HashMap<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = (Map<String, Map<String, String>>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public void writeTicketsIssueHistory(Map<String, Map<String, String>> map) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/issueHistory.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, String>> readTicketsIssueHistory() {
        String path = "/data/data/" + c.getPackageName()
                + "/issueHistory.ser";
        File f = new File(path);
        Map<String, Map<String, String>> map = new HashMap<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = (Map<String, Map<String, String>>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }


    public void writeBatteryHistory(Map<String, Map<String, String>> map) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/batteryHistory.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Map<String, String>> readBatteryHistory() {
        String path = "/data/data/" + c.getPackageName()
                + "/batteryHistory.ser";
        File f = new File(path);
        Map<String, Map<String, String>> map = new HashMap<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                map = (Map<String, Map<String, String>>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public void writeMessage(List<TicketInfoClass> device) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/message.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(device);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SdCardPath")
    public List<TicketInfoClass> readMessage() {
        String path = "/data/data/" + c.getPackageName()
                + "/message.ser";
        File f = new File(path);
        ArrayList<TicketInfoClass> device = new ArrayList<>();
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                device = (ArrayList<TicketInfoClass>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return device;
    }

    public void writeSavedStatusValues(Map<String,String []> values) {
        try {
            String path = "/data/data/" + c.getPackageName()
                    + "/savedStatusValues.ser";
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(values);
            out.close();
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SdCardPath")
    public Map<String,String []> readSavedStatusValue() {
        String path = "/data/data/" + c.getPackageName()
                + "/savedStatusValues.ser";
        File f = new File(path);
        Map<String,String []> device = null;
        if (f.exists()) {
            try {
                System.gc();
                FileInputStream fileIn = new FileInputStream(path);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                device = (Map<String,String []>) in.readObject();
                in.close();
                fileIn.close();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (OptionalDataException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return device;
    }

}
