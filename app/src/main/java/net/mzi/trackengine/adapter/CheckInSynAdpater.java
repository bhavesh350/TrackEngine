package net.mzi.trackengine.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.mzi.trackengine.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 1/23/2018.
 */

public class CheckInSynAdpater extends RecyclerView.Adapter<CheckInSynAdpater.ViewHolder> {
    List<String> lCheckInTime = new ArrayList<String>();
    List<String> lCheckInStatus = new ArrayList<String>();
    List<Integer> lColors = new ArrayList<Integer>();

    public CheckInSynAdpater(List<String> lCheckInTime, List<String> lCheckInStatus, List<Integer> lColors) {
        this.lCheckInTime = lCheckInTime;
        this.lCheckInStatus = lCheckInStatus;
        this.lColors = lColors;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offline_checkin_sync_card, parent, false);
        return new CheckInSynAdpater.CheckInSyncHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckInSyncHolder syncHolder = (CheckInSyncHolder) holder;
        syncHolder.tCheckInStatus.setText(lCheckInStatus.get(position));
        syncHolder.tCheckInTime.setText(lCheckInTime.get(position));
        syncHolder.tktlayout.setBackgroundResource(lColors.get(position));

    }

    @Override
    public int getItemCount() {
        return lCheckInStatus.size();
        //return 51;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class CheckInSyncHolder extends CheckInSynAdpater.ViewHolder {
        TextView tCheckInTime, tCheckInStatus;
        LinearLayout tktlayout;

        public CheckInSyncHolder(View itemView) {
            super(itemView);
            this.tCheckInTime = itemView.findViewById(R.id.id_date);
            this.tCheckInStatus = itemView.findViewById(R.id.id_status);
            this.tktlayout = itemView.findViewById(R.id.checkinlayout);
        }
    }


}
