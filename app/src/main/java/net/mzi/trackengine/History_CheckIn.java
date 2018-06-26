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

import net.mzi.trackengine.adapter.CheckInHistoryAdapter;
import net.mzi.trackengine.model.PostUrl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Poonam on 8/23/2017.
 */
public class History_CheckIn extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    CheckInHistoryAdapter mAdapter;
    public static final int TASK = 0;
    RecyclerView mRecyclerView;
    String API_URL = null;
    List<String> lCheckInTime=new ArrayList<String>();
    List<String> lCheckOutTime=new ArrayList<String>();
    List<String> lCheckInLocation=new ArrayList<String>();
    List<String> lCheckOutLocation=new ArrayList<String>();
    List<String> lDuration=new ArrayList<String>();
    List<Integer> mDatasetTypes=new ArrayList<Integer>();
    //ProgressDialog progress;
    public History_CheckIn(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.historycheckin, container, false);
        Date cDate = new Date();
        String currentDateTimeString = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

        //API_URL = "http://192.168.1.20/TrackEngine/api/Post/GetCheckInCheckoutReport?iUserId="+MainActivity.LOGINID+"&dtFromDate="+"2017-04-13"+"&dtToDate="+"2017-04-13";
        API_URL = PostUrl.sUrl+"GetCheckInCheckoutReport?iUserId="+MainActivity.LOGINID+"&dtFromDate="+currentDateTimeString+"&dtToDate="+currentDateTimeString;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.history_view);
        Log.e( "onCreateView: ",API_URL);

        new History_CheckIn.CheckInInfo().execute();
        return view;
    }
    private class CheckInInfo extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(getContext()!=null){
                Toast.makeText(getContext(),"Loading data!!!",Toast.LENGTH_LONG).show();
            }
            /*if(!((Activity) getActivity()).isFinishing()) {
                progress = ProgressDialog.show(getActivity(), "Loading data", "Please wait...");
            }*/
            mDatasetTypes.clear();
            lCheckInLocation.clear();
            lCheckInTime.clear();
            lCheckOutLocation.clear();
            lCheckOutTime.clear();
            lDuration.clear();
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
                Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
            } else {
                Log.i("INFO", s);
                try {

                    JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                    JSONArray jdata = jsonObject.getJSONArray("UserCheckInCheckOutDetails");
                    for (int i = 0; i < jdata.length(); i++) {
                        JSONObject object = jdata.getJSONObject(i);
                        lCheckInTime.add(object.getString("CheckInTime"));
                        lCheckOutTime.add(object.getString("CheckOutTime"));
                        lCheckInLocation.add(object.getString("CheckInLocation"));
                        lCheckOutLocation.add(object.getString("CheckOutLocation"));
                        lDuration.add(object.getString("Duration"));
                        mDatasetTypes.add(TASK);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mAdapter = new CheckInHistoryAdapter(lCheckInTime, lCheckOutTime, lCheckInLocation, lCheckOutLocation,lDuration, mDatasetTypes);
                mRecyclerView.setAdapter(mAdapter);
            }
        }
    }
}
