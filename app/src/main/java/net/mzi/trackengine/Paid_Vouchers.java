package net.mzi.trackengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.mzi.trackengine.adapter.PendingVoucherAdpater;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Poonam on 1/19/2018.
 */
public class Paid_Vouchers extends Fragment {
    ApiInterface apiInterface;
    private RecyclerView.LayoutManager mLayoutManager;
    PendingVoucherAdpater mAdapter;
    RecyclerView mRecyclerView;
    SQLiteDatabase sql=null;
    SharedPreferences pref;
    List<ApiResult.Voucher> lVoucherList=new ArrayList<ApiResult.Voucher>();
    private String sUserId;
    public void Paid_Vouchers(){

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.pending_voucher, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = getContext().getSharedPreferences("login", 0);
        sUserId = pref.getString("userid", "userid");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.voucher_view);
        sql = getContext().openOrCreateDatabase("MZI.sqlite",getContext().MODE_PRIVATE,null);
        getData();
        return view;
    }
    public void getData(){
        lVoucherList.clear();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sql = getContext().openOrCreateDatabase("MZI.sqlite", Context.MODE_PRIVATE, null);
        Call<ApiResult.VoucherList> call1 = apiInterface.GetConveyanceApprover(sUserId,"2018-04-01","2018-04-30","0");
        call1.enqueue(new Callback<ApiResult.VoucherList>() {
            @Override
            public void onResponse(Call<ApiResult.VoucherList> call, Response<ApiResult.VoucherList> response) {
                try{
                    ApiResult.VoucherList iData= response.body();
                    ApiResult.Voucher temp=null;
                    ApiResult o = new ApiResult();
                    temp=o.new Voucher();
                    for(int i = 0; i <iData.Vouchers.length;i++){
                        temp.AccountApproved=iData.Vouchers[i].AccountApproved;
                        temp.ConveyanceAmount=iData.Vouchers[i].ConveyanceAmount;
                        temp.DistanceTravel=iData.Vouchers[i].DistanceTravel;
                        temp.FromDate=iData.Vouchers[i].FromDate;
                        temp.ToDate=iData.Vouchers[i].ToDate;
                        temp.SeniorApproved=iData.Vouchers[i].SeniorApproved;
                        temp.VoucherNumber=iData.Vouchers[i].VoucherNumber;
                        temp.VoucherId=iData.Vouchers[i].VoucherId;
                        temp.Id=iData.Vouchers[i].Id;
                        lVoucherList.add(temp);
                    }
                    temp.AccountApproved="vaou";
                    temp.ConveyanceAmount="snksjdska";
                    temp.DistanceTravel="dmcm";
                    temp.FromDate="mlxckmlxck";
                    temp.ToDate="cmzxcmz";
                    temp.SeniorApproved="skkdssdk";
                    temp.VoucherNumber="zlsczslc";
                    temp.VoucherId="zcmdklc";
                    temp.Id="zcmdklc";
                    lVoucherList.add(temp);
                    mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mAdapter = new PendingVoucherAdpater(lVoucherList,R.drawable.cardbk_green);
                    mRecyclerView.setAdapter(mAdapter);
                }catch (Exception e){}
            }
            @Override
            public void onFailure(Call<ApiResult.VoucherList> call, Throwable t) {
                call.cancel();
                try {
                    SOMTracker.showMassage(getContext(),getContext().getString(R.string.internet_error));
//                    Toast.makeText(getContext(), R.string.internet_error, Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.getMessage();
                }
            }
        });
    }
}
