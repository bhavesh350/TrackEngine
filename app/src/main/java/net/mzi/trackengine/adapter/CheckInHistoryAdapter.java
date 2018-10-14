package net.mzi.trackengine.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mzi.trackengine.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 9/1/2017.
 */

public class CheckInHistoryAdapter extends RecyclerView.Adapter<CheckInHistoryAdapter.ViewHolder> {
    List<String> lCheckInTime = new ArrayList<String>();
    List<String> lCheckOutTime = new ArrayList<String>();
    List<String> lCheckInLocation = new ArrayList<String>();
    List<String> lCheckOutLocation = new ArrayList<String>();
    List<String> lDuration = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();

    public CheckInHistoryAdapter(List<String> lCheckInTime, List<String> lCheckOutTime, List<String> lCheckInLocation, List<String> lCheckOutLocation, List<String> lDuration, List<Integer> mDatasetTypes) {
        this.lCheckInTime = lCheckInTime;
        this.lCheckOutTime = lCheckOutTime;
        this.lCheckInLocation = lCheckInLocation;
        this.lCheckOutLocation = lCheckOutLocation;
        this.lDuration = lDuration;
        this.mDatasetTypes = mDatasetTypes;
    }

    @Override
    public CheckInHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkincard, parent, false);
        return new CheckInHistoryAdapter.HistoryCheckInHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryCheckInHolder historyHolder = (HistoryCheckInHolder) holder;
        historyHolder.tCheckInTime.setText(lCheckInTime.get(position));
        historyHolder.tCheckOutTime.setText(lCheckOutTime.get(position));
        historyHolder.tDuration.setText(lDuration.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatasetTypes.size();
    }

    private class HistoryCheckInHolder extends CheckInHistoryAdapter.ViewHolder {
        TextView tCheckInTime, tCheckOutTime, tDuration;

        public HistoryCheckInHolder(View itemView) {
            super(itemView);
            this.tCheckInTime = itemView.findViewById(R.id.checkintime);
            this.tCheckOutTime = itemView.findViewById(R.id.checkouttime);
            this.tDuration = itemView.findViewById(R.id.durationHistory);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

}
