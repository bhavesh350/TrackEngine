package net.mzi.trackengine;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.mzi.trackengine.adapter.CheckInSynAdpater;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Poonam on 1/19/2018.
 */
public class Offline_CheckIn extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    CheckInSynAdpater mAdapter;
    RecyclerView mRecyclerView;
    Button bClearData, bForceSync;
//    SQLiteDatabase sql = null;
    SharedPreferences pref;
    List<String> lCheckInTime = new ArrayList<String>();
    List<String> lCheckInStatus = new ArrayList<String>();
    List<Integer> lColors = new ArrayList<Integer>();
    private String sUserId;

    public void Offline_CheckIn() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offline_sync, container, false);
        pref = getContext().getSharedPreferences("login", 0);
        sUserId = pref.getString("userid", "userid");
        mRecyclerView = view.findViewById(R.id.sync_view);
        bClearData = view.findViewById(R.id.idClearData);
        bForceSync = view.findViewById(R.id.IdForcedSync);
        bClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeData();
            }
        });

        bForceSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternetConnector icDataSyncing = new InternetConnector();
                icDataSyncing.offlineSyncing(getContext().getApplicationContext(), 1);
                getData();
            }
        });
//        sql = getContext().openOrCreateDatabase("MZI.sqlite", getContext().MODE_PRIVATE, null);
        getData();

        return view;
    }

    public void getData() {
        lColors.clear();
        lCheckInStatus.clear();
        lCheckInTime.clear();

        final Map<String, Map<String, String>> mapData = MyApp.getApplication().readCheckInOutData();
//        final Cursor cquery = sql.rawQuery("select * from User_AppCheckIn'" + sUserId + "'", null);
        if (mapData.keySet().size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(String key : mapData.keySet()){
                        if(mapData.get(key).get("IsCheckedIn").equals("false")){
                            lColors.add(R.drawable.cardbk_red);
                            lCheckInStatus.add("Checked Out");
                        }else{
                            lColors.add(R.drawable.cardbk_green);
                            lCheckInStatus.add("Checked In");
                        }
                        lCheckInTime.add(mapData.get(key).get("ActivityDate"));
                    }
//                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
//                        if (cquery.getString(4).toString().equals("true"))
//                            lColors.add(R.drawable.cardbk_green);
//                        else
//                            lColors.add(R.drawable.cardbk_red);
//
//                        if (cquery.getString(2).toString().equals("true"))
//                            lCheckInStatus.add("Checked In");
//                        else
//                            lCheckInStatus.add("Checked Out");
//                        lCheckInTime.add(cquery.getString(3).toString());
//                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mAdapter = new CheckInSynAdpater(lCheckInTime, lCheckInStatus, lColors);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            cquery.close();
//                        }
//                    });
                }


            }).start();

        } else {
            MyApp.showMassage(getContext(), "Data is already synced!!!");
        }

    }

    public void removeData() {
        MyApp.getApplication().writeCheckInOutData(new HashMap<String, Map<String, String>>());
//        sql.delete("User_AppCheckIn", null, null);
        lCheckInTime.clear();
        lCheckInStatus.clear();
        mAdapter = new CheckInSynAdpater(lCheckInTime, lCheckInStatus, lColors);
        mRecyclerView.setAdapter(mAdapter);

    }
}
