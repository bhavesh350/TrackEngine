package net.mzi.trackengine;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.Firebase;

import net.mzi.trackengine.model.PostUrl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    ApiInterface apiInterface;
    SQLiteDatabase sql;
    String currentDateTimeString;
    Cursor cquery;
    EditText Ephonelogin, Epswdlogin;
    Button Elogin;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    SessionManager session;
    Firebase ref;
    String uname, pwd;
    TextView forgotPaswd;
    Dialog progress;
    private ProgressBar progressBar;
    Map<String, String> loginInfo = new HashMap<String, String>();
    private String sDeviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Firebase.setAndroidContext(getApplicationContext());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = getSharedPreferences("login", 0);
        editor = pref.edit();
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        ref = new Firebase("https://trackengine-c6122.firebaseio.com/UserLoginInfo");
        session = new SessionManager(getApplicationContext());
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                sDeviceId = getUniqueID();
            } catch (Exception e) {
                e.printStackTrace();
//                sDeviceId = "";
            }
        } else {
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        forgotPaswd = (TextView) findViewById(R.id.forgotPaswdId);
        Ephonelogin = (EditText) findViewById(R.id.userlogin);
        Epswdlogin = (EditText) findViewById(R.id.pswdlogin);
        progressBar = (ProgressBar) findViewById(R.id.pbHeaderProgress);


        Elogin = (Button) findViewById(R.id.login);
        forgotPaswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder Dialog = new AlertDialog.Builder(LoginActivity.this);
                Dialog.setTitle("Enter Login Id: ");
                Dialog.setCancelable(false);
                LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = li.inflate(R.layout.rejection, null);
                final EditText commemt = dialogView.findViewById(R.id.rejectionReason);
                commemt.setHint("Enter Login Id");
                Dialog.setView(dialogView);
                Dialog.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        });
                Dialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog dialog = Dialog.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Boolean wantToCloseDialog = true;
                        if (commemt.getText().toString().length() == 0) {
                            commemt.setError("Please enter E_mail Id");
                            wantToCloseDialog = false;
                            //Dialog.show();
                        }
                        if (wantToCloseDialog) {
                            new ForgotPassword(commemt.getText().toString(), v).execute();
                            dialog.dismiss();
                        }
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });

            }
        });
        Elogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sDeviceId == null) {
                    showAlert(LoginActivity.this, "Unable to receive phone info, kindly try again!!!");
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        try {
                            sDeviceId = getUniqueID();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ActivityCompat.requestPermissions(LoginActivity.this,
                                new String[]{android.Manifest.permission.READ_PHONE_STATE},
                                1);
                    }
                } else {
                    deleteDataFromTables();
                    progress = ProgressDialog.show(LoginActivity.this,
                            "Connecting to server", "Powering up engine!!!");
                    uname = Ephonelogin.getText().toString();
                    pwd = Epswdlogin.getText().toString();
                    if (uname.equals("")) {
                        Ephonelogin.setError("Enter LoginID");
                    } else if (pwd.equals("")) {
                        Epswdlogin.setError("Enter Password");
                    } else {
                        String manufacturer = Build.MANUFACTURER;
                        String model = Build.MODEL;
                        Date cDate = new Date();
                        currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                        //new LongOperation(jsonString, view).execute();
                        final ApiResult apiResult = new ApiResult();
                        final ApiResult.User loginDetail = apiResult.new User(uname, pwd, sDeviceId, manufacturer + "(" + model + ")", currentDateTimeString, androidOsName, versionCode, versionName);

                        Call<ApiResult.User> call1 = apiInterface.isLogin(loginDetail);
                        call1.enqueue(new Callback<ApiResult.User>() {
                            @Override
                            public void onResponse(Call<ApiResult.User> call, Response<ApiResult.User> response) {
                                ApiResult.User user = response.body();
                                MyApp.getApplication().writeUser(user);
                                ref.child(user.data.UserId).child("DeviceId").setValue(sDeviceId);
                                if (user.data.Status.equals("true")) {
                                    Cursor cqueryTemp = sql.rawQuery("select * from Issue_Status where DepartmentId='" + user.data.DepartmentId + "'", null);
                                    if (cqueryTemp.getCount() > 0) ;
                                    else {
//                                        str.replaceAll("[^\\d.]", "");
                                        MyApp.getApplication().writeIssuesStatusList(user.data.dataStatus);
                                        for (int i = 0; i < user.data.dataStatus.length; i++) {
                                            Log.e("TAG", "onPostExecute: " + user.data.dataStatus[i]);
                                            sql.execSQL("INSERT INTO Issue_Status(StatusId,StatusName,CommentRequired,MainStatusId,IsMobileStatus,CompanyId,DepartmentId,ParentStatus,StartingForSite)VALUES" +
                                                    "('" + user.data.dataStatus[i].Id + "','" + user.data.dataStatus[i].StatusName + "','" + user.data.dataStatus[i].CommentRequired + "','" + user.data.dataStatus[i].MainStatusId + "','" + user.data.dataStatus[i].IsMobileStatus + "','" + user.data.CompanyId + "','" + user.data.DepartmentId + "','" + user.data.dataStatus[i].ParentStatuses + "','" + user.data.dataStatus[i].StartingForSite + "" + "')");
                                            String sAllParentStatuses = user.data.dataStatus[i].ParentStatuses;
                                            List<String> listsAllParentStatuses = Arrays.asList(sAllParentStatuses.split(","));
                                            for (int j = 0; j < listsAllParentStatuses.size(); j++) {
                                                sql.execSQL("INSERT INTO Issue_StatusHiererchy(StatusId,ParentStatus,WaitForEntry,ActionDate,RedirectToStatus)VALUES" +
                                                        "('" + user.data.dataStatus[i].Id + "','" + listsAllParentStatuses.get(j).trim() + "','0','0','0')");
                                            }
                                        }

                                    }
                                    cqueryTemp = sql.rawQuery("select * from ModeOfTrasportList", null);
                                    if (cqueryTemp.getCount() > 0) ;
                                    else {
                                        try {
                                            for (int i = 0; i < user.data.modeOfTrasportList.length; i++) {
                                                sql.execSQL("INSERT INTO ModeOfTrasportList(TransportId,TransportMode,IsPublic)VALUES" +
                                                        "('" + user.data.modeOfTrasportList[i].Id + "','" + user.data.modeOfTrasportList[i].TransportMode + "','" + user.data.modeOfTrasportList[i].IsPublic + "" + "')");
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                    cqueryTemp.close();
                                    session.createLoginSession(user.data.Username, pwd, user.data.UserId, user.data.DepartmentId, user.data.RoleId, user.data.IsCoordinator, user.data.IsFieldAgent, user.data.UserType, user.data.CompanyId, user.data.ParentCompanyId, user.data.CheckedInTime, user.data.CheckedInStatus, user.data.IsDefaultDepartment, user.data.AppLocationSendingFrequency, user.data.AppBatterySendingFrequency, user.data.CSATEnable, user.data.AssetVerification, "2017-01-01", sDeviceId, "0");
                                    MyApp.setStatus("statusByHierarchy", user.StatusByHierarchy);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    LoginActivity.this.finish();
                                    intent.putExtra("fromLogin", true);
                                    startActivity(intent);
                                } else {
                                    showAlert(LoginActivity.this, user.data.Message);
                                    sql.delete("Issue_Status", null, null);
                                    sql.delete("Issue_StatusHiererchy", null, null);
                                }
                                progress.dismiss();
                            }

                            @Override
                            public void onFailure(Call<ApiResult.User> call, Throwable t) {
                                call.cancel();
                                progress.dismiss();
                                showAlert(LoginActivity.this, "Oops, looks like there is some connectivity problem. Please try again!!!");
                            }
                        });

                    }
                }
            }
        });

        String str = "TCPLIN180904352";
        str = str.replaceAll("[^\\d.]", "");
        Log.e("alphanumericString", str);


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

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        androidOsName = "OS: " + builder.toString();
        Log.d("alphanumericString", "OS: " + builder.toString() + "\n" + versionName);

//        String sample = "St. Mary's and St. John's".replace("'", "''");
//        Log.d("alphanumericString", sample);

    }

    private void deleteDataFromTables() {
        MyApp.getApplication().writeCheckInOutData(new HashMap<String, Map<String, String>>());
        MyApp.getApplication().writeTicketsIssueHistory(new HashMap<String, Map<String, String>>());
        try {
            MyApp.getApplication().writeBatteryHistory(new HashMap<String, Map<String, String>>());
//            sql.execSQL("delete from User_BatteryLevel");
            sql.execSQL("delete from Issue_StatusHiererchy");
            sql.execSQL("delete from User_MobileData");
            sql.execSQL("delete from User_Gps");
            sql.execSQL("delete from User_Location");
            sql.execSQL("delete from User_Login");
//            sql.execSQL("delete from User_AppCheckIn");
            sql.execSQL("delete from ModeOfTrasportList");
            sql.execSQL("delete from Issue_Status");
            sql.execSQL("delete from Issue_Detail");
            sql.execSQL("delete from Issue_Attachment");
//            sql.execSQL("delete from Issue_History");
            sql.execSQL("delete from FirebaseIssueData");
            sql.execSQL("delete from MobileManualLog");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String androidOsName = "";
    private String versionName = "";
    private String versionCode = "";

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void showAlert(Context context, String msg) {

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setMessage(msg).setTitle("Response from Server")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private class ForgotPassword extends AsyncTask<String, String, String> {
        View v;
        String sLoginId;

        public ForgotPassword(String sLoginId, View view) {
            v = view;
            this.sLoginId = sLoginId;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(PostUrl.sUrl + "ForgotPassword?LoginId=" + sLoginId);
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
            if (s == null || s.equals("")) {
                showAlert(LoginActivity.this, "Try after sometime!!!");
            } else {
                s = "No Error";
                final Snackbar snackBar = Snackbar.make(v, "Password sent to your email-id", Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();

                    }
                }).show();
            }
            Log.e("TAG", "onPostExecute: " + s);
        }
    }

    public String getUniqueID() {
        String myAndroidDeviceId = "";
        TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (mTelephony.getDeviceId() != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            myAndroidDeviceId = mTelephony.getDeviceId();
        } else {
            myAndroidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return myAndroidDeviceId;
    }
}
