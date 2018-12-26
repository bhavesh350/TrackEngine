package net.mzi.trackengine;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import net.mzi.trackengine.adapter.TicketSynAdpater;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Poonam on 1/19/2018.
 */
public class Offline_Tickets extends Fragment {
    private RecyclerView.LayoutManager mLayoutManager;
    TicketSynAdpater mAdapter;
    RecyclerView mRecyclerView;
    Button bClearData, bForceSync;
    SQLiteDatabase sql = null;
    SharedPreferences pref;
    List<String> lTicketNumber = new ArrayList<String>();
    List<String> lTicketStatus = new ArrayList<String>();
    List<String> lTicketComment = new ArrayList<String>();
    List<String> lTicketTime = new ArrayList<String>();
    List<Integer> lColors = new ArrayList<Integer>();
    private String sUserId;

    public void Offline_Tickets() {

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
        sql = getContext().openOrCreateDatabase("MZI.sqlite", getContext().MODE_PRIVATE, null);
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
                if (MyApp.isConnectingToInternet(getActivity())) {
//                    lTicketNumber.clear();
//                    lTicketStatus.clear();
//                    lTicketTime.clear();
//                    lTicketComment.clear();
//                    lColors.clear();
                    MyApp.showMassage(getActivity(), "Syncing...");
                    getActivity().finish();
//                    mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//                    mRecyclerView.setLayoutManager(mLayoutManager);
//                    mAdapter = new TicketSynAdpater(lTicketNumber, lTicketStatus, lTicketTime, lTicketComment, lColors);
//                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    MyApp.popMessage("Alert!", "Please connect to a working internet connection.", getActivity());
                }
            }
        });
        getData();
        return view;
    }

    public void getData() {

        lTicketNumber.clear();
        lTicketComment.clear();
        lTicketStatus.clear();
        lTicketTime.clear();
        lColors.clear();
        final Map<String, Map<String, String>> savedMap = MyApp.getApplication().readTicketsIssueHistory();
//        final Cursor cquery = sql.rawQuery("select * from Issue_History where UserId='" + sUserId + "'", null);
        if (savedMap.keySet().size() > 0) {

                ArrayList<String> keys = new ArrayList<>(savedMap.keySet());
                List<String> arrayList = new ArrayList<>();
                for (int i = 0; i < keys.size(); i++) {
                    arrayList.add(savedMap.get(keys.get(i)).get("ActivityDate") + "@@@" + keys.get(i));
                }

                class StringDateComparator implements Comparator<String> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    public int compare(String lhs, String rhs) {
                        try {
                            return dateFormat.parse(rhs.split("@@@")[0]).compareTo(dateFormat.parse(lhs.split("@@@")[0]));
                        } catch (ParseException e) {
                            return 0;
                        }
                    }
                }

                Collections.sort(arrayList, new StringDateComparator());

                for (String key : arrayList) {
                    key = key.split("@@@")[1];
                    try {
                        Cursor cqueryTemp = sql.rawQuery("select TicketNumber from Issue_Detail where issueid='" + savedMap.get(key.contains("@@") ? key.split("@@")[0] : key).get("TicketId") + "'", null);
                        if (savedMap.get(key).get("SyncStatus").equals("true")) {
                            lColors.add(R.drawable.cardbk_green);
                        } else
                            lColors.add(R.drawable.cardbk_red);
                        if (cqueryTemp.getCount() > 0) {
                            cqueryTemp.moveToFirst();
                            lTicketNumber.add(cqueryTemp.getString(0));
                        } else {
                            try {
                                lTicketNumber.add(savedMap.get(key).get("ticketNumber"));
                            } catch (Exception e) {
                                try {
                                    lTicketNumber.add(savedMap.get(key).get("TicketId"));
                                } catch (Exception ee) {
                                    lTicketNumber.add("NA");
                                }
                            }
                        }
                        cqueryTemp = sql.rawQuery("select StatusName from Issue_Status where StatusId=" + savedMap.get(key.contains("@@") ? key.split("@@")[0] : key).get("StatusId"), null);
                        cqueryTemp.moveToFirst();
                        lTicketComment.add(savedMap.get(key).get("Comment"));
                        try {

                            if (savedMap.get(key).get("StatusId").equals("2"))
                                lTicketStatus.add("Start");
                            else if (savedMap.get(key).get("StatusId").equals("3"))
                                lTicketStatus.add("Reach");
                            else if (savedMap.get(key).get("StatusId").equals("4107"))
                                lTicketStatus.add("Accepted");
                            else if (savedMap.get(key).get("StatusId").equals("4481"))
                                lTicketStatus.add("In Progress");
                            else if (savedMap.get(key).get("StatusId").equals("4105"))
                                lTicketStatus.add("Closed");
                            else
                                lTicketStatus.add(cqueryTemp.getString(0));
                        } catch (Exception e) {
                            if (savedMap.get(key).get("StatusId").equals("2"))
                                lTicketStatus.add("Start");
                            else
                                lTicketStatus.add("Reach");
                        }
                        lTicketTime.add(savedMap.get(key).get("ActivityDate"));
                    } catch (Exception e) {
                    }
                }
//                }
//                    for(cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext())
//
//                {
//                    if (cquery.getString(6).toString().equals("true"))
//                        lColors.add(R.drawable.cardbk_green);
//                    else
//                        lColors.add(R.drawable.cardbk_red);
//                    if (cqueryTemp.getCount() > 0) {
//                        cqueryTemp.moveToFirst();
//                        lTicketNumber.add(cqueryTemp.getString(0).toString());
//                    } else {
//                        lTicketNumber.add("NA");
//                    }
//                    cqueryTemp = sql.rawQuery("select StatusName from Issue_Status where StatusId=" + cquery.getString(3).toString(), null);
//                    cqueryTemp.moveToFirst();
//                    lTicketComment.add(cquery.getString(4).toString());
//                    lTicketStatus.add(cqueryTemp.getString(0).toString());
//                    lTicketTime.add(cquery.getString(5).toString());
//                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mAdapter = new TicketSynAdpater(lTicketNumber, lTicketStatus, lTicketTime, lTicketComment, lColors);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });

            } else

            {
                try {
                    Toast.makeText(getContext(), "Data is already synced!!!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }

        }

        public void removeData () {
            MyApp.getApplication().writeTicketsIssueHistory(new HashMap<String, Map<String, String>>());
//        sql.delete("Issue_History", null, null);
            lTicketComment.clear();
            lTicketTime.clear();
            lTicketNumber.clear();
            lTicketStatus.clear();
            mAdapter = new TicketSynAdpater(lTicketNumber, lTicketStatus, lTicketTime, lTicketComment, lColors);
            mRecyclerView.setAdapter(mAdapter);
        }
    }
