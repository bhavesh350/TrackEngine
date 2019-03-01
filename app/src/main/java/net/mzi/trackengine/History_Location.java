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
import android.widget.RelativeLayout;

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
public class History_Location extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    UserLocationBatteryHistoryAdapter mAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    String API_URL = null;
    List<String> mDeviceId = new ArrayList<String>();
    List<String> mAddress = new ArrayList<String>();
    List<String> mAutocaptured = new ArrayList<String>();
    List<String> mUpdatedDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    ProgressDialog progress;
    private RelativeLayout rl_progress;

    public History_Location() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historylocation, container, false);
        rl_progress = view.findViewById(R.id.rl_progress);
        API_URL = PostUrl.sUrl + "GetLocationHistory?iUserId=" + MainActivity.LOGINID;
        mRecyclerView = view.findViewById(R.id.history_view);
        rl_progress.setVisibility(View.VISIBLE);
        new History_Location.UserLocation().execute();
        return view;
    }

    private class UserLocation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (getContext() != null) {
//                MyApp.showMassage(getContext(),"Loading data!!!");
//                Toast.makeText(getContext(), "Loading data!!!", Toast.LENGTH_LONG).show();
            }
            mDatasetTypes.clear();
            mDeviceId.clear();
            mUpdatedDate.clear();
            mAddress.clear();
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
            rl_progress.setVisibility(View.GONE);
            /*if (progress!=null) {
                progress.dismiss();
            }*/
            if (s == null) {
                try{
                    MyApp.showMassage(getActivity(), getString(R.string.internet_error));
                }catch (Exception e){}

            } else {
                Log.i("INFO", s);

                try {

                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("UserLocationHistories");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        mDeviceId.add(object.getString("DeviceId"));
                        mUpdatedDate.add(object.getString("ActionTime"));
                        mAddress.add(object.getString("Address"));
                        mAutocaptured.add(object.getString("AutoCaptured"));
                        mDatasetTypes.add(TASK);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new UserLocationBatteryHistoryAdapter(mDeviceId, mUpdatedDate, mAddress, mAutocaptured, mDatasetTypes, "Location");
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
