package net.mzi.trackengine;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * Created by Poonam on 2/14/2017.
 */
public class History_User extends Fragment {
    String API_URL = null;
    private RecyclerView.LayoutManager mLayoutManager;
    FollowUpAdapter followUpAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    List<String> mStatus = new ArrayList<String>();
    List<String> mComment = new ArrayList<String>();
    List<String> mAssignedTo = new ArrayList<String>();
    List<String> mCreadteDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> mIssueId = new ArrayList<String>();
    private ProgressDialog progress;

    public History_User() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historyuser, container, false);
        API_URL = PostUrl.sUrl + "GetWorkedUpOnIssue?iUserId=" + MainActivity.LOGINID + "&dtFromDate=1900-01-01&dtToDate=2200-01-01";
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_view);
        new UserFollowUp().execute();
        return view;
    }

    private class UserFollowUp extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getContext() != null) {
                MyApp.showMassage(getContext(), "Loading data!!!");
//                Toast.makeText(getContext(), "Loading data!!!", Toast.LENGTH_LONG).show();
            }
           /* if(!((Activity) getActivity()).isFinishing()) {
                progress = ProgressDialog.show(getActivity(), "Loading data", "Please wait...");
            }*/
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
            if (progress != null) {
                progress.dismiss();
            }
            if (s == null) {
                try {
                    MyApp.showMassage(getContext(), getContext().getString(R.string.internet_error));
                } catch (Exception e) {
                }
//                Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                return;
            }
            Log.i("INFO", s);

            try {

                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("IssueHistories");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    mAssignedTo.add(object.getString("ActionedBy"));
                    mComment.add(object.getString("Comment"));
                    mCreadteDate.add(object.getString("CreatedDate"));
                    mStatus.add(object.getString("IssueStatus"));
                    mIssueId.add(object.getString("IssueId"));
                    mDatasetTypes.add(TASK);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            followUpAdapter = new FollowUpAdapter(getActivity(), mAssignedTo, mComment, mCreadteDate, mStatus, mDatasetTypes, mIssueId,new ArrayList<String>());
            mRecyclerView.setAdapter(followUpAdapter);
        }
    }
}
