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
 * Created by Poonam on 2/22/2017.
 */
public class UserLocationBatteryHistoryAdapter extends RecyclerView.Adapter<UserLocationBatteryHistoryAdapter.ViewHolder> {
    String value;
    List<String> mDeviceId = new ArrayList<String>();
    List<String> mBatterylevel = new ArrayList<String>();
    List<String> mAutocaptured = new ArrayList<String>();
    List<String> mUpdatedDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();

    public UserLocationBatteryHistoryAdapter(List<String> mDeviceId, List<String> mUpdatedDate, List<String> mBatterylevel, List<String> mAutocaptured, List<Integer> mDatasetTypes, String value) {
        this.mDeviceId = mDeviceId;
        this.mBatterylevel = mBatterylevel;
        this.mUpdatedDate = mUpdatedDate;
        this.mAutocaptured = mAutocaptured;
        this.mDatasetTypes = mDatasetTypes;
        this.value = value;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.histroycombine_card, parent, false);
        return new UserLocationBatteryHistoryAdapter.HistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryHolder historyHolder = (HistoryHolder) holder;
        historyHolder.DeviceId.setText("Device ID : " + mDeviceId.get(position));
        historyHolder.Batterylevel.setText(value + " : " + mBatterylevel.get(position));
        if (mAutocaptured.get(position).equals("false"))
            historyHolder.Autocaptured.setText("Captured : " + "Manual");
        else
            historyHolder.Autocaptured.setText("Captured : " + "Automatic");
        historyHolder.UpdatedDate.setText("Updated Date : " + mUpdatedDate.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatasetTypes.size();
    }

    private class HistoryHolder extends UserLocationBatteryHistoryAdapter.ViewHolder {
        TextView DeviceId, Batterylevel, Autocaptured, UpdatedDate;

        public HistoryHolder(View itemView) {
            super(itemView);
            this.DeviceId = (TextView) itemView.findViewById(R.id.deveiceidHistory);
            this.Batterylevel = (TextView) itemView.findViewById(R.id.batteryHistory);
            this.Autocaptured = (TextView) itemView.findViewById(R.id.autoCapruredHistory);
            this.UpdatedDate = (TextView) itemView.findViewById(R.id.dateHistory);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}