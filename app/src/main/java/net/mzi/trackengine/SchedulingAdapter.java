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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.google.gson.Gson;

import net.mzi.trackengine.fragment.EngineerFeedbackFragment;
import net.mzi.trackengine.model.PostUrl;

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
    boolean isNetworkEnabled = false;
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
    String sAcceptStatus;

    List<String> mName = new ArrayList<String>();
    List<String> mTime = new ArrayList<String>();
    List<String> mSub = new ArrayList<String>();
    List<String> mMob = new ArrayList<String>();
    List<String> mLoc = new ArrayList<String>();
    List<String> mCardType = new ArrayList<String>();
    List<String> mIssueID = new ArrayList<String>();
    List<Integer> mCardColor = new ArrayList<Integer>();
    static int poss;
    String currentStatus;
    static View nhn;
    static Firebase ref = null;
    Context ctx = null;
    Context context;
    private boolean statusByHierarchy;

    public SchedulingAdapter() {

    }

    public SchedulingAdapter(List<String> mIssueID, List<String> mName, List<String> mTime, List<String> mLoc, List<String> mMob, List<String> mSub, List<Integer> mDatasetTypes, List<Integer> mCardColor, List<String> mCardType, List<String> mTicketNumber, Context context) {
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
        TextView IssueID, IssueText, time, Adress, StatusId, schedule_date;
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
        }
    }

    private class SchedulingViewHolder extends ViewHolder {
        TextView name, time, sub, location, mobile, stats, sIssueID, preTime, schedule_date_value;
        ImageView mbut;
        LinearLayout bot;
        RelativeLayout top;
        final PopupMenu popup;

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
            this.bot = v.findViewById(R.id.bottomLayout);
            this.sIssueID = v.findViewById(R.id.issueId_Id);
            popup = new PopupMenu(ctx, mbut);
            popup.inflate(R.menu.toolbar_card_menu);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        ctx = parent.getContext();
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
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.schedulinglistcard, parent, false);
            nhn = v;
            return new SchedulingViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (holder.getItemViewType() == New) {
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
            ticketHolder.StatusId.setText(mCardType.get(position));
            ticketHolder.time.setText(sDateUI);
            ticketHolder.IssueText.setText(mSub.get(position));
            ticketHolder.schedule_date.setText(map.get(mTicketNumber.get(position)));
            ticketHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAttended = "0";
                    Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "' ", null);
                    if (cquery.getCount() > 0) {
                        cquery.moveToFirst();
                        sAcceptStatus = cquery.getString(0).toString();
                    } else
                        sAcceptStatus = "0";
                    Date cDate = new Date();
                    String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                    NewTaskAdapter s = new NewTaskAdapter();
                    Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                    Map<String, String> map = new HashMap<>();
                    map.put("TicketId", mIssueID.get(position));
                    map.put("UserId", nh_userid);
                    map.put("StatusId", sAcceptStatus);
                    map.put("ParentCompanyId", sParentComapnyId);
                    map.put("Comment", "Accepted By Engineer");
                    map.put("ActivityDate", currentDateTimeString);
                    map.put("SyncStatus", "-1");
                    ticketsMap.put(mIssueID.get(position), map);
                    MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);

//                    sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                            "('" + mIssueID.get(position) + "','" + nh_userid + "','" + sAcceptStatus + "','" + sParentComapnyId + "','Accepted By Engineer','" + currentDateTimeString + "','-1')");
//                    Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                    String sColumnId = null;
//                    if (cque.getCount() > 0) {
//                        cque.moveToLast();
//                        sColumnId = cquery.getString(0).toString();
//                    }
                    // s.new UpdateTask(ctx, tktStatus, "0",sColumnId).execute();
                    cquery = sql.rawQuery("select IssueId,TicketHolder,SLADate,Address,PhoneNo,Subject,StatusId,TicketNumber from Issue_Detail where IssueId ='" + mIssueID.get(position) + "'", null);
                    cquery.moveToFirst();

                    ContentValues newValues = new ContentValues();
                    newValues.put("IsAccepted", "1");
                    newValues.put("PreviousStatus", cquery.getString(6).toString());
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("UpdatedDate", currentDateTimeString);
                    sql.update("Issue_Detail", newValues, "IssueId=" + mIssueID.get(position), null);

                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId,
                            mIssueID.get(position), sAcceptStatus, "Accepted by Engineer", currentDateTimeString, DepartmentId, "", "", "", sDeviceId, "true", "", "", "");
                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

//                    final String finalColumnId = sColumnId;

                    call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                        @Override
                        public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                            ApiResult.IssueDetail iData = response.body();
                            if (iData.resData.Status == null || iData.resData.Status.equals("")
                                    || iData.resData.Status.equals("0")) {
                                try {
                                    MyApp.showMassage(context, context.getString(R.string.internet_error));
//                                    Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.getMessage();
                                }
                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                try {
                                    Map<String, String> map = issueMap.get(mIssueID.get(position));
                                    map.put("SyncStatus", "false");
                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                } catch (Exception e) {
                                }
//                                ContentValues newValues = new ContentValues();
//                                newValues.put("SyncStatus", "false");
//                                sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                            } else {
//                                ContentValues newValues = new ContentValues();
//                                newValues.put("SyncStatus", "true");
                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                try {
                                    Map<String, String> map = issueMap.get(mIssueID.get(position));
                                    map.put("SyncStatus", "true");
                                    issueMap.remove(mIssueID.get(position));
                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                } catch (Exception e) {
                                }
//                                sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
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


                    Cursor cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId ='" + cquery.getString(6).toString() + "'", null);
                    cqueryForStatus.moveToFirst();
                    addCard(cquery.getString(0).toString(), cquery.getString(1).toString(), cquery.getString(2).toString(), cquery.getString(3).toString(), cquery.getString(4).toString(), cquery.getString(5).toString(), cqueryForStatus.getString(0).toString(), R.color.orange, Pending, position, cquery.getString(7).toString());
//                    cquery.close();
                }
            });
            ticketHolder.rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isAttended = "0";
                    final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                    Dialog.setTitle("Rejection Reason: ");
                    Dialog.setCancelable(false);
                    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View dialogView = li.inflate(R.layout.rejection, null);
                    final EditText commemt = (EditText) dialogView.findViewById(R.id.rejectionReason);
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
                                commemt.setError("Please enter comment");
                                wantToCloseDialog = false;
                            }
                            if (wantToCloseDialog) {
                                Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "'", null);
                                if (cquery.getCount() > 0) {
                                    cquery.moveToFirst();
                                    sAcceptStatus = cquery.getString(0).toString();
                                } else
                                    sAcceptStatus = "0";
                                Firstfrag f = new Firstfrag();
                                //Log.e( "onClick:accept ",data.get(position).IssueID );
                                Date cDate = new Date();
                                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                             /*   postTktStatus.put("UserId", nh_userid);
                                postTktStatus.put("ParentCompanyId", sParentComapnyId);
                                postTktStatus.put("TicketId", mIssueID.get(position));
                                postTktStatus.put("StatusId", sAcceptStatus);//cquery.getString(0).toString();
                                postTktStatus.put("Comment", commemt.getText().toString());
                                postTktStatus.put("ActivityDate", currentDateTimeString);
                                postTktStatus.put("DepartmentId", DepartmentId);
                                postTktStatus.put("RealtimeUpdate", "true");
                                String tktStatus = new Gson().toJson(postTktStatus);*/
                                NewTaskAdapter s = new NewTaskAdapter();

                                Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                                Map<String, String> map = new HashMap<>();
                                map.put("TicketId", mIssueID.get(position));
                                map.put("UserId", nh_userid);
                                map.put("StatusId", sAcceptStatus);
                                map.put("ParentCompanyId", sParentComapnyId);
                                map.put("Comment", commemt.getText().toString());
                                map.put("ActivityDate", currentDateTimeString);
                                map.put("SyncStatus", "-1");
                                ticketsMap.put(mIssueID.get(position), map);
                                MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);


//                                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                                        "('" + mIssueID.get(position) + "','" + nh_userid + "','" + sAcceptStatus + "','" + sParentComapnyId + "','" + commemt.getText().toString() + "','" + currentDateTimeString + "','-1')");
//                                Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                                String sColumnId = null;
//                                if (cque.getCount() > 0) {
//                                    cque.moveToLast();
//                                    sColumnId = cquery.getString(0).toString();
//                                }
//                                cquery.close();
                                final ApiResult apiResult = new ApiResult();
                                final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId, mIssueID.get(position), sAcceptStatus, commemt.getText().toString(), currentDateTimeString, DepartmentId, "", "", "", sDeviceId, "-1", "", "", "");
                                Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

//                                final String finalColumnId = sColumnId;
//
//                                final String finalSColumnId = sColumnId;
                                call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                    @Override
                                    public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                        ApiResult.IssueDetail iData = response.body();
                                        if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                            try {
                                                MyApp.showMassage(context, context.getString(R.string.internet_error));
//                                                Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                e.getMessage();
                                            }
//                                            ContentValues newValues = new ContentValues();
//                                            newValues.put("SyncStatus", "false");
                                            Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                            try {
                                                Map<String, String> map = issueMap.get(mIssueID.get(position));
                                                map.put("SyncStatus", "false");
                                                MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                            } catch (Exception e) {
                                            }
//                                            sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                        } else {
                                            MainActivity m = new MainActivity();
                                            m.updateCounter(context);
//                                            ContentValues newValues = new ContentValues();
//                                            newValues.put("SyncStatus", "true");
//                                            sql.update("Issue_History", newValues, "Id=" + finalSColumnId, null);
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
//                                            cqueryTemp.close();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                                        call.cancel();
                                    }
                                });


                                //s.new UpdateTask(ctx, tktStatus, "1",sColumnId).execute();
                                MainActivity m = new MainActivity();
                                removeCard(position);
                                dialog.dismiss();
                            }

                        }
                    });

                }
            });

        } else {
            Map<String, String> map = MyApp.getApplication().readTicketCaptureSchedule();
            final SchedulingViewHolder schedulingholder = (SchedulingViewHolder) holder;
            //fetchStatus(currentStatus,position);
            schedulingholder.mobile.setText(mMob.get(position));
            schedulingholder.sub.setText(mSub.get(position));
            schedulingholder.location.setText(mLoc.get(position));
            schedulingholder.time.setText(mTime.get(position));
            schedulingholder.name.setText(mName.get(position));
            schedulingholder.schedule_date_value.setText(map.get(mTicketNumber.get(position)));
            schedulingholder.stats.setText(mCardType.get(position));
            schedulingholder.top.setBackgroundResource(mCardColor.get(position));
            schedulingholder.bot.setBackgroundResource(mCardColor.get(position));
            schedulingholder.mbut.setBackgroundResource(mCardColor.get(position));
            schedulingholder.sIssueID.setText(mTicketNumber.get(position));
            schedulingholder.mobile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mMob.get(position)));
                    ctx.startActivity(in);
                }
            });
            schedulingholder.stats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentStatus = mCardType.get(position);
                    Cursor cqueryIsVerified = sql.rawQuery("select IsVerified,PreviousStatus from Issue_Detail where IssueId='" + mIssueID.get(position) + "'", null);
                    cqueryIsVerified.moveToFirst();
                    sPreviousId = cqueryIsVerified.getString(1).toString();
                    if (cqueryIsVerified.getString(0).toString().equals("0")) {
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
                                Boolean wantToCloseDialog = true;
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

//                    cqueryIsVerified.close();
                }
            });
            String sMainStatusIdTemp = null;
            Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId='" + mIssueID.get(position) + "'", null);
            if (cqueryTemp.getCount() > 0) {
                cqueryTemp.moveToFirst();
                Cursor cqueryTemp1 = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + cqueryTemp.getString(0).toString() + "'", null);
                if (cqueryTemp1.getCount() > 0) {
                    cqueryTemp1.moveToFirst();
                    sMainStatusIdTemp = cqueryTemp1.getString(0).toString();
                } else
                    sMainStatusIdTemp = "4";
//                cqueryTemp1.close();
            } else {
                sMainStatusIdTemp = "4";
            }
//            cqueryTemp.close();
            if (mTime.get(position).equals(""))
                schedulingholder.time.setText("NA!!");
            else if (sMainStatusIdTemp.equals("4")) {
                schedulingholder.time.setText("--");
            } else {
                Date dSLAdate = null, dCurrentDate = null;
                String dtStart = mTime.get(position);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                try {
                    dSLAdate = format.parse(dtStart);
                    System.out.println(dSLAdate);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Date cDate = new Date();
                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(cDate);
                try {
                    dCurrentDate = format.parse(currentDateTimeString);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
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
            schedulingholder.popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nroute:

                            Intent searchAddress = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + mLoc.get(position)));
                            ctx.startActivity(searchAddress);
                            return true;
//                        case R.id.atkt:
//                            MyApp.showMassage(ctx, "Coming Soon!!!");
//                            return true;
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
                            if (cqueryIsVerified.getString(0).toString().equals("0")) {
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
                                        Boolean wantToCloseDialog = true;
                                        if (commemtVerify.getText().toString().length() == 0) {
                                            commemtVerify.setError("Please enter Asset Serial Number");
                                            wantToCloseDialog = false;

                                        }
                                        if (wantToCloseDialog) {
                                            //dialogMain.dismiss();
                                            populateStatus(mIssueID.get(position), position, commemtVerify.getText().toString(), sPreviousId);
                                            dialog.dismiss();
                                        }
                                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                                    }
                                });
                            } else {
                                populateStatus(mIssueID.get(position), position, "", sPreviousId);
                            }
//                            cqueryIsVerified.close();
                            return true;
                        case R.id.showDistance:
                            DecimalFormat df = new DecimalFormat("####.00");
                            getLocation();
                            String sDistance = String.valueOf(df.format(showDistance(latitude, longitude, mLoc.get(position), context)));
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                            alertDialogBuilder.setTitle("Distance in KM");
                            alertDialogBuilder
                                    .setMessage("Distance:" + sDistance + " KM")
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
                            final EditText commemt = (EditText) dialogView.findViewById(R.id.rejectionReason);
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
                    schedulingholder.popup.show();
                }
            });
        }
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
//            Toast.makeText(ctx, "Loading Data!!!", Toast.LENGTH_LONG).show();
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
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonString);
                SQLiteDatabase sql;
                String currentDateTimeString = null;
                sql = ctx.openOrCreateDatabase("MZI.sqlite", ctx.MODE_PRIVATE, null);

                if (s == null) {
                    MyApp.showMassage(ctx, ctx.getString(R.string.internet_error));
//                    Toast.makeText(ctx, R.string.internet_error, Toast.LENGTH_LONG).show();
                } else {
                    MyApp.showMassage(ctx, "OEM Updated Successfully!!!");
//                    Toast.makeText(ctx, "OEM Updated Successfully!!!", Toast.LENGTH_LONG).show();
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

    private Double showDistance(Double lat, Double lon, String sAddress, Context context) {
        Geocoder coder = new Geocoder(context);
        Double lati, longi;
        lati = null;
        longi = null;
        List<Address> address;
        try {
            address = coder.getFromLocationName(sAddress, 5);
            if (address == null || address.size() == 0) {
                return 0.0;
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
            return 10.0;
        }


    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void addCard(String sIssueId, String sName, String sTime, String sLoc, String sMob, String sSub, String sStatus, int iColor, int iDatatype, int position, String sTicketNumber) {

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
    }

    private void populateStatus(final String id, final int position, final String sAssetVerificationText, final String sPreviousId) {
        postTktStatus.put("ModeOfTransport", "0");
        postTktStatus.put("Expense", "0");
        postTktStatus.put("AssignedUserId", "0");
        final LinearLayout layoutTransport;
        final String sCurrentStatusId;
        cquery = sql.rawQuery("select IsAccepted, StatusId from Issue_Detail where IssueId='" + id + "'", null);
        cquery.moveToFirst();
        String sCardType = cquery.getString(0).toString();
        sCurrentStatusId = cquery.getString(1).toString();
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
                    Boolean wantToCloseDialog = true;
                    if (commemt.getText().toString().length() == 0) {
                        commemt.setError("Please enter comment");
                        wantToCloseDialog = false;
                        //Dialog.show();
                    }
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (wantToCloseDialog) {
                        String sSelectedStatus;
                        cquery = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + statusListIds.get(poss) + "'", null);
                        cquery.moveToFirst();
                        sSelectedStatus = statusListIds.get(poss);
                        Date cDate = new Date();
                        currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                        //if (cquery.getString(1).toString().equals("1")) {
                        if (cquery.getString(0).toString().equals("4")) {

                            sMainStatusId = cquery.getString(0).toString();

                            if (bCSATEnable) {
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
                                //engineerFeedbackFragment.sendDataToEngineerFeedBack(MainActivity.LOGINID,id,commemt.getText().toString(),currentTime,sMainStatusId);
                                //session.createLoginSession(Login.uname,Login.pwd);
                            } else {

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
                                MainActivity m = new MainActivity();
                                m.updateCounter(ctx);
                                Snackbar.make(v, "Status updated successfully!!!", Snackbar.LENGTH_LONG).show();
                                postTktStatus.put("UserId", nh_userid);
                                postTktStatus.put("ParentCompanyId", sParentComapnyId);
                                postTktStatus.put("TicketId", id);
                                postTktStatus.put("StatusId", sSelectedStatus);
                                postTktStatus.put("Comment", commemt.getText().toString());
                                postTktStatus.put("ActivityDate", currentTime);
                                postTktStatus.put("DepartmentId", DepartmentId);
                                postTktStatus.put("RealtimeUpdate", "true");
                                postTktStatus.put("Latitude", String.valueOf(latitude));
                                postTktStatus.put("Latitude", String.valueOf(latitude));
                                postTktStatus.put("Longitude", String.valueOf(longitude));
                                postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                                postTktStatus.put("Expense", eTransport.getText().toString());
                                postTktStatus.put("SyncStatus", "-1");
                                Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();
                                issuesMap.put(id, postTktStatus);
                                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);

//                                String tktStatus = new Gson().toJson(postTktStatus);
//                                try {
//                                    sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                                            "('" + postTktStatus.get("TicketId")
//                                            + "','" + postTktStatus.get("UserId")
//                                            + "','" + postTktStatus.get("StatusId")
//                                            + "','" + postTktStatus.get("Comment")
//                                            + "','" + postTktStatus.get("ActivityDate") + "','-1')");
//                                } catch (Exception e) {
//                                }

                                try {
//                                    Cursor cquery = sql.rawQuery("select * from Issue_History ", null);
//                                    String sColumnId = null;
//                                    if (cquery.getCount() > 0) {
//                                        cquery.moveToLast();
//                                        sColumnId = cquery.getString(0).toString();
//                                    }
                                    Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + id + "'", null);
                                    if (cqueryTemp.getCount() > 0) {
                                        cqueryTemp.moveToFirst();
                                        if (!nh_userid.equals("0"))
                                            ref.child(nh_userid).child(mIssueID.get(position)).child("Action").setValue("Update");
                                    }
                                    UpdateTask(context, postTktStatus,"true");
                                } catch (Exception e) {
                                }

                                LastTransportMode = postTktStatus.get("ModeOfTransport");

                                editor.putString("LastTransport", LastTransportMode);
                                editor.apply();
                                editor.commit();

                                removeCard(position);
                            }

                        } else if (cquery.getString(0).toString().equals("1")
                                || cquery.getString(0).toString().equals("6") ||
                                cquery.getString(0).toString().equals("5")) {
                            Cursor cqueryCurrentStatus_MainId = sql.rawQuery("select MainStatusId from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                            cqueryCurrentStatus_MainId.moveToFirst();
                            if (cqueryCurrentStatus_MainId.getString(0).equals("1") || cqueryCurrentStatus_MainId.getString(0).toString().equals("6") || cqueryCurrentStatus_MainId.getString(0).toString().equals("5")) {
                                ContentValues newValues = new ContentValues();
                                newValues.put("StatusId", sSelectedStatus);
                                newValues.put("IsAccepted", "1");
                                newValues.put("UpdatedDate", currentTime);
                                newValues.put("PreviousStatus", sCurrentStatusId);
                                sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                flag = false;
                            } else {
                                flag = true;
                                ContentValues newValues = new ContentValues();
                                newValues.put("StatusId", sSelectedStatus);
                                newValues.put("IsAccepted", "2");
                                newValues.put("UpdatedDate", currentTime);
                                newValues.put("PreviousStatus", sCurrentStatusId);
                                sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                isAttended = "1";
                            }
                        } else {
                            Cursor cqueryCurrentStatus_MainId = sql.rawQuery("select MainStatusId from Issue_Status where StatusId ='" + sCurrentStatusId + "'", null);
                            if (cqueryCurrentStatus_MainId.getCount() > 0) {
                                cqueryCurrentStatus_MainId.moveToFirst();

                                if (cqueryCurrentStatus_MainId.getString(0).equals("1") || cqueryCurrentStatus_MainId.getString(0).toString().equals("6") || cqueryCurrentStatus_MainId.getString(0).toString().equals("5")) {
                                    flag = true;
                                    ContentValues newValues = new ContentValues();
                                    newValues.put("StatusId", sSelectedStatus);
                                    newValues.put("IsAccepted", "2");
                                    newValues.put("UpdatedDate", currentTime);
                                    newValues.put("PreviousStatus", sCurrentStatusId);
                                    sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                    isAttended = "1";
                                } else {
                                    flag = false;
                                    ContentValues newValues = new ContentValues();
                                    newValues.put("StatusId", sSelectedStatus);
                                    newValues.put("IsAccepted", "2");
                                    newValues.put("UpdatedDate", currentTime);
                                    newValues.put("PreviousStatus", sCurrentStatusId);
                                    sql.update("Issue_Detail", newValues, "IssueId=" + id, null);
                                    isAttended = "1";
                                }
                            }
                        }

                        if (TaskActivity.card.equals("-1")) {
                            cquery = sql.rawQuery("select IssueId,TicketHolder,SLADate,Address,PhoneNo,Subject,StatusId,IsAccepted,TicketNumber from Issue_Detail where IssueId ='" + id + "'", null);
                            cquery.moveToFirst();
                            Cursor cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId ='" + cquery.getString(6).toString() + "'", null);
                            cqueryForStatus.moveToFirst();
                            int iColor, iDatatype;
                            if (cquery.getString(7).toString().equals("1")) {
                                iColor = R.color.orange;
                                iDatatype = Pending;
                            } else if (cquery.getString(7).toString().equals("2")) {
                                iColor = R.color.blue;
                                iDatatype = Attended;
                            } else {
                                iColor = R.color.green;
                                iDatatype = Complete;
                            }
                            addCard(cquery.getString(0).toString(), cquery.getString(1).toString(), cquery.getString(2).toString(), cquery.getString(3).toString(), cquery.getString(4).toString(), cquery.getString(5).toString(), cqueryForStatus.getString(0).toString(), iColor, iDatatype, position, cquery.getString(8).toString());
                        } else {
                            Cursor cqueryTempStatus = sql.rawQuery("select MainStatusId from Issue_Status where StatusId='" + sSelectedStatus + "'", null);
                            cqueryTempStatus.moveToFirst();
                            if (sMainStatusId.equals(cqueryTempStatus.getString(0).toString())) ;
                            else {
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
                                postTktStatus.put("AssetSerialNo", sAssetVerificationText);
                                postTktStatus.put("DeviceId", sDeviceId);
                                postTktStatus.put("Expense", eTransport.getText().toString());
                                postTktStatus.put("SyncStatus", "-1");

                                String issueStatus = postTktStatus.get("Comment").replace("'", "''");

                                Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();
                                issuesMap.put(id, postTktStatus);
                                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);

//                                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                                        "('" + postTktStatus.get("TicketId") + "','" + postTktStatus.get("UserId") + "','" + postTktStatus.get("StatusId") + "','" + issueStatus + "','" + postTktStatus.get("ActivityDate") + "','-1')");
//                                Cursor cquery = sql.rawQuery("select * from Issue_History ", null);
//                                String sColumnId = null;
//                                if (cquery.getCount() > 0) {
//                                    cquery.moveToLast();
//                                    sColumnId = cquery.getString(0).toString();
//                                }
                                LastTransportMode = postTktStatus.get("ModeOfTransport");
                                editor.putString("LastTransport", postTktStatus.get("ModeOfTransport"));
                                editor.apply();
                                editor.commit();

                                UpdateTask(ctx, postTktStatus,"true");

                                Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + id + "'", null);
                                if (cqueryTemp.getCount() > 0) {
                                    cqueryTemp.moveToFirst();
                                    if (!nh_userid.equals("0"))
                                        ref.child(nh_userid).child(id).child("Action").setValue("Update");
                                }
                                MainActivity m = new MainActivity();
                                m.updateCounter(ctx);
                                Log.e("onClick: mcard ", String.valueOf(statusList.size()));
                                mCardType.add(position, statusList.get(poss));
                                notifyItemChanged(position);
                                Snackbar.make(nhn, "Status update successfully!!!-completed", Snackbar.LENGTH_LONG).show();
                                if (flag) {
                                    removeCard(position);

                                }
                                //removeCard(position);
                            }
                        }
                        /***********************************************************************************************************************/


                    }
                    dialogMain.dismiss();
                }
                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.

            });
            //Dialog.show();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, statusList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnercategory.setAdapter(adapter);

            ArrayAdapter<String> adapterTransport = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, mtTransportlistName);
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
                            if (cquery.getString(0).toString().equals("true")) {
                                commemt.setText("");
                                commemt.setVisibility(View.VISIBLE);
                            } else {
                                commemt.setVisibility(View.INVISIBLE);
                                commemt.setText("n/a");
                            }
                            if (cquery.getString(1).toString().equals("true")) {
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
                        if (cquery1.getString(0).toString().equals("true") && cquery2.getString(0).toString().equals("true")) {
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
                    // Toast.makeText(ctx,String.valueOf(arg2+arg3),Toast.LENGTH_LONG).show();
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });


            spinnerTransport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int arg2, long arg3) {
                    //String selItem = parent.getSelectedItem().toString();
                    postTktStatus.put("ModeOfTransport", mtTransportlistIds.get(arg2));
                    postTktStatus.put("Expense", "0");
                    postTktStatus.put("AssignedUserId", nh_userid);
                    MyApp.showMassage(ctx, String.valueOf(arg2 + arg3));
//                    Toast.makeText(ctx, String.valueOf(arg2 + arg3), Toast.LENGTH_LONG).show();
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                }
            });


        }
    }

    private void fetchTransportMode() {
        mtTransportlistName.clear();
        mtTransportlistIds.clear();
        ApiResult.ModeOfTrasportList temp = null;
        cquery = sql.rawQuery("select * from ModeOfTrasportList", null);
        if (cquery.getCount() > 0) {
            for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                mtTransportlistIds.add(cquery.getString(1).toString());
                mtTransportlistName.add(cquery.getString(2).toString());
            }
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
                        String cardType = cquery.getString(0).toString();
                        if (cardType.equals("-1")) ;
                        else {
                            Intent i = new Intent(v.getContext(), TicketInfo.class);
                            i.putExtra("CardType", cardType);
                            i.putExtra("IssueId", mIssueID.get(getAdapterPosition()));
                            //Log.d("TAG", "onClick: ");
                            ctx.startActivity(i);
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    public void UpdateTask(final Context ctx, final Map<String, String> postTktStatus,String realTimeUpdate) {

        Log.e("TicketStatusTable", "status value " + postTktStatus.get("StatusId"));
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        final ApiResult apiResult = new ApiResult();
        //final ApiResult.IssueDetail issueDetail=apiResult.new IssueDetail(postTktStatus.get("UserId"),sParentComapnyId,id, sSelectedStatus ,commemt.getText().toString(),currentTime,DepartmentId,String.valueOf(latitude),String.valueOf(longitude),sAssetVerificationText,sDeviceId,"-1");
        try {
            final ApiResult.IssueDetail issueDetail =
                    apiResult.new IssueDetail(postTktStatus.get("UserId"),
                            postTktStatus.get("ParentCompanyId"),
                            postTktStatus.get("TicketId"),
                            postTktStatus.get("StatusId"),
                            postTktStatus.get("Comment"),
                            postTktStatus.get("ActivityDate"),
                            postTktStatus.get("DepartmentId"),
                            postTktStatus.get("Latitude"),
                            postTktStatus.get("Longitude"),
                            postTktStatus.get("AssetSerialNo"),
                            postTktStatus.get("DeviceId"),
                            postTktStatus.get(realTimeUpdate),
                            postTktStatus.get("ModeOfTransport"),
                            postTktStatus.get("Expense"),
                            postTktStatus.get("AssignedUserId"));

//            final String finalColumnId = sColumnId;
            if (postTktStatus.get("ModeOfTransport").equals("0")) ;
            else {
                LastTransportMode = postTktStatus.get("ModeOfTransport").toString();
                editor.putString("LastTransport", LastTransportMode);
                editor.apply();
                editor.commit();
            }
            Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);
            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                @Override
                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                    try {
                        ApiResult.IssueDetail iData = response.body();
                        if (iData.resData.Status == null || iData.resData.Status.toString().equals("") || iData.resData.Status.toString().equals("0")) {
                            try {
                                MyApp.showMassage(ctx, ctx.getString(R.string.internet_error));
//                                Toast.makeText(ctx, R.string.internet_error, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.getMessage();
                            }
                            Log.e("TicketStatusTable", "Success but not true");
                            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                            Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                            map.put("SyncStatus", "false");
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
//                            ContentValues newValues = new ContentValues();
//                            newValues.put("SyncStatus", "false");
//                            sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                        } else {
                            Log.e("TicketStatusTable", "Success" + " response " + iData.toString());
                            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                            savedMap.remove(postTktStatus.get("TicketId"));
                            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
//                            ContentValues newValues = new ContentValues();
//                            newValues.put("SyncStatus", "true");
//                            sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                        }
                    } catch (Exception e) {
                        Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                        Map<String, String> map = savedMap.get(postTktStatus.get("TicketId"));
                        map.put("SyncStatus", "false");
                        MyApp.getApplication().writeTicketsIssueHistory(savedMap);
//                        ContentValues newValues = new ContentValues();
//                        newValues.put("SyncStatus", "false");
//                        sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                    }
                }

                @Override
                public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                    Log.e("TicketStatusTable", "On failure");
                    call.cancel();
                    Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
                    Map<String,String> map = savedMap.get(postTktStatus.get("TicketId"));
                    map.put("SyncStatus","false");
                    MyApp.getApplication().writeTicketsIssueHistory(savedMap);
//                    ContentValues newValues = new ContentValues();
//                    newValues.put("SyncStatus", "false");
//                    sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);

                }
            });
        } catch (Exception e) {
            Log.e("TicketStatusTable", "came to exception " + e.toString());
            Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
            Map<String,String> map = savedMap.get(postTktStatus.get("TicketId"));
            map.put("SyncStatus","false");
            MyApp.getApplication().writeTicketsIssueHistory(savedMap);
//            ContentValues newValues = new ContentValues();
//            newValues.put("SyncStatus", "false");
//            sql.update("Issue_History", newValues, "Id=" + sColumnId, null);
            return;
        }


    }

    private void fetchStatus(String currentStatus, String sIssueId, String sCurrentStatusId) {
        statusListIds.clear();
        statusList.clear();
        Cursor cqueryFetchingStatus = sql.rawQuery("select IsMobileStatus from Issue_Status where StatusId='" + sCurrentStatusId + "'", null);
        if (cqueryFetchingStatus.getCount() > 0) {
            cqueryFetchingStatus.moveToFirst();
            if (cqueryFetchingStatus.getString(0).toString().equals("1")) {
                Cursor cTemp = sql.rawQuery("select PreviousStatus from Issue_Detail where IssueId='" + sIssueId + "'", null);
                if (cTemp.getCount() > 0) {
                    cTemp.moveToFirst();
                    Cursor cqueryFetchingChildStatus = sql.rawQuery("select StatusId from Issue_StatusHiererchy where ParentStatus='" + cTemp.getString(0).toString() + "'", null);
                    if (cqueryFetchingChildStatus.getCount() > 0) {
                        for (cqueryFetchingChildStatus.moveToFirst(); !cqueryFetchingChildStatus.isAfterLast(); cqueryFetchingChildStatus.moveToNext()) {
                            Cursor cqueryFetchingStatusName = sql.rawQuery("select StatusName from Issue_Status where StatusId='" + cqueryFetchingChildStatus.getString(0).toString() + "'", null);
                            if (cqueryFetchingStatusName.getCount() > 0) {
                                cqueryFetchingStatusName.moveToFirst();
                                statusList.add(cqueryFetchingStatusName.getString(0).toString());//Name
                                statusListIds.add(cqueryFetchingChildStatus.getString(0).toString());//Id
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
                        Cursor cqueryFetchingStatusName = sql.rawQuery("select StatusName from Issue_Status where StatusId='" + cqueryFetchingChildStatus.getString(0).toString() + "'", null);
                        if (cqueryFetchingStatusName.getCount() > 0) {
                            cqueryFetchingStatusName.moveToFirst();
                            statusList.add(cqueryFetchingStatusName.getString(0).toString());
                            statusListIds.add(cqueryFetchingChildStatus.getString(0).toString());
                            Log.e("Sceline number:1293 ", cqueryFetchingChildStatus.getString(0).toString());
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

//        if(!statusByHierarchy){
//            statusList.clear();
//            statusListIds.clear();
//            ApiResult.IssueStatus.lstDetails data[] = MyApp.getApplication().readIssuesStatusList();
//            for (int i = 0; i <data.length ; i++) {
//                statusList.add(data[i].StatusName);
//                statusListIds.add(data[i].Id);
//            }
//        }
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
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
                    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            500, 100, this);
                    Log.e("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
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
}
