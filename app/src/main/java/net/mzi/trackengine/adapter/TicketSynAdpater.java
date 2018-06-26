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

public class TicketSynAdpater extends RecyclerView.Adapter<TicketSynAdpater.ViewHolder> {
    List<String> lTicketTime=new ArrayList<String>();
    List<String> lTicketStatus=new ArrayList<String>();
    List<String> lTicketComment=new ArrayList<String>();
    List<String> lTicketNumber=new ArrayList<String>();
    List<Integer> lColors=new ArrayList<Integer>();



    public TicketSynAdpater(List<String> lTicketNumber,List<String> lTicketStatus,List<String> lTicketTime, List<String> lTicketComment, List<Integer> lColors){
        this.lTicketTime=lTicketTime;
        this.lTicketStatus=lTicketStatus;
        this.lTicketComment=lTicketComment;
        this.lTicketNumber=lTicketNumber;
        this.lColors=lColors;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.offline_tkt_sync_card, parent, false);
        return new TicketSynAdpater.TicketSyncHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TicketSyncHolder syncHolder=(TicketSyncHolder) holder;
        syncHolder.tTicketTime.setText(lTicketTime.get(position));
        syncHolder.tTicketStatus.setText(lTicketStatus.get(position));
        syncHolder.tTicketNumber.setText(lTicketNumber.get(position));
        syncHolder.tTicketComment.setText(lTicketComment.get(position));
        syncHolder.tktLayout.setBackgroundResource(lColors.get(position));
    }

    @Override
    public int getItemCount() {
        return lTicketNumber.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
    private class TicketSyncHolder extends TicketSynAdpater.ViewHolder{
        TextView tTicketTime,tTicketStatus,tTicketNumber,tTicketComment;
        LinearLayout tktLayout;
        public TicketSyncHolder(View itemView) {
            super(itemView);
            this.tTicketTime=(TextView)itemView.findViewById(R.id.id_date);
            this.tTicketStatus=(TextView)itemView.findViewById(R.id.id_status);
            this.tTicketNumber=(TextView)itemView.findViewById(R.id.id_TNumber);
            this.tTicketComment=(TextView)itemView.findViewById(R.id.id_comment);
            this.tktLayout=(LinearLayout) itemView.findViewById(R.id.tktsynclayout);
        }
    }

}
