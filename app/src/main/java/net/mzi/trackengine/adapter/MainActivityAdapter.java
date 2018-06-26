package net.mzi.trackengine.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.mzi.trackengine.NewTaskActivity;
import net.mzi.trackengine.R;
import net.mzi.trackengine.TaskActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roam12 on 11/28/2016.
 */
public class MainActivityAdapter extends RecyclerView.Adapter<MainActivityAdapter.ViewHolder> {
    private List<String> mDataSet=new ArrayList<String>();
    List<String> mDatasetCount=new ArrayList<String>();
    //private String[] mDataSetCount;
    private List<Integer> mDataSetTypes;
    List<Integer> mCardColor=new ArrayList<Integer>();
    Context mContext;


    public MainActivityAdapter(){

    }
    public MainActivityAdapter(List<String> mDataset, List<String> dataSetCount, List<Integer> cardColor, List<Integer> dataSetTypes, Context current) {
        mDataSet = mDataset;
        mDatasetCount=dataSetCount;
        mDataSetTypes = dataSetTypes;
        mCardColor=cardColor;
        this.mContext=current;
    }

    public class TaskViewHolder extends ViewHolder {
        TextView task,taskCount;
        RelativeLayout r1;

        public TaskViewHolder(View v) {
            super(v);
            this.task = (TextView) v.findViewById(R.id.task);
            this.taskCount=(TextView)v.findViewById(R.id.taskCounter);
            this.r1=(RelativeLayout)v.findViewById(R.id.r1);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taskcard, parent, false);
        return new TaskViewHolder(v);
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        TaskViewHolder taskholder = (TaskViewHolder) holder;

        taskholder.task.setText(mDataSet.get(position));

        taskholder.r1.setBackgroundResource(mCardColor.get(position));
        taskholder.taskCount.setText(mDatasetCount.get(position));

        }

    @Override
    public int getItemCount() {
        return mDataSetTypes.size();
    }
    @Override
    public int getItemViewType(int position) {
        return mDataSetTypes.get(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            View v=itemView;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos=getAdapterPosition();
                    if(pos==0){
                        Intent i = new Intent(view.getContext(), NewTaskActivity.class);
                        view.getContext().startActivity(i);
                    }else{
                        Intent i = new Intent(view.getContext(), TaskActivity.class);
                        i.putExtra("cardpos", String.valueOf(pos));
                        view.getContext().startActivity(i);
                    }
                }
            });
        }
    }
}