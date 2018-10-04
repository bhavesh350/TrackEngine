package net.mzi.trackengine;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daprlabs.cardstack.SwipeDeck;
import com.daprlabs.cardstack.SwipeFrameLayout;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

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

import static net.mzi.trackengine.MainActivity.locationAlarmManager;

/**
 * Created by Poonam on 2/2/2017.
 */

public class Firstfrag extends Fragment {
    String DepartmentId, sLastAction;
    ApiInterface apiInterface;
    SharedPreferences.Editor editor;
    String nh_userid;
    boolean sIsAssetVerification;
    static SwipeDeck cardStack;
    String sTicketIds = "";
    FirebaseTicketData Pi = new FirebaseTicketData();
    SwipeFrameLayout sfl;
    SQLiteDatabase sql;
    public Map<String, String> mTicketIdList = new HashMap<>();
    int MULTIPLE_NOTIFICATION = 0;
    Context ctx;
    SharedPreferences pref;

    static List<TicketInfoClass> newTickets = new ArrayList<TicketInfoClass>();
    final static ArrayList<String> testData = new ArrayList<>();
    Firebase ref = null;

    FirebaseDatabase databaseFirebase = FirebaseDatabase.getInstance();
    DatabaseReference drRef;

    public Firstfrag() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = context;
    }

    @Override
    public void onDetach() {
        try {
            ctx.unregisterReceiver(receiver);
        } catch (Exception E) {
        }
        super.onDetach();
    }

    private NetworkChangeReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();

        receiver = new NetworkChangeReceiver();

        ctx.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.first, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = getActivity().getApplicationContext().openOrCreateDatabase("MZI.sqlite",
                getActivity().getApplicationContext().MODE_PRIVATE, null);
        Firebase.setAndroidContext(getActivity().getApplicationContext());
        databaseFirebase.getInstance();
        cardStack = view.findViewById(R.id.swipe_deck);
        sfl = view.findViewById(R.id.ma);
        pref = getActivity().getSharedPreferences("login", 0);
        editor = pref.edit();
        //editor = pref.edit();
        if (pref.contains("userid")) {
            nh_userid = pref.getString("userid", "0");
            DepartmentId = pref.getString("DepartmentId", "0");
            sIsAssetVerification = pref.getBoolean("AssetVerification", false);
            sLastAction = pref.getString("LastAction", "2017-01-01");
        }

        try {
            drRef = databaseFirebase.getReference().child(PostUrl.sFirebaseRef).child(nh_userid);
            drRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    sTicketIds = "";
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
                        sTicketIds = sTicketIds + postSnapshot.getKey() + ",";
                        //ref.child(nh_userid).child(P.IssueId).child("Flag").setValue(0);
                        //}
                    }
                    String sAsset;
                    if (sIsAssetVerification)
                        sAsset = "true";
                    else
                        sAsset = "false";

                    mTicketIdList.put("IssueIds", sTicketIds);
                    mTicketIdList.put("UserId", nh_userid);
                    mTicketIdList.put("IsAssetVerificationEnable", sAsset);
                    mTicketIdList.put("DepartmentId", DepartmentId);
                    mTicketIdList.put("LastAction", sLastAction);

                    if (!(nh_userid.equals("0"))) ;
                    {
                        String sTktInJson = new Gson().toJson(mTicketIdList);
                        NewTicketsInfo(mTicketIdList);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("onCancelled: ", "firebase issue");
                }
            });


            cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
                @Override
                public void cardSwipedLeft(int position) {
                    Log.i("MainActivity", "card was swiped left, position in adapter: " + position);
                }

                @Override
                public void cardSwipedRight(int position) {
                    Log.i("MainActivity", "card was swiped right, position in adapter: " + position);
                }

                @Override
                public void cardsDepleted() {
                    Log.i("MainActivity", "no more cards");
                    MainActivity.removeTkt();
                    //  getActivity().getSupportFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.fragment)).commit();
                }

                @Override
                public void cardActionDown() {
                    Log.i("MainActivity", "card was swiped down, position in adapter: ");
                }

                @Override
                public void cardActionUp() {
                    Log.i("MainActivity", "card was swiped up, position in adapter: ");
                }
            });
        } catch (Exception e) {
        }

//        h.postDelayed(dataReSendEvery10Mins, 10 * 60 * 1000);

        Intent locationIntent = new Intent(getActivity(), ServiceDataUpdateFirstFragment.class);
        PendingIntent locationPendingIntent = PendingIntent.getService(getActivity(), 10, locationIntent, 0);
        locationAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        locationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                (System.currentTimeMillis() + (60 * 10 * 1000)), 60 * 10 * 1000, locationPendingIntent);

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(String sNotificationMessage, Context activity, String ticketNumber) {
        String str = ticketNumber;
        str = str.replaceAll("[^\\d.]", "");
        int ticket = Integer.parseInt(str);
        //Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent intent = new Intent(getActivity(), MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getActivity(), (int) System.currentTimeMillis(), intent, 0);
        Notification noti = new Notification.Builder(getContext()).setContentTitle("MZS Notifier")
                //.setContentText("New Updates in your Task Manager for "+ sIssueId)
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
            noti = new Notification.Builder(getContext()).setContentTitle("MZS Notifier")
                    //.setContentText("New Updates in your Task Manager for "+ sIssueId)
                    .setSmallIcon(R.mipmap.som)
                    .setContentText(sNotificationMessage)
                    .setContentIntent(pIntent)
                    .setChannelId(ticket + "")
                    .setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone))
                    .addAction(R.drawable.som, "View", pIntent).build();
            CharSequence name = getActivity().getString(R.string.app_name);
            NotificationChannel mChannel = new NotificationChannel(ticket + "", name, importance);
//            AudioAttributes att = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                    .build();
//            mChannel.setSound(Uri.parse("android.resource://" + "net.mzi.trackengine" + "/" + R.raw.message_tone),att);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(ticket, noti);
        //

        ++MULTIPLE_NOTIFICATION;
    }

    private void fetchDataFromLocal() {
        newTickets.clear();
        testData.clear();
        Cursor cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);
        for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {

            TicketInfoClass t = new TicketInfoClass();
            t.IssueID = cquery.getString(1).toString();
            t.CategoryName = cquery.getString(2).toString();
            t.Subject = cquery.getString(3).toString();
            t.IssueText = cquery.getString(4).toString();
            t.ServiceItemNumber = cquery.getString(5).toString();
            t.AssetSerialNumber = cquery.getString(6).toString();
            t.CreatedDate = cquery.getString(7).toString();
            t.SLADate = cquery.getString(8).toString();
            t.CorporateName = cquery.getString(9).toString();
            t.Address = cquery.getString(10).toString();
            t.PhoneNo = cquery.getString(11).toString();
            t.StatusId = cquery.getString(12);
            t.TicketNumber = cquery.getString(20);
            newTickets.add(t);
        }
        cquery.close();
        if (newTickets.size() == 0) {
            MainActivity.removeTkt();
        } else {
            MainActivity.showTkt();
            MainActivity m = new MainActivity();
            m.updateCounter(ctx);
            final SwipeDeckAdapter adapter = new SwipeDeckAdapter(newTickets, getActivity());
            cardStack.setAdapter(adapter);
            for (int i = 0; i < newTickets.size(); i++) {
                sendNotification("New Ticket: " + newTickets.get(i).TicketNumber, ctx, newTickets.get(i).TicketNumber);
            }

        }
    }

    public void swipeLeft(String issueId, Context context) {
        Date cDate = new Date();
        MainActivity m = new MainActivity();
        cardStack.swipeTopCardLeft(1000);
        m.updateCounter(context);
    }

    public void swipeRight(String issueId, Context context) {
        Date cDate = new Date();
        MainActivity m = new MainActivity();
        cardStack.swipeTopCardRight(1000);
        m.updateCounter(context);

    }


    public void NewTicketsInfo(Map mTicketIdList) {
        Log.d("Bhavesh call", "updating data by fragment for the ticketes info");
        try {
            final MainActivity obj = new MainActivity();
            final ApiResult apiResult = new ApiResult();
            final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(mTicketIdList.get("IssueIds").toString(), mTicketIdList.get("UserId").toString(), mTicketIdList.get("IsAssetVerificationEnable").toString(), mTicketIdList.get("DepartmentId").toString(), mTicketIdList.get("LastAction").toString());
            Call<ApiResult.IssueDetail> call1 = apiInterface.GetIssuesForFireBase(issueDetail);
            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                    ApiResult.IssueDetail resData = response.body();
                    if (resData == null || resData.equals("") || resData.equals("0")) {
                        try {
                            MainActivity m = new MainActivity();
                            m.updateCounter(getActivity());
                            MyApp.showMassage(ctx, getString(R.string.internet_error));
//                            Toast.makeText(ctx, R.string.internet_error, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.getMessage();
                        }
                    } else {
                        try {

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
                                        strUpdatedDate = cqueryTempForLatestStatusId.getString(1).toString();
                                    }

                                    //SimpleDateFormat liveUpdatedate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try {
                                        localDate = Updatedate.parse(strUpdatedDate);
                                        liveDate = Updatedate.parse(resData.IssueDetail[i].UpdatedOn);
                                        if (localDate.getTime() > liveDate.getTime()) {
                                            t.UpdatedDate = strUpdatedDate;
                                            t.StatusId = cqueryTempForLatestStatusId.getString(0).toString();
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
                                editor.putString("LastTransport", resData.IssueDetail[0].LastTransportMode);
                                editor.apply();
                                editor.commit();
                                Cursor cquery = sql.rawQuery("select Action from FirebaseIssueData where IssueId ='" + t.IssueID + "'", null);
                                if (cquery.getCount() > 0) {
                                    cquery.moveToFirst();
                                    if (cquery.getString(0).toString().equals("Delete") || cquery.getString(0).toString().equals("delete")) {
                                        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                        ref.child(nh_userid).child(t.IssueID).removeValue();
                                        sql.delete("Issue_Detail", "IssueId" + "=" + t.IssueID, null);

                                    } else if (cquery.getString(0).toString().equals("New") || cquery.getString(0).toString().equals("new")) {
                                        Cursor forMainTable = sql.rawQuery("select * from Issue_Detail where IssueId ='" + t.IssueID + "'", null);
                                        if (forMainTable.getCount() > 0) {
                                        } else {

                                            try {
                                                sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                        "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','-1','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                sendNotification("New Ticket: " + t.TicketNumber, ctx, t.TicketNumber);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        try {
                                            sendNotification("New Ticket: " + t.TicketNumber, ctx, t.TicketNumber);
                                        } catch (Exception e) {
                                            Log.e("Notification error ", "at 280");
                                        }
                                        // comment if upper one is not commented
                                    } else if (cquery.getString(0).toString().equals("Update") || cquery.getString(0).toString().equals("update")) {
                                        char str = cquery.getString(0).toString().charAt(0);
                                        if (str == 'W') {

                                        }
                                        cquery.close();
                                        Cursor forMainTable = sql.rawQuery("select * from Issue_Detail where IssueId ='" + t.IssueID + "'", null);
                                        if (forMainTable.getCount() > 0) {
                                            ContentValues newValues = new ContentValues();
                                            Cursor cursorIssuesWhichComplete = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + t.StatusId + "'", null);
                                            if (cursorIssuesWhichComplete.getCount() > 0) {
                                                cursorIssuesWhichComplete.moveToFirst();
                                                if (cursorIssuesWhichComplete.getString(0).toString().equals("4")) {
                                                    newValues.put("IsAccepted", "3");//resolved
                                                } else if (cursorIssuesWhichComplete.getString(0).toString().equals("1") || cursorIssuesWhichComplete.getString(0).toString().equals("5") || cursorIssuesWhichComplete.getString(0).toString().equals("6")) {
                                                    newValues.put("IsAccepted", "1");//pending
                                                } else {
                                                    newValues.put("IsAccepted", "2");//Attended
                                                }
                                            }
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
                                            sql.update("Issue_Detail", newValues, "IssueId=" + t.IssueID, null);
                                            forMainTable.close();
                                        } else {
                                            Cursor cursorIssuesWhichComplete = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + t.StatusId + "'", null);
                                            if (cursorIssuesWhichComplete.getCount() > 0) {
                                                cursorIssuesWhichComplete.moveToFirst();
                                                if (cursorIssuesWhichComplete.getString(0).toString().equals("4")) {
                                                    sql.execSQL("INSERT INTO Issue_Detail(IssueId ,CategoryName,Subject,IssueText,ServiceItemNumber,AssetSerialNumber,CreatedDate,SLADate,CorporateName,Address,Latitude,Longitude,PhoneNo,IsAccepted,StatusId,AssetType,AssetSubType,UpdatedDate,TicketHolder,TicketNumber,IsVerified,OEMNumber,AssetDetail,ContractSubTypeName,ContractName,PreviousStatus)VALUES" +
                                                            "('" + t.IssueID + "','" + t.CategoryName + "','" + t.Subject + "','" + t.IssueText + "','" + t.ServiceItemNumber + "','" + t.AssetSerialNumber + "','" + t.CreatedDate + "','" + t.SLADate + "','" + t.CorporateName + "','" + t.Address + "','" + t.Latitude + "','" + t.Longitude + "','" + t.PhoneNo + "','3','" + t.StatusId + "','" + t.AssetType + "','" + t.AssetSubType + "','" + t.UpdatedDate + "','" + t.TicketHolder + "','" + t.TicketNumber + "','" + t.IsVerified + "','" + t.OEMNumber + "','" + t.AssetDetail + "','" + t.ContractSubTypeName + "','" + t.ContractName + "','" + t.PreviousStatus + "')");
                                                } else if (cursorIssuesWhichComplete.getString(0).toString().equals("1") || cursorIssuesWhichComplete.getString(0).toString().equals("5") || cursorIssuesWhichComplete.getString(0).toString().equals("6")) {
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
                                    MainActivity m = new MainActivity();
                                    m.updateCounter(getActivity());
                                }
                            }
                            fetchDataFromLocal();
                            obj.setHideAlert();
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
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (isOnline(context)) {
                    drRef = databaseFirebase.getReference().child(PostUrl.sFirebaseRef).child(nh_userid);
                    drRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                            sTicketIds = "";
                            for (com.google.firebase.database.DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                Pi = postSnapshot.getValue(FirebaseTicketData.class);
                                //firebaseIssueData.add(P);
                                try {
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
                                    sTicketIds = sTicketIds + postSnapshot.getKey() + ",";
                                    cquery.close();
                                } catch (Exception e) {
                                }
                                //ref.child(nh_userid).child(P.IssueId).child("Flag").setValue(0);
                                //}
                            }
                            String sAsset;
                            if (sIsAssetVerification)
                                sAsset = "true";
                            else
                                sAsset = "false";

                            mTicketIdList.put("IssueIds", sTicketIds);
                            mTicketIdList.put("UserId", nh_userid);
                            mTicketIdList.put("IsAssetVerificationEnable", sAsset);
                            mTicketIdList.put("DepartmentId", DepartmentId);
                            mTicketIdList.put("LastAction", sLastAction);

                            if (!(nh_userid.equals("0"))) ;
                            {
//                                String sTktInJson = new Gson().toJson(mTicketIdList);
                                NewTicketsInfo(mTicketIdList);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("onCancelled: ", "firebase issue");
                        }
                    });
//                    dialog(true);
                    Log.e("keshav", "Online Connect Intenet ");
                } else {
//                    Toast.makeText(getContext(), "Offline", Toast.LENGTH_SHORT).show();
//                    dialog(false);
                    Log.e("keshav", "Conectivity Failure !!! ");
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
                return false;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        sendNotification("test",getActivity());
    }
}