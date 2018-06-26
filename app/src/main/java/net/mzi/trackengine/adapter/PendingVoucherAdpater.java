package net.mzi.trackengine.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mzi.trackengine.ApiResult;
import net.mzi.trackengine.R;
import net.mzi.trackengine.VoucherHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 4/18/2018.
 */
public class PendingVoucherAdpater extends RecyclerView.Adapter<PendingVoucherAdpater.ViewHolder>{

    List<ApiResult.Voucher> lVoucherList=new ArrayList<ApiResult.Voucher>();
    int cardBackground;

    public PendingVoucherAdpater(List<ApiResult.Voucher> lVoucherList, int cardBackground) {
        this.lVoucherList=lVoucherList;
        this.cardBackground=cardBackground;
    }

    public class PendingViewHolder extends ViewHolder {
        TextView tv_VId,tv_VPeriod,tv_VDistance,tv_VSApproved,tv_VAApproved,tv_VAmount;
        LinearLayout r1;

        public PendingViewHolder(View v) {
            super(v);
            this.tv_VId = (TextView) v.findViewById(R.id.id_VId);
            this.tv_VPeriod=(TextView)v.findViewById(R.id.id_VPeriod);
            this.tv_VDistance=(TextView)v.findViewById(R.id.id_VDistance);
            this.tv_VSApproved=(TextView)v.findViewById(R.id.id_VSApproved);
            this.tv_VAApproved=(TextView)v.findViewById(R.id.id_VAApproved);
            this.tv_VAmount=(TextView)v.findViewById(R.id.id_VAmount);
            this.r1=(LinearLayout)v.findViewById(R.id.tktvoucherlayout);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.voucher_listing_card, parent, false);
        return new PendingVoucherAdpater.PendingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PendingViewHolder pendingholder = (PendingViewHolder) holder;
        pendingholder.tv_VId.setText(lVoucherList.get(position).VoucherId);
        pendingholder.tv_VAmount.setText(lVoucherList.get(position).ConveyanceAmount);
        pendingholder.tv_VDistance.setText(lVoucherList.get(position).DistanceTravel);
        pendingholder.tv_VPeriod.setText(lVoucherList.get(position).FromDate+" : "+lVoucherList.get(position).ToDate);
        pendingholder.tv_VSApproved.setText(lVoucherList.get(position).SeniorApproved);
        pendingholder.tv_VAApproved.setText(lVoucherList.get(position).AccountApproved);
        pendingholder.tv_VId.setText(lVoucherList.get(position).VoucherId);

        pendingholder.r1.setBackgroundResource(cardBackground);
    }


    @Override
    public int getItemCount() {
        return lVoucherList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            View v=itemView;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), VoucherHistory.class);
                    i.putExtra("VoucherId", lVoucherList.get(getAdapterPosition()).Id);
                    i.putExtra("VoucherNumber", lVoucherList.get(getAdapterPosition()).VoucherNumber);
                    //Log.d("TAG", "onClick: ");
                    v.getContext().startActivity(i);
                }
            });
        }
    }
}
