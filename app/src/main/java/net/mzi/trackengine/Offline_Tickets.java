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

import java.util.ArrayList;
import java.util.List;

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
        mRecyclerView = (RecyclerView) view.findViewById(R.id.sync_view);
        sql = getContext().openOrCreateDatabase("MZI.sqlite", getContext().MODE_PRIVATE, null);
        bClearData = (Button) view.findViewById(R.id.idClearData);
        bForceSync = (Button) view.findViewById(R.id.IdForcedSync);
        bClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext().getApplicationContext(),"hii",Toast.LENGTH_LONG).show();
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
        getData();
        return view;
    }

    public void getData() {

        lTicketNumber.clear();
        lTicketComment.clear();
        lTicketStatus.clear();
        lTicketTime.clear();
        lColors.clear();
        final Cursor cquery = sql.rawQuery("select * from Issue_History where UserId='" + sUserId + "'", null);
        if (cquery.getCount() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        Cursor cqueryTemp = sql.rawQuery("select TicketNumber from Issue_Detail where issueid='" + cquery.getString(1).toString() + "'", null);
                        if (cquery.getString(6).toString().equals("true"))
                            lColors.add(R.drawable.cardbk_green);
                        else
                            lColors.add(R.drawable.cardbk_red);
                        if (cqueryTemp.getCount() > 0) {
                            cqueryTemp.moveToFirst();
                            lTicketNumber.add(cqueryTemp.getString(0).toString());
                        } else {
                            lTicketNumber.add("NA");
                        }
                        cqueryTemp = sql.rawQuery("select StatusName from Issue_Status where StatusId=" + cquery.getString(3).toString(), null);
                        cqueryTemp.moveToFirst();
                        lTicketComment.add(cquery.getString(4).toString());
                        lTicketStatus.add(cqueryTemp.getString(0).toString());
                        lTicketTime.add(cquery.getString(5).toString());
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            mAdapter = new TicketSynAdpater(lTicketNumber, lTicketStatus, lTicketTime, lTicketComment, lColors);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(getContext(), "Data is already synced!!!", Toast.LENGTH_LONG).show();
        }

    }

    public void removeData() {
        sql.delete("Issue_History", null, null);
        lTicketComment.clear();
        lTicketTime.clear();
        lTicketNumber.clear();
        lTicketStatus.clear();
        mAdapter = new TicketSynAdpater(lTicketNumber, lTicketStatus, lTicketTime, lTicketComment, lColors);
        mRecyclerView.setAdapter(mAdapter);
    }
}
