package net.mzi.trackengine;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import net.mzi.trackengine.model.PostUrl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AgentPerformance extends AppCompatActivity {
    String API_URL = null;
    int newTktCounter,completeTktCounter,attendedTktCounter;
    ArrayList<Entry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();
    ArrayList<String> count = new ArrayList<String>();
    SQLiteDatabase sql=null;
    PieChart pieChart;
    PieDataSet dataset;
    PieData data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_performance);
        sql = getApplicationContext().openOrCreateDatabase("MZI.sqlite",getApplicationContext().MODE_PRIVATE,null);
        Cursor cquery = sql.rawQuery("select IssueId from Issue_Detail where IsAccepted = -1", null);
        newTktCounter=cquery.getCount();
        cquery = sql.rawQuery("select IssueId from Issue_Detail where IsAccepted = 2", null);
        attendedTktCounter=cquery.getCount();
        cquery = sql.rawQuery("select IssueId from Issue_Detail where IsAccepted = 3", null);
        completeTktCounter=cquery.getCount();
        API_URL = PostUrl.sUrl+"GetIssueCountSummary?iUserId="+MainActivity.LOGINID;
        getSupportActionBar().setTitle("Performance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        pieChart = (PieChart) findViewById(R.id.chart);
        new AgentPerformance.UserPerformance().execute();
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setXEntrySpace(5);
        l.setYEntrySpace(5);
        l.setTextSize(12);
        // creating data values<br />
        cquery.close();

        pieChart.animateY(5000);
        pieChart.setCenterText("Analysing Performance");
        //pieChart.setData(data); //set data into chart<br />
        pieChart.setClickable(true);
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if(e.getXIndex()==0){
                    Intent i = new Intent(AgentPerformance.this,NewTaskActivity.class);
                    startActivity(i);
                }
                else{
                    String pos=String.valueOf(dataSetIndex);
                    Intent i = new Intent(AgentPerformance.this,TaskActivity.class);
                    i.putExtra("cardpos",pos);
                    startActivity(i);
                }
            }
            @Override
            public void onNothingSelected() {

                Toast.makeText(getApplicationContext(),"Nothing Selected",Toast.LENGTH_LONG).show();
            }
        });

       // pieChart.setDescription("Description");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class UserPerformance extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            entries.clear();
            labels.clear();
            count.clear();
        }
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("FollowUp CLASS,", e.getMessage(), e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == null) {
                s = "THERE WAS AN ERROR";
            }
            Log.i("INFO", s);
            try {
                labels.add("New");
                entries.add(new Entry(newTktCounter,0));
                JSONObject jsonObject = (JSONObject) new JSONTokener(s).nextValue();
                JSONArray jdata = jsonObject.getJSONArray("IssueCountSummary");
                for(int i=0;i<jdata.length();i++)
                {
                    JSONObject object=jdata.getJSONObject(i);
                    labels.add(object.getString("Status"));
                    entries.add(new Entry(object.getInt("IssueCount"),i+1));
                    Log.e("TAG",labels.get(i)+entries.get(i+1) +"\n new "+newTktCounter );
                }
                labels.add("Complete");
                entries.add(new Entry(completeTktCounter,2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataset = new PieDataSet(entries,"");
            dataset.setValueTextSize(14);
            dataset.setValueTypeface(Typeface.SANS_SERIF);
            dataset.setColors(ColorTemplate.COLORFUL_COLORS);
            dataset.setSliceSpace(3);
            dataset.setSelectionShift(3);
            data = new PieData(labels, dataset);
            pieChart.setData(data);
        }
    }
}
