package net.mzi.trackengine;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 1/30/2017.
 */
public class SwipeDeckAdapter extends RecyclerView.Adapter<SwipeDeckAdapter.ViewHolder> {
    ApiInterface apiInterface;
    Firebase ref = null;
    SQLiteDatabase sql;
    public List<TicketInfoClass> data;
    public Context context;
    String sParentComapnyId, sDepartmentId;
    SharedPreferences pref;
    String sUserId;
    String sDeviceId;
    Map<String, String> postTktStatus = new HashMap<String, String>();
    private LayoutInflater inflater;
    private Firstfrag ff;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.cardxml, parent, false));
    }

    public SwipeDeckAdapter(List<TicketInfoClass> data, Context context, Firstfrag f) {
        this.context = context;
        sql = context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        this.data = data;
        this.inflater = LayoutInflater.from(context);
        ff = f;
    }

//    public void addSpots(List<TicketInfoClass> spots) {
//        this.data.addAll(spots);
//    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView2, subject, adrs, newtktstatus, beontime, schedule_date;
        //        FloatingActionButton reject, accept;
        FloatingActionButton btn_accept, btn_reject;

        ViewHolder(View view) {
            super(view);
            this.textView2 = view.findViewById(R.id.textView2);
            this.subject = view.findViewById(R.id.subject);
            this.adrs = view.findViewById(R.id.adrs);
            newtktstatus = view.findViewById(R.id.newtktstatus);
            schedule_date = view.findViewById(R.id.schedule_date);
            beontime = view.findViewById(R.id.beontime);
            btn_reject = view.findViewById(R.id.btn_reject);
            btn_accept = view.findViewById(R.id.btn_accept);
            btn_reject.setOnClickListener(this);
            btn_accept.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == btn_accept) {
                SchedulingAdapter.isAttended = "0";
                String sAcceptStatus = null;
                Log.e("onClick:accept ", data.get(getLayoutPosition()).IssueID);
                Firstfrag f = new Firstfrag();
                Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '" + sDepartmentId + "' ", null);
                if (cquery.getCount() > 0) {
                    cquery.moveToFirst();
                    sAcceptStatus = cquery.getString(0).toString();
                } else
                    sAcceptStatus = "0";
                cquery.close();
                Date cDate = new Date();
                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

                NewTaskAdapter s = new NewTaskAdapter();

                Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                Map<String, String> map = new HashMap<>();
                map.put("TicketId", data.get(getLayoutPosition()).IssueID);
                map.put("UserId", sUserId);
                map.put("StatusId", sAcceptStatus);
                map.put("ParentCompanyId", sParentComapnyId);
                map.put("Comment", "Accepted By Engineer");
                map.put("ActivityDate", currentDateTimeString);
                map.put("SyncStatus", "-1");
                ticketsMap.put(data.get(getLayoutPosition()).IssueID, map);
                MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);

//                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                        "('" + data.get(position).IssueID + "','" + sUserId + "','" + sAcceptStatus + "','Accepted By Engineer','" + currentDateTimeString + "','-1')");
//                Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                String sColumnId = null;
//                if (cque.getCount() > 0) {
//                    cque.moveToLast();
//                    sColumnId = cque.getString(0).toString();
//                }
//                cque.close();
                Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId ='" + data.get(getLayoutPosition()).IssueID + "'", null);
                if (cqueryTemp.getCount() > 0) {
                    cqueryTemp.moveToFirst();
                    //s.new UpdateTask(context, tktStatus, "0", sColumnId).execute();
                    ContentValues newValues = new ContentValues();
                    newValues.put("PreviousStatus", cqueryTemp.getString(0).toString());
                    newValues.put("IsAccepted", "1");
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("UpdatedDate", currentDateTimeString);

                    sql.update("Issue_Detail", newValues, "IssueId=" + data.get(getLayoutPosition()).IssueID, null);

                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(sUserId, sParentComapnyId, data.get(getLayoutPosition()).IssueID, sAcceptStatus, "Accept by Engineer", currentDateTimeString, sDepartmentId, "", "", "", sDeviceId, "true", "", "", "");
                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

//                    final String finalColumnId = sColumnId;

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
                                        Map<String, String> map = issueMap.get(data.get(getLayoutPosition()).IssueID);
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
                                        Map<String, String> map = issueMap.get(data.get(getLayoutPosition()).IssueID);
                                        map.put("SyncStatus", "true");
                                        issueMap.remove(data.get(getLayoutPosition()).IssueID);
                                        MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                    } catch (Exception e) {
                                    }
//                                    ContentValues newValues = new ContentValues();
//                                    newValues.put("SyncStatus", "true");
//                                    sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                    try {
                                        Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + data.get(getLayoutPosition()).IssueID + "'", null);
                                        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                        if (cqueryTemp.getCount() > 0) {
                                            cqueryTemp.moveToFirst();
                                            ref.child(MainActivity.LOGINID).child(data.get(getLayoutPosition()).IssueID).child("Action").setValue("Update");

                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                            call.cancel();
                        }
                    });

                }

                ff.swipeLeft(getLayoutPosition());
                cqueryTemp.close();
            } else if (v == btn_reject) {
                SchedulingAdapter.isAttended = "0";
                AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
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
                        String sAcceptStatus = null;
                        Boolean wantToCloseDialog = true;
                        if (commemt.getText().toString().length() == 0) {
                            commemt.setError("Please enter comment");
                            wantToCloseDialog = false;
                            //Dialog.show();
                        }

                        if (wantToCloseDialog) {

                            Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 0 and DepartmentId = '" + sDepartmentId + "'", null);
                            if (cquery.getCount() > 0) {
                                cquery.moveToFirst();
                                sAcceptStatus = cquery.getString(0).toString();
                            } else
                                sAcceptStatus = "0";
                            Firstfrag f = new Firstfrag();
//                            Log.e( "onClick:reject ",data.get(position).IssueID );
                            Date cDate = new Date();
                            String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                            try {
                                Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId ='" + data.get(getLayoutPosition()).IssueID + "'", null);
                                cqueryTemp.moveToFirst();

                                Map<String, Map<String, String>> ticketsMap = MyApp.getApplication().readTicketsIssueHistory();
                                Map<String, String> map = new HashMap<>();
                                map.put("TicketId", data.get(getLayoutPosition()).IssueID);
                                map.put("UserId", sUserId);
                                map.put("StatusId", sAcceptStatus);
                                map.put("ParentCompanyId", sParentComapnyId);
                                map.put("Comment", commemt.getText().toString());
                                map.put("ActivityDate", currentDateTimeString);
                                map.put("SyncStatus", "-1");
                                ticketsMap.put(data.get(getLayoutPosition()).IssueID, map);
                                MyApp.getApplication().writeTicketsIssueHistory(ticketsMap);

//                                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                                        "('" + data.get(position).IssueID + "','" + sUserId + "','" + sAcceptStatus + "','" + commemt.getText().toString() + "','" + currentDateTimeString + "','-1')");
//                                Cursor cque = sql.rawQuery("select * from Issue_History ", null);
//                                String sColumnId = null;
//                                if (cque.getCount() > 0) {
//                                    cque.moveToLast();
//                                    sColumnId = cque.getString(0).toString();
//                                }
                                NewTaskAdapter s = new NewTaskAdapter();


                                final ApiResult apiResult = new ApiResult();
                                final ApiResult.IssueDetail issueDetail = apiResult.new IssueDetail(sUserId, sParentComapnyId, data.get(getLayoutPosition()).IssueID, sAcceptStatus, commemt.getText().toString(), currentDateTimeString, sDepartmentId, "", "", "", sDeviceId, "-1", "", "", "");
                                Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

//                                final String finalColumnId = sColumnId;
//                                final String finalSColumnId = sColumnId;

                                call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                    @Override
                                    public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                        ApiResult.IssueDetail iData = response.body();
                                        try {
                                            if (iData.resData.Status == null || iData.resData.Status.equals("") || iData.resData.Status.equals("0")) {
                                                try {
                                                    MyApp.showMassage(context, context.getString(R.string.internet_error));
//                                                  Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    e.getMessage();
                                                }
//                                                ContentValues newValues = new ContentValues();
//                                                newValues.put("SyncStatus", "false");
//                                                sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);

                                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                                try {
                                                    Map<String, String> map = issueMap.get(data.get(getLayoutPosition()).IssueID);
                                                    map.put("SyncStatus", "false");
                                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                                } catch (Exception e) {
                                                }
                                            } else {
                                                MainActivity m = new MainActivity();
                                                m.updateCounter(context);
//                                                ContentValues newValues = new ContentValues();
//                                                newValues.put("SyncStatus", "true");
//                                                sql.update("Issue_History", newValues, "Id=" + finalSColumnId, null);

                                                Map<String, Map<String, String>> issueMap = MyApp.getApplication().readTicketsIssueHistory();
                                                try {
                                                    Map<String, String> map = issueMap.get(data.get(getLayoutPosition()).IssueID);
                                                    map.put("SyncStatus", "true");
                                                    issueMap.remove(data.get(getLayoutPosition()).IssueID);
                                                    MyApp.getApplication().writeTicketsIssueHistory(issueMap);
                                                } catch (Exception e) {
                                                }

                                                Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + data.get(getLayoutPosition()).IssueID + "'", null);
                                                ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                                if (cqueryTemp.getCount() > 0) {
                                                    cqueryTemp.moveToFirst();
                                                    ref.child(MainActivity.LOGINID).child(data.get(getLayoutPosition()).IssueID).child("Action").setValue("Delete");
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                                        call.cancel();
                                    }
                                });


                                // s.new UpdateTask(context,tktStatus,"1",sColumnId).execute();
                                ff.swipeRight(getLayoutPosition());
                            } catch (Exception e) {
                            }
                            dialog.dismiss();
                        }

                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });


            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        pref = context.getSharedPreferences("login", 0);
        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
        sParentComapnyId = pref.getString("ParentCompanyId", "0");
        sDepartmentId = pref.getString("DepartmentId", "0");
        sUserId = pref.getString("userid", "0");
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sDeviceId = telephonyManager.getDeviceId().toString();
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        String sDateUI;
        sDateUI = data.get(position).getSLADate();
        if (sDateUI.equals("")) {
            sDateUI = "NA";
        } else {
            sDateUI = sDateUI.substring(11, 19);
        }
        Map<String, String> map = MyApp.getApplication().readTicketCaptureSchedule();
        holder.textView2.setText(data.get(position).TicketNumber);
        holder.subject.setText(data.get(position).getSubject());
        holder.adrs.setText(data.get(position).getAdress());
        holder.newtktstatus.setText("New");
        holder.schedule_date.setText(map.get(data.get(position).TicketNumber));
        holder.beontime.setText(sDateUI);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
