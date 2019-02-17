package net.mzi.trackengine;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

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
 * Created by Poonam on 3/23/2017.
 */
public class NewTaskAdapter extends RecyclerView.Adapter<NewTaskAdapter.ViewHolder> {
    ApiInterface apiInterface;
    List<String> mStatus = new ArrayList<String>();
    SharedPreferences pref;
    String sParentComapnyId;
    String DepartmentId;
    String nh_userid;
    List<String> mTime = new ArrayList<String>();
    List<String> mSub = new ArrayList<String>();
    List<String> mTicketNumber = new ArrayList<String>();
    List<String> mLoc = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    Firebase ref = null;
    List<String> mIssueID = new ArrayList<String>();
    Context context = null;
    String sAcceptStatus;
    SQLiteDatabase sql;
    Cursor cquery;
    String sDeviceId;
    Map<String, TicketInfoClass> issuesHistoryMap;


    public NewTaskAdapter() {

    }

    public NewTaskAdapter(List<String> mIssueID, List<String> mTime, List<String> mLoc, List<String> mSub, List<String> mStatus, List<Integer> mDatasetTypes, List<String> mTicketNumber, Context context) {
        this.context = context;
        this.mIssueID = mIssueID;
        this.mTime = mTime;
        this.mSub = mSub;
        this.mStatus = mStatus;
        this.mLoc = mLoc;
        this.mTicketNumber = mTicketNumber;
        this.mDatasetTypes = mDatasetTypes;
        issuesHistoryMap = MyApp.getApplication().readIssueDetailsHistory();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //context=parent.getContext();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = context.getSharedPreferences("login", 0);
        nh_userid = pref.getString("userid", "userid");
        sParentComapnyId = pref.getString("ParentCompanyId", "ParentCompanyId");
        DepartmentId = pref.getString("DepartmentId", "DepartmentId");
        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
        sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardxml, parent, false);

        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sDeviceId = telephonyManager.getDeviceId();
        }
        return new TicketHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String sDateUI;
        sDateUI = mTime.get(position);
        if (sDateUI.equals("")) {
            sDateUI = "NA";
        } else {
            if (sDateUI.length() >= 19)
                sDateUI = sDateUI.substring(11, 19);
        }
        final TicketHolder ticketHolder = (TicketHolder) holder;
        ticketHolder.IssueID.setText(mTicketNumber.get(position));
        ticketHolder.Adress.setText(mLoc.get(position));
        ticketHolder.schedule_date.setText(MyApp.getApplication().readTicketCaptureSchedule().get(mTicketNumber.get(position)));
        if (ticketHolder.schedule_date.getText().toString().isEmpty()) {

        }

        if (issuesHistoryMap.containsKey(mIssueID.get(position))) {
            if (issuesHistoryMap.get(mIssueID.get(position)).getType().equals("Ticket")) {
                ticketHolder.txt_lable.setText("Ticket Id:");
            } else {
                ticketHolder.txt_lable.setText("Task Id:");
            }
        }
        ticketHolder.StatusId.setText(mStatus.get(position));
        ticketHolder.time.setText(sDateUI);
        ticketHolder.IssueText.setText(mSub.get(position));
        ticketHolder.btn_accetp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.setStatus("isTicketUpdating", true);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        MyApp.setStatus("isTicketUpdating", false);
//                    }
//                }, 30000);
                SchedulingAdapter.isAttended = "0";
                Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "' ", null);
                if (cquery.getCount() > 0) {
                    cquery.moveToFirst();
                    sAcceptStatus = cquery.getString(0).toString();
                } else
                    sAcceptStatus = "0";

                Date cDate = new Date();
                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                try {
                    Log.d("debuggingStatus", mIssueID.get(position));
                    Cursor cqueryTemp = sql.rawQuery("select PreviousStatus from Issue_Detail where IssueId ='" + mIssueID.get(position) + "'", null);
                    cqueryTemp.moveToFirst();
                    ContentValues newValues = new ContentValues();
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("IsAccepted", "1");
                    newValues.put("UpdatedDate", currentDateTimeString);
                    newValues.put("PreviousStatus", cqueryTemp.getString(0));
                    Log.d("debuggingStatus", cqueryTemp.getString(0));
                    cqueryTemp.close();
                    MyApp.spinnerStart(context, "Please wait...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MyApp.spinnerStop();
                        }
                    }, 10000);
//                    String deletableId = issueDetail.IssueId;
//                    if (deletableId.equals("0"))
//                        deletableId = issueDetail.taskId;
                    Log.d("debuggingStatus", mIssueID.get(position));
                    sql.update("Issue_Detail", newValues, "IssueId=" + mIssueID.get(position), null);
//                    ((MainActivity) context).updateCounter(context, false);
                    Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                    final Map<String, String> map = new HashMap<>();
                    map.put("TicketId", mIssueID.get(position));
                    map.put("UserId", nh_userid);
                    map.put("StatusId", sAcceptStatus);
                    map.put("ParentCompanyId", sParentComapnyId);
                    map.put("Comment", "Accepted By Engineer");
                    map.put("ActivityDate", currentDateTimeString);
                    map.put("SyncStatus", "-1");
                    map.put("ticketNumber", mTicketNumber.get(position));
                    map.put("TaskId", "0");
                    Map<String, TicketInfoClass> issueDetailsMap = MyApp.getApplication().readIssueDetailsHistory();
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

                    try {
                        cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);
                        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                        if (MyApp.getApplication().readIssueDetailsHistory().containsKey(mIssueID.get(position))) {
                            if (MyApp.getApplication().readIssueDetailsHistory().get(mIssueID.get(position)).getType().equals("Ticket")) {
                                ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                            } else {
                                ref = new Firebase(PostUrl.sFirebaseUrlIssues);
                            }
                        }
                        if (cqueryTemp.getCount() > 0) {
                            cqueryTemp.moveToFirst();
                            ref.child(MainActivity.LOGINID).child(mIssueID.get(position)).child("Action").setValue("Update");
                        }
                        cqueryTemp.close();
                    } catch (Exception e) {
                    }

                    MainActivity m = new MainActivity();
                    m.updateCounter(context, false);
                    cquery.close();
                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId,
                            mIssueID.get(position), sAcceptStatus, "Accepted by Engineer", currentDateTimeString,
                            DepartmentId, "", "", "", sDeviceId, "-1",
                            "", "", "", "", "");
                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                    call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                        @Override
                        public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                            MyApp.spinnerStop();
                            try {
                                MyApp.setStatus("isTicketUpdating", false);
                                ApiResult.IssueDetail iData = response.body();
                                if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                    try {
                                        Toast.makeText(context, "Server response error. Data has been saved to offline, it will be synced after some time.", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        e.getMessage();
                                    }
                                    Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();

                                    try {
                                        Map<String, String> mapp = issueMap.get(map.get("TicketId"));
                                        mapp.put("SyncStatus", "false");
                                        issueMap.put(map.get("TicketId"), mapp);
                                        MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                    } catch (Exception e) {
                                    }
                                } else {
                                    Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                    Map<String, String> mapUpdate;
                                    String key = map.get("TicketId");
                                    if (!key.equals("0")) {
                                        mapUpdate = issueMap.get(map.get("TicketId"));
                                    } else {
                                        key = map.get("TaskId");
                                        mapUpdate = issueMap.get(map.get("TaskId"));
                                    }
                                    mapUpdate.put("SyncStatus", "true");
                                    issueMap.remove(key);
                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                    String issueID = key.contains("@@") ? key.split("@@")[0] : key;


                                    try {
                                        if (iData.resData.Status.equals("false")) {
                                            MyApp.popMessage("Error", iData.resData.Message, context);
                                        }
                                        Firstfrag f = new Firstfrag();
                                    } catch (Exception e) {
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                            MyApp.spinnerStop();
                            MyApp.setStatus("isTicketUpdating", false);
                            call.cancel();
                        }
                    });
                } catch (Exception e) {
                    MyApp.setStatus("isTicketUpdating", false);
                    MyApp.spinnerStop();
                }
                // new UpdateTask(context,postTktStatus,"0",sColumnId).execute();

                removeCard(position);
            }
        });
        ticketHolder.btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp.setStatus("isTicketUpdating", true);

                SchedulingAdapter.isAttended = "0";
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
                        boolean wantToCloseDialog = true;
                        if (commemt.getText().toString().length() == 0) {
                            commemt.setError("Please enter comment");
                            wantToCloseDialog = false;
                            //Dialog.show();
                        }
                        MyApp.spinnerStart(context, "Please wait...");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MyApp.spinnerStop();
                            }
                        }, 10000);

                        if (wantToCloseDialog) {
                            Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 0 and DepartmentId = '" + DepartmentId + "'", null);
                            if (cquery.getCount() > 0) {
                                cquery.moveToFirst();
                                sAcceptStatus = cquery.getString(0);
                            } else
                                sAcceptStatus = "0";
                            Date cDate = new Date();
                            String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                            Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                            final Map<String, String> map = new HashMap<>();
                            map.put("TicketId", mIssueID.get(position));
                            map.put("UserId", nh_userid);
                            map.put("StatusId", sAcceptStatus);
                            map.put("ParentCompanyId", sParentComapnyId);
                            map.put("Comment", commemt.getText().toString());
                            map.put("ActivityDate", currentDateTimeString);
                            map.put("SyncStatus", "-1");
                            map.put("ticketNumber", mTicketNumber.get(position));
                            map.put("TaskId", "0");
                            Map<String, TicketInfoClass> issueDetailsMap = MyApp.getApplication().readIssueDetailsHistory();
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
                            final ApiResult.IssueDetail issueDetail = apiResult.new
                                    IssueDetail(nh_userid, sParentComapnyId, mIssueID.get(position),
                                    sAcceptStatus, commemt.getText().toString(),
                                    currentDateTimeString, DepartmentId, "",
                                    "", "", sDeviceId, "true",
                                    "", "", "", "", "");
                            Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                            final Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);

                            String deletableId = issueDetail.IssueId;
                            if (deletableId.equals("0"))
                                deletableId = issueDetail.taskId;

                            ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                            if (MyApp.getApplication().readIssueDetailsHistory().containsKey(deletableId)) {
                                if (MyApp.getApplication().readIssueDetailsHistory().get(deletableId).getType().equals("Ticket")) {
                                    ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                } else {
                                    ref = new Firebase(PostUrl.sFirebaseUrlIssues);
                                }
                            }

                            if (cqueryTemp.getCount() > 0) {
                                cqueryTemp.moveToFirst();
                                try {
                                    if (!MainActivity.LOGINID.isEmpty() || MainActivity.LOGINID != null || deletableId != null
                                            || !deletableId.equals("null"))
                                        ref.child(MainActivity.LOGINID).child(deletableId).child("Action").setValue("Delete");
                                } catch (Exception e) {
                                }
                            }

                            sql.delete("Issue_Detail", "IssueId" + "=" + deletableId, null);
                            MainActivity m = new MainActivity();
                            m.updateCounter(context, false);
//                            ((MainActivity) context).updateCounter(context, false);

                            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                @Override
                                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                    MyApp.spinnerStop();
                                    MyApp.setStatus("isTicketUpdating", false);
                                    ApiResult.IssueDetail iData = response.body();
                                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                        try {
                                            Toast.makeText(context, "Server response error. Data has been saved to offline, it will be synced after some time.", Toast.LENGTH_LONG).show();
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
                                            Map<String, String> mapUpdate;
                                            String key = map.get("TicketId");
                                            if (!key.equals("0")) {
                                                mapUpdate = issueMap.get(map.get("TicketId"));
                                            } else {
                                                key = map.get("TaskId");
                                                mapUpdate = issueMap.get(map.get("TaskId"));
                                            }
                                            mapUpdate.put("SyncStatus", "true");
                                            issueMap.remove(key);
                                            MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                        } catch (Exception e) {
                                        }
                                        try {
                                            cqueryTemp.close();
                                            if (iData.resData.Status.equals("false")) {
                                                MyApp.popMessage("Error", iData.resData.Message, context);
                                            }
                                            Firstfrag f = new Firstfrag();
                                        } catch (Exception e) {
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                                    MyApp.spinnerStop();
                                    MyApp.setStatus("isTicketUpdating", false);
                                    call.cancel();
                                }
                            });


                            removeCard(position);
                            dialog.dismiss();


                        }
                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatasetTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDatasetTypes.get(position);
    }

    private class TicketHolder extends ViewHolder {
        TextView IssueID, IssueText, time, Adress, StatusId, schedule_date, txt_lable;
        //        FloatingActionButton acceptButton, rejectButton;
        FloatingActionButton btn_accetp, btn_reject;

        public TicketHolder(View itemView) {
            super(itemView);
            View v = itemView;
            IssueID = v.findViewById(R.id.textView2);
            schedule_date = v.findViewById(R.id.schedule_date);
            IssueText = v.findViewById(R.id.subject);
            Adress = v.findViewById(R.id.adrs);
            StatusId = v.findViewById(R.id.newtktstatus);
            time = v.findViewById(R.id.beontime);
            btn_accetp = v.findViewById(R.id.btn_accept);
            btn_reject = v.findViewById(R.id.btn_reject);
            txt_lable = v.findViewById(R.id.txt_lable);
        }
    }

    private void removeCard(int position) {
        try {
            mIssueID.remove(position);
            mTime.remove(position);
            mSub.remove(position);
            mStatus.remove(position);
            mLoc.remove(position);
            mTicketNumber.remove(position);
            mDatasetTypes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDatasetTypes.size());

            if (mDatasetTypes.size() == 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ((NewTaskActivity) context).finish();
                        } catch (Exception e) {
                        }
                    }
                }, 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);

        }
    }
}
