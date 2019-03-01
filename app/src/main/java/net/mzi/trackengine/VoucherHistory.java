package net.mzi.trackengine;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import net.mzi.trackengine.adapter.VoucherHistoryAdpater;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherHistory extends AppCompatActivity {
    ApiInterface apiInterface;
    private RecyclerView.LayoutManager mLayoutManager;
    VoucherHistoryAdpater mAdapter;
    RecyclerView mRecyclerView;
    SQLiteDatabase sql = null;
    SharedPreferences pref;
    String VoucherId, VoucherNumber = "0";
    List<ApiResult.VoucherHistory> lVoucherHistoryList = new ArrayList<ApiResult.VoucherHistory>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_history);
        Bundle bundle = getIntent().getExtras();
        VoucherId = bundle.getString("VoucherId");
        VoucherNumber = bundle.getString("VoucherNumber");
        getSupportActionBar().setTitle(VoucherNumber);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mRecyclerView = findViewById(R.id.voucherhistory_view);
        getData();
    }

    public void getData() {
        lVoucherHistoryList.clear();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ApiResult.VoucherList> call1 = apiInterface.GetConveyanceVoucherHistory(VoucherId);
        call1.enqueue(new Callback<ApiResult.VoucherList>() {
            @Override
            public void onResponse(Call<ApiResult.VoucherList> call, Response<ApiResult.VoucherList> response) {
                try {
                    ApiResult.VoucherList iData = response.body();
                    ApiResult.VoucherHistory temp = null;
                    ApiResult o = new ApiResult();
                    temp = o.new VoucherHistory();
                    for (int i = 0; i < iData.VouchersHistory.length; i++) {
                        temp.CreatedDate = iData.VouchersHistory[i].CreatedDate;
                        temp.Action = iData.VouchersHistory[i].Action;
                        temp.DoneBy = iData.VouchersHistory[i].DoneBy;
                        temp.Remark = iData.VouchersHistory[i].Remark;

                        lVoucherHistoryList.add(temp);
                    }
                    temp.CreatedDate = "hfghfgh";
                    temp.Action = "fghfg";
                    temp.DoneBy = "dfgfdg";
                    temp.Remark = "asfsf";

                    lVoucherHistoryList.add(temp);
                    mLayoutManager = new LinearLayoutManager(VoucherHistory.this, LinearLayoutManager.VERTICAL, false);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mAdapter = new VoucherHistoryAdpater(lVoucherHistoryList, R.drawable.cardbk_blue, VoucherNumber);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(Call<ApiResult.VoucherList> call, Throwable t) {
                call.cancel();
                try {
                    Toast.makeText(getApplicationContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });
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
