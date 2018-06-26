package net.mzi.trackengine;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class EULA_PP_TC extends AppCompatActivity {
    WebView eulaWebView;
    private String htmlFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eula_pp_tc);
        Bundle bundle = getIntent().getExtras();
        if(bundle.getString("name")!= null) {
            if(bundle.getString("name").equals("EU")){
                getSupportActionBar().setTitle("End User License");
                htmlFilename="EULA.html";
            }else if(bundle.getString("name").equals("TC")){
                getSupportActionBar().setTitle("Terms and Conditions");
                htmlFilename="tc.html";
            }else if (bundle.getString("name").equals("PP")){
                getSupportActionBar().setTitle("Privacy Policy");
                htmlFilename="pp.html";
            }
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        eulaWebView=(WebView)findViewById(R.id.eula);
        eulaWebView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings webSettings=eulaWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(true);
        AssetManager mgr=getBaseContext().getAssets();
        try {
            InputStream inputStream=mgr.open(htmlFilename,AssetManager.ACCESS_BUFFER);
            String htmlContent=StreamToString(inputStream);
            inputStream.close();
            eulaWebView.loadDataWithBaseURL(null,htmlContent,"text/html","utf-8",null);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public static String StreamToString(InputStream inputStream) throws  IOException {
        if(inputStream==null){
            return "";
        }
        Writer writer=new StringWriter();
        char[] buffer=new char[1024];
        try {
            Reader reader=new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            int n;
            while ((n=reader.read(buffer))!=-1) {
                writer.write(buffer, 0, n);
            }
        }
        finally {
        }
        return writer.toString();
    }
}
