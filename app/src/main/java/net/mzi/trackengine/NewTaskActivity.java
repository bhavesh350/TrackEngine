package net.mzi.trackengine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity {
    private RecyclerView.LayoutManager mLayoutManager;
    NewTaskAdapter newtaskAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    List<String> mStatus = new ArrayList<String>();
    List<String> mTime = new ArrayList<String>();
    List<String> mSub = new ArrayList<String>();
    List<String> mLoc = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> mIssueID = new ArrayList<String>();
    List<String> mTicketNumber = new ArrayList<String>();
    //List<StatusInfoClass> statusList=new ArrayList<StatusInfoClass>();
    SQLiteDatabase sql;
    Cursor cquery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        getSupportActionBar().setTitle("New Tickets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mStatus.add("New");
        mIssueID.clear();
        mTime.clear();
        mSub.clear();
        mLoc.clear();
        mTicketNumber.clear();
        mDatasetTypes.clear();
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = -1", null);
        for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
            mStatus.add("New");
            mIssueID.add(cquery.getString(1).toString());
            mTime.add(cquery.getString(8).toString());
            mSub.add(cquery.getString(3).toString());
            mLoc.add(cquery.getString(10).toString());
            mTicketNumber.add(cquery.getString(20).toString());
            mDatasetTypes.add(TASK);

        }
        mRecyclerView = (RecyclerView) findViewById(R.id.new_task_view);
        mLayoutManager = new LinearLayoutManager(NewTaskActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        newtaskAdapter = new NewTaskAdapter(mIssueID, mTime, mLoc, mSub, mStatus, mDatasetTypes, mTicketNumber, NewTaskActivity.this);
        mRecyclerView.setAdapter(newtaskAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
