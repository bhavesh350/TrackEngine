package net.mzi.trackengine;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;


public class SplashActivity extends AppCompatActivity {
    private SessionManager session;
    public static final int MULTIPLE_PERMISSIONS = 1;
    String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    static String deviceId;
    SQLiteDatabase sql;
    Cursor cquery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_splash);
//        getSupportActionBar().hide();
        //if(checkAndRequestPermissions()) {}

//        call_permissions();
        Firebase.setAndroidContext(this);
        try {
            sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        } catch (Exception e) {
        }
        session = new SessionManager(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sql.execSQL("create table if not exists User_BatteryLevel("
                            + "Id integer primary key autoincrement,"
                            + "UserId integer,"//1
                            + "BatteryLevel integer,"//2
                            + "AutoCaptured integer,"//3
                            + "ActionDate integer,"//4
                            + "SyncStatus integer);");//5

                    sql.execSQL("create table if not exists Issue_StatusHiererchy("
                            + "Id integer primary key autoincrement,"
                            + "StatusId integer,"//1
                            + "ParentStatus integer,"//2
                            + "WaitForEntry integer,"//3
                            + "ActionDate integer,"//4
                            + "RedirectToStatus integer);");//5

                    sql.execSQL("create table if not exists User_MobileData("
                            + "Id integer primary key autoincrement,"
                            + "UserId integer,"//1
                            + "Enabled integer,"//2
                            + "ActionDate integer,"//3
                            + "SyncStatus integer);");//4

                    sql.execSQL("create table if not exists User_Gps("
                            + "Id integer primary key autoincrement,"
                            + "UserId integer,"//1
                            + "Enabled integer,"//2
                            + "ActionDate integer,"//3
                            + "SyncStatus integer);");//4

                    sql.execSQL("create table if not exists User_Location("
                            + "Id integer primary key autoincrement,"//0
                            + "UserId integer,"//1
                            + "Latitude integer,"//2
                            + "Longitude integer,"//3
                            + "AutoCaptured integer,"//4
                            + "ActivityDate varchar,"//5
                            + "AddressLine varchar,"//6
                            + "City varchar,"//7
                            + "State varchar,"//8
                            + "Country varchar,"//9
                            + "PostalCode varchar,"//10
                            + "KnownName varchar,"//11
                            + "Premises varchar,"//12
                            + "SubLocality varchar,"//13
                            + "SubAdminArea varchar,"//14
                            + "SyncStatus integer);");//15


                    sql.execSQL("create table if not exists User_Login("
                            + "Id integer primary key autoincrement,"//0
                            + "UserId integer,"//1
                            + "IsLogin integer,"//2
                            + "ActionDate integer,"//3
                            + "SyncStatus integer);");//4

                    sql.execSQL("create table if not exists IssueStatus_Main("
                            + "Id integer primary key autoincrement,"
                            + "IssueStatus_MainId varchar,"
                            + "StatusName varchar);");

                    sql.execSQL("create table if not exists User_AppCheckIn("
                            + "Id integer primary key autoincrement,"//0
                            + "UserId integer,"//1
                            + "IsCheckIn varchar,"//2
                            + "ActionDate integer,"//3
                            + "SyncStatus varchar);");//4

                    sql.execSQL("create table if not exists ModeOfTrasportList("
                            + "Id integer primary key autoincrement,"//0
                            + "TransportId integer,"//1
                            + "TransportMode varchar,"//2
                            + "IsPublic varchar);");//3

                    sql.execSQL("create table if not exists Issue_Status("
                            + "Id integer primary key autoincrement,"//0
                            + "StatusId integer,"//1
                            + "MainStatusId varchar,"//2
                            + "StatusName varchar,"//3
                            + "CommentRequired varchar,"//4
                            + "IsMobileStatus varchar,"//5
                            + "CompanyId varchar,"//6
                            + "DepartmentId varchar,"//7
                            + "ParentStatus varchar,"//7
                            + "StartingForSite varchar);");//8

                    sql.execSQL("create table if not exists Issue_Detail("
                            + "Id integer primary key autoincrement,"//0
                            + "IssueId integer,"//1
                            + "CategoryName varchar,"//2
                            + "Subject varchar,"//3
                            + "IssueText varchar,"//4
                            + "ServiceItemNumber varchar,"//5
                            + "AssetSerialNumber varchar,"//6
                            + "CreatedDate varchar,"//7
                            + "SLADate varchar,"//8
                            + "CorporateName varchar,"//9
                            + "Address varchar,"//10
                            + "Latitude varchar,"//11
                            + "Longitude varchar,"//12
                            + "PhoneNo varchar,"//13
                            + "IsAccepted varchar,"//14
                            + "StatusId varchar,"//15
                            + "AssetType varchar,"//16
                            + "AssetSubType varchar,"//17
                            + "UpdatedDate varchar,"//18
                            + "TicketHolder varchar,"//19
                            + "TicketNumber varchar,"//20
                            + "IsVerified varchar,"//21
                            + "OEMNumber varchar,"//22
                            + "AssetDetail varchar,"//23
                            + "ContractSubTypeName varchar,"//24
                            + "ContractName varchar,"//25
                            + "PreviousStatus varchar,"
                            + "TaskId integer);");//26
//                        + "PreviousStatus varchar);");//26


                    sql.execSQL("create table if not exists Issue_Attachment("
                            + "Id integer primary key autoincrement,"
                            + "AttachmentId integer,"
                            + "IssueId integer,"
                            + "AttachedFile varchar,"
                            + "AttachedOn integer,"
                            + "Comment varchar,"
                            + "SyncStatus integer);");

                    sql.execSQL("create table if not exists Issue_History("
                            + "Id integer primary key autoincrement,"//0
                            + "IssueId integer,"//1
                            + "UserId integer,"//2
                            + "IssueStatus integer,"//3
                            + "Comment varchar,"//4
                            + "CreatedDate integer,"//5
                            + "SyncStatus integer);");//6

                    sql.execSQL("create table if not exists FirebaseIssueData("
                            + "Id integer primary key autoincrement,"//0
                            + "Action varchar,"//4
                            + "IssueId varchar);");//2


                    sql.execSQL("create table if not exists MobileManualLog("
                            + "Id integer primary key autoincrement,"//0
                            + "UserId varchar,"//1
                            + "ActivityName varchar,"//2
                            + "InputPrefrences varchar,"//3
                            + "ActivityStatus varchar,"//4
                            + "Remark varchar,"//5
                            + "DeviceId varchar,"//6
                            + "IsLocationOn varchar,"//7
                            + "IsDataOn varchar,"//8
                            + "SyncStatus varchar,"//9
                            + "AppVersion varchar,"//10
                            + "AndroidVersion varchar,"//11
                            + "PackageName varchar,"//12
                            + "Model varchar,"//13
                            + "CreatedOn varchar,"//14
                            + "SDK varchar,"//15
                            + "SentOn varchar);");//16

                    cquery = sql.rawQuery("select * from IssueStatus_Main", null);
                    if (cquery.getCount() > 0) ;
                    else {
                        sql.execSQL("INSERT INTO IssueStatus_Main(IssueStatus_MainId,StatusName)VALUES('1','Open')");
                        sql.execSQL("INSERT INTO IssueStatus_Main(IssueStatus_MainId,StatusName)VALUES('2','Worked UpOn')");
                        sql.execSQL("INSERT INTO IssueStatus_Main(IssueStatus_MainId,StatusName)VALUES('3','Interim')");
                        sql.execSQL("INSERT INTO IssueStatus_Main(IssueStatus_MainId,StatusName)VALUES('4','Close')");
                        sql.execSQL("INSERT INTO IssueStatus_Main(IssueStatus_MainId,StatusName)VALUES('5','App Specific')");
                    }

                }catch (Exception e){}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int versionCode = 0;
                        try {
                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            versionCode = pInfo.versionCode;
                        } catch (PackageManager.NameNotFoundException e) {
                            versionCode = 0;
                            e.printStackTrace();
                        }
                        if (MyApp.getStatus("resetAppData")) {
                            if (MyApp.getSharedPrefInteger("updatingVersion") == versionCode) {
                                AlertDialog.Builder b = new AlertDialog.Builder(SplashActivity.this);
                                b.setTitle("Reset Data").setMessage("You just have updated your app, so we recommend you to reset your app data.")
                                        .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                MyApp.setStatus("resetAppData", false);
                                                clearPreferences();
                                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).create().show();

                            } else {
                                if (session.isLoggedIn() && MyApp.getStatus("forceReLogin")) {
                                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    SplashActivity.this.finish();
                                } else {
                                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    SplashActivity.this.finish();
                                }
                            }
                        } else {
                            if (session.isLoggedIn() && MyApp.getStatus("forceReLogin")) {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                SplashActivity.this.finish();
                            } else {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                                SplashActivity.this.finish();
//                    session.createLoginSession(Login.uname,Login.pwd);
                            }
                        }
                    }
                });
                try {
                    cquery.close();
                } catch (Exception e) {
                }
            }
        }).start();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void call_permissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
        }
        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    deviceId = telephonyManager.getDeviceId().toString();
                    // permissions granted.
                } else {
                    // no permissions granted.
                }
                return;
            }
        }
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
}
