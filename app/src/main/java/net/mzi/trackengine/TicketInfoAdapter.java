package net.mzi.trackengine;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;


import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Poonam on 2/1/2017.
 */

public class TicketInfoAdapter extends RecyclerView.Adapter<TicketInfoAdapter.ViewHolder> {
    private static final String TEMP_IMAGE_NAME = "tempImage";
    Map<String, String> postTktStatus = new HashMap<String, String>();
    private int mDatasetTypes;
    private int mCardColor;
    String mcardType;
    Cursor cquery;
    Context ctx;
    SQLiteDatabase sql;
    String ID, cname, mNumber, tIssue, cAdrs, tStatus, expectedTime, createdDate, updatedDate, cAssetName, cAssetType, cAssetSubtype, cAssetSerialNumber, corporateName;

    long totalSize = 0;
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    public static final int MEDIA_TYPE_IMAGE = 1;

    private Uri fileUri; // file url to store image/video
    View nhn = null;

    public TicketInfoAdapter(String ID, String cname, String mNumber, String tIssue, String cAdrs, String tStatus, String expectedTime, String createdDate, String updatedDate, String cAssetName, String cAssetType, String cAssetSubtype, String cAssetSerialNumber, String corporateName, int mDatasetTypes, int mCardColor, Context ctx) {
        this.ID = ID;
        this.cname = cname;
        this.mNumber = mNumber;
        this.tIssue = tIssue;
        this.cAdrs = cAdrs;
        this.tStatus = tStatus;
        this.expectedTime = expectedTime;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.cAssetName = cAssetName;
        this.cAssetType = cAssetType;
        this.cAssetSubtype = cAssetSubtype;
        this.cAssetSerialNumber = cAssetSerialNumber;
        this.corporateName = corporateName;
        this.mDatasetTypes = mDatasetTypes;
        this.mCardColor = mCardColor;
        this.ctx = ctx;
    }

    private class TktInfoViewHolder extends TicketInfoAdapter.ViewHolder {
        TextView tcname, tmNumber, ttIssue, tcAdrs, ttStatus, texpectedTime, tcreatedDate, tupdatedDate, tcAssetName, tcAssetType, tcAssetSubtype, tcAssetSerialNumber, tcorporateName;
        RadioButton vCamera, vGallery;
        EditText comment;
        Button Submit;

        public TktInfoViewHolder(View itemView) {
            super(itemView);
            this.tcname = itemView.findViewById(R.id.contactpersonname);
            this.tcAdrs = itemView.findViewById(R.id.adrs);
            this.tmNumber = itemView.findViewById(R.id.cntctprsnmob);
            this.texpectedTime = itemView.findViewById(R.id.beontime);
            this.ttIssue = itemView.findViewById(R.id.subject);
            this.ttStatus = itemView.findViewById(R.id.status);
            this.tcreatedDate = itemView.findViewById(R.id.createddate);
            this.tupdatedDate = itemView.findViewById(R.id.updateddate);
            this.tcAssetName = itemView.findViewById(R.id.assetName);
            this.tcAssetType = itemView.findViewById(R.id.assetType);
            this.tcAssetSubtype = itemView.findViewById(R.id.assetSubType);
            this.tcAssetSerialNumber = itemView.findViewById(R.id.assetserialNumber);
            this.tcorporateName = itemView.findViewById(R.id.corporateName);
            this.vCamera = itemView.findViewById(R.id.vCam);
            this.vGallery = itemView.findViewById(R.id.vGal);
            //this.upload=(ImageView)itemView.findViewById(R.id.uploadImage);
            this.comment = itemView.findViewById(R.id.agentComment);
            this.Submit = itemView.findViewById(R.id.submit);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        sql = ctx.openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        //ctx=parent.getContext();
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tktinfocrd, parent, false);
        nhn = v;
        return new TktInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final TktInfoViewHolder tktInfoViewHolder = (TktInfoViewHolder) holder;
        tktInfoViewHolder.tcname.setText(cname);
        tktInfoViewHolder.tmNumber.setText(mNumber);
        tktInfoViewHolder.ttIssue.setText(tIssue);
        tktInfoViewHolder.tcAdrs.setText(cAdrs);
        tktInfoViewHolder.ttStatus.setText(tStatus);
        tktInfoViewHolder.texpectedTime.setText(expectedTime);
        tktInfoViewHolder.tcreatedDate.setText(createdDate);
        tktInfoViewHolder.tupdatedDate.setText(updatedDate);
        tktInfoViewHolder.tcAssetName.setText(cAssetName);
        tktInfoViewHolder.tcAssetType.setText(cAssetType);
        tktInfoViewHolder.tcAssetSubtype.setText(cAssetSubtype);
        tktInfoViewHolder.tcAssetSerialNumber.setText(cAssetSerialNumber);
        tktInfoViewHolder.tcorporateName.setText(corporateName);
        tktInfoViewHolder.vCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tktInfoViewHolder.vGallery.setChecked(false);
                    //captureImage();
                }
            }
        });
        tktInfoViewHolder.vGallery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tktInfoViewHolder.vCamera.setChecked(false);
                }

            }
        });
        tktInfoViewHolder.Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchedulingAdapter s = new SchedulingAdapter();
                s.getLocation();
                String currentTime;
                Date cDate = new Date();
                currentTime = new SimpleDateFormat("MMM-dd-yyyy HH:mm:ss").format(cDate);
                ContentValues newValues = new ContentValues();
                newValues.put("UpdatedDate", currentTime);
                sql.update("Issue_Detail", newValues, "IssueId=" + ID, null);
                postTktStatus.put("UserId", MainActivity.LOGINID);
                postTktStatus.put("TicketId", ID);
                postTktStatus.put("StatusId", tStatus);
                postTktStatus.put("Comment", tktInfoViewHolder.comment.getText().toString());
                postTktStatus.put("ActivityDate", currentTime);
                postTktStatus.put("RealtimeUpdate", "true");
                postTktStatus.put("Latitude", String.valueOf(SchedulingAdapter.latitude));
                postTktStatus.put("Longitude", String.valueOf(SchedulingAdapter.longitude));
                postTktStatus.put("AssetVerificationText", "-");
                postTktStatus.put("ModeOfTransport", "0");
                postTktStatus.put("Expense", "0");
                postTktStatus.put("AssignedUserId", "0");
                postTktStatus.put("SyncStatus", "-1");
                postTktStatus.put("StartingForSite", "");
                postTktStatus.put("CustomDestination", "");
                Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();
                issuesMap.put(ID, postTktStatus);
                MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
                String tktStatus = new Gson().toJson(postTktStatus);
                Log.d("onClick: ", tktStatus);
//                sql.execSQL("INSERT INTO Issue_History(IssueId,UserId,IssueStatus,Comment,CreatedDate,SyncStatus)VALUES" +
//                        "('" + postTktStatus.get("TicketId") + "','" + postTktStatus.get("UserId") + "','" + postTktStatus.get("StatusId") + "','" + postTktStatus.get("Comment") + "','" + postTktStatus.get("ActivityDate") + "','-1')");
//                Cursor cquery = sql.rawQuery("select * from Issue_History ", null);
//                String sColumnId = null;
//                if (cquery.getCount() > 0) {
//                    cquery.moveToLast();
//                    sColumnId = cquery.getString(0).toString();
//                }
                s.UpdateTask(ctx, postTktStatus, "true");
                tktInfoViewHolder.comment.setText("");
                Snackbar.make(nhn, "Data has been sent successfully!!!", Snackbar.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
