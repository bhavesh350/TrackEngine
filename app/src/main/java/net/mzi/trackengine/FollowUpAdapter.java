package net.mzi.trackengine;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Poonam on 3/27/2017.
 */
public class FollowUpAdapter extends RecyclerView.Adapter<FollowUpAdapter.ViewHolder> {
    List<String> mStatus = new ArrayList<String>();
    List<String> mComment = new ArrayList<String>();
    List<String> mAssignedTo = new ArrayList<String>();
    List<String> mCreadteDate = new ArrayList<String>();
    List<Integer> mDatasetTypes = new ArrayList<Integer>();
    List<String> mIssueId = new ArrayList<String>();
    List<String> attachments = new ArrayList<String>();
    Context context;

    public FollowUpAdapter(Context context, List<String> mAssignedTo, List<String> mComment, List<String> mCreadteDate, List<String> mStatus, List<Integer> mDatasetTypes, List<String> mIssueId, List<String> attachments) {
        this.attachments = attachments;
        this.mStatus = mStatus;
        this.mComment = mComment;
        this.mAssignedTo = mAssignedTo;
        this.mCreadteDate = mCreadteDate;
        this.mDatasetTypes = mDatasetTypes;
        this.mIssueId = mIssueId;
        this.context = context;
    }

    private class TicketFollowUpHolder extends FollowUpAdapter.ViewHolder {
        TextView Status, Comment, AssignedTo, CreadteDate, IssueID;

        public TicketFollowUpHolder(View itemView) {
            super(itemView);
            View v = itemView;
            IssueID = v.findViewById(R.id.issueIdFollowup);
            Status = v.findViewById(R.id.statusFollowup);
            Comment = v.findViewById(R.id.commentFollowup);
            AssignedTo = v.findViewById(R.id.assignedTo);
            CreadteDate = v.findViewById(R.id.dateFollowup);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (attachments.size() > 0)
                        if (!attachments.get(getLayoutPosition()).isEmpty()) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            WebView wv = new WebView(context);
                            wv.getSettings().setBuiltInZoomControls(true);
                            wv.loadUrl(attachments.get(getLayoutPosition()));
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.MATCH_PARENT);
                            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                            wv.setLayoutParams(params);

                            wv.setWebViewClient(new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    view.loadUrl(url);

                                    return true;
                                }
                            });

                            alert.setView(wv);
                            alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = alert.create();
                            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                            layoutParams.copyFrom(dialog.getWindow().getAttributes());
                            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                            dialog.getWindow().setAttributes(layoutParams);
                            dialog.show();
                        }
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        //ctx=parent.getContext();
        //sql =ctx. openOrCreateDatabase("MZI", Context.MODE_PRIVATE, null);
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.followup_card, parent, false);
        return new TicketFollowUpHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            final TicketFollowUpHolder ticketHolder = (TicketFollowUpHolder) holder;
            ticketHolder.IssueID.setText("ID: " + mIssueId.get(position));
            ticketHolder.Status.setText("Status: " + mStatus.get(position));
            ticketHolder.Comment.setText("Comment: " + mComment.get(position));
            ticketHolder.AssignedTo.setText("Updated By: " + mAssignedTo.get(position));
            ticketHolder.CreadteDate.setText("Updated On: " + mCreadteDate.get(position));
            if (attachments.size() > 0) {
                if (attachments.get(position).isEmpty()) {
                    ticketHolder.IssueID.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    ticketHolder.IssueID.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attachment, 0);
                }
            } else {
                ticketHolder.IssueID.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
