package net.mzi.trackengine;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.model.TicketInfoClass;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowUpHistory extends AppCompatActivity {
    static String API_URL = null;
    private RecyclerView.LayoutManager mLayoutManager;
    FollowUpAdapter followUpAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    String id, TNumber;
    List<String> mStatus = new ArrayList<String>();
    List<String> mComment = new ArrayList<String>();
    List<String> mAssignedTo = new ArrayList<String>();
    List<String> attachments = new ArrayList<String>();
    List<String> mCreadteDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> mIssueId = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_up_history);
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("ID");
        TNumber = bundle.getString("TNumber");
        getSupportActionBar().setTitle("Follow up History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mRecyclerView = findViewById(R.id.folllowUpview);
        Map<String, TicketInfoClass> issueDetailsMap = MyApp.getApplication().readIssueDetailsHistory();
        API_URL = PostUrl.sUrl + "GetIssueHistory?IssueId=" + id;
        if (issueDetailsMap.containsKey(id)) {
            if (issueDetailsMap.get(id).getType().equals("Ticket")) {
                API_URL = PostUrl.sUrl + "GetIssueHistory?IssueId=" + id;
            } else {
                API_URL = PostUrl.sUrl + "GetTaskHistory?TaskId=" + id;
            }
        }
        new FollowUp().execute();
    }

    private class FollowUp extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDatasetTypes.clear();
            mStatus.clear();
            mCreadteDate.clear();
            mComment.clear();
            attachments.clear();
            mAssignedTo.clear();
            mIssueId.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);

            try {
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("IssueHistories");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    mAssignedTo.add(object.getString("ActionedBy"));
                    mComment.add(object.getString("Comment"));
                    attachments.add(object.getString("AttachmentUrl"));
                    mCreadteDate.add(object.getString("CreatedDate"));
                    mStatus.add(object.getString("IssueStatus"));
                    mIssueId.add(TNumber);
                    mDatasetTypes.add(TASK);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            mLayoutManager = new LinearLayoutManager(FollowUpHistory.this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            followUpAdapter = new FollowUpAdapter(FollowUpHistory.this, mAssignedTo, mComment, mCreadteDate, mStatus, mDatasetTypes, mIssueId, attachments);
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
