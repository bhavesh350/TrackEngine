package net.mzi.trackengine;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 3/27/2017.
 */
public class FollowUpAdapter extends RecyclerView.Adapter<FollowUpAdapter.ViewHolder>  {
    List<String> mStatus=new ArrayList<String>();
    List<String> mComment=new ArrayList<String>();
    List<String> mAssignedTo=new ArrayList<String>();
    List<String> mCreadteDate=new ArrayList<String>();
    List<Integer> mDatasetTypes=new ArrayList<Integer>();
    List<String> mIssueId=new ArrayList<String>();
    Context context;
    public FollowUpAdapter(Context context, List<String> mAssignedTo, List<String> mComment, List<String> mCreadteDate, List<String> mStatus, List<Integer> mDatasetTypes,List<String> mIssueId) {
        this.mStatus=mStatus;
        this.mComment=mComment;
        this.mAssignedTo=mAssignedTo;
        this.mCreadteDate=mCreadteDate;
        this.mDatasetTypes=mDatasetTypes;
        this.mIssueId=mIssueId;
        this.context=context;
    }
    private class TicketFollowUpHolder extends FollowUpAdapter.ViewHolder {
        TextView Status,Comment,AssignedTo,CreadteDate, IssueID;
        public TicketFollowUpHolder(View itemView) {
            super(itemView);
            View v =itemView;
            IssueID=(TextView)v.findViewById(R.id.issueIdFollowup);
            Status=(TextView) v.findViewById(R.id.statusFollowup);
            Comment=(TextView) v.findViewById(R.id.commentFollowup);
            AssignedTo=(TextView) v.findViewById(R.id.assignedTo);
            CreadteDate=(TextView) v.findViewById(R.id.dateFollowup);
           /* v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context,TicketInfo.class);
                    i.putExtra("CardType","1");
                    i.putExtra("IssueId",mIssueId.get(getAdapterPosition()));
                    context.startActivity(i);
                }
            });*/
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //ctx=parent.getContext();
        //sql =ctx. openOrCreateDatabase("MZI", Context.MODE_PRIVATE, null);
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.followup_card, parent, false);
        return new TicketFollowUpHolder(v);    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TicketFollowUpHolder ticketHolder = (TicketFollowUpHolder) holder;
        ticketHolder.IssueID.setText("ID: "+mIssueId.get(position));
        ticketHolder.Status.setText("Status: "+mStatus.get(position));
        ticketHolder.Comment.setText("Comment: "+mComment.get(position));
        ticketHolder.AssignedTo.setText("Updated By: "+mAssignedTo.get(position));
        ticketHolder.CreadteDate.setText("Updated On: "+mCreadteDate.get(position));
        //ticketHolder.Status.setBackgroundColor(mColors.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatasetTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDatasetTypes.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
