package net.mzi.trackengine.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.mzi.trackengine.model.InternalIssueClass;
import net.mzi.trackengine.InternalIssueInfo;
import net.mzi.trackengine.model.PostUrl;
import net.mzi.trackengine.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Poonam on 1/12/2018.
 */

public class ComplainAdapter extends RecyclerView.Adapter<ComplainAdapter.ViewHolder>  {
    List<InternalIssueClass> lComplainData=new ArrayList<InternalIssueClass>();
    Context ctx;
    String sUserId,sUserType;
    public ComplainAdapter(List<InternalIssueClass> lComplainData,Context ctx) {
        this.lComplainData=lComplainData;
        this.ctx=ctx;
    }

    public void ComplainAdapter(){

    }
    private class ComlpainViewHolder extends ComplainAdapter.ViewHolder {
        TextView tCreatedDate,tIssueId,tDescription,tStatus,tLastModified;
        EditText eComment;
        ImageView ivSendComment;
        public ComlpainViewHolder(View itemView) {
            super(itemView);
            this.tCreatedDate=(TextView)itemView.findViewById(R.id.date);
            this.tIssueId=(TextView)itemView.findViewById(R.id.issueId_Id);
            this.tDescription=(TextView)itemView.findViewById(R.id.desc_Id);
            this.tStatus=(TextView)itemView.findViewById(R.id.status);
            this.tLastModified=(TextView)itemView.findViewById(R.id.lastmodifieddate);
            this.eComment=(EditText)itemView.findViewById(R.id.commentEditText);
            this.ivSendComment=(ImageView)itemView.findViewById(R.id.sendImageView);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        SharedPreferences pref = null;
        pref = ctx.getSharedPreferences("login", 0);
        sUserId = pref.getString("userid", "userid");
        sUserType = pref.getString("UserType", "UserType");

        //ctx=parent.getContext();
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.internal_issue_card, parent, false);
        return new ComlpainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ComlpainViewHolder oComlpainViewHolder= (ComlpainViewHolder) holder;
        oComlpainViewHolder.tIssueId.setText(lComplainData.get(position).IssueID);
        oComlpainViewHolder.tLastModified.setText(lComplainData.get(position).UpdatedDate);
        oComlpainViewHolder.tCreatedDate.setText(lComplainData.get(position).CreatedDate);
        oComlpainViewHolder.tStatus.setText(lComplainData.get(position).StatusName);
        oComlpainViewHolder.tDescription.setText(lComplainData.get(position).IssueText);
        oComlpainViewHolder.ivSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,String> postComment=new HashMap<String, String>();
                String sCommentData=oComlpainViewHolder.eComment.getText().toString();
                if(sCommentData.equals("")){
                    oComlpainViewHolder.eComment.setError("Enter Comment !!!!");
                }else {
                    postComment.put("ComplainId", lComplainData.get(position).IssueID);
                    postComment.put("UserId", sUserId);
                    postComment.put("UserType", sUserType);
                    postComment.put("Comment", sCommentData);
                    postComment.put("UpdateSource", "M");
                    postComment.put("InternalOnly", "false");
                    postComment.put("ComplainStatus", "0");
                    String tktStatus = new Gson().toJson(postComment);
                    Date cDate = new Date();
                    String currentDateTimeString = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(cDate);
                /*{"ComplainId":4,"UserId":"5064","UserType":"C","Comment":"Resolved",
                "UpdateSource":"M","InternalOnly":false,"ComplainStatus":2}*/
                    new SendComment(tktStatus).execute();
                    oComlpainViewHolder.eComment.setText("");
                    oComlpainViewHolder.tLastModified.setText(currentDateTimeString);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return lComplainData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(),InternalIssueInfo.class);
                    i.putExtra("ID",lComplainData.get(getAdapterPosition()).IssueID);
                    ctx.startActivity(i);
                }
            });
        }
    }

    public class SendComment extends AsyncTask<String, Void, String> {
        String jsonString;
        SQLiteDatabase sql;

        public SendComment( String jsonString) {
            this.jsonString = jsonString;
            Log.e("onPostExecute: ", jsonString);
        }
        @Override
        protected String doInBackground(String... params) {
            //String uri = PostUrl.sUrl + "PostTicketStatus";
            String uri = PostUrl.sUrl+"PostComplainStatus";
            String result = "";
            try {
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(uri).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                //Call parserUsuarioJson() inside write(),Make sure it is returning proper json string .
                writer.write(jsonString);
                writer.close();
                outputStream.close();

                //Read
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();
                result = sb.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (RejectedExecutionException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.e("onPostExecute: ", s);
            super.onPostExecute(s);

            try{Toast.makeText(ctx, "Send Successfully!!!", Toast.LENGTH_SHORT).show();}catch (Exception e){}
        }
    }
}
