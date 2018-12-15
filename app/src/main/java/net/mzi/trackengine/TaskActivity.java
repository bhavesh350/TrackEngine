package net.mzi.trackengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import net.mzi.trackengine.fragment.FilterListFragment;
import net.mzi.trackengine.model.PostUrl;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.mzi.trackengine.SchedulingAdapter.latitude;
import static net.mzi.trackengine.SchedulingAdapter.longitude;

public class TaskActivity extends AppCompatActivity implements FilterListFragment.OnDataPass, View.OnClickListener {
    private RecyclerView.LayoutManager mLayoutManager;
    SchedulingAdapter scehduleAdapter;
    public static final int New = 0;
    public static final int Pending = 1;
    public static final int Attended = 2;
    public static final int Complete = 3;
    Button bFilterList, bSorting;
    //static  String parentId;
    RecyclerView mRecyclerView;
    //static List<String> statusList=new ArrayList<String>();
    List<DistanceSortingClassParameter> lDistance = new ArrayList<DistanceSortingClassParameter>();
    List<String> mTicketNumber = new ArrayList<String>();
    List<String> mName = new ArrayList<String>();
    List<String> mTime = new ArrayList<String>();
    List<String> mSub = new ArrayList<String>();
    List<String> mMob = new ArrayList<String>();
    List<String> mLoc = new ArrayList<String>();
    List<String> mcardType = new ArrayList<String>();
    List<Integer> mCardColor = new ArrayList<Integer>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> mIssueID = new ArrayList<String>();
    List<String> selectedItems = new ArrayList<String>();
    //List<StatusInfoClass> statusList=new ArrayList<StatusInfoClass>();
    static String card;
    SQLiteDatabase sql;
    Cursor cquery;
    Cursor cqueryForStatus;
    Double lat, lon, curLat, curLon;
    Gps gps;
    private boolean isShowExtra;
    private String address;
    private CardView cardview_misl, cardview_office;
    private ImageButton btn_office_start, btn_office_reached, btn_misl_start, btn_misl_reached;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        getSupportActionBar().setTitle("Task List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        gps = new Gps(getApplicationContext());
        isShowExtra = getIntent().getBooleanExtra("isShowExtra", false);
        address = getIntent().getStringExtra("address");
        bSorting = findViewById(R.id.SortingId);
        bFilterList = findViewById(R.id.FilterStatusId);
        cardview_office = findViewById(R.id.cardview_office);
        cardview_misl = findViewById(R.id.cardview_misl);
        btn_office_start = findViewById(R.id.btn_office_start);
        btn_office_reached = findViewById(R.id.btn_office_reached);
        btn_misl_start = findViewById(R.id.btn_misl_start);
        btn_misl_reached = findViewById(R.id.btn_misl_reached);
        btn_misl_reached.setOnClickListener(this);
        btn_misl_start.setOnClickListener(this);
        btn_office_reached.setOnClickListener(this);
        btn_office_start.setOnClickListener(this);
        if (isShowExtra) {
            cardview_misl.setVisibility(View.GONE);
            cardview_office.setVisibility(View.GONE);
//            ((TextView) findViewById(R.id.txt_mis_address)).setText(address);
//            ((TextView) findViewById(R.id.txt_office_address)).setText(address);
        }
        sql = openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        bSorting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lDistance.clear();
                curLat = gps.getLatitude();
                curLon = gps.getLongitude();
                cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = '" + card + "'", null);
                for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                    if (cquery.getDouble(11) == 0.0) {
                        Geocoder coder = new Geocoder(TaskActivity.this);
                        List<Address> address;
                        try {
                            address = coder.getFromLocationName(cquery.getString(10).toString(), 5);
                            if (address.size() > 0) {
                                Address location = address.get(0);
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                            }
                        } catch (Exception e) {
                            Log.e("FullScreenMap", "exception" + e.getMessage());
                            lat = cquery.getDouble(2);
                            lon = cquery.getDouble(3);
                        }
                    } else {
                        lat = cquery.getDouble(2);
                        lon = cquery.getDouble(3);
                    }
                    lDistance.add(new DistanceSortingClassParameter(showDistance(curLat, curLon, lat, lon), cquery.getString(1).toString()));
                }
                Collections.sort(lDistance, new Comparator<DistanceSortingClassParameter>() {
                    @Override
                    public int compare(DistanceSortingClassParameter s1, DistanceSortingClassParameter s2) {
                        return s1.dDistance.compareTo(s2.dDistance);
                    }
                });
                mIssueID.clear();
                mName.clear();
                mTime.clear();
                mLoc.clear();
                mMob.clear();
                mSub.clear();
                mTicketNumber.clear();
                mDatasetTypes.clear();
                mCardColor.clear();
                mcardType.clear();
                for (int i = 0; i < lDistance.size(); i++) {
                    Log.e("onCreate: ", lDistance.get(i).sIssueId + "sdfsfd" + lDistance.get(i).dDistance);
                    cquery = sql.rawQuery("select * from Issue_Detail where IssueId = '" + lDistance.get(i).sIssueId + "'", null);
                    if (cquery.getCount() > 0) {
                        cquery.moveToFirst();
                        mIssueID.add(cquery.getString(1).toString());
                        mName.add(cquery.getString(19).toString());
                        mTime.add(cquery.getString(8).toString());
                        mSub.add(cquery.getString(3).toString());
                        mMob.add(cquery.getString(13).toString());
                        mLoc.add(cquery.getString(10).toString());
                        mTicketNumber.add(cquery.getString(20).toString());
                        //parentId=cquery.getString(13).toString();
                        if (cquery.getString(14).toString().equals("-1")) {
                            mcardType.add("New");
                            mCardColor.add(R.color.purple);
                            mDatasetTypes.add(New);
                        } else {
                            if (cquery.getString(14).toString().equals("1")) {
                                mCardColor.add(R.color.orange);
                                mDatasetTypes.add(Pending);
                            } else if (cquery.getString(14).toString().equals("2")) {
                                mCardColor.add(R.color.blue);
                                mDatasetTypes.add(Attended);
                            } else if (cquery.getString(14).toString().equals("3")) {
                                mCardColor.add(R.color.green);
                                mDatasetTypes.add(Complete);
                            }
                            cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                            if (cqueryForStatus.getCount() > 0) {
                                cqueryForStatus.moveToFirst();
                                mcardType.add(cqueryForStatus.getString(0).toString());
                            } else {
                                mcardType.add("NA");
                            }

                        }
                    }
                }
                mRecyclerView = (RecyclerView) findViewById(R.id.task_view);
                mLayoutManager = new LinearLayoutManager(TaskActivity.this, LinearLayoutManager.VERTICAL, false);
                mRecyclerView.setLayoutManager(mLayoutManager);
                scehduleAdapter = new SchedulingAdapter(mIssueID, mName, mTime, mLoc, mMob, mSub,
                        mDatasetTypes, mCardColor, mcardType, mTicketNumber, TaskActivity.this);
                mRecyclerView.setNestedScrollingEnabled(false);
                mRecyclerView.setAdapter(scehduleAdapter);

            }

        });
        bFilterList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment hello = new FilterListFragment();
                FragmentManager fragmentManager = ((AppCompatActivity) TaskActivity.this).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.activity_task, hello);
                fragmentTransaction.commit();

            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("cardpos") != null) {
            card = bundle.get("cardpos").toString();

            if (selectedItems.size() > 0) {
                // selectedItem is used for Filtered Data, so if it contains the data , it means filter option is clicked. It contain all the statuses that is selected by user for filtering.
            } else {
                if (card.equals("1")) {
                    cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 1", null);
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        mIssueID.add(cquery.getString(1).toString());
                        mName.add(cquery.getString(19).toString());
                        mTime.add(cquery.getString(8).toString());
                        mSub.add(cquery.getString(3).toString());
                        mMob.add(cquery.getString(13).toString());
                        mLoc.add(cquery.getString(10).toString());
                        mTicketNumber.add(cquery.getString(20).toString());

                        cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                        if (cqueryForStatus.getCount() > 0) {
                            cqueryForStatus.moveToFirst();
                            mcardType.add(cqueryForStatus.getString(0).toString());
                        } else
                            mcardType.add("N/A");

                        mCardColor.add(R.color.orange);
                        mDatasetTypes.add(Pending);
                    }

                } else if (card.equals("2")) {
                    cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 2", null);
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        mIssueID.add(cquery.getString(1).toString());
                        mName.add(cquery.getString(19).toString());
                        mTime.add(cquery.getString(8).toString());
                        mSub.add(cquery.getString(3).toString());
                        mMob.add(cquery.getString(13).toString());
                        mLoc.add(cquery.getString(10).toString());
                        mTicketNumber.add(cquery.getString(20).toString());
                        cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                        if (cqueryForStatus.getCount() > 0) {
                            cqueryForStatus.moveToFirst();
                            mcardType.add(cqueryForStatus.getString(0).toString());
                        } else {
                            mcardType.add("N/A");
                        }
                        //mcardType.add("Complete");
                        mCardColor.add(R.color.blue);
                        mDatasetTypes.add(Attended);
                    }
                } else if (card.equals("3")) {

                    cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = 3", null);
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                        mIssueID.add(cquery.getString(1).toString());
                        mName.add(cquery.getString(19).toString());
                        mTime.add(cquery.getString(8).toString());
                        mSub.add(cquery.getString(3).toString());
                        mMob.add(cquery.getString(13).toString());
                        mLoc.add(cquery.getString(10).toString());
                        mTicketNumber.add(cquery.getString(20).toString());

                        cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                        if (cqueryForStatus.getCount() > 0) {
                            cqueryForStatus.moveToFirst();
                            mcardType.add(cqueryForStatus.getString(0).toString());
                        } else {
                            mcardType.add("N/A");
                        }
                        //mcardType.add("Closed");
                        mCardColor.add(R.color.green);
                        mDatasetTypes.add(Complete);
                    }

                } else {
                    cquery = sql.rawQuery("select * from Issue_Detail", null);
                    for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {

                        Log.e("onCreate: is Accepted", cquery.getString(12).toString());
                        if (cquery.getString(12).toString().equals("0")) ;
                        else {
                            mIssueID.add(cquery.getString(1).toString());
                            mName.add(cquery.getString(19).toString());
                            mTime.add(cquery.getString(8).toString());
                            mSub.add(cquery.getString(3).toString());
                            mMob.add(cquery.getString(13).toString());
                            mLoc.add(cquery.getString(10).toString());
                            mTicketNumber.add(cquery.getString(20).toString());
                            //parentId=cquery.getString(13).toString();
                            if (cquery.getString(14).toString().equals("-1")) {
                                mcardType.add("New");
                                mCardColor.add(R.color.purple);
                                mDatasetTypes.add(New);
                            } else {
                                if (cquery.getString(14).toString().equals("1")) {
                                    mCardColor.add(R.color.orange);
                                    mDatasetTypes.add(Pending);
                                } else if (cquery.getString(14).toString().equals("2")) {
                                    mCardColor.add(R.color.blue);
                                    mDatasetTypes.add(Attended);
                                } else if (cquery.getString(14).toString().equals("3")) {
                                    mCardColor.add(R.color.green);
                                    mDatasetTypes.add(Complete);
                                }
                                cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                                if (cqueryForStatus.getCount() > 0) {
                                    cqueryForStatus.moveToFirst();
                                    mcardType.add(cqueryForStatus.getString(0).toString());
                                } else {
                                    mcardType.add("N/A");
                                }

                            }
                        }


                    }
                }
            }
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.task_view);
        mLayoutManager = new LinearLayoutManager(TaskActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        scehduleAdapter = new SchedulingAdapter(mIssueID, mName, mTime, mLoc, mMob, mSub, mDatasetTypes,
                mCardColor, mcardType, mTicketNumber, TaskActivity.this);
        mRecyclerView.setAdapter(scehduleAdapter);
        mRecyclerView.setNestedScrollingEnabled(false);


        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = getSharedPreferences("login", 0);
        nh_userid = pref.getString("userid", "userid");
        sParentComapnyId = pref.getString("ParentCompanyId", "ParentCompanyId");
        DepartmentId = pref.getString("DepartmentId", "DepartmentId");
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            sDeviceId = telephonyManager.getDeviceId().toString();
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


    @Override
    public void onDataPass(List<String> data, String sIsAccepted) {
        mIssueID.clear();
        mName.clear();
        mTime.clear();
        mLoc.clear();
        mMob.clear();
        mSub.clear();
        mDatasetTypes.clear();
        mCardColor.clear();
        mcardType.clear();
        mTicketNumber.clear();
        selectedItems = data;
        for (int i = 0; i < selectedItems.size(); i++) {
            Log.e("onCreate: ", String.valueOf(selectedItems.size()));
            //Log.e("onCreate:", cqueryForStatus.getString(0).toString());
            if (sIsAccepted.equals("0")) {
                cquery = sql.rawQuery("select * from Issue_Detail where StatusId = '" + selectedItems.get(i) + "'", null);
                for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                    mIssueID.add(cquery.getString(1).toString());
                    mName.add(cquery.getString(19).toString());
                    mTime.add(cquery.getString(8).toString());
                    mSub.add(cquery.getString(3).toString());
                    mMob.add(cquery.getString(13).toString());
                    mLoc.add(cquery.getString(10).toString());
                    mTicketNumber.add(cquery.getString(20).toString());
                    cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                    if (cqueryForStatus.getCount() > 0) {
                        cqueryForStatus.moveToFirst();
                        mcardType.add(cqueryForStatus.getString(0).toString());
                    }
                    cqueryForStatus = sql.rawQuery("select IsAccepted from Issue_Detail where StatusId = '" + selectedItems.get(i) + "'", null);
                    if (cqueryForStatus.getCount() > 0) {
                        cqueryForStatus.moveToFirst();
                        if (cqueryForStatus.getString(0).toString().equals("1")) {

                            mCardColor.add(R.color.orange);
                            mDatasetTypes.add(Pending);
                        } else if (cqueryForStatus.getString(0).toString().equals("2")) {

                            mCardColor.add(R.color.blue);
                            mDatasetTypes.add(Attended);
                        } else if (cqueryForStatus.getString(0).toString().equals("3")) {

                            mCardColor.add(R.color.green);
                            mDatasetTypes.add(Complete);
                        }

                    }
                }
            } else {
                cquery = sql.rawQuery("select * from Issue_Detail where IsAccepted = '" + selectedItems.get(i) + "'", null);
                for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {
                    mIssueID.add(cquery.getString(1).toString());
                    mName.add(cquery.getString(19).toString());
                    mTime.add(cquery.getString(8).toString());
                    mSub.add(cquery.getString(3).toString());
                    mMob.add(cquery.getString(13).toString());
                    mLoc.add(cquery.getString(10).toString());
                    mTicketNumber.add(cquery.getString(20).toString());
                    cqueryForStatus = sql.rawQuery("select StatusName from Issue_Status where StatusId = '" + cquery.getString(15).toString() + "'", null);
                    if (cqueryForStatus.getCount() > 0) {
                        cqueryForStatus.moveToFirst();
                        mcardType.add(cqueryForStatus.getString(0).toString());
                    }
                    if (selectedItems.get(i).equals("1")) {

                        mCardColor.add(R.color.orange);
                        mDatasetTypes.add(Pending);
                    } else if (selectedItems.get(i).equals("2")) {

                        mCardColor.add(R.color.blue);
                        mDatasetTypes.add(Attended);
                    } else if (selectedItems.get(i).equals("3")) {

                        mCardColor.add(R.color.green);
                        mDatasetTypes.add(Complete);
                    }

                    // }
                }
            }
            //mcardType.add("Complete");
        }

        cqueryForStatus.close();
        mRecyclerView = findViewById(R.id.task_view);
        mLayoutManager = new LinearLayoutManager(TaskActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        scehduleAdapter = new SchedulingAdapter(mIssueID, mName, mTime, mLoc, mMob, mSub, mDatasetTypes, mCardColor, mcardType, mTicketNumber, TaskActivity.this);
        mRecyclerView.setAdapter(scehduleAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    private Double showDistance(Double curlat, Double curlon, Double desLat, Double desLon) {
        if (desLat == null || desLon == null || desLat == 0.0 || desLon == 0.0) {
            return 0.0;
        } else {
            final int R = 6371; // Radious of the earth
            Double latDistance = deg2rad(desLat - curlat);
            Double lonDistance = deg2rad(desLon - curlon);
            Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                    Math.cos(deg2rad(curlat)) * Math.cos(deg2rad(desLat)) *
                            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            Double distance = R * c;
            return distance;
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_office_start) {
            MyApp.setStatus("OFFICE", true);
            btn_office_start.setEnabled(false);
            btn_office_reached.setEnabled(true);

            callStartReachApi(true, true);
        } else if (v == btn_office_reached) {
            btn_office_start.setEnabled(true);
            btn_office_reached.setEnabled(false);
            MyApp.setStatus("OFFICE", false);

            callStartReachApi(false, true);
        } else if (v == btn_misl_start) {
            btn_misl_start.setEnabled(false);
            btn_misl_reached.setEnabled(true);
            MyApp.setStatus("MSLVISIT", true);

            callStartReachApi(true, false);
        } else if (v == btn_misl_reached) {
            btn_misl_start.setEnabled(true);
            btn_misl_reached.setEnabled(false);
            MyApp.setStatus("MSLVISIT", false);
            callStartReachApi(false, false);
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean officeVisit = MyApp.getStatus("OFFICE");
        boolean mslVisit = MyApp.getStatus("MSLVISIT");

        if (mslVisit) {
            btn_misl_start.setImageResource(R.drawable.btn_d_start);
            btn_misl_reached.setImageResource(R.drawable.btn_reach);
        } else {
            btn_misl_start.setImageResource(R.drawable.btn_start);
            btn_misl_reached.setImageResource(R.drawable.btn_d_reach);
        }

        if (officeVisit) {
            btn_office_start.setImageResource(R.drawable.btn_d_start);
            btn_office_reached.setImageResource(R.drawable.btn_reach);
        } else {
            btn_office_start.setImageResource(R.drawable.btn_start);
            btn_office_reached.setImageResource(R.drawable.btn_d_reach);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean officeVisit = MyApp.getStatus("OFFICE");
        boolean mslVisit = MyApp.getStatus("MSLVISIT");

        if (mslVisit) {
            btn_misl_start.setEnabled(false);
            btn_misl_reached.setEnabled(true);
        } else {
            btn_misl_start.setEnabled(true);
            btn_misl_reached.setEnabled(false);
        }

        if (officeVisit) {
            btn_office_start.setEnabled(false);
            btn_office_reached.setEnabled(true);
        } else {
            btn_office_start.setEnabled(true);
            btn_office_reached.setEnabled(false);
        }

        updateButtons();
    }

    private void callStartReachApi(boolean isStart, boolean isOffice) {
        HashMap<String, String> postTktStatus = new HashMap<>();
        scehduleAdapter.getLocation();
        Date cDate1 = new Date();
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cDate1);
        postTktStatus.put("UserId", nh_userid);
        postTktStatus.put("ParentCompanyId", sParentComapnyId);
        postTktStatus.put("TicketId", currentTime);
        postTktStatus.put("StatusId", "");
        postTktStatus.put("StartingForSite", isStart ? 1 + "" : 0 + "");
        postTktStatus.put("CustomDestination", isOffice ? "Office" : "Miscellaneous");
        postTktStatus.put("Comment", "");
        postTktStatus.put("ActivityDate", currentTime);
        postTktStatus.put("DepartmentId", DepartmentId);
        postTktStatus.put("RealtimeUpdate", "true");
        postTktStatus.put("Latitude", String.valueOf(latitude));
        postTktStatus.put("Longitude", String.valueOf(longitude));
        postTktStatus.put("AssetSerialNo", "");
        postTktStatus.put("DeviceId", sDeviceId);
        postTktStatus.put("Expense", "");
        postTktStatus.put("SyncStatus", "-1");
        postTktStatus.put("ModeOfTransport", "");
        postTktStatus.put("Expense", "0");
        postTktStatus.put("AssignedUserId", "0");



        if (isStart) {
            scehduleAdapter.populateStatusStartReachTaskActivity("",0,"",true,isOffice ? "Office" : "Miscellaneous");
        } else {
            Map<String, Map<String, String>> issuesMap = MyApp.getApplication().readTicketsIssueHistory();
            issuesMap.put(currentTime, postTktStatus);
            MyApp.getApplication().writeTicketsIssueHistory(issuesMap);
            scehduleAdapter.UpdateTask(scehduleAdapter.context, postTktStatus, "true");
        }
    }

    String nh_userid;
    ApiInterface apiInterface;
    SharedPreferences pref;
    String sParentComapnyId;
    String DepartmentId;
    String sDeviceId;
}