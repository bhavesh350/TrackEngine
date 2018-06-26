package net.mzi.trackengine.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mzi.trackengine.ApiResult;
import net.mzi.trackengine.R;

import java.util.List;

/**
 * Created by Poonam on 4/19/2018.
 */
public class VoucherHistoryAdpater extends RecyclerView.Adapter<VoucherHistoryAdpater.ViewHolder> {
    List<ApiResult.VoucherHistory> lVoucherHistoryList;
    int cardbk_blue;
    String VoucherNumber;
    public VoucherHistoryAdpater(List<ApiResult.VoucherHistory> lVoucherHistoryList, int cardbk_blue, String VoucherNumber) {
        this.lVoucherHistoryList=lVoucherHistoryList;
        this.cardbk_blue=cardbk_blue;
        this.VoucherNumber=VoucherNumber;
    }

    public class VoucherHistoryViewHolder extends VoucherHistoryAdpater.ViewHolder {
        TextView tvh_VHdate,tvh_VHaction,tvh_VHdoneby,tvh_VHremarks,tvh_VHnumber;
        LinearLayout r1;

        public VoucherHistoryViewHolder(View v) {
            super(v);
            this.tvh_VHdate = (TextView) v.findViewById(R.id.id_VHdate);
            this.tvh_VHaction=(TextView)v.findViewById(R.id.id_VHaction);
            this.tvh_VHdoneby=(TextView)v.findViewById(R.id.id_VHdoneby);
            this.tvh_VHremarks=(TextView)v.findViewById(R.id.id_VHremarks);
            this.tvh_VHnumber=(TextView)v.findViewById(R.id.id_VHNumber);

            //this.r1=(LinearLayout)v.findViewById(R.id.tktvoucherlayout);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_history_listing_card, parent, false);
        return new VoucherHistoryAdpater.VoucherHistoryViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VoucherHistoryViewHolder vhHolder = (VoucherHistoryViewHolder) holder;
        vhHolder.tvh_VHdoneby.setText(lVoucherHistoryList.get(position).DoneBy);
        vhHolder.tvh_VHdate.setText(lVoucherHistoryList.get(position).CreatedDate);
        vhHolder.tvh_VHaction.setText(lVoucherHistoryList.get(position).Action);
        vhHolder.tvh_VHremarks.setText(lVoucherHistoryList.get(position).Remark);
        vhHolder.tvh_VHnumber.setText(VoucherNumber);


    }



    @Override
    public int getItemCount() {
        return lVoucherHistoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            View v=itemView;

        }
    }
}
