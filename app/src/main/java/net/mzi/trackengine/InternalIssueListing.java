package net.mzi.trackengine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.mzi.trackengine.adapter.ComplainAdapter;
import net.mzi.trackengine.model.InternalIssueClass;
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

public class InternalIssueListing extends AppCompatActivity {
    SharedPreferences pref;
    String sUserId, companyID;
    String API_URL;
    private RecyclerView.LayoutManager mLayoutManager;
    ComplainAdapter oComplainAdapter;
    RecyclerView mRecyclerView;
    List<InternalIssueClass> lComplainData = new ArrayList<InternalIssueClass>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_issue_listing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Tickets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        pref = getSharedPreferences("login", 0);
        sUserId = pref.getString("userid", "userid");//CreatedBy
        companyID = pref.getString("CompanyId", "CompanyId");//locationId
        API_URL = PostUrl.sUrl + "GetComplainDetail?iId=0&iUserId=" + sUserId + "&iCompanyId=" + companyID;
        new FetchIssues().execute();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(InternalIssueListing.this, RaiseTicket.class);
                startActivity(i);
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
    }

    private class FetchIssues extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            /*if (progress!=null) {
                progress.dismiss();
            }*/
            if (s == null) {
                try {
                    Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }

                //s = "THERE WAS AN ERROR";
            } else {
                Log.i("INFO", s);

                try {
                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("lstComplainDetail");
                    for (int i = 0; i < jdata.length(); i++) {
                        InternalIssueClass oInternalIssueClass = new InternalIssueClass();
                        JSONObject object = jdata.getJSONObject(i);
                        oInternalIssueClass.IssueID = object.getString("Id");
                        oInternalIssueClass.IssueText = object.getString("ComplainText");
                        oInternalIssueClass.CreatedDate = object.getString("CreatedOn");
                        oInternalIssueClass.UpdatedDate = object.getString("LastUpdateOn");
                        oInternalIssueClass.StatusName = object.getString("StatusName");
                        lComplainData.add(oInternalIssueClass);
                    }
                    mRecyclerView = findViewById(R.id.internal_issue_task_view);
                    mLayoutManager = new LinearLayoutManager(InternalIssueListing.this, LinearLayoutManager.VERTICAL, false);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    oComplainAdapter = new ComplainAdapter(lComplainData, getApplicationContext());
                    mRecyclerView.setAdapter(oComplainAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
