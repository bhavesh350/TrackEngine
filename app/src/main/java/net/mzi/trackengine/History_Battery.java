package net.mzi.trackengine;

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
import android.widget.Toast;

import net.mzi.trackengine.adapter.UserLocationBatteryHistoryAdapter;
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
public class History_Battery extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    UserLocationBatteryHistoryAdapter mAdapter;
    public static final int TASK = 0;
    String API_URL = null;
    RecyclerView mRecyclerView;
    List<String> mDeviceId = new ArrayList<String>();
    List<String> mBatterylevel = new ArrayList<String>();
    List<String> mAutocaptured = new ArrayList<String>();
    List<String> mUpdatedDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();

    //ProgressDialog progress;
    public History_Battery() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historybattery, container, false);
        API_URL = PostUrl.sUrl + "GetBetteryLevelHistory?iUserId=" + MainActivity.LOGINID;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_view);
        new UserBattery().execute();
        return view;
    }

    private class UserBattery extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*if(!((Activity) getActivity()).isFinishing()) {
                progress = ProgressDialog.show(getActivity(), "Loading data", "Please wait...");
            } */
            if (getContext() != null) {
                try {
                    Toast.makeText(getContext(), "Loading data!!!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
            mDatasetTypes.clear();
            mDeviceId.clear();
            mUpdatedDate.clear();
            mBatterylevel.clear();
            mAutocaptured.clear();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
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
                try {
                    SOMTracker.showMassage(getActivity(), getActivity().getString(R.string.internet_error));
                } catch (Exception e) {
                }
                return;
            }
            Log.i("INFO", s);

            try {

                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("UserBatteryLevelHistory");
                for (int i = 0; i < jdata.length(); i++) {
                    JSONObject object = jdata.getJSONObject(i);
                    mDeviceId.add(object.getString("DeviceId"));
                    mUpdatedDate.add(object.getString("ActionTime"));
                    mBatterylevel.add(object.getString("BatteryLevel"));
                    mAutocaptured.add(object.getString("AutoCaptured"));
                    mDatasetTypes.add(TASK);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new UserLocationBatteryHistoryAdapter(mDeviceId, mUpdatedDate, mBatterylevel, mAutocaptured, mDatasetTypes, "Battery Level");
            mRecyclerView.setAdapter(mAdapter);
        }
    }
}
