package net.mzi.trackengine;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import net.mzi.trackengine.model.PostUrl;

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
            sDeviceId = telephonyManager.getDeviceId().toString();
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
            sDateUI = sDateUI.substring(11, 19);
        }
        final TicketHolder ticketHolder = (TicketHolder) holder;
        ticketHolder.IssueID.setText(mTicketNumber.get(position));
        ticketHolder.Adress.setText(mLoc.get(position));
        ticketHolder.schedule_date.setText(MyApp.getApplication().readTicketCaptureSchedule().get(mTicketNumber.get(position)));
        ticketHolder.StatusId.setText(mStatus.get(position));
        ticketHolder.time.setText(sDateUI);
        ticketHolder.IssueText.setText(mSub.get(position));
        ticketHolder.btn_accetp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId ='" + mIssueID.get(position) + "'", null);
                    cqueryTemp.moveToFirst();
                    ContentValues newValues = new ContentValues();
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("IsAccepted", "1");
                    newValues.put("UpdatedDate", currentDateTimeString);
                    newValues.put("PreviousStatus", cqueryTemp.getString(0).toString());
                    sql.update("Issue_Detail", newValues, "IssueId=" + mIssueID.get(position), null);

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
//                            "('" + mIssueID.get(position) + "','" + nh_userid + "','" + sAcceptStatus + "','Accepted By Engineer','" + currentDateTimeString + "','-1')");
//                    Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                    String sColumnId = null;
//                    if (cque.getCount() > 0) {
//                        cque.moveToLast();
//                        sColumnId = cquery.getString(0).toString();
//
//                    }
                    cquery.close();
                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(nh_userid, sParentComapnyId,
                            mIssueID.get(position), sAcceptStatus, "Accepted by Engineer", currentDateTimeString,
                            DepartmentId, "", "", "", sDeviceId, "-1",
                            "", "", "");
                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                    call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                        @Override
                        public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                            try {
                                ApiResult.IssueDetail iData = response.body();
                                if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                    try {
                                        Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
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
//                                    ContentValues newValues = new ContentValues();
//                                    newValues.put("SyncStatus", "false");
//                                    sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                } else {
                                    Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                    try {
                                        Map<String, String> map = issueMap.get(mIssueID.get(position));
                                        map.put("SyncStatus", "true");
                                        issueMap.remove(mIssueID.get(position));
                                        MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                    } catch (Exception e) {
                                    }
//                                    ContentValues newValues = new ContentValues();
//                                    newValues.put("SyncStatus", "true");
//                                    sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                    Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);
                                    ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                    if (cqueryTemp.getCount() > 0) {
                                        cqueryTemp.moveToFirst();
                                        ref.child(MainActivity.LOGINID).child(mIssueID.get(position)).child("Action").setValue("Update");

                                    }
                                    cqueryTemp.close();
                                }
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                            call.cancel();
                        }
                    });
                } catch (Exception e) {
                }
                // new UpdateTask(context,postTktStatus,"0",sColumnId).execute();

                removeCard(position);
            }
        });
        ticketHolder.btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        Boolean wantToCloseDialog = true;
                        if (commemt.getText().toString().length() == 0) {
                            commemt.setError("Please enter comment");
                            wantToCloseDialog = false;
                            //Dialog.show();
                        }

                        if (wantToCloseDialog) {
                            Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + DepartmentId + "'", null);
                            if (cquery.getCount() > 0) {
                                cquery.moveToFirst();
                                sAcceptStatus = cquery.getString(0).toString();
                            } else
                                sAcceptStatus = "0";
                            Firstfrag f = new Firstfrag();
                            Date cDate = new Date();
                            String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

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

//                            sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                                    "('" + mIssueID.get(position) + "','" + nh_userid + "','" + sAcceptStatus + "','" + commemt.getText().toString() + "','" + currentDateTimeString + "','-1')");
//                            Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                            String sColumnId = null;
//                            if (cque.getCount() > 0) {
//                                cque.moveToLast();
//                                sColumnId = cquery.getString(0).toString();
//                            }
//                            cquery.close();
                            final ApiResult apiResult = new ApiResult();
                            final ApiResult.IssueDetail issueDetail = apiResult.new
                                    IssueDetail(nh_userid, sParentComapnyId, mIssueID.get(position),
                                    sAcceptStatus, commemt.getText().toString(),
                                    currentDateTimeString, DepartmentId, "",
                                    "", "", sDeviceId, "true",
                                    "", "", "");
                            Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

//                            final String finalColumnId = sColumnId;

//                            final String finalSColumnId = sColumnId;
                            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                @Override
                                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                    ApiResult.IssueDetail iData = response.body();
                                    if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                        try {
                                            Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
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
//                                        ContentValues newValues = new ContentValues();
//                                        newValues.put("SyncStatus", "false");
//                                        sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                    } else {
                                        MainActivity m = new MainActivity();
                                        m.updateCounter(context);
//                                        ContentValues newValues = new ContentValues();
//                                        newValues.put("SyncStatus", "true");
//                                        sql.update("Issue_History", newValues, "Id=" + finalSColumnId, null);
                                        Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                        try {
                                            Map<String, String> map = issueMap.get(mIssueID.get(position));
                                            map.put("SyncStatus", "true");
                                            issueMap.remove(mIssueID.get(position));
                                            MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                        } catch (Exception e) {
                                        }
                                        try {
                                            Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + mIssueID.get(position) + "'", null);
                                            ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                            if (cqueryTemp.getCount() > 0) {
                                                cqueryTemp.moveToFirst();
                                                ref.child(MainActivity.LOGINID).child(mIssueID.get(position)).child("Action").setValue("Delete");
                                            }
                                            cqueryTemp.close();
                                        } catch (Exception e) {
                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
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
        TextView IssueID, IssueText, time, Adress, StatusId, schedule_date;
        //        FloatingActionButton acceptButton, rejectButton;
        FloatingActionButton btn_accetp, btn_reject;

        public TicketHolder(View itemView) {
            super(itemView);
            View v = itemView;
            IssueID = (TextView) v.findViewById(R.id.textView2);
            schedule_date = (TextView) v.findViewById(R.id.schedule_date);
            IssueText = (TextView) v.findViewById(R.id.subject);
            Adress = (TextView) v.findViewById(R.id.adrs);
            StatusId = (TextView) v.findViewById(R.id.newtktstatus);
            time = (TextView) v.findViewById(R.id.beontime);
            btn_accetp = v.findViewById(R.id.btn_accept);
            btn_reject = v.findViewById(R.id.btn_reject);
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
