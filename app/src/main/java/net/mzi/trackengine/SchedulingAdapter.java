package net.mzi.trackengine;


import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.gson.Gson;

import net.mzi.trackengine.fragment.EngineerFeedbackFragment;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Roam12 on 11/30/2016.
 */
public class SchedulingAdapter extends RecyclerView.Adapter<SchedulingAdapter.ViewHolder> implements LocationListener {
    ApiInterface apiInterface;
    LocationManager locationManager;
    SharedPreferences.Editor editor;
    boolean isGPSEnabled = false;
    Location location;
    static Double latitude, longitude;
    //    boolean isNetworkEnabled = false;
    SharedPreferences pref;
    String DepartmentId, LastTransportMode = "0";
    static String isAttended;
    String nh_userid, sParentComapnyId;
    boolean bCSATEnable, bAssetVerified;
    List<String> mTicketNumber = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> statusList = new ArrayList<String>();
    List<String> statusListIds = new ArrayList<String>();
    List<String> mtTransportlistName = new ArrayList<String>();
    List<String> mtTransportlistIds = new ArrayList<String>();
    SQLiteDatabase sql;
    boolean flag = true;
    static String sMainStatusId = "0";
    Cursor cquery;
    String currentTime;
    String sDeviceId, sPreviousId;
    String API_URL = null;
    Map<String, String> postTktStatus = new HashMap<>();
    public static final int New = 0;
    public static final int Pending = 1;
    public static final int Complete = 3;
    public static final int Attended = 2;
    public static final int Scheduled = 4;
    public static final int PendingScheduled = 5;
    String sAcceptStatus;

    List<String> mName = new ArrayList<>();
    List<String> mLatLng = new ArrayList<>();
    List<String> mTime = new ArrayList<>();
    List<String> mSub = new ArrayList<>();
    List<String> mMob = new ArrayList<>();
    List<String> mLoc = new ArrayList<>();
    List<String> mCardType = new ArrayList<>();
    List<String> mIssueID = new ArrayList<>();
    List<Integer> mCardColor = new ArrayList<>();
    static int poss;
    String currentStatus;
    static View nhn;
    static Firebase ref = null;
    Context ctx = null;
    Context context;
    private boolean statusByHierarchy;
    private Map<String, TicketInfoClass> issueDetailsMap;

    public SchedulingAdapter() {

    }

    public SchedulingAdapter(List<String> mIssueID, List<String> mName, List<String> mTime, List<String> mLoc, List<String> mMob,
                             List<String> mSub, List<Integer> mDatasetTypes, List<Integer> mCardColor, List<String> mCardType, List<String> mTicketNumber, Map<String, TicketInfoClass> issueDetailsMap, Context context, List<String> mLatLnt) {
        this.statusByHierarchy = MyApp.getStatus("statusByHierarchy");
        this.mName = mName;
        this.mTime = mTime;
        this.mLoc = mLoc;
        this.mMob = mMob;
        this.mSub = mSub;
        this.mDatasetTypes = mDatasetTypes;
        this.mCardColor = mCardColor;
        this.mCardType = mCardType;
        this.mIssueID = mIssueID;
        this.mTicketNumber = mTicketNumber;
        this.context = context;
        this.issueDetailsMap = issueDetailsMap;
        this.ctx = context;
        this.mLatLng = mLatLnt;

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class TicketHolder extends ViewHolder {
        TextView IssueID, IssueText, time, Adress, StatusId, schedule_date, txt_lable;
        FloatingActionButton acceptButton, rejectButton;

        public TicketHolder(View itemView) {
            super(itemView);
            View v = itemView;
            IssueID = v.findViewById(R.id.textView2);
            schedule_date = v.findViewById(R.id.schedule_date);
            IssueText = v.findViewById(R.id.subject);
            Adress = v.findViewById(R.id.adrs);
            StatusId = v.findViewById(R.id.newtktstatus);
            time = v.findViewById(R.id.beontime);
            acceptButton = v.findViewById(R.id.accept);
            rejectButton = v.findViewById(R.id.reject);
            txt_lable = v.findViewById(R.id.txt_lable);
        }
    }

    private class SchedulingViewHolder extends ViewHolder {
        TextView name, time, sub, location, mobile, stats, sIssueID, preTime, schedule_date_value, txt_lable;
        ImageView mbut;
        LinearLayout bot;
        RelativeLayout top, pendinglayout;
        final PopupMenu popup;
        ImageButton btn_start, btn_reached;


        public SchedulingViewHolder(View v) {
            super(v);
            this.name = v.findViewById(R.id.contactpersonname);
            this.schedule_date_value = v.findViewById(R.id.schedule_date_value);
            this.location = v.findViewById(R.id.adrs);
            this.mobile = v.findViewById(R.id.cntctprsnmob);
            this.time = v.findViewById(R.id.beontime);
            this.sub = v.findViewById(R.id.subject);
            this.mbut = v.findViewById(R.id.mb);
            this.stats = v.findViewById(R.id.status);
            this.top = v.findViewById(R.id.topLayout2);
            this.pendinglayout = v.findViewById(R.id.pendinglayout);
            this.bot = v.findViewById(R.id.bottomLayout);
            this.sIssueID = v.findViewById(R.id.issueId_Id);
            this.txt_lable = v.findViewById(R.id.txt_lable);
            popup = new PopupMenu(ctx, mbut);
            popup.inflate(R.menu.toolbar_card_menu);

            try {
                btn_start = v.findViewById(R.id.btn_start);
                btn_reached = v.findViewById(R.id.btn_reached);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
//        ctx = parent.getContext();
        Firebase.setAndroidContext(ctx);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = ctx.getSharedPreferences("login", 0);
        editor = pref.edit();
        nh_userid = pref.getString("userid", "userid");
        LastTransportMode = pref.getString("LastTransport", "LastTransportMode");
        bCSATEnable = pref.getBoolean("CSATEnable", false);
        bAssetVerified = pref.getBoolean("AssetVerification", false);
        sParentComapnyId = pref.getString("ParentCompanyId", "0");
        DepartmentId = pref.getString("DepartmentId", "0");
        sDeviceId = pref.getString("DeviceId", "0");
        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        if (viewType == New) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cardxml, parent, false);
            return new TicketHolder(v);
        } else if (viewType == Scheduled || viewType == PendingScheduled) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cardxml_pending, parent, false);
            return new TicketHolder(v);
        } else if (viewType == Complete) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedulinglistcard, parent, false);
            nhn = v;
            return new SchedulingViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedulinglistcard_new, parent, false);
            nhn = v;
            return new SchedulingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
//        Boolean isFuture = false;
//        if (MyApp.isSmallDate(mTime.get(position))) {
//            isFuture = true;
//        }


        if (holder.getItemViewType() == New || holder.getItemViewType() == Scheduled || holder.getItemViewType() == PendingScheduled) {
            final TicketHolder ticketHolder = (TicketHolder) holder;
            String sDateUI;
            sDateUI = mTime.get(position);
            if (sDateUI.equals("")) {
                sDateUI = "NA";
            } else {
                sDateUI = sDateUI.substring(11, 19);
            }
            Map<String, String> map = MyApp.getApplication().readTicketCaptureSchedule();
            ticketHolder.IssueID.setText(mTicketNumber.get(position));
            ticketHolder.Adress.setText(mLoc.get(position));
            ticketHolder.StatusId.setText(mCardType.get(position).equals("Open") ? "Accepted" : mCardType.get(position));
            ticketHolder.time.setText(sDateUI);
            ticketHolder.IssueText.setText(mSub.get(position));
            ticketHolder.schedule_date.setText(map.get(mTicketNumber.get(position)));

            Map<String, TicketInfoClass> savedDetailsMap = MyApp.getApplication().readIssueDetailsHistory();

            if (savedDetailsMap.containsKey(mIssueID.get(position))) {
                if (savedDetailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                    ticketHolder.txt_lable.setText("Issue ID");
                } else {
                    ticketHolder.txt_lable.setText("Task ID");
                }
            } else {
                ticketHolder.txt_lable.setText("Issue ID");
            }

            ticketHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAttended = "0";
                    Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "' ", null);
                    if (cquery.getCount() > 0) {
                        cquery.moveToFirst();
                        sAcceptStatus = cquery.getString(0);
                    } else
                        sAcceptStatus = "0";
                    Date cDate = new Date();
                    String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    NewTaskAdapter s = new NewTaskAdapter();
                    Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                    Map<String, String> map = new HashMap<>();
                    map.put("TicketId", mIssueID.get(position));
                    map.put("ticketNumber", mTicketNumber.get(position));
                    map.put("UserId", nh_userid);
                    map.put("StatusId", sAcceptStatus);
                    map.put("ParentCompanyId", sParentComapnyId);
                    map.put("Comment", "Accepted By Engineer");
                    map.put("ActivityDate", currentDateTimeString);
                    map.put("SyncStatus", "-1");
                    map.put("TaskId", "0");
                    if (issueDetailsMap.containsKey(mIssueID.get(position)))
                        if (issueDetailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                            map.put("TaskId", "0");
                        } else {
                            map.put("TicketId", "0");
                            map.put("TaskId", mIssueID.get(position));
                        }

                    if (ticketsMap.containsKey(mIssueID.get(position))) {
                        ticketsMap.put(mIssueID.get(position) + "@@" + System.currentTimeMillis(), map);
                        MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);
                    } else {
                        ticketsMap.put(mIssueID.get(position), map);
                        MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);
                    }

                    cquery = sql.rawQuery("select IssueId,TicketHolder,SLADate,Address,PhoneNo,Subject,StatusId,TicketNumber from Issue_Detail where IssueId ='" + mIssueID.get(position) + "'", null);
                    cquery.moveToFirst();

                    ContentValues newValues = new ContentValues();
                    newValues.put("IsAccepted", "1");
                    newValues.put("PreviousStatus", cquery.getString(6));
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("UpdatedDate", currentDateTimeString);
                    try {
                        sql.update("Issue_Detail", newValues, "IssueId=" + mIssueID.get(position), null);
                    } catch (Exception e) {
                    }

                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId,
                            mIssueID.get(position), sAcceptStatus, "Accepted by Engineer", currentDateTimeString, DepartmentId, "", "", "", sDeviceId, "true", "", "", "", "", "");

                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);
                    call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                        @Override
                        public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                            ApiResult.IssueDetail iData = response.body();
                            if (iData.resData.Status == null || iData.resData.Status.equals("")
                                    || iData.resData.Status.equals("0")) {
                                try {
                                    MyApp.showMassage(context, context.getString(R.string.internet_error));
                                } catch (Exception e) {
                                    e.getMessage();
                                }
                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                try {
                                    Map<String, String> map = issueMap.get(mIssueID.get(position));
                                    map.put("SyncStatus", "false");
                                    issueMap.put(mIssueID.get(position), map);
                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                } catch (Exception e) {
                                }
                            } else {
                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                try {
                                    Map<String, String> map = issueMap.get(mIssueID.get(position));
                                    map.put("SyncStatus", "true");
                                    issueMap.remove(mIssueID.get(position));
                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                } catch (Exception e) {
                                }
                                Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);
                                ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                if (cqueryTemp.getCount() > 0) {
                                    cqueryTemp.moveToFirst();
                                    ref.child(MainActivity.LOGINID).child(mIssueID.get(position)).child("Action").setValue("Update");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                            call.cancel();
                        }
                    });


                    Cursor cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId ='" + cquery.getString(6) + "'", null);
                    cqueryForStatus.moveToFirst();
                    addCard(cquery.getString(0), cquery.getString(1), cquery.getString(2), cquery.getString(3), cquery.getString(4), cquery.getString(5), cqueryForStatus.getString(0), R.color.orange, Pending, position, cquery.getString(7));
                    cquery.close();
                }
            });
//            final Boolean finalIsFuture = isFuture;
            ticketHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (finalIsFuture) {
//                        MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                        return;
//                    }
                    isAttended = "0";
                    final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                    Dialog.setTitle("Rejection Reason: ");
                    Dialog.setCancelable(false);
                    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = li.inflate(R.layout.rejection, null);
                    final EditText commemt = dialogView.findViewById(R.id.rejectionReason);
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
                            boolean wantToCloseDialog = true;
                            if (commemt.getText().toString().length() == 0) {
                                commemt.setError("Please enter comment");
                                wantToCloseDialog = false;
                            }
                            if (wantToCloseDialog) {
                                Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "'", null);
                                if (cquery.getCount() > 0) {
                                    cquery.moveToFirst();
                                    sAcceptStatus = cquery.getString(0);
                                } else
                                    sAcceptStatus = "0";
                                Firstfrag f = new Firstfrag();
                                Date cDate = new Date();
                                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                                NewTaskAdapter s = new NewTaskAdapter();

                                Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                                Map<String, String> map = new HashMap<>();
                                map.put("TicketId", mIssueID.get(position));
                                map.put("ticketNumber", mTicketNumber.get(position));
                                map.put("UserId", nh_userid);
                                map.put("StatusId", sAcceptStatus);
                                map.put("ParentCompanyId", sParentComapnyId);
                                map.put("Comment", commemt.getText().toString());
                                map.put("ActivityDate", currentDateTimeString);
                                map.put("SyncStatus", "-1");
                                map.put("TaskId", "0");
                                if (issueDetailsMap.containsKey(mIssueID.get(position)))
                                    if (issueDetailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                                        map.put("TaskId", "0");
                                    } else {
                                        map.put("TicketId", "0");
                                        map.put("TaskId", mIssueID.get(position));
                                    }
                                if (ticketsMap.containsKey(mIssueID.get(position))) {
                                    ticketsMap.put(mIssueID.get(position) + "@@" + System.currentTimeMillis(), map);
                                    MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);
                                } else {
                                    ticketsMap.put(mIssueID.get(position), map);
                                    MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);
                                }

                                final ApiResult apiResult = new ApiResult();
                                final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId, mIssueID.get(position), sAcceptStatus, commemt.getText().toString(), currentDateTimeString, DepartmentId, "", "", "", sDeviceId, "-1", "", "", "", "", "");
                                Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                                call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                    @Override
                                    public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                        ApiResult.IssueDetail iData = response.body();
                                        if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                            try {
                                                MyApp.showMassage(context, context.getString(R.string.internet_error));
                                            } catch (Exception e) {
                                                e.getMessage();
                                            }
                                            Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                            try {
                                                Map<String, String> map = issueMap.get(mIssueID.get(position));
                                                map.put("SyncStatus", "false");
                                                issueMap.put(mIssueID.get(position), map);
                                                MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                            } catch (Exception e) {
                                            }
                                        } else {
                                            MainActivity m = new MainActivity();
                                            m.updateCounter(context, false);
                                            Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                            try {
                                                Map<String, String> map = issueMap.get(mIssueID.get(position));
                                                map.put("SyncStatus", "true");
                                                issueMap.remove(mIssueID.get(position));
                                                MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                            } catch (Exception e) {
                                            }
                                            Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);
                                            ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                            if (cqueryTemp.getCount() > 0) {
                                                cqueryTemp.moveToFirst();
                                                ref.child(MainActivity.LOGINID).child(mIssueID.get(position)).child("Action").setValue("Delete");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                                        call.cancel();
                                    }
                                });
                                MainActivity m = new MainActivity();
                                removeCard(position);
                                dialog.dismiss();
                            }

                        }
                    });

                }
            });

        } else {
//            final Boolean finalIsFuture1 = isFuture;

            Map<String, String> map = MyApp.getApplication().readTicketCaptureSchedule();
            final SchedulingViewHolder schedulingholder = (SchedulingViewHolder) holder;

            String savedCardId = MyApp.getSharedPrefString("savedCardId");
            boolean savedCardStatus = MyApp.getStatus("savedCardStatus");
            boolean isBothDisable = MyApp.getStatus("isBothDisable");
            try {
                if (!savedCardId.isEmpty()) {
                    if (mIssueID.get(position).equals(savedCardId) && !isBothDisable) {
                        if (savedCardStatus) {
                            if (MyApp.getStatus("isTransportChange")) {
                                schedulingholder.btn_start.setImageResource(R.drawable.btn_start);
                                schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                                schedulingholder.btn_reached.setEnabled(false);
                                schedulingholder.btn_start.setEnabled(true);
                            } else {
                                schedulingholder.btn_start.setImageResource(R.drawable.btn_d_start);
                                schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop);
                                schedulingholder.btn_reached.setEnabled(true);
                                schedulingholder.btn_start.setEnabled(false);
                            }

                        } else {
                            schedulingholder.btn_start.setImageResource(R.drawable.btn_start);
                            schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                            schedulingholder.btn_reached.setEnabled(false);
                            schedulingholder.btn_start.setEnabled(true);
                        }
                    } else if (isBothDisable/* && mIssueID.get(position).equals(savedCardId)*/) {
                        schedulingholder.btn_start.setImageResource(R.drawable.btn_d_start);
                        schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                        schedulingholder.btn_start.setEnabled(false);
                        schedulingholder.btn_reached.setEnabled(false);
                        try {
                            ((TaskActivity) context).updateButtons();
                        } catch (Exception e) {
                        }
                    } /*else if (isBothDisable) {
                        schedulingholder.btn_start.setImageResource(R.drawable.btn_start);
                        schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                        schedulingholder.btn_start.setEnabled(true);
                        schedulingholder.btn_reached.setEnabled(false);
                    }*/ else {
                        schedulingholder.btn_start.setImageResource(R.drawable.btn_d_start);
                        schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                        schedulingholder.btn_start.setEnabled(false);
                        schedulingholder.btn_reached.setEnabled(false);
                    }
                } else {
                    schedulingholder.btn_start.setImageResource(R.drawable.btn_start);
                    schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                    schedulingholder.btn_start.setEnabled(true);
                    schedulingholder.btn_reached.setEnabled(false);
                }

                if (MyApp.getStatus(mIssueID.get(position) + "RED")) {
                    schedulingholder.btn_start.setImageResource(R.drawable.btn_d_start);
                    schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                    schedulingholder.btn_start.setEnabled(false);
                    schedulingholder.btn_reached.setEnabled(false);
                }

                if (mCardColor.get(position) == R.color.colorPrimaryDark
                        || mCardColor.get(position) == R.color.colorBackgroundEnd) {
                    schedulingholder.btn_start.setImageResource(R.drawable.btn_d_start);
                    schedulingholder.btn_reached.setImageResource(R.drawable.btn_stop_grey);
                    schedulingholder.btn_start.setEnabled(false);
                    schedulingholder.btn_reached.setEnabled(false);
                }

                schedulingholder.btn_start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        if (finalIsFuture1) {
//                            MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                            return;
//                        }
                        MyApp.setStatus("isBothDisable", false);
                        MyApp.setStatus("isTransportChange", false);
                        populateStatusStartReach(mIssueID.get(position), position, "", true);
                    }
                });

                schedulingholder.btn_reached.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MyApp.setStatus("isTransportChange", false);
//                        if (finalIsFuture1) {
//                            MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                            return;
//                        }
                        AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setTitle("Change transport?").setMessage("Do you want to change transport mode?")
                                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        if (LastTransportMode.equals("0")) {
                                            Log.e("Transport", "zero");
                                        } else {
                                            Log.e("Transport", LastTransportMode);
                                        }
                                        Cursor cquery1 = sql.rawQuery("select IsPublic from ModeOfTrasportList where TransportId='" + LastTransportMode + "'", null);
                                        if (cquery1.getCount() > 0) {
                                            cquery1.moveToFirst();
                                            if (cquery1.getString(0).equals("true")) {
                                                {
                                                    android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
                                                    alertDialog.setTitle("Transport Expense");
                                                    alertDialog.setMessage("Enter amount");
                                                    alertDialog.setCancelable(false);
                                                    final EditText input = new EditText(context);
                                                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                            300,
                                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                                    input.setGravity(Gravity.CENTER);
                                                    lp.setMargins(20, 0, 20, 0);
                                                    input.setLayoutParams(lp);
                                                    input.setHint("Amount");
                                                    alertDialog.setView(input);
                                                    alertDialog.setIcon(R.mipmap.som);

                                                    alertDialog.setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    String password = input.getText().toString();
                                                                    if (!password.isEmpty()) {
                                                                        goForStartReachStatus("4", position, password);
                                                                        MyApp.setStatus("isTransportChange", true);
//                                                                        MyApp.setSharedPrefString("savedCardId", "");
                                                                        notifyDataSetChanged();
                                                                        try {
                                                                            ((TaskActivity) context).updateButtons();
                                                                        } catch (Exception e) {
                                                                        }
                                                                    } else {
                                                                        MyApp.popMessage("Alert!", "Please enter amount.", context);
                                                                    }
                                                                }
                                                            });
                                                    alertDialog.show();
                                                }
                                            } else {
                                                goForStartReachStatus("4", position, "0");
                                                MyApp.setStatus("isTransportChange", true);
//                                                MyApp.setStatus("savedCardStatus", false);
//                                                MyApp.setSharedPrefString("savedCardId", "");
                                                notifyDataSetChanged();
//                                                try {
//                                                    ((TaskActivity) context).updateButtons();
//                                                } catch (Exception e) {
//                                                }
//                                                Log.e("Transport", "no need to show");
                                            }
                                        } else {
                                            goForStartReachStatus("4", position, "0");
                                            MyApp.setStatus("savedCardStatus", false);
//                                            MyApp.setSharedPrefString("savedCardId", "");
                                            notifyDataSetChanged();
                                            try {
                                                ((TaskActivity) context).updateButtons();
                                            } catch (Exception e) {
                                            }
                                            Log.e("Transport", "no need to show 610");
                                        }

                                    }
                                }).setNegativeButton("Reached", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if (LastTransportMode.equals("0")) {
                                    Log.e("Transport", "zero");
                                } else {
                                    Log.e("Transport", LastTransportMode);
                                }
                                Cursor cquery1 = sql.rawQuery("select IsPublic from ModeOfTrasportList where TransportId='" + LastTransportMode + "'", null);
                                if (cquery1.getCount() > 0) {
                                    cquery1.moveToFirst();
                                    if (cquery1.getString(0).equals("true")) {
                                        {
                                            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(context);
                                            alertDialog.setTitle("Transport Expense");
                                            alertDialog.setMessage("Enter amount");
                                            alertDialog.setCancelable(false);
                                            final EditText input = new EditText(context);
                                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                    300,
                                                    LinearLayout.LayoutParams.MATCH_PARENT);
                                            input.setGravity(Gravity.CENTER);
                                            lp.setMargins(20, 0, 20, 0);
                                            input.setLayoutParams(lp);
                                            input.setHint("Amount");
                                            alertDialog.setView(input);
                                            alertDialog.setIcon(R.mipmap.som);

                                            alertDialog.setPositiveButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            String password = input.getText().toString();
                                                            if (!password.isEmpty()) {
                                                                goForStartReachStatus("3", position, password);
                                                                MyApp.setStatus("savedCardStatus", false);
                                                                MyApp.setStatus("isBothDisable", true);
//                                                                MyApp.setSharedPrefString("savedCardId", "");
                                                                notifyDataSetChanged();
                                                                try {
                                                                    ((TaskActivity) context).updateButtons();
                                                                } catch (Exception e) {
                                                                }
                                                            } else {
                                                                MyApp.popMessage("Alert!", "Please enter amount.", context);
                                                            }
                                                        }
                                                    });
                                            alertDialog.show();
                                        }
                                    } else {
                                        goForStartReachStatus("3", position, "0");
                                        MyApp.setStatus("savedCardStatus", false);
                                        MyApp.setStatus("isBothDisable", true);
//                                        MyApp.setSharedPrefString("savedCardId", "");
                                        notifyDataSetChanged();
                                        try {
                                            ((TaskActivity) context).updateButtons();
                                        } catch (Exception e) {
                                        }
                                        Log.e("Transport", "no need to show");
                                    }
                                } else {
                                    MyApp.setStatus("isBothDisable", true);
                                    goForStartReachStatus("3", position, "0");
                                    MyApp.setStatus("savedCardStatus", false);
//                                    MyApp.setSharedPrefString("savedCardId", "");
                                    notifyDataSetChanged();
                                    try {
                                        ((TaskActivity) context).updateButtons();
                                    } catch (Exception e) {
                                    }
                                    Log.e("Transport", "no need to show 610");
                                }

                            }
                        }).create().show();


                    }
                });
            } catch (Exception e) {
            }

            //fetchStatus(currentStatus,position);
            schedulingholder.mobile.setText(mMob.get(position));
            schedulingholder.sub.setText(mSub.get(position));
            schedulingholder.location.setText(mLoc.get(position));
            schedulingholder.time.setText(mTime.get(position));
            schedulingholder.name.setText(mName.get(position));
            schedulingholder.schedule_date_value.setText(map.get(mTicketNumber.get(position)));
            schedulingholder.stats.setText(mCardType.get(position).equals("Open") ? "Accepted" : mCardType.get(position));
            schedulingholder.top.setBackgroundResource(mCardColor.get(position));
            schedulingholder.bot.setBackgroundResource(mCardColor.get(position));
            schedulingholder.mbut.setBackgroundResource(mCardColor.get(position));

            schedulingholder.pendinglayout.setBackgroundResource(R.color.white);
            if (mIssueID.get(position).equals(savedCardId)) {
                schedulingholder.pendinglayout.setBackgroundResource(mCardColor.get(position));
            }

            if (MyApp.getStatus(mIssueID.get(position) + "RED")) {
                schedulingholder.pendinglayout.setBackgroundResource(R.color.black_overlay);
            }


//            if (isFuture) {
//                schedulingholder.top.setBackgroundResource(R.color.black_overlay);
//                schedulingholder.bot.setBackgroundResource(R.color.black_overlay);
//                schedulingholder.mbut.setBackgroundResource(R.color.black_overlay);
//            }

            schedulingholder.sIssueID.setText(mTicketNumber.get(position));
            schedulingholder.mobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (finalIsFuture1) {
//                        MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                        return;
//                    }
                    Intent in = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mMob.get(position)));
                    ctx.startActivity(in);
                }
            });
            Map<String, TicketInfoClass> savedDetailsMap = MyApp.getApplication().readIssueDetailsHistory();

            if (savedDetailsMap.containsKey(mIssueID.get(position))) {
                if (savedDetailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                    schedulingholder.txt_lable.setText("Issue ID");
                } else {
                    schedulingholder.txt_lable.setText("Task ID");
                }
            } else {
                schedulingholder.txt_lable.setText("Issue ID");
            }

            schedulingholder.stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (finalIsFuture1) {
//                        MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                        return;
//                    }
                    currentStatus = mCardType.get(position);
                    Cursor cqueryIsVerified = sql.rawQuery("select IsVerified,PreviousStatus from Issue_Detail where IssueId='" + mIssueID.get(position) + "'", null);
                    cqueryIsVerified.moveToFirst();
                    try {
                        sPreviousId = cqueryIsVerified.getString(1);
                    } catch (Exception e) {
                        MyApp.showMassage(context, "Try again or refresh data.");
                        return;
                    }
                    if (cqueryIsVerified.getString(0).equals("0")) {
                        final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                        Dialog.setTitle("Verify Asset Serial Number");
                        Dialog.setCancelable(false);
                        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View dialogView = li.inflate(R.layout.rejection, null);
                        final EditText commentVerify = dialogView.findViewById(R.id.rejectionReason);
                        commentVerify.setHint("Asset Serial Number");
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
                                boolean wantToCloseDialog = true;
                                if (commentVerify.getText().toString().length() == 0) {
                                    commentVerify.setError("Please enter Asset Serial Number");
                                    wantToCloseDialog = false;
                                }
                                if (wantToCloseDialog) {
                                    dialog.dismiss();
                                    populateStatus(mIssueID.get(position), position, commentVerify.getText().toString(), sPreviousId);

                                }
                            }
                        });
                    } else {
                        populateStatus(mIssueID.get(position), position, "", sPreviousId);
                    }
                }
            });
            String sMainStatusIdTemp = null;
            Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId='" + mIssueID.get(position) + "'", null);
            if (cqueryTemp.getCount() > 0) {
                cqueryTemp.moveToFirst();
                Cursor cqueryTemp1 = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + cqueryTemp.getString(0) + "'", null);
                if (cqueryTemp1.getCount() > 0) {
                    cqueryTemp1.moveToFirst();
                    sMainStatusIdTemp = cqueryTemp1.getString(0);
                } else
                    sMainStatusIdTemp = "4";
            } else {
                sMainStatusIdTemp = "4";
            }
            HashMap<String, TicketInfoClass> mm = MyApp.getApplication().readIssueDetailsHistory();

            try {
                if (mTime.get(position).equals(""))
                    schedulingholder.time.setText("NA!!");
                else if (sMainStatusIdTemp.equals("4")) {
                    schedulingholder.time.setText("--");
                } else if (isHoldTat(mm.get(mIssueID.get(position)).getStatusId())) {
                    Date dSLAdate = null, dCurrentDate = null, dStatsChangeDate = null;
                    String dtStart = mTime.get(position);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//2019-01-30 10:00:00
                    try {
                        dSLAdate = format.parse(dtStart);
                    } catch (ParseException e) {
                        dSLAdate = new Date();
                        e.printStackTrace();
                    }

                    try {
                        dStatsChangeDate = format2.parse(mm.get(mIssueID.get(position)).getStatusChangeDate());
                    } catch (ParseException e) {
                        dStatsChangeDate = new Date();
                        e.printStackTrace();
                    } catch (Exception ee) {
                        dStatsChangeDate = new Date();
                    }

                    Date cDate = new Date();
                    String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cDate);
                    try {
                        dCurrentDate = format.parse(currentDateTimeString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long l = printDifference(dSLAdate, dCurrentDate);
                    long sct = differenceStatusChangeDate(dStatsChangeDate, dCurrentDate);
                    if (sct > 0) {
                        l = l + sct;
                    }
                    final long secondsInMilli = 1000;
                    final long minutesInMilli = secondsInMilli * 60;
                    final long hoursInMilli = minutesInMilli * 60;
                    new CountDownTimer(l, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long elapsedHours = millisUntilFinished / hoursInMilli;
                            millisUntilFinished = millisUntilFinished % hoursInMilli;

                            long elapsedMinutes = millisUntilFinished / minutesInMilli;
                            millisUntilFinished = millisUntilFinished % minutesInMilli;

                            long elapsedSeconds = millisUntilFinished / secondsInMilli;
                            schedulingholder.time.setText(elapsedHours + "h " + elapsedMinutes + "m " + elapsedSeconds + "s ");
                            //here you can have your logic to set text to edittext
                        }

                        public void onFinish() {
                            schedulingholder.time.setTextColor(Color.RED);
                            schedulingholder.time.setText("SLA Breached!!");
                        }

                    }.start();
                } else {

                    Date dSLAdate = null, dCurrentDate = null;
                    String dtStart = mTime.get(position);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    try {
                        dSLAdate = format.parse(dtStart);
//                    System.out.println(dSLAdate);
                    } catch (ParseException e) {
                        dSLAdate = new Date();
                        e.printStackTrace();
                    }

                    Date cDate = new Date();
                    String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cDate);
                    try {
                        dCurrentDate = format.parse(currentDateTimeString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long l = printDifference(dSLAdate, dCurrentDate);
                    final long secondsInMilli = 1000;
                    final long minutesInMilli = secondsInMilli * 60;
                    final long hoursInMilli = minutesInMilli * 60;
                    new CountDownTimer(l, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long elapsedHours = millisUntilFinished / hoursInMilli;
                            millisUntilFinished = millisUntilFinished % hoursInMilli;

                            long elapsedMinutes = millisUntilFinished / minutesInMilli;
                            millisUntilFinished = millisUntilFinished % minutesInMilli;

                            long elapsedSeconds = millisUntilFinished / secondsInMilli;
                            schedulingholder.time.setText(elapsedHours + "h " + elapsedMinutes + "m " + elapsedSeconds + "s ");
                            //here you can have your logic to set text to edittext
                        }

                        public void onFinish() {
                            schedulingholder.time.setTextColor(Color.RED);
                            schedulingholder.time.setText("SLA Breached!!");
                        }

                    }.start();
                }
            } catch (Exception e) {
            }
            schedulingholder.popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (MyApp.getStatus(mIssueID.get(position) + "RED") || mCardColor.get(position) == R.color.colorPrimaryDark
                            || mCardColor.get(position) == R.color.colorBackgroundEnd) {
                        return true;
                    }
//                    if (finalIsFuture1) {
//                        MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                        return true;
//                    }
                    switch (item.getItemId()) {

                        case R.id.nroute:

                            Intent searchAddress = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + mLoc.get(position)));
                            ctx.startActivity(searchAddress);
                            return true;
                        case R.id.ctc:
                            Intent in = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mMob.get(position)));
                            ctx.startActivity(in);
                            return true;
                        case R.id.chStatus:

                            currentStatus = mCardType.get(position);
                            Cursor cqueryIsVerified = sql.rawQuery("select IsVerified from Issue_Detail where IssueId='" + mIssueID.get(position) + "'", null);
                            if (cqueryIsVerified.getCount() == 0) {
                                return false;
                            }
                            cqueryIsVerified.moveToFirst();
                            if (cqueryIsVerified.getString(0).equals("0")) {
                                final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                                Dialog.setTitle("Verify Asset Serial Number");
                                Dialog.setCancelable(false);
                                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View dialogView = li.inflate(R.layout.rejection, null);
                                final EditText commemtVerify = dialogView.findViewById(R.id.rejectionReason);
                                commemtVerify.setHint("Asset Serial Number");
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
                                        boolean wantToCloseDialog = true;
                                        if (commemtVerify.getText().toString().length() == 0) {
                                            commemtVerify.setError("Please enter Asset Serial Number");
                                            wantToCloseDialog = false;

                                        }
                                        if (wantToCloseDialog) {
                                            populateStatus(mIssueID.get(position), position, commemtVerify.getText().toString(), sPreviousId);
                                            dialog.dismiss();
                                        }
                                    }
                                });
                            } else {
                                populateStatus(mIssueID.get(position), position, "", sPreviousId);
                            }
                            return true;
                        case R.id.showDistance:
                            DecimalFormat df = new DecimalFormat("####.00");
                            getLocation();
                            Double distance = showDistance(latitude, longitude, mLoc.get(position), mLatLng.get(position), context);
                            if (distance == -1.0) {
                                MyApp.showMassage(context, "Location not found, please try again.");
                                return true;
                            }
                            String sDistance = String.valueOf(df.format(distance));
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle("Distance in KM");
                            alertDialogBuilder
                                    .setMessage("Distance:" + sDistance + " KM approx")
                                    .setCancelable(true)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                            return true;
                        case R.id.updateOem:
                            final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                            Dialog.setTitle("OEM Number: ");
                            Dialog.setCancelable(false);
                            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View dialogView = li.inflate(R.layout.rejection, null);
                            final EditText commemt = dialogView.findViewById(R.id.rejectionReason);
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
                                    boolean wantToCloseDialog = true;
                                    if (commemt.getText().toString().length() == 0) {
                                        commemt.setError("Please enter OEM Ticket Number");
                                        wantToCloseDialog = false;
                                    }

                                    if (wantToCloseDialog) {
                                        Map<String, String> postTktOEM = new HashMap<String, String>();
                                        postTktOEM.put("UserId", nh_userid);
                                        postTktOEM.put("TicketId", mIssueID.get(position));
                                        postTktOEM.put("OEMTicketId", commemt.getText().toString());
                                        String tktOEMNumber = new Gson().toJson(postTktOEM);
                                        new UpdateOEM(ctx, tktOEMNumber).execute();
                                        dialog.dismiss();
                                    }
                                }
                            });
                            return true;
                        default:
                            return false;
                    }
                }
            });
            schedulingholder.mbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (finalIsFuture1) {
//                        MyApp.popMessage("Alert!", "You cannot perform action on future ticket or task.", context);
//                        return;
//                    }
                    schedulingholder.popup.show();
                }
            });


        }
    }

    private void goForStartReachStatus(String isStart, int position, String expense) {
        MyApp.spinnerStart(context, "Please wait...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyApp.spinnerStop();
            }
        }, 10000);
        getLocation();
        Date cDate1 = new Date();
        currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate1);
        postTktStatus.put("UserId", nh_userid);
        postTktStatus.put("ParentCompanyId", sParentComapnyId);
        postTktStatus.put("TicketId", mIssueID.get(position));
        postTktStatus.put("StatusId", isStart);
        postTktStatus.put("StartingForSite", isStart.equals("2") ? 1 + "" : 0 + "");
        postTktStatus.put("CustomDestination", "");
        postTktStatus.put("Comment", "");
        postTktStatus.put("ticketNumber", mTicketNumber.get(position));
        postTktStatus.put("ActivityDate", currentTime);
        postTktStatus.put("DepartmentId", DepartmentId);
        postTktStatus.put("RealtimeUpdate", "true");
        postTktStatus.put("Latitude", String.valueOf(latitude));
        postTktStatus.put("Longitude", String.valueOf(longitude));
        postTktStatus.put("AssetSerialNo", "");
        postTktStatus.put("DeviceId", sDeviceId);
        postTktStatus.put("Expense", expense);
        postTktStatus.put("SyncStatus", "-1");
        postTktStatus.put("ModeOfTransport", "");
        postTktStatus.put("AssignedUserId", "0");
        postTktStatus.put("TaskId", "0");
        if (issueDetailsMap.containsKey(mIssueID.get(position)))
            if (issueDetailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                postTktStatus.put("TaskId", "0");
            } else {
                postTktStatus.put("TicketId", "0");
                postTktStatus.put("TaskId", mIssueID.get(position));
            }

        Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();

        if (issuesMap.containsKey(mIssueID.get(position))) {
            issuesMap.put(mIssueID.get(position) + "@@" + System.currentTimeMillis(), postTktStatus);
            MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
        } else {
            issuesMap.put(mIssueID.get(position), postTktStatus);
            MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
        }

//        issuesMap.put(mIssueID.get(position), postTktStatus);
//        MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
        UpdateTask(ctx, postTktStatus, "true");

        Snackbar.make(nhn, "Status update successfully!", Snackbar.LENGTH_LONG).show();
    }

    public void populateStatusStartReach(final String id, final int position, final String sAssetVerificationText, final boolean isStart) {
        currentStatus = mCardType.get(position);
        postTktStatus.put("ModeOfTransport", "0");
        postTktStatus.put("Expense", "0");
        postTktStatus.put("AssignedUserId", "0");
        final LinearLayout layoutTransport;
        final String sCurrentStatusId;
        final TextView txt_transport_lable;
        cquery = sql.rawQuery("select IsAccepted, StatusId from Issue_Detail where IssueId='" + id + "'", null);
        cquery.moveToFirst();
//        String sCardType = cquery.getString(0);
        try {
            sCurrentStatusId = cquery.getString(1);
        } catch (Exception e) {
            return;
        }

        fetchStatus(currentStatus, id, sCurrentStatusId);
        fetchTransportMode();

        final AlertDialog.Builder DialogMain = new AlertDialog.Builder(ctx);
        DialogMain.setTitle("Select Transport Mode");
        DialogMain.setCancelable(false);
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = li.inflate(R.layout.option, null);
        Spinner spinnercategory = dialogView
                .findViewById(R.id.viewSpin);
        spinnercategory.setVisibility(View.GONE);
        final Spinner spinnerTransport = dialogView
                .findViewById(R.id.id_transportMode);
        final EditText commemt = dialogView.findViewById(R.id.comment);
        txt_transport_lable = dialogView.findViewById(R.id.txt_transport_lable);
        commemt.setVisibility(View.GONE);
        final EditText eTransport = dialogView.findViewById(R.id.amount);
        layoutTransport = dialogView.findViewById(R.id.id_transportlayout);
        DialogMain.setView(dialogView);
        DialogMain.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        DialogMain.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialogMain = DialogMain.create();
        dialogMain.show();
        dialogMain.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int pos = spinnerTransport.getSelectedItemPosition();
                    cquery = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                    cquery.moveToFirst();
                    sMainStatusId = cquery.getString(0);
                } catch (Exception e) {
//                    MyApp.showMassage(context, "Some problem occurred, Please try again.");
                }

                Date cDate = new Date();
                currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                getLocation();
                MyApp.spinnerStart(context, "Please wait...");
                try {
                    ((TaskActivity) context).updateButtons();
                } catch (Exception e) {
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyApp.spinnerStop();
                    }
                }, 10000);
                Snackbar.make(v, "Status updated successfully!!!", Snackbar.LENGTH_LONG).show();
                postTktStatus.put("UserId", nh_userid);
                postTktStatus.put("ParentCompanyId", sParentComapnyId);
                postTktStatus.put("TicketId", id);
                postTktStatus.put("StatusId", "2");
                postTktStatus.put("Comment", "");
                postTktStatus.put("DeviceId", sDeviceId);
                postTktStatus.put("ActivityDate", currentTime);
                postTktStatus.put("DepartmentId", DepartmentId);
                postTktStatus.put("ticketNumber", mTicketNumber.get(position));
                postTktStatus.put("RealtimeUpdate", "true");
                postTktStatus.put("Latitude", String.valueOf(latitude));
                postTktStatus.put("Latitude", String.valueOf(latitude));
                postTktStatus.put("Longitude", String.valueOf(longitude));
                postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                postTktStatus.put("Expense", eTransport.getText().toString());
                postTktStatus.put("StartingForSite", "1");
                postTktStatus.put("CustomDestination", "");
                postTktStatus.put("SyncStatus", "-1");
                postTktStatus.put("TaskId", "0");
                if (issueDetailsMap.containsKey(id))
                    if (issueDetailsMap.get(id).getType().equals("Ticket")) {
                        postTktStatus.put("TaskId", "0");
                    } else {
                        postTktStatus.put("TicketId", "0");
                        postTktStatus.put("TaskId", id);
                    }
                Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();

                if (issuesMap.containsKey(mIssueID.get(position))) {
                    issuesMap.put(mIssueID.get(position) + "@@" + System.currentTimeMillis(), postTktStatus);
                    MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                } else {
                    issuesMap.put(mIssueID.get(position), postTktStatus);
                    MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                }

//                issuesMap.put(id, postTktStatus);
//                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);

                try {
                    Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + id + "'", null);
                    if (cqueryTemp.getCount() > 0) {
                        cqueryTemp.moveToFirst();
                    }
                    UpdateTask(context, postTktStatus, "true");
                    MyApp.setStatus("savedCardStatus", isStart);
                    MyApp.setSharedPrefString("savedCardId", isStart ? id : "");
                    try {
                        ((TaskActivity) context).updateButtons();
                    } catch (Exception e) {
                    }
                    notifyDataSetChanged();

                } catch (Exception e) {
                }

                LastTransportMode = postTktStatus.get("ModeOfTransport");
                editor.putString("LastTransport", LastTransportMode);
                editor.apply();
                editor.commit();
                Snackbar.make(nhn, "Status update successfully!", Snackbar.LENGTH_LONG).show();
                dialogMain.dismiss();
            }
            //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

        });

        ArrayAdapter<String> adapterTransport = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, mtTransportlistName);
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);

        if (LastTransportMode.equals("0")) {
            spinnerTransport.setSelection(0);
        } else {
            spinnerTransport.setSelection(mtTransportlistIds.indexOf(LastTransportMode));
        }
        txt_transport_lable.setVisibility(View.INVISIBLE);
        layoutTransport.setVisibility(View.VISIBLE);
        spinnercategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View arg1,
                                       int arg2, long arg3) {

                try {
                    poss = arg2;
                    cquery = sql.rawQuery("select CommentRequired,StartingForSite from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                    cquery.moveToFirst();
                    sMainStatusId = cquery.getString(0);
                    if (cquery.getCount() > 0) {
                        cquery.moveToFirst();
                        if (cquery.getString(0).equals("true")) {
                            commemt.setText("");
                            commemt.setVisibility(View.INVISIBLE);
                        } else {
                            commemt.setVisibility(View.INVISIBLE);
                            commemt.setText("n/a");
                        }
                        if (cquery.getString(1).equals("true")) {
                            layoutTransport.setVisibility(View.VISIBLE);
                        } else {
                            layoutTransport.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    MyApp.showMassage(context, "Local database Problem found with status id = " + statusListIds.get(poss));
                    return;
                }

                Cursor cquery1 = sql.rawQuery("select IsPublic from ModeOfTrasportList where TransportId='" + LastTransportMode + "'", null);
                Cursor cquery2 = sql.rawQuery("select StartingForSite from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                if (cquery1.getCount() > 0 && cquery2.getCount() > 0) {
                    cquery1.moveToFirst();
                    cquery2.moveToFirst();
                    if (cquery1.getString(0).equals("true") && cquery2.getString(0).equals("true")) {
                        eTransport.setText("");
                        eTransport.setVisibility(View.VISIBLE);
                    } else {
                        eTransport.setText("0");
                        eTransport.setVisibility(View.GONE);
                    }
                } else {
                    eTransport.setText("0");
                    eTransport.setVisibility(View.GONE);
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


        spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View arg1,
                                       int arg2, long arg3) {
                postTktStatus.put("ModeOfTransport", mtTransportlistIds.get(arg2));
                postTktStatus.put("Expense", "0");
                postTktStatus.put("AssignedUserId", nh_userid);
//                MyApp.showMassage(ctx, String.valueOf(arg2 + arg3));
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
    }


    public void populateStatusStartReachTaskActivity(final String issueId, final int pos, final String sAssetVerificationText, final boolean isStart, final String type) {
        postTktStatus.put("ModeOfTransport", "0");
        postTktStatus.put("Expense", "0");
        postTktStatus.put("AssignedUserId", "0");
        final LinearLayout layoutTransport;
        final String sCurrentStatusId;
        final TextView txt_transport_lable;

//        fetchStatus(currentStatus, id, "3");
        fetchTransportMode();

        final AlertDialog.Builder DialogMain = new AlertDialog.Builder(ctx);
        DialogMain.setTitle("Select Transport Mode");
        DialogMain.setCancelable(false);
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = li.inflate(R.layout.option, null);
        Spinner spinnercategory = dialogView
                .findViewById(R.id.viewSpin);
        spinnercategory.setVisibility(View.GONE);
        Spinner spinnerTransport = dialogView
                .findViewById(R.id.id_transportMode);
        final EditText commemt = dialogView.findViewById(R.id.comment);
        txt_transport_lable = dialogView.findViewById(R.id.txt_transport_lable);
        commemt.setVisibility(View.GONE);
        final EditText eTransport = dialogView.findViewById(R.id.amount);
        layoutTransport = dialogView.findViewById(R.id.id_transportlayout);
        DialogMain.setView(dialogView);
        DialogMain.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
        DialogMain.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog dialogMain = DialogMain.create();
        dialogMain.show();
        dialogMain.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date cDate = new Date();
                currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                getLocation();
                Snackbar.make(v, "Status updated successfully!!!", Snackbar.LENGTH_LONG).show();
                postTktStatus.put("UserId", nh_userid);
                postTktStatus.put("ParentCompanyId", sParentComapnyId);
                postTktStatus.put("TicketId", "0");
                postTktStatus.put("StatusId", isStart ? "2" : "3");
                postTktStatus.put("Comment", "");
                postTktStatus.put("DeviceId", sDeviceId);
                postTktStatus.put("ActivityDate", currentTime);
                postTktStatus.put("DepartmentId", DepartmentId);
                postTktStatus.put("ticketNumber", issueId);
                postTktStatus.put("RealtimeUpdate", "true");
                postTktStatus.put("Latitude", String.valueOf(latitude));
                postTktStatus.put("Latitude", String.valueOf(latitude));
                postTktStatus.put("Longitude", String.valueOf(longitude));
                postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                postTktStatus.put("Expense", eTransport.getText().toString());
                postTktStatus.put("StartingForSite", isStart ? 1 + "" : 0 + "");
                postTktStatus.put("CustomDestination", type);
                postTktStatus.put("SyncStatus", "-1");
                postTktStatus.put("TaskId", "0");

                Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();

                issuesMap.put(currentTime, postTktStatus);
                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                MyApp.spinnerStart(context, "Please wait...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyApp.spinnerStop();
                    }
                }, 10000);
                try {
                    UpdateTask(context, postTktStatus, "true");
                } catch (Exception e) {
                }

                LastTransportMode = postTktStatus.get("ModeOfTransport");
                dialogMain.dismiss();
            }
            //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

        });

        ArrayAdapter<String> adapterTransport = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, mtTransportlistName);
        adapterTransport.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTransport.setAdapter(adapterTransport);

        spinnerTransport.setSelection(0);

        txt_transport_lable.setVisibility(View.INVISIBLE);
        layoutTransport.setVisibility(View.VISIBLE);
        spinnercategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View arg1,
                                       int arg2, long arg3) {

                eTransport.setText("0");
                eTransport.setVisibility(View.GONE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View arg1,
                                       int arg2, long arg3) {
                postTktStatus.put("ModeOfTransport", mtTransportlistIds.get(arg2));
                postTktStatus.put("Expense", "0");
                postTktStatus.put("AssignedUserId", nh_userid);
                MyApp.showMassage(ctx, String.valueOf(arg2 + arg3));
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private class UpdateOEM extends AsyncTask<String, Void, String> {
        String jsonString;
        Context ctx;

        public UpdateOEM(Context ctx, String jsonString) {
            this.ctx = ctx;
            this.jsonString = jsonString;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MyApp.showMassage(context, "Loading Data!!!");
        }

        @Override
        protected String doInBackground(String... params) {

            String uri = PostUrl.sUrl + "PostOEMTicket";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(15000);
                urlConnection.connect();
                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                writer.write(jsonString);
                writer.close();
                outputStream.close();
                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
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
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
                SQLiteDatabase sql;
                String currentDateTimeString = null;
                sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);

                if (s == null) {
                    MyApp.showMassage(ctx, ctx.getString(R.string.internet_error));
                } else {
                    MyApp.showMassage(ctx, "OEM Updated Successfully!!!");
                    ContentValues newValues = new ContentValues();
                    newValues.put("OEMNumber", jsonObject.getString("OEMTicketId"));
                    sql.update("Issue_Detail", newValues, "IssueId=" + jsonObject.getString("TicketId"), null);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public long printDifference(Date dCurrentDate, Date dSLAdate) {
        long different = dCurrentDate.getTime() - dSLAdate.getTime();
        long m = different;
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds);

        return m;
    }

    public long differenceStatusChangeDate(Date statusChangeDate, Date dSLAdate) {
        long different = statusChangeDate.getTime() - dSLAdate.getTime();
        long m = different;
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        System.out.printf(
                "%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays,
                elapsedHours, elapsedMinutes, elapsedSeconds);

        return m;
    }

    private Double showDistance(Double lat, Double lon, String sAddress, String latLng, Context context) {
//        Geocoder coder = new Geocoder(context);
        Double lati, longi;
        try {
            lati = Double.parseDouble(latLng.split("##")[0]);
            longi = Double.parseDouble(latLng.split("##")[1]);
        } catch (Exception e) {
            return showDistanceOldWay(lat, lon, sAddress, context);
        }

        final int R = 6371; // Radious of the earth

        try {
            Double latDistance = deg2rad(lati - lat);
            Double lonDistance = deg2rad(longi - lon);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                    Math.cos(deg2rad(lat)) * Math.cos(deg2rad(lati)) *
                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            Double distance = R * c;
            return distance;
        } catch (Exception e) {
            return -1.0;
        }


    }


    private Double showDistanceOldWay(Double lat, Double lon, String sAddress, Context context) {
        Geocoder coder = new Geocoder(context);
        Double lati, longi;
        lati = null;
        longi = null;
        List<Address> address;
        try {
            address = coder.getFromLocationName(sAddress, 5);
            if (address == null || address.size() == 0) {
                return -1.0;
            } else {
                Address location = address.get(0);
                lati = location.getLatitude();
                longi = location.getLongitude();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        final int R = 6371; // Radious of the earth

        try {
            Double latDistance = deg2rad(lati - lat);
            Double lonDistance = deg2rad(longi - lon);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                    Math.cos(deg2rad(lat)) * Math.cos(deg2rad(lati)) *
                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            Double distance = R * c;
            return distance;
        } catch (Exception e) {
            return -1.0;
        }


    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void addCard(String sIssueId, String sName, String sTime, String sLoc, String sMob, String sSub, String sStatus, int iColor, int iDatatype, int position, String sTicketNumber) {
        try {
            mIssueID.remove(position);
            mName.remove(position);
            mTime.remove(position);
            mLoc.remove(position);
            mMob.remove(position);
            mSub.remove(position);
            mDatasetTypes.remove(position);
            mCardColor.remove(position);
            mCardType.remove(position);
            mTicketNumber.remove(position);

            mIssueID.add(position, sIssueId);
            mName.add(position, sName);
            mTime.add(position, sTime);
            mLoc.add(position, sLoc);
            mMob.add(position, sMob);
            mSub.add(position, sSub);
            mTicketNumber.add(position, sTicketNumber);
            mDatasetTypes.add(position, iDatatype);
            mCardColor.add(position, iColor);
            mCardType.add(position, sStatus);


            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDatasetTypes.size());
        } catch (Exception e) {
            notifyDataSetChanged();
        }
    }

    String[] previousValues;

    private void populateStatus(final String id, final int position, final String sAssetVerificationText, final String sPreviousId) {
        try {

//        MyApp.setStatus(postTktStatus.get("TicketId") + "RED", true);
            if (MyApp.getStatus(id + "RED")) {
                return;
            }
            if (mCardColor.get(position) == R.color.colorPrimaryDark
                    || mCardColor.get(position) == R.color.colorBackgroundEnd) {
                return;
            }

            final String savedCardId = MyApp.getSharedPrefString("savedCardId");
            final boolean isBothDisable = MyApp.getStatus("isBothDisable");
            final boolean isEngaged = MyApp.getStatus("savedCardStatus");

            if (isBothDisable && !mIssueID.get(position).equals(savedCardId)) {
                MyApp.popMessage("Alert!", "Please finish your journey first.", context);
                return;
            } else if (isEngaged && !MyApp.getSharedPrefString("savedCardId").isEmpty() && !isBothDisable) {
                MyApp.popMessage("Alert!", "Please finish your journey first.", context);
                return;
            }

            if (!isEngaged && !isBothDisable && !MyApp.getSharedPrefString("savedCardId").isEmpty()
                    && !mIssueID.get(position).equals(savedCardId)) {
                MyApp.popMessage("Alert!", "Please finish your journey first.", context);
                return;
            }
            postTktStatus.put("ModeOfTransport", "0");
            postTktStatus.put("Expense", "0");
            postTktStatus.put("AssignedUserId", "0");
            final LinearLayout layoutTransport;
            final String sCurrentStatusId;
            cquery = sql.rawQuery("select IsAccepted, StatusId from Issue_Detail where IssueId='" + id + "'", null);
            cquery.moveToFirst();
            String sCardType = cquery.getString(0);
            sCurrentStatusId = cquery.getString(1);
            if (sCardType.equals("3"))
                MyApp.showMassage(ctx, "This ticket is already closed!!!");
            else {
                fetchStatus(currentStatus, id, sCurrentStatusId);
                fetchTransportMode();
                final AlertDialog.Builder DialogMain = new AlertDialog.Builder(ctx);
                DialogMain.setTitle("Select Option");
                DialogMain.setCancelable(false);
                LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = li.inflate(R.layout.option, null);
                Spinner spinnercategory = dialogView
                        .findViewById(R.id.viewSpin);
                Spinner spinnerTransport = dialogView
                        .findViewById(R.id.id_transportMode);
                final EditText commemt = dialogView.findViewById(R.id.comment);
                final EditText eTransport = dialogView.findViewById(R.id.amount);
                layoutTransport = dialogView.findViewById(R.id.id_transportlayout);
                DialogMain.setView(dialogView);
                DialogMain.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                DialogMain.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog dialogMain = DialogMain.create();
                dialogMain.show();
                dialogMain.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean wantToCloseDialog = true;
                        boolean isWritten = false;
//                        if (commemt.getText().toString().length() == 0) {
//                            commemt.setError("Please enter comment");
//                            commemt.setText("No comment");
//                            wantToCloseDialog = false;
//                        }
                        if (isCommentRequired(commemt.getText().toString(), statusListIds.get(poss))) {
                            if (isBothDisable && mIssueID.get(position).equals(savedCardId)) {
                                MyApp.setStatus("isBothDisable", false);
                                MyApp.setStatus("savedCardStatus", false);
                                MyApp.setSharedPrefString("savedCardId", "");
                                try {
                                    ((TaskActivity) context).updateButtons();
                                } catch (Exception e) {
                                }
                                notifyDataSetChanged();
                            }
                            String sSelectedStatus;
                            cquery = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                            cquery.moveToFirst();
                            sSelectedStatus = statusListIds.get(poss);
                            Date cDate = new Date();
                            currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                            boolean isCloseWentToCSAT = false;

                            if (cquery.getString(0).equals("4")) {
                                sMainStatusId = cquery.getString(0);
                                if (bCSATEnable) {
                                    isCloseWentToCSAT = true;
                                    Fragment hello = new EngineerFeedbackFragment();
                                    Bundle bdl = new Bundle(2);
                                    bdl.putString("LoginID", nh_userid);
                                    bdl.putString("IssueID", id);
                                    bdl.putString("Comment", commemt.getText().toString());
                                    bdl.putString("currentTime", currentTime);
                                    bdl.putString("StatusId", sSelectedStatus);
                                    bdl.putString("CardType", TaskActivity.card);
                                    hello.setArguments(bdl);
                                    FragmentManager fragmentManager = ((AppCompatActivity) ctx).getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.add(R.id.activity_task, hello);
                                    fragmentTransaction.commit();
                                } else {
                                    previousValues = new String[4];
                                    Cursor cc = sql.rawQuery("select StatusId, IsAccepted, UpdatedDate, PreviousStatus from Issue_Detail where IssueId='" + id + "'", null);
                                    cc.moveToFirst();

                                    previousValues[0] = cc.getString(0);
                                    previousValues[1] = cc.getString(1);
                                    previousValues[2] = cc.getString(2);
                                    previousValues[3] = cc.getString(3);

                                    Map<String, String[]> savedMap = MyApp.getApplication().readSavedStatusValue();
                                    if (savedMap == null) savedMap = new HashMap<>();
                                    savedMap.put(id, previousValues);
                                    MyApp.getApplication().writeSavedStatusValues(savedMap);

                                    getLocation();
                                    ContentValues newValues = new ContentValues();
                                    newValues.put("StatusId", sSelectedStatus);
                                    newValues.put("IsAccepted", "3");
                                    newValues.put("UpdatedDate", currentTime);
                                    newValues.put("PreviousStatus", sCurrentStatusId);
                                    try {
                                        sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                    } catch (Exception e) {
                                    }
                                    try {
                                        MainActivity m = new MainActivity();
                                        m.updateCounter(ctx, false);
                                    } catch (Exception e) {
                                    }
                                    Snackbar.make(v, "Status updated successfully!!!", Snackbar.LENGTH_LONG).show();
                                    postTktStatus.put("UserId", nh_userid);
                                    postTktStatus.put("ParentCompanyId", sParentComapnyId);
                                    postTktStatus.put("TicketId", id);
                                    postTktStatus.put("StatusId", sSelectedStatus);
                                    postTktStatus.put("Comment", commemt.getText().toString());
                                    postTktStatus.put("ActivityDate", currentTime);
                                    try {
                                        postTktStatus.put("ticketNumber", mTicketNumber.get(position));
                                    } catch (Exception e) {
                                        postTktStatus.put("ticketNumber", "Not captured");
                                    }
                                    postTktStatus.put("DepartmentId", DepartmentId);
                                    postTktStatus.put("RealtimeUpdate", "true");
                                    postTktStatus.put("Latitude", String.valueOf(latitude));
                                    postTktStatus.put("Latitude", String.valueOf(latitude));
                                    postTktStatus.put("Longitude", String.valueOf(longitude));
                                    postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                                    postTktStatus.put("Expense", eTransport.getText().toString());
                                    postTktStatus.put("StartingForSite", "");
                                    postTktStatus.put("CustomDestination", "");
                                    postTktStatus.put("SyncStatus", "-1");
                                    postTktStatus.put("TaskId", "0");
                                    if (issueDetailsMap.containsKey(id))
                                        if (issueDetailsMap.get(id).getType().equals("Ticket")) {
                                            postTktStatus.put("TaskId", "0");
                                        } else {
                                            postTktStatus.put("TicketId", "0");
                                            postTktStatus.put("TaskId", id);
                                        }
                                    else
                                        postTktStatus.put("TaskId", "0");
                                    Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();

                                    if (issuesMap.containsKey(id)) {
                                        issuesMap.put(id + "@@" + System.currentTimeMillis(), postTktStatus);
                                        MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                                    } else {
                                        issuesMap.put(id, postTktStatus);
                                        MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                                    }
                                    isWritten = true;
//                                issuesMap.put(id, postTktStatus);
//                                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);

                                    try {
                                        Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + id + "'", null);
                                        if (cqueryTemp.getCount() > 0) {
                                            cqueryTemp.moveToFirst();
                                            if (!nh_userid.equals("0")) {
                                                Map<String, TicketInfoClass> detailsMap = MyApp.getApplication().readIssueDetailsHistory();
                                                if (detailsMap.containsKey(mIssueID.get(position))) {
                                                    if (detailsMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                                                        ref.child(nh_userid).child(mIssueID.get(position)).child("Action").setValue("Update");
                                                    } else {
                                                        Firebase ref = new Firebase(PostUrl.sFirebaseUrlIssues);
                                                        ref.child(nh_userid).child(mIssueID.get(position)).child("Action").setValue("Update");
                                                    }
                                                } else {
                                                    ref.child(nh_userid).child(mIssueID.get(position)).child("Action").setValue("Update");
                                                }

                                            }
                                        }
                                    } catch (Exception e) {
                                        MyApp.popMessage("Error", e.getMessage(), context);
                                    }
                                    MyApp.spinnerStart(context, "Please wait...");
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MyApp.spinnerStop();
                                        }
                                    }, 10000);
                                    UpdateTask(context, postTktStatus, "true");
                                    LastTransportMode = postTktStatus.get("ModeOfTransport");
                                    editor.putString("LastTransport", LastTransportMode);
                                    editor.apply();
                                    editor.commit();
                                    removeCard(position);
                                }

                            } else if (cquery.getString(0).equals("1")
                                    || cquery.getString(0).equals("6") ||
                                    cquery.getString(0).equals("5")) {
                                Cursor cqueryCurrentStatus_MainId = sql.rawQuery("select MainStatusId from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                                cqueryCurrentStatus_MainId.moveToFirst();

                                previousValues = new String[4];
                                Cursor cc = sql.rawQuery("select StatusId, IsAccepted, UpdatedDate, PreviousStatus from Issue_Detail where IssueId='" + id + "'", null);
                                cc.moveToFirst();

                                previousValues[0] = cc.getString(0);
                                previousValues[1] = cc.getString(1);
                                previousValues[2] = cc.getString(2);
                                previousValues[3] = cc.getString(3);

                                Map<String, String[]> savedMap = MyApp.getApplication().readSavedStatusValue();
                                if (savedMap == null) savedMap = new HashMap<>();
                                savedMap.put(id, previousValues);
                                MyApp.getApplication().writeSavedStatusValues(savedMap);

                                if (cqueryCurrentStatus_MainId.getString(0).equals("1") || cqueryCurrentStatus_MainId.getString(0).equals("6") || cqueryCurrentStatus_MainId.getString(0).equals("5")) {

                                    ContentValues newValues = new ContentValues();
                                    newValues.put("StatusId", sSelectedStatus);
                                    newValues.put("IsAccepted", "1");
                                    newValues.put("UpdatedDate", currentTime);
                                    newValues.put("PreviousStatus", sCurrentStatusId);
                                    try {
                                        sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                    } catch (Exception e) {
//                                        MyApp.popMessage("Error", e.getMessage(), context);
                                    }
                                    flag = false;
                                } else {
                                    flag = true;
                                    ContentValues newValues = new ContentValues();
                                    newValues.put("StatusId", sSelectedStatus);
                                    newValues.put("IsAccepted", "2");
                                    newValues.put("UpdatedDate", currentTime);
                                    newValues.put("PreviousStatus", sCurrentStatusId);
                                    try {
                                        sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                    } catch (Exception e) {
                                        MyApp.popMessage("Error", e.getMessage(), context);
                                    }

                                    isAttended = "1";
                                }
                            } else {
                                Cursor cqueryCurrentStatus_MainId = sql.rawQuery("select MainStatusId from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                                if (cqueryCurrentStatus_MainId.getCount() > 0) {
                                    cqueryCurrentStatus_MainId.moveToFirst();

                                    previousValues = new String[4];
                                    Cursor cc = sql.rawQuery("select StatusId, IsAccepted, UpdatedDate, PreviousStatus from Issue_Detail where IssueId='" + id + "'", null);
                                    cc.moveToFirst();

                                    previousValues[0] = cc.getString(0);
                                    previousValues[1] = cc.getString(1);
                                    previousValues[2] = cc.getString(2);
                                    previousValues[3] = cc.getString(3);

                                    Map<String, String[]> savedMap = MyApp.getApplication().readSavedStatusValue();
                                    if (savedMap == null) savedMap = new HashMap<>();
                                    savedMap.put(id, previousValues);
                                    MyApp.getApplication().writeSavedStatusValues(savedMap);

                                    if (cqueryCurrentStatus_MainId.getString(0).equals("1") || cqueryCurrentStatus_MainId.getString(0).equals("6") || cqueryCurrentStatus_MainId.getString(0).equals("5")) {
                                        flag = true;
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("StatusId", sSelectedStatus);
                                        newValues.put("IsAccepted", "2");
                                        newValues.put("UpdatedDate", currentTime);
                                        newValues.put("PreviousStatus", sCurrentStatusId);
                                        try {
                                            sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                        } catch (Exception e) {
                                        }
                                        isAttended = "1";
                                    } else {
                                        flag = false;
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("StatusId", sSelectedStatus);
                                        newValues.put("IsAccepted", "2");
                                        newValues.put("UpdatedDate", currentTime);
                                        newValues.put("PreviousStatus", sCurrentStatusId);
                                        try {
                                            sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                        } catch (Exception e) {
                                        }
                                        isAttended = "1";
                                    }
                                }
                            }

                            if (isCloseWentToCSAT) {
                                dialogMain.dismiss();
                                return;
                            }

                            if (TaskActivity.card.equals("-1")) {
                                cquery = sql.rawQuery("select IssueId,TicketHolder,SLADate,Address,PhoneNo,Subject,StatusId,IsAccepted,TicketNumber from Issue_Detail where IssueId ='" + id + "'", null);
                                cquery.moveToFirst();
                                Cursor cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId ='" + cquery.getString(6) + "'", null);
                                cqueryForStatus.moveToFirst();
                                int iColor, iDatatype;
                                if (cquery.getString(7).equals("1")) {
                                    iColor = R.color.orange;
                                    iDatatype = Pending;
                                } else if (cquery.getString(7).equals("2")) {
                                    iColor = R.color.blue;
                                    iDatatype = Attended;
                                } else if (cquery.getString(7).equals("3")) {
                                    iColor = R.color.green;
                                    iDatatype = Complete;
                                } else if (cquery.getString(7).equals("4")) {
                                    iColor = R.color.colorBackgroundEnd;
                                    iDatatype = Scheduled;
                                } else {
                                    iColor = R.color.colorPrimaryDark;
                                    iDatatype = PendingScheduled;
                                }
                                addCard(cquery.getString(0), cquery.getString(1), cquery.getString(2), cquery.getString(3), cquery.getString(4), cquery.getString(5), cqueryForStatus.getString(0), iColor, iDatatype, position, cquery.getString(8));
                            } else {
                                Cursor cqueryTempStatus = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + sSelectedStatus + "'", null);
                                cqueryTempStatus.moveToFirst();
                            /*if (sMainStatusId.equals(cqueryTempStatus.getString(0))) {
                            } else */
                                if (!isWritten) {
                                    getLocation();
                                    Date cDate1 = new Date();
                                    currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate1);
                                    postTktStatus.put("UserId", nh_userid);
                                    postTktStatus.put("ParentCompanyId", sParentComapnyId);
                                    postTktStatus.put("TicketId", id);
                                    postTktStatus.put("StatusId", sSelectedStatus);
                                    postTktStatus.put("Comment", commemt.getText().toString());
                                    postTktStatus.put("ActivityDate", currentTime);
                                    postTktStatus.put("DepartmentId", DepartmentId);
                                    postTktStatus.put("RealtimeUpdate", "true");
                                    postTktStatus.put("Latitude", String.valueOf(latitude));
                                    postTktStatus.put("Longitude", String.valueOf(longitude));
                                    try {
                                        postTktStatus.put("ticketNumber", mTicketNumber.get(position));
                                    } catch (Exception e) {
                                        postTktStatus.put("ticketNumber", "Not captured");

                                    }
                                    postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                                    postTktStatus.put("DeviceId", sDeviceId);
                                    postTktStatus.put("Expense", eTransport.getText().toString());
                                    postTktStatus.put("StartingForSite", "");
                                    postTktStatus.put("CustomDestination", "");
                                    postTktStatus.put("SyncStatus", "-1");
                                    postTktStatus.put("TaskId", "0");
                                    if (issueDetailsMap.containsKey(id))
                                        if (issueDetailsMap.get(id).getType().equals("Ticket")) {
                                            postTktStatus.put("TaskId", "0");
                                        } else {
                                            postTktStatus.put("TicketId", "0");
                                            postTktStatus.put("TaskId", id);
                                        }
                                    else
                                        postTktStatus.put("TaskId", "0");
                                    String issueStatus = postTktStatus.get("Comment").replace("'", "''");

                                    Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();

                                    if (issuesMap.containsKey(id)) {
                                        issuesMap.put(id + "@@" + System.currentTimeMillis(), postTktStatus);
                                        MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                                    } else {
                                        issuesMap.put(id, postTktStatus);
                                        MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                                    }

//                                issuesMap.put(id, postTktStatus);
//                                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);

                                    LastTransportMode = postTktStatus.get("ModeOfTransport");
                                    editor.putString("LastTransport", postTktStatus.get("ModeOfTransport"));
                                    editor.apply();
                                    editor.commit();


                                    MyApp.spinnerStart(context, "Please wait...");
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MyApp.spinnerStop();
                                        }
                                    }, 10000);
                                    UpdateTask(ctx, postTktStatus, "true");
                                    Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + id + "'", null);
                                    if (cqueryTemp.getCount() > 0) {
                                        cqueryTemp.moveToFirst();
                                        Map<String, TicketInfoClass> detailsMap = MyApp.getApplication().readIssueDetailsHistory();
                                        if (!nh_userid.equals("0"))
                                            if (detailsMap.containsKey(id)) {
                                                if (detailsMap.get(id).getType().equals("Ticket")) {
                                                    ref.child(nh_userid).child(id).child("Action").setValue("Update");
                                                } else {
                                                    Firebase ref = new Firebase(PostUrl.sFirebaseUrlIssues);
                                                    ref.child(nh_userid).child(id).child("Action").setValue("Update");
                                                }
                                            } else {
                                                ref.child(nh_userid).child(id).child("Action").setValue("Update");
                                            }

                                    }
                                    MainActivity m = new MainActivity();
                                    m.updateCounter(ctx, false);
                                    Log.e("onClick: mcard ", String.valueOf(statusList.size()));
                                    mCardType.add(position, statusList.get(poss));
                                    notifyItemChanged(position);
                                    Snackbar.make(nhn, "Status update successfully!!!-completed", Snackbar.LENGTH_LONG).show();
                                    if (flag) {
                                        removeCard(position);

                                    }
                                }
                            }
                            dialogMain.dismiss();
                        } else {
                            commemt.setError("Please enter comment");
                            commemt.setText("No comment");
                        }

                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

                });
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, statusList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnercategory.setAdapter(adapter);

                ArrayAdapter<String> adapterTransport = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item, mtTransportlistName);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTransport.setAdapter(adapterTransport);
                if (LastTransportMode.equals("0")) {
                    spinnerTransport.setSelection(0);
                } else {
                    spinnerTransport.setSelection(mtTransportlistIds.indexOf(LastTransportMode));
                }
                spinnercategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parent, View arg1,
                                               int arg2, long arg3) {

                        try {
                            poss = arg2;
                            cquery = sql.rawQuery("select CommentRequired,StartingForSite from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                            cquery.moveToFirst();
                            if (cquery.getCount() > 0) {
                                cquery.moveToFirst();
                                if (cquery.getString(0).equals("true")) {
                                    commemt.setText("");
                                    commemt.setVisibility(View.VISIBLE);
                                } else {
                                    commemt.setVisibility(View.INVISIBLE);
                                    commemt.setText("n/a");
                                }
                                if (cquery.getString(1).equals("true")) {
                                    layoutTransport.setVisibility(View.VISIBLE);
                                } else {
                                    layoutTransport.setVisibility(View.GONE);
                                }
                            }
                        } catch (Exception e) {
                            MyApp.showMassage(context, "Local database Problem found with status id = " + statusListIds.get(poss));
                            return;
                        }

                        Cursor cquery1 = sql.rawQuery("select IsPublic from ModeOfTrasportList where TransportId='" + LastTransportMode + "'", null);
                        Cursor cquery2 = sql.rawQuery("select StartingForSite from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                        if (cquery1.getCount() > 0 && cquery2.getCount() > 0) {
                            cquery1.moveToFirst();
                            cquery2.moveToFirst();
                            if (cquery1.getString(0).equals("true") && cquery2.getString(0).equals("true")) {
                                eTransport.setText("");
                                eTransport.setVisibility(View.VISIBLE);
                            } else {
                                eTransport.setText("0");
                                eTransport.setVisibility(View.GONE);
                            }
                        } else {
                            eTransport.setText("0");
                            eTransport.setVisibility(View.GONE);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                        try {
                            poss = 0;
                            cquery = sql.rawQuery("select CommentRequired,StartingForSite from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                            cquery.moveToFirst();
                            if (cquery.getCount() > 0) {
                                cquery.moveToFirst();
                                if (cquery.getString(0).equals("true")) {
                                    commemt.setText("");
                                    commemt.setVisibility(View.VISIBLE);
                                } else {
                                    commemt.setVisibility(View.INVISIBLE);
                                    commemt.setText("n/a");
                                }
                                if (cquery.getString(1).equals("true")) {
                                    layoutTransport.setVisibility(View.VISIBLE);
                                } else {
                                    layoutTransport.setVisibility(View.GONE);
                                }
                            }
                        } catch (Exception e) {
                            MyApp.showMassage(context, "Local database Problem found with status id = " + statusListIds.get(poss));
                            return;
                        }

                        Cursor cquery1 = sql.rawQuery("select IsPublic from ModeOfTrasportList where TransportId='" + LastTransportMode + "'", null);
                        Cursor cquery2 = sql.rawQuery("select StartingForSite from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                        if (cquery1.getCount() > 0 && cquery2.getCount() > 0) {
                            cquery1.moveToFirst();
                            cquery2.moveToFirst();
                            if (cquery1.getString(0).equals("true") && cquery2.getString(0).equals("true")) {
                                eTransport.setText("");
                                eTransport.setVisibility(View.VISIBLE);
                            } else {
                                eTransport.setText("0");
                                eTransport.setVisibility(View.GONE);
                            }
                        } else {
                            eTransport.setText("0");
                            eTransport.setVisibility(View.GONE);
                        }
                    }
                });


                spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    public void onItemSelected(AdapterView<?> parent, View arg1,
                                               int arg2, long arg3) {
                        postTktStatus.put("ModeOfTransport", mtTransportlistIds.get(arg2));
                        postTktStatus.put("Expense", "0");
                        postTktStatus.put("AssignedUserId", nh_userid);
                        MyApp.showMassage(ctx, String.valueOf(arg2 + arg3));
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });


            }
        } catch (Exception e) {
        }
    }


    private void fetchTransportMode() {
        mtTransportlistName.clear();
        mtTransportlistIds.clear();
        try {
            ApiResult.ModeOfTrasportList temp = null;
            cquery = sql.rawQuery("select * from ModeOfTrasportList", null);
            if (cquery.getCount() > 0) {
                for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                    mtTransportlistIds.add(cquery.getString(1));
                    mtTransportlistName.add(cquery.getString(2));
                }
            }
        } catch (Exception e) {
        }
    }

    public void removeCard(int position) {
        try {
            mIssueID.remove(position);
            mName.remove(position);
            mTime.remove(position);
            mLoc.remove(position);
            mMob.remove(position);
            mSub.remove(position);
            mDatasetTypes.remove(position);
            mCardColor.remove(position);
            mCardType.remove(position);
            mTicketNumber.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDatasetTypes.size());
        } catch (Exception e) {

        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDatasetTypes.get(position);
    }

    @Override
    public int getItemCount() {
        return mDatasetTypes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        cquery = sql.rawQuery("select IsAccepted from Issue_Detail where IssueId='" + mIssueID.get(getAdapterPosition()) + "'", null);
                        cquery.moveToFirst();
                        String cardType = cquery.getString(0);
                        if (cardType.equals("-1")) ;
                        else {
                            Intent i = new Intent(v.getContext(), TicketInfo.class);
                            i.putExtra("CardType", cardType);
                            i.putExtra("IssueId", mIssueID.get(getAdapterPosition()));
                            ctx.startActivity(i);
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    public void UpdateTask(final Context ctx, final Map<String, String> postTktStatus, String realTimeUpdate) {
        MyApp.setStatus("isTicketUpdating", true);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                MyApp.setStatus("isTicketUpdating", false);
//            }
//        }, 10000);
//        Log.e("TicketStatusTable", "status value " + postTktStatus.get("StatusId"));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
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
                            "",
                            ""
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
                            "",
                            ""
                    );

                }
            }

            if (issueDetail.taskId.equals("0") && issueDetail.IssueId.equals("0") &&
                    !(issueDetail.Status.equals("2") || issueDetail.Status.equals("3"))) {
                Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                savedMap.remove(postTktStatus.get("TicketId"));
                try {
                    savedMap.remove(postTktStatus.get("ActivityDate"));
                    savedMap.remove(postTktStatus.get("TaskId"));
                } catch (Exception e) {
                }
                MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                int issuesCount = savedMap.keySet().size();
                try {
                    ((MainActivity) ctx).txt_issues_count.setText(issuesCount + "");
                } catch (Exception e) {
                    MyApp.spinnerStop();
                }

                return;
            }

            try {
                if (postTktStatus.get("ModeOfTransport").equals("0")) ;
                else {
                    try {
                        LastTransportMode = postTktStatus.get("ModeOfTransport");
                        editor.putString("LastTransport", LastTransportMode);
                        editor.apply();
                        editor.commit();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                MyApp.spinnerStop();
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
                                MyApp.showMassage(ctx, ctx.getString(R.string.internet_error));
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
                            if (iData.resData.TicketRedirection) {
                                MyApp.setStatus(postTktStatus.get("TicketId") + "RED", true);
                            }
                            Log.e("TicketStatusTable", "Success" + " response " + iData.toString());

                            if (iData.resData.Status.equals("false")) {
                                try {
                                    Map<String, String[]> statusArr = MyApp.getApplication().readSavedStatusValue();
                                    String[] previousStatus = statusArr.get(postTktStatus.get("TicketId"));
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

                            }
                            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                            String removableKey = "";
                            for (String keyValue : savedMap.keySet()) {
                                if (keyValue.contains(postTktStatus.get("TicketId"))) {
                                    removableKey = keyValue;
                                } else if (keyValue.contains(postTktStatus.get("TaskId"))) {
                                    removableKey = keyValue;
                                }
                            }
                            savedMap.remove(removableKey);
                            try {
                                savedMap.remove(postTktStatus.get("ActivityDate"));
                            } catch (Exception e) {
                            }
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
                            int issuesCount = savedMap.keySet().size();
                            try {
                                ((MainActivity) ctx).txt_issues_count.setText(issuesCount + "");
                                if (issuesCount == 0) {
                                    ((MainActivity) ctx).refreshData(true);
                                }
                            } catch (Exception e) {
                            }
                            if (iData.resData.Status.equals("false")) {
                                MyApp.popMessage("Error", iData.resData.Message, ctx);
                            }
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
                        MyApp.setStatus("isTicketUpdating", false);
                    }
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
        }

    }


    private void fetchStatus(String currentStatus, String sIssueId, String sCurrentStatusId) {

        Log.d("debuggingStatus", "statusId " + sIssueId);
        Log.d("debuggingStatus", currentStatus);
        statusListIds.clear();
        statusList.clear();
        Cursor cqueryFetchingStatus = sql.rawQuery("select IsMobileStatus from Issue_Status where StatusId='" + sCurrentStatusId + "'", null);
        if (cqueryFetchingStatus.getCount() > 0) {
            cqueryFetchingStatus.moveToFirst();
            Log.d("debuggingStatus", "currentStatus " + cqueryFetchingStatus.getString(0));
            if (cqueryFetchingStatus.getString(0).equals("1")) {
                Cursor cTemp = sql.rawQuery("select PreviousStatus from Issue_Detail where IssueId='" + sIssueId + "'", null);

                if (cTemp.getCount() > 0) {
                    cTemp.moveToFirst();
                    Log.d("debuggingStatus", "count " + cTemp.getCount() + " previous " + cTemp.getString(0));

                    cTemp.moveToFirst();
                    String parentStatus = cTemp.getString(0);
//                    if (parentStatus.equals("4102")) {
//                        parentStatus = "4481";
//                    }
                    Cursor cqueryFetchingChildStatus = sql.rawQuery("select StatusId from Issue_StatusHiererchy where ParentStatus='" + parentStatus + "'", null);
//                    Log.d("debuggingStatus", "childStatus " + cqueryFetchingChildStatus.getCount());
                    int childStatusCount = cqueryFetchingChildStatus.getCount();
                    if (childStatusCount > 0) {

                        for (cqueryFetchingChildStatus.moveToFirst(); !cqueryFetchingChildStatus.isAfterLast(); cqueryFetchingChildStatus.moveToNext()) {
                            Cursor cqueryFetchingStatusName = sql.rawQuery("select StatusName from Issue_Status where StatusId='" + cqueryFetchingChildStatus.getString(0) + "'", null);
                            Log.d("debuggingStatus", "statusName " + cqueryFetchingStatusName.getCount());
                            if (cqueryFetchingStatusName.getCount() > 0) {
                                cqueryFetchingStatusName.moveToFirst();
                                statusList.add(cqueryFetchingStatusName.getString(0));//Name
                                statusListIds.add(cqueryFetchingChildStatus.getString(0));//Id
                            } else {
                                statusList.add("N/A");
                                statusListIds.add("0");
                            }
                        }
                    } else {
                        statusList.add(currentStatus);
                        statusListIds.add(sCurrentStatusId);
                    }
                }
            } else {
                Cursor cqueryFetchingChildStatus = sql.rawQuery("select StatusId from Issue_StatusHiererchy where ParentStatus='" + sCurrentStatusId + "'", null);
                if (cqueryFetchingChildStatus.getCount() > 0) {
                    for (cqueryFetchingChildStatus.moveToFirst(); !cqueryFetchingChildStatus.isAfterLast(); cqueryFetchingChildStatus.moveToNext()) {
                        Cursor cqueryFetchingStatusName = sql.rawQuery("select StatusName from Issue_Status where StatusId='" + cqueryFetchingChildStatus.getString(0) + "'", null);
                        if (cqueryFetchingStatusName.getCount() > 0) {
                            cqueryFetchingStatusName.moveToFirst();
                            statusList.add(cqueryFetchingStatusName.getString(0));
                            statusListIds.add(cqueryFetchingChildStatus.getString(0));
                            Log.e("Sceline number:1293 ", cqueryFetchingChildStatus.getString(0));
                        } else {
                            statusList.add("N/A");
                            statusListIds.add("0");
                        }
                    }
                } else {
                    statusList.add(currentStatus);
                    statusListIds.add(sCurrentStatusId);
                }
            }
        }

    }

    void getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
//            isNetworkEnabled = locationManager
//                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled) {
            } else {
//                if (isNetworkEnabled) {
//                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        return;
//                    }
//                    locationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER,
//                            500, 100, this);
//                    Log.e("Network", "Network");
//                    if (locationManager != null) {
//                        location = locationManager
//                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                        if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
//                        }
//                    }
//                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                100,
                                100, this);
                        Log.e("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isCommentRequired(String comment, String id) {
        ApiResult.IssueStatus.lstDetails[] list = MyApp.getApplication().readIssuesStatusList();
        boolean isClosed = false;
        for (int i = 0; i < list.length; i++) {

            if (list[i].Id.equals(id)) {
                if (list[i].CommentRequired.equals("true")) {
//                    Log.d("commentRequired", list[i].CommentRequired);
                    isClosed = true;
                }
            }
        }
        if (isClosed)
            return !comment.isEmpty();
        return !isClosed;
    }

    private boolean isHoldTat(String id) {
        ApiResult.IssueStatus.lstDetails[] list = MyApp.getApplication().readIssuesStatusList();
        boolean isClosed = false;
        for (int i = 0; i < list.length; i++) {

            if (list[i].Id.equals(id)) {
                if (list[i].holdTat) {
//                    Log.d("commentRequired", list[i].holdTat + "");
                    isClosed = true;
                }
            }
        }

        return isClosed;
    }
}
