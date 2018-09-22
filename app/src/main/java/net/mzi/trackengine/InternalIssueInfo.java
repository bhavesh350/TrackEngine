package net.mzi.trackengine;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import net.mzi.trackengine.model.PostUrl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InternalIssueInfo extends AppCompatActivity {
    static String API_URL = null;
    private RecyclerView.LayoutManager mLayoutManager;
    FollowUpAdapter followUpAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    List<String> mStatus=new ArrayList<String>();
    List<String> mComment=new ArrayList<String>();
    List<String> mAssignedTo=new ArrayList<String>();
    List<String> mCreadteDate=new ArrayList<String>();
    List<Integer> mDatasetTypes=new ArrayList<Integer>();
    List<String> mIssueId=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_issue_info);
        Bundle bundle = getIntent().getExtras();
        String id=bundle.getString("ID");
        getSupportActionBar().setTitle("Follow up History: "+id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.folllowUpviewInternal);
        //API_URL = PostUrl.sUrl+"GetIssueHistory?IssueId="+id;
        API_URL = PostUrl.sUrl+"GetComplainHistory?iComplainId="+id+"&iInternalOnly=0";
        new InternalIssueFollowUp().execute();

    }
    private class InternalIssueFollowUp extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatasetTypes.clear();
            mStatus.clear();
            mCreadteDate.clear();
            mComment.clear();
            mAssignedTo.clear();
            mIssueId.clear();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                try{Toast.makeText(getApplicationContext(),R.string.internet_error,Toast.LENGTH_LONG).show();}catch (Exception e){}
            }
            Log.i("INFO", s);
            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("lstComplainHistoryDetail");
                for(int i=0;i<jdata.length();i++)
                {
                    JSONObject object=jdata.getJSONObject(i);
                    mAssignedTo.add(object.getString("ActionBy"));
                    mComment.add(object.getString("Comment"));
                    mCreadteDate.add(object.getString("CreatedOn"));
                    mStatus.add(object.getString("StatusName"));
                    mIssueId.add(object.getString("Id"));
                    mDatasetTypes.add(TASK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mLayoutManager = new LinearLayoutManager(InternalIssueInfo.this,LinearLayoutManager.VERTICAL,false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            followUpAdapter=new FollowUpAdapter(InternalIssueInfo.this,mAssignedTo,mComment,mCreadteDate, mStatus,mDatasetTypes,mIssueId);
            mRecyclerView.setAdapter(followUpAdapter);
        }
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
