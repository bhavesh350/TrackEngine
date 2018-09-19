package net.mzi.trackengine;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.gson.Gson;

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
public class SwipeDeckAdapter extends BaseAdapter {
    ApiInterface apiInterface;
    Firebase ref=null;
    SQLiteDatabase sql;
    private List<TicketInfoClass> data;
    private Context context;
    String sParentComapnyId, sDepartmentId;
    SharedPreferences pref;
    String sUserId;
    String sDeviceId;
    Map<String,String> postTktStatus=new HashMap<String, String>();
    public SwipeDeckAdapter(){

    }
    public SwipeDeckAdapter(List<TicketInfoClass> data, Context context) {
        this.context = context;
        sql =context.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        this.data = data;

    }
    @Override
    public int getCount() {
        Log.e( "getCount: data",String.valueOf(data.size() ));
        return data.size();    }
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        //ref = new Firebase(PostUrl.sFirebaseUrlTickets);
        pref = context.getSharedPreferences("login", 0);
        //editor = pref.edit();
        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
        sParentComapnyId=pref.getString("ParentCompanyId","0");
        sDepartmentId=pref.getString("DepartmentId","0");
        sUserId=pref.getString("userid","0");
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            sDeviceId = telephonyManager.getDeviceId().toString();
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if(v == null){
            LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            v = inflater.inflate(R.layout.cardxml, parent, false);
        }
        String sDateUI;
        sDateUI=data.get(position).getSLADate();
        if(sDateUI.equals("")){
            sDateUI="NA";
        }
        else {
            sDateUI=sDateUI.substring(11,19);
        }
        ((TextView) v.findViewById(R.id.textView2)).setText(data.get(position).TicketNumber);
        ((TextView) v.findViewById(R.id.subject)).setText(data.get(position).getSubject());
        ((TextView) v.findViewById(R.id.adrs)).setText(data.get(position).getAdress());
        ((TextView) v.findViewById(R.id.newtktstatus)).setText("New");
        //((TextView) v.findViewById(R.id.newtktstatus)).setText(String.valueOf(data.get(position).getStatusId()));
        ((TextView) v.findViewById(R.id.beontime)).setText(sDateUI);

        ((FloatingActionButton) v.findViewById(R.id.reject)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchedulingAdapter.isAttended="0";
                final AlertDialog.Builder Dialog = new AlertDialog.Builder(context);
                Dialog.setTitle("Rejection Reason: ");
                Dialog.setCancelable(false);
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = li.inflate(R.layout.rejection, null);
                final EditText commemt =(EditText) dialogView.findViewById(R.id.rejectionReason);
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String sAcceptStatus = null;
                        Boolean wantToCloseDialog=true;
                        if (commemt.getText().toString().length()==0) {
                            commemt.setError("Please enter comment");
                            wantToCloseDialog= false;
                            //Dialog.show();
                        }

                        if(wantToCloseDialog){

                            Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 0 and DepartmentId = '"+sDepartmentId+"'", null);
                            if(cquery.getCount()>0) {
                                cquery.moveToFirst();
                                sAcceptStatus=cquery.getString(0).toString();
                            }
                            else
                                sAcceptStatus="0";
                            Firstfrag f=new Firstfrag();
//                            Log.e( "onClick:reject ",data.get(position).IssueID );
                            Date cDate = new Date();
                            String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);
                         /*   postTktStatus.put("UserId",sUserId);
                            postTktStatus.put("TicketId",data.get(position).IssueID);
                            postTktStatus.put("ParentCompanyId",sParentComapnyId );
                            postTktStatus.put("DepartmentId",sDepartmentId );
                            postTktStatus.put("StatusId",sAcceptStatus);//cquery.getString(0).toString();
                            postTktStatus.put("Comment",commemt.getText().toString());
                            postTktStatus.put("ActivityDate",currentDateTimeString);
                            postTktStatus.put("RealtimeUpdate","true");
                            postTktStatus.put("Latitude", " ");
                            postTktStatus.put("Latitude", " ");
                            postTktStatus.put("AssetVerificationText","-");

                            String tktStatus = new Gson().toJson(postTktStatus);*/
                            Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId ='" + data.get(position).IssueID+ "'", null);
                            cqueryTemp.moveToFirst();

                            sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
                                    "('" + data.get(position).IssueID + "','" + sUserId+ "','" + sAcceptStatus+ "','" + commemt.getText().toString() + "','" + currentDateTimeString+ "','-1')");
                            Cursor cque = sql.rawQuery("select * from Issue_History ", null);
                            String sColumnId = null;
                            if(cque.getCount()>0){
                                cque.moveToLast();
                                sColumnId=cque.getString(0).toString();
                            }
                            NewTaskAdapter s=new NewTaskAdapter();


                            final ApiResult apiResult = new ApiResult();
                            final ApiResult.IssueDetail issueDetail=apiResult.new IssueDetail(sUserId,sParentComapnyId,data.get(position).IssueID, sAcceptStatus ,commemt.getText().toString(),currentDateTimeString,sDepartmentId,"","","",sDeviceId,"-1","","","");
                            Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                            final String finalColumnId = sColumnId;

                            final String finalSColumnId = sColumnId;
                            call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                                @Override
                                public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                                    ApiResult.IssueDetail  iData = response.body();
                                    if(iData.resData.Status==null||iData.resData.Status.equals("")||iData.resData.Status.equals("0")){
                                        try {
                                            SOMTracker.showMassage(context, context.getString(R.string.internet_error));
//                                            Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                                        }catch (Exception e){
                                            e.getMessage();
                                        }
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("SyncStatus", "false");
                                        sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                                    }
                                    else {
                                        MainActivity m=new MainActivity();
                                        m.updateCounter(context);
                                        ContentValues newValues = new ContentValues();
                                        newValues.put("SyncStatus", "true");
                                        sql.update("Issue_History", newValues, "Id=" + finalSColumnId, null);
                                        Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" +data.get(position).IssueID + "'", null);
                                        ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                        if (cqueryTemp.getCount() > 0) {
                                            cqueryTemp.moveToFirst();
                                            ref.child(MainActivity.LOGINID).child(data.get(position).IssueID).child("Action").setValue("Delete");
                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                                    call.cancel();
                                }
                            });


                            // s.new UpdateTask(context,tktStatus,"1",sColumnId).execute();
                            f.swipeRight(data.get(position).IssueID,context);
                           /* Cursor cqueryTmp = sql.rawQuery("select * from Issue_Detail where IssueId='" + data.get(position).IssueID+ "'", null);
                            if(cqueryTmp.getCount()>0) {
                                sql.delete("Issue_Detail", "IssueId" + "=" + data.get(position).IssueID, null);
                                sql.delete("FirebaseIssueData", "IssueId" + "=" + data.get(position).IssueID, null);
                            }

                            ref.child(sUserId).child(data.get(position).IssueID).removeValue();*/
                            //removeNewCard(position);

                            dialog.dismiss();
                        }

                        //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                    }
                });


            }
        });
        ((FloatingActionButton) v.findViewById(R.id.accept)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchedulingAdapter.isAttended="0";
                String sAcceptStatus = null;
                Log.e( "onClick:accept ",data.get(position).IssueID );
                Firstfrag f=new Firstfrag();
                Cursor cquery = sql.rawQuery("select StatusId from Issue_Status where IsMobileStatus = 1 and DepartmentId = '"+sDepartmentId+"' ", null);
                if(cquery.getCount()>0) {
                    cquery.moveToFirst();
                    sAcceptStatus=cquery.getString(0).toString();
                }
                else
                    sAcceptStatus="0";
                cquery.close();
                Date cDate = new Date();
                String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate);

 /*               postTktStatus.put("UserId",sUserId);
                postTktStatus.put("TicketId",data.get(position).IssueID);
                postTktStatus.put("ParentCompanyId",sParentComapnyId );
                postTktStatus.put("DepartmentId",sDepartmentId );
                postTktStatus.put("StatusId",sAcceptStatus);//cquery.getString(0).toString();
                postTktStatus.put("Comment","Accepted by Engineer");
                postTktStatus.put("ActivityDate",currentDateTimeString);
                postTktStatus.put("RealtimeUpdate","true");
                postTktStatus.put("Latitude", " ");
                postTktStatus.put("Latitude", " ");
                postTktStatus.put("AssetVerificationText","-");
                String tktStatus = new Gson().toJson(postTktStatus);*/
                NewTaskAdapter s=new NewTaskAdapter();
                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
                        "('" + data.get(position).IssueID + "','" + sUserId+ "','" + sAcceptStatus+ "','Accepted By Engineer','" + currentDateTimeString+ "','-1')");
                Cursor cque = sql.rawQuery("select * from Issue_History ", null);
                String sColumnId = null;
                if(cque.getCount()>0){
                    cque.moveToLast();
                    sColumnId=cque.getString(0).toString();
                }
                cque.close();
                Cursor cqueryTemp = sql.rawQuery("select StatusId from Issue_Detail where IssueId ='" + data.get(position).IssueID+ "'", null);
                if(cqueryTemp.getCount()>0) {
                    cqueryTemp.moveToFirst();
                    //s.new UpdateTask(context, tktStatus, "0", sColumnId).execute();
                    ContentValues newValues = new ContentValues();
                    newValues.put("PreviousStatus", cqueryTemp.getString(0).toString());
                    newValues.put("IsAccepted", "1");
                    newValues.put("StatusId", sAcceptStatus);
                    newValues.put("UpdatedDate", currentDateTimeString);

                    sql.update("Issue_Detail", newValues, "IssueId=" + data.get(position).IssueID, null);

                    final ApiResult apiResult = new ApiResult();
                    final ApiResult.IssueDetail issueDetail=apiResult.new IssueDetail(sUserId,sParentComapnyId,data.get(position).IssueID, sAcceptStatus ,"Accept by Engineer",currentDateTimeString,sDepartmentId,"","","",sDeviceId,"true","","","");
                    Call<ApiResult.IssueDetail> call1 = apiInterface.PostTicketStatus(issueDetail);

                    final String finalColumnId = sColumnId;

                    call1.enqueue(new Callback<ApiResult.IssueDetail>() {
                        @Override
                        public void onResponse(Call<ApiResult.IssueDetail> call, Response<ApiResult.IssueDetail> response) {
                            ApiResult.IssueDetail  iData = response.body();
                            if(iData.resData.Status==null||iData.resData.Status.equals("")||iData.resData.Status.equals("0")){
                                try {
                                    Toast.makeText(context, R.string.internet_error, Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    e.getMessage();
                                }
                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "false");
                                sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                            }
                            else {
                                ContentValues newValues = new ContentValues();
                                newValues.put("SyncStatus", "true");
                                sql.update("Issue_History", newValues, "Id=" + finalColumnId, null);
                               try{
                                   Cursor cqueryTemp = sql.rawQuery("select * from FirebaseIssueData where IssueId = '" + data.get(position).IssueID + "'", null);
                                   ref = new Firebase(PostUrl.sFirebaseUrlTickets);
                                   if (cqueryTemp.getCount() > 0) {
                                       cqueryTemp.moveToFirst();
                                       ref.child(MainActivity.LOGINID).child(data.get(position).IssueID).child("Action").setValue("Update");

                                   }
                               }catch (Exception e){}
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResult.IssueDetail> call, Throwable t) {
                            call.cancel();
                        }
                    });

                }
                f.swipeLeft(data.get(position).IssueID,context);
                cqueryTemp.close();
            }
        });

        return v;
    }
    private void removeNewCard(int position) {
        data.remove(position);
        notifyDataSetChanged();

    }

}
