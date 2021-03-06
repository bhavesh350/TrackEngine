package net.mzi.trackengine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.mzi.trackengine.model.FirebaseTicketData;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 4/19/2017.
 */
public class ServiceDataUpdateFirstFragment extends Service {
    public ServiceDataUpdateFirstFragment(Context c) {
        super();
    }

    public ServiceDataUpdateFirstFragment() {
    }

    String DepartmentId, sLastAction;
    ApiInterface apiInterface;
    SharedPreferences.Editor editor;
    String nh_userid;
    boolean sIsAssetVerification;
    String sTicketIds = "";
    String sTaskIds = "";
    FirebaseTicketData Pi = new FirebaseTicketData();
    SQLiteDatabase sql = null;
    Map<String, String> mTicketIdList = new HashMap<>();
    int MULTIPLE_NOTIFICATION = 0;
    //    Context ctx;
    SharedPreferences pref;

    static List<TicketInfoClass> newTickets = new ArrayList<TicketInfoClass>();
    final static ArrayList<String> testData = new ArrayList<>();
    Firebase ref = null;

    FirebaseDatabase databaseFirebase = FirebaseDatabase.getInstance();
    DatabaseReference drRef;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void onCreate() {
        super.onCreate();

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        try {
            sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite",
                    getApplicationContext().MODE_PRIVATE, null);
        } catch (Exception e) {
        }
        Firebase.setAndroidContext(getApplicationContext());
        FirebaseDatabase.getInstance();
        pref = getApplicationContext().getSharedPreferences("login", 0);
        editor = pref.edit();

        //editor = pref.edit();
        if (pref.contains("userid")) {
            nh_userid = pref.getString("userid", "0");
            DepartmentId = pref.getString("DepartmentId", "0");
            sIsAssetVerification = pref.getBoolean("AssetVerification", false);
            sLastAction = pref.getString("LastAction", "2017-01-01");
        }

        try {
            ApiResult.User u = MyApp.getApplication().readUser();
            if (nh_userid.equals("0")) {
                if (u == null)
                    return Service.START_STICKY;
                else
                    nh_userid = MyApp.getApplication().readUser().data.UserId;

            }
            drRef = databaseFirebase.getReference().child(PostUrl.sFirebaseRef).child(nh_userid);
            drRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    sTicketIds = "";
                    for (com.google.firebase.database.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Pi = postSnapshot.getValue(FirebaseTicketData.class);
                        //firebaseIssueData.add(P);
                        Cursor cquery = sql.rawQuery("select IssueId from FirebaseIssueData where IssueId ='" + postSnapshot.getKey() + "'", null);
                        try {
                            if (cquery.getCount() > 0) {
                                ContentValues newValues = new ContentValues();
                                newValues.put("Action", Pi.getAction());
                                newValues.put("IssueId", postSnapshot.getKey());
                                sql.update("FirebaseIssueData", newValues, "IssueId=" + postSnapshot.getKey(), null);
                            } else {
                                sql.execSQL("INSERT INTO FirebaseIssueData(Action,IssueId)VALUES" +
                                        "('" + Pi.getAction() + "','" + postSnapshot.getKey() + "')");

                            }
                        } catch (Exception e) {
                        }
                        cquery.close();
                        sTicketIds = sTicketIds + postSnapshot.getKey() + ",";
                        //ref.child(nh_userid).child(P.IssueId).child("Flag").setValue(0);
                        //}
                    }
                    drRef = databaseFirebase.getReference().child(PostUrl.sFirebaseRefTask).child(nh_userid);
                    drRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            sTaskIds = "";
                            for (com.google.firebase.database.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Pi = postSnapshot.getValue(FirebaseTicketData.class);
                                //firebaseIssueData.add(P);
                                Cursor cquery = sql.rawQuery("select IssueId from FirebaseIssueData where IssueId ='" + postSnapshot.getKey() + "'", null);
                                if (cquery.getCount() > 0) {
                                    ContentValues newValues = new ContentValues();
                                    newValues.put("Action", Pi.getAction());
                                    newValues.put("IssueId", postSnapshot.getKey());
                                    sql.update("FirebaseIssueData", newValues, "IssueId=" + postSnapshot.getKey(), null);
                                } else {
                                    sql.execSQL("INSERT INTO FirebaseIssueData(Action,IssueId)VALUES" +
                                            "('" + Pi.getAction() + "','" + postSnapshot.getKey() + "')");

                                }
                                cquery.close();
                                sTaskIds = sTaskIds + postSnapshot.getKey() + ",";
                                //ref.child(nh_userid).child(P.IssueId).child("Flag").setValue(0);
                                //}
                            }
                            String sAsset;
                            if (sIsAssetVerification)
                                sAsset = "true";
                            else
                                sAsset = "false";

                            mTicketIdList.put("IssueIds", sTicketIds);
                            mTicketIdList.put("TaskIds", sTaskIds);
                            mTicketIdList.put("UserId", nh_userid);
                            mTicketIdList.put("IsAssetVerificationEnable", sAsset);
                            mTicketIdList.put("DepartmentId", DepartmentId);
                            mTicketIdList.put("LastAction", sLastAction);

                            if (!(nh_userid.equals("0"))) {
                                int issuesCount = MyApp.getApplication().readTicketsIssueHistory().keySet().size();
                                if (issuesCount == 0)
                                    if (!MyApp.getStatus("isFirebaseCalled")) {
                                        MyApp.setStatus("isFirebaseCalled", true);


                                        if (issuesCount == 0)
                                            NewTicketsInfo(mTicketIdList);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                MyApp.setStatus("isFirebaseCalled", false);
                                            }
                                        }, 5000);
                                    }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("onCancelled: ", "firebase issue");
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("onCancelled: ", "firebase issue");
                }
            });
        } catch (Exception e) {
        }


        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "locationserviceonDestrcalled");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void NewTicketsInfo(Map mTicketIdList) {
        if (MyApp.isConnectingToInternet(getApplicationContext()))
            try {
                final MainActivity obj = new MainActivity();
                final ApiResult apiResult = new ApiResult();
                final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(mTicketIdList.get("IssueIds").toString(), mTicketIdList.get("TaskIds").toString(), mTicketIdList.get("UserId").toString(), mTicketIdList.get("IsAssetVerificationEnable").toString(), mTicketIdList.get("DepartmentId").toString(), mTicketIdList.get("LastAction").toString());
                Call<ApiResult.IssueDetail> call1 = apiInterface.GetIssuesForFireBase(issueDetail);
                call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                        ApiResult.IssueDetail resData = response.body();
                        if (resData == null || resData.equals("") || resData.equals("0")) {
                            try {
//                            MainActivity m = new MainActivity();
//                            m.updateCounter(getActivity());
//                                MyApp.showMassage(ctx, getString(R.string.internet_error));
//                            Toast.makeText(ctx, R.string.internet_error, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.getMessage();
                            }
                        } else {
//                        sql.execSQL("delete from FirebaseIssueData");
                            if (MyApp.getApplication().readTicketsIssueHistory().keySet().size() == 0)
                                try {
                                    sql.execSQL("delete from Issue_Detail");
                                } catch (Exception e) {
                                }//                        sql.execSQL("delete from Issue_Status");

                            try {
                                Map<String, String> scheduleMap = MyApp.getApplication().readTicketCaptureSchedule();
                                Map<String, TicketInfoClass> issueDetailsHistory = MyApp.getApplication().readIssueDetailsHistory();
                                SimpleDateFormat Updatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                for (int i = 0; i < resData.IssueDetail.length; i++) {
                                    TicketInfoClass t = new TicketInfoClass();
                                    Cursor cqueryTempForLatestStatusId = sql.rawQuery("select StatusId,UpdatedDate from Issue_Detail where IssueId ='" + resData.IssueDetail[i].Id + "'", null);
                                    if (cqueryTempForLatestStatusId.getCount() > 0) {
                                        cqueryTempForLatestStatusId.moveToFirst();
                                        Date localDate = null;
                                        Date liveDate = null;
                                        String strUpdatedDate = "1900-01-01 12:00:00";
                                        if (cqueryTempForLatestStatusId.getString(1) == null) {
                                            strUpdatedDate = resData.IssueDetail[i].UpdatedOn;
                                        } else {
                                            strUpdatedDate = cqueryTempForLatestStatusId.getString(1);
                                        }

                                        //SimpleDateFormat liveUpdatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        try {
                                            localDate = Updatedate.parse(strUpdatedDate);
                                            liveDate = Updatedate.parse(resData.IssueDetail[i].UpdatedOn);
                                            if (localDate.getTime() > liveDate.getTime()) {
                                                t.UpdatedDate = strUpdatedDate;
                                                t.StatusId = cqueryTempForLatestStatusId.getString(0);
                                            } else {
                                                t.UpdatedDate = resData.IssueDetail[i].UpdatedOn;
                                                t.StatusId = resData.IssueDetail[i].StatusId;
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        cqueryTempForLatestStatusId.close();
                                    } else {
                                        t.UpdatedDate = resData.IssueDetail[i].UpdatedOn;
                                        t.StatusId = resData.IssueDetail[i].StatusId;

                                    }
                                    t.Address = resData.IssueDetail[i].EnterpriseAddress;
                                    Log.e("FirstFrag", "StatusId:" + t.StatusId + "IssueID:" + resData.IssueDetail[i].Id);

                                    t.CreatedDate = resData.IssueDetail[i].CreatedOn;
                                    //t.UpdatedDate = object.getString("UpdatedOn");
                                    t.PhoneNo = resData.IssueDetail[i].MobileNumber;
                                    t.SLADate = resData.IssueDetail[i].SLABreachDate;
//                        t.StatusId = object.getString("StatusId");
                                    t.IssueID = resData.IssueDetail[i].Id;
                                    //t.Address=object.getString("StatusName");
                                    t.AssetSubType = resData.IssueDetail[i].AssetSubType;
                                    t.AssetType = resData.IssueDetail[i].AssetType;
                                    t.AssetSerialNumber = resData.IssueDetail[i].AssetSerialNo;
                                    t.CategoryName = resData.IssueDetail[i].CategoryName;
                                    t.CorporateName = resData.IssueDetail[i].CorporateName;
                                    t.IssueText = resData.IssueDetail[i].IssueText;
                                    t.Latitude = resData.IssueDetail[i].Latitude;
                                    t.Longitude = resData.IssueDetail[i].Longitude;
                                    t.ServiceItemNumber = resData.IssueDetail[i].ServiceItemNo;
                                    t.Subject = resData.IssueDetail[i].Subject;
                                    t.TicketHolder = resData.IssueDetail[i].TicketHolder;
                                    t.TicketNumber = resData.IssueDetail[i].TicketNumber;
                                    t.OEMNumber = resData.IssueDetail[i].OEMTicketId;
                                    t.AssetDetail = resData.IssueDetail[i].AssetDetail;
                                    t.ContractSubTypeName = resData.IssueDetail[i].ContractSubTypeName;
                                    t.ContractName = resData.IssueDetail[i].ContractName;
                                    t.IsVerified = resData.IssueDetail[i].IsAssetVerified;
                                    t.PreviousStatus = resData.IssueDetail[i].PreviousStatusId;
                                    t.ScheduleDate = resData.IssueDetail[i].scheduleDate;
                                    t.setType(resData.IssueDetail[i].type);
                                    t.setJourneyStatus(resData.IssueDetail[i].journeyStatus);

                                    scheduleMap.put(t.TicketNumber, MyApp.parseDateTime(t.ScheduleDate).replace(" 12:00 AM", ""));
                                    String savedCardId = MyApp.getSharedPrefString("savedCardId");
                                    if (savedCardId.isEmpty()) {
                                        editor.putString("LastTransport", resData.IssueDetail[0].LastTransportMode);
                                        editor.apply();
                                        editor.commit();
                                    }
                                    Cursor cquery = sql.rawQuery("select Action from FirebaseIssueData where IssueId ='" + t.IssueID + "'", null);
                                    if (cquery.getCount() > 0) {
                                        cquery.moveToFirst();
                                        if (cquery.getString(0).equals("Delete") || cquery.getString(0).equals("delete")) {
                                            ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                            if (!nh_userid.equals("0")) {
                                                ref.child(nh_userid).child(t.IssueID).removeValue();
                                                sql.delete("Issue_Detail", "IssueId" + "=" + t.IssueID, null);
                                            }


                                        } else if (cquery.getString(0).equals("New") || cquery.getString(0).equals("new")) {
                                            Cursor forMainTable = sql.rawQuery("select * from Issue_Detail where IssueId ='" + t.IssueID + "'", null);
                                            if (forMainTable.getCount() > 0) {
                                                sql.delete("Issue_Detail", "IssueId" + "=" + t.IssueID, null);
                                                try {
                                                    if (MyApp.isSmallDate(t.getScheduleDate())) {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','4','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    } else
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','-1','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                try {
                                                    if (MyApp.isSmallDate(t.getScheduleDate())) {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','4','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    } else
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','-1','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
//                                                sendNotification("New Ticket: " + t.TicketNumber, ctx, t.TicketNumber);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            // comment if upper one is not commented
                                        } else if (cquery.getString(0).equals("Update")
                                                || cquery.getString(0).equals("update")) {
                                            char str = cquery.getString(0).charAt(0);
                                            if (str == 'W') {

                                            }
                                            cquery.close();
                                            Cursor forMainTable = sql.rawQuery("select * from Issue_Detail where IssueId ='" + t.IssueID + "'", null);
                                            if (forMainTable.getCount() > 0) {
                                                ContentValues newValues = new ContentValues();
                                                Cursor cursorIssuesWhichComplete = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + t.StatusId + "'", null);
//                                            if (!MyApp.getStatus("isTicketUpdating")) {
                                                if (cursorIssuesWhichComplete.getCount() > 0) {
                                                    cursorIssuesWhichComplete.moveToFirst();
                                                    if (cursorIssuesWhichComplete.getString(0).equals("4")) {
                                                        newValues.put("IsAccepted", "3");//resolved
                                                    } else if (cursorIssuesWhichComplete.getString(0).equals("1") ||
                                                            cursorIssuesWhichComplete.getString(0).equals("5") ||
                                                            cursorIssuesWhichComplete.getString(0).equals("6")) {
                                                        newValues.put("IsAccepted", "1");//pending
                                                    } else {
                                                        newValues.put("IsAccepted", "2");//Attended
                                                    }
                                                }
//                                            } else {
//
//                                            }
                                                newValues.put("StatusId", t.StatusId);
                                                newValues.put("CategoryName", t.CategoryName);
                                                newValues.put("IssueText", t.IssueText);
                                                newValues.put("Subject", t.Subject);
                                                newValues.put("ServiceItemNumber", t.ServiceItemNumber);
                                                newValues.put("AssetSerialNumber", t.AssetSerialNumber);
                                                newValues.put("CreatedDate", t.CreatedDate);
                                                newValues.put("SLADate", t.SLADate);
                                                newValues.put("CorporateName", t.CorporateName);
                                                newValues.put("Address", t.Address);
                                                newValues.put("Latitude", t.Latitude);
                                                newValues.put("Longitude", t.Longitude);
                                                newValues.put("AssetType", t.AssetType);
                                                newValues.put("AssetSubType", t.AssetSubType);
                                                newValues.put("UpdatedDate", t.UpdatedDate);
                                                newValues.put("TicketHolder", t.TicketHolder);
                                                newValues.put("TicketNumber", t.TicketNumber);
                                                newValues.put("IsVerified", t.IsVerified);
                                                newValues.put("OEMNumber", t.OEMNumber);
                                                newValues.put("AssetDetail", t.AssetDetail);
                                                newValues.put("ContractName", t.ContractName);
                                                newValues.put("ContractSubTypeName", t.ContractSubTypeName);
                                                newValues.put("PreviousStatus", t.PreviousStatus);
//                                            newValues.put("ScheduleDate", t.ScheduleDate);
                                                sql.update("Issue_Detail", newValues, "IssueId=" + t.IssueID, null);
                                                forMainTable.close();
                                            } else {
                                                Cursor cursorIssuesWhichComplete = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + t.StatusId + "'", null);
                                                int counter = cursorIssuesWhichComplete.getCount();
                                                if (counter > 0) {
                                                    cursorIssuesWhichComplete.moveToFirst();
                                                    if (MyApp.isSmallDate(t.getScheduleDate())) {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','5','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    } else if (cursorIssuesWhichComplete.getString(0).equals("4")) {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','3','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    } else if (cursorIssuesWhichComplete.getString(0).equals("1") || cursorIssuesWhichComplete.getString(0).equals("5") || cursorIssuesWhichComplete.getString(0).equals("6")) {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','1','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    } else {
                                                        sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                                "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','2','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                    }
                                                }
                                                cursorIssuesWhichComplete.close();
                                            }
                                        }
//
                                    }
                                    issueDetailsHistory.put(t.getIssueID(), t);
                                }


//                                boolean isStatusHaveValue = false;
//
//                                for (String key : issueDetailsHistory.keySet()) {
//                                    try {
//                                        if (!issueDetailsHistory.get(key).getJourneyStatus().equals("0")) {
//                                            isStatusHaveValue = true;
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                                if (isStatusHaveValue) {
                                MyApp.setStatus("isBothDisable", false);
                                MyApp.setStatus("savedCardStatus", false);
                                MyApp.setStatus("isTransportChange", false);
                                MyApp.setSharedPrefString("savedCardId", "");
//                                }
                                for (String key : issueDetailsHistory.keySet()) {

                                    if (MyApp.getSharedPrefString("savedCardId").isEmpty())
                                        if (issueDetailsHistory.get(key).getJourneyStatus().equals("2")) {
                                            if (!getMainStatusId(issueDetailsHistory.get(key).getStatusId())) {
                                                MyApp.setStatus("isBothDisable", false);
                                                MyApp.setStatus("savedCardStatus", true);
                                                MyApp.setStatus("isTransportChange", false);
                                                MyApp.setSharedPrefString("savedCardId", issueDetailsHistory.get(key).getIssueID());
                                            }
                                        } else if (issueDetailsHistory.get(key).getJourneyStatus().equals("4")) {
                                            if (!getMainStatusId(issueDetailsHistory.get(key).getStatusId())) {
                                                MyApp.setStatus("isTransportChange", true);
                                                MyApp.setStatus("isBothDisable", false);
                                                MyApp.setStatus("savedCardStatus", true);
                                                MyApp.setSharedPrefString("savedCardId", issueDetailsHistory.get(key).getIssueID());
                                            }
                                        } else if (issueDetailsHistory.get(key).getJourneyStatus().equals("3")) {
                                            if (!getMainStatusId(issueDetailsHistory.get(key).getStatusId())) {
                                                MyApp.setStatus("isTransportChange", true);
                                                MyApp.setStatus("isBothDisable", true);
                                                MyApp.setStatus("savedCardStatus", true);
                                                MyApp.setSharedPrefString("savedCardId", issueDetailsHistory.get(key).getIssueID());
                                            }
                                        }
                                }


                                obj.setHideAlert();
                                MainActivity m = new MainActivity();
                                m.updateCounter(getApplicationContext(), false);
                                MyApp.getApplication().writeIssueDetailsHistory(issueDetailsHistory);
                                MyApp.getApplication().writeTicketCaptureSchedule(scheduleMap);
                                fetchDataFromLocal();
//                                ((MainActivity) getActivity()).updateCounter(getActivity(), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                        call.cancel();
                        obj.setHideAlert();
                    }
                });
            } catch (Exception e) {
            }
        else {
            fetchDataFromLocal();
        }
    }

    private void fetchDataFromLocal() {
        newTickets.clear();
        testData.clear();
        if (sql != null) {
            Cursor cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);
            for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {

                TicketInfoClass t = new TicketInfoClass();
                t.IssueID = cquery.getString(1);
                t.CategoryName = cquery.getString(2);
                t.Subject = cquery.getString(3);
                t.IssueText = cquery.getString(4);
                t.ServiceItemNumber = cquery.getString(5);
                t.AssetSerialNumber = cquery.getString(6);
                t.CreatedDate = cquery.getString(7);
                t.SLADate = cquery.getString(8);
                t.CorporateName = cquery.getString(9);
                t.Address = cquery.getString(10);
                t.PhoneNo = cquery.getString(11);
                t.StatusId = cquery.getString(12);
                t.TicketNumber = cquery.getString(20);
                newTickets.add(t);
            }
            cquery.close();
        }

        try {
            if (newTickets.size() == 0) {
                MainActivity.removeTkt();
            } else {
//                MainActivity.showTkt();
                MainActivity m = new MainActivity();
                m.updateCounter(getApplicationContext(), false);

                HashMap<String, TicketInfoClass> map = MyApp.getApplication().readTicketCapture();
                for (int i = 0; i < newTickets.size(); i++) {
                    sendNotification("New Ticket: " + newTickets.get(i).TicketNumber, getApplicationContext(), newTickets.get(i).TicketNumber);
                    if (!map.containsKey(newTickets.get(i).IssueID)) {
                        map.put(newTickets.get(i).IssueID, newTickets.get(i));
                    }
                }
                MyApp.getApplication().writeTicketCapture(map);
            }
        } catch (Exception e) {
        }

        captureAllNow();
    }

    private void captureAllNow() {
        HashMap<String, TicketInfoClass> map = MyApp.getApplication().readTicketCapture();
        for (String key : map.keySet()) {
            if (!map.get(key).isCaptured()) {
                callApiToMakeCapture(key);
            } else {
                map.get(key).setCaptured(true);
            }
        }
        MyApp.getApplication().writeTicketCapture(map);
    }

    public void callApiToMakeCapture(final String key) {
        Date cDate = new Date();
        String date = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(cDate);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Log.e("Capture Operation key: ", key);

        String sType = "Ticket";
        Map<String, TicketInfoClass> issueDetailsMap = MyApp.getApplication().readIssueDetailsHistory();
        if (issueDetailsMap.containsKey(key)) {
            if (issueDetailsMap.get(key).getType().equals("Ticket")) {
                sType = "Ticket";
            } else {
                sType = "Task";
            }
        }


        Call<ApiResult.CaptureTicket> call1 = apiInterface.captureTicket(key, nh_userid, date, sType);
        call1.enqueue(new Callback<ApiResult.CaptureTicket>() {
            @Override
            public void onResponse(Call<ApiResult.CaptureTicket> call, Response<ApiResult.CaptureTicket> response) {
                try {
                    ApiResult.CaptureTicket iData = response.body();
                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                        // not uploaded
                    } else {
                        // uploaded
                        Map<String, TicketInfoClass> map = MyApp.getApplication().readTicketCapture();
                        if (map.containsKey(key)) {
                            map.get(key).setCaptured(true);
                            MyApp.getApplication().writeTicketCapture(map);
                        }
                    }
                } catch (Exception e) {
//                   MyApp.showMassage();
                }
            }

            @Override
            public void onFailure(Call<ApiResult.CaptureTicket> call, Throwable t) {
                call.cancel();

            }
        });
    }

    private Map<String, String> notifMap;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String sNotificationMessage, Context activity, String ticketNumber) {
        try {
            if (nh_userid.equals("0") || nh_userid.isEmpty()) {
                return;
            }
        } catch (Exception e) {
        }
        notifMap = MyApp.getApplication().readNotifMap();
        if (!notifMap.containsKey(ticketNumber)) {
            String str = ticketNumber;
            str = str.replaceAll("[^\\d.]", "");
            int ticket = Integer.parseInt(str);
            Intent intent = new Intent(activity, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(activity, (int) System.currentTimeMillis(), intent, 0);
            Notification noti = new Notification.Builder(activity).setContentTitle("MZS Notifier")
                    .setSmallIcon(R.mipmap.som)
                    .setContentText(sNotificationMessage)
                    .setContentIntent(pIntent)
                    .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
                    .addAction(R.drawable.som, "View", pIntent).build();
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            int importance = NotificationManager.IMPORTANCE_HIGH;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                noti = new Notification.Builder(activity).setContentTitle("MZS Notifier")
                        //.setContentText("New Updates in your Task Manager for "+ sIssueId)
                        .setSmallIcon(R.mipmap.som)
                        .setContentText(sNotificationMessage)
                        .setContentIntent(pIntent)
                        .setChannelId(ticket + "")
                        .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
                        .addAction(R.drawable.som, "View", pIntent).build();
                CharSequence name = activity.getString(R.string.app_name);
                NotificationChannel mChannel = new NotificationChannel(ticket + "", name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(ticket, noti);
            //

            ++MULTIPLE_NOTIFICATION;
            notifMap.put(ticketNumber, ticketNumber);
            MyApp.getApplication().writeNotifMap(notifMap);
        }

    }

    private boolean getMainStatusId(String id) {
        ApiResult.IssueStatus.lstDetails[] list = MyApp.getApplication().readIssuesStatusList();
        boolean isClosed = false;
        for (int i = 0; i < list.length; i++) {
            Log.d("maineStatusId", list[i].MainStatusId);
            if (list[i].Id.equals(id)) {
                if (list[i].MainStatusId.equals("4")) {

                    isClosed = true;
                }
            }
        }
        return isClosed;
    }


}
