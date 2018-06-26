package net.mzi.trackengine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutUs extends AppCompatActivity {
    TextView EU,PP,TC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        getSupportActionBar().setTitle("About Us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        EU=(TextView)findViewById(R.id.endUser);
        PP=(TextView)findViewById(R.id.privacyPolicy);
        TC=(TextView)findViewById(R.id.termsCondition);
        EU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AboutUs.this,EULA_PP_TC.class);
                i.putExtra("name","EU");
                startActivity(i);
            }
        });
        PP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AboutUs.this,EULA_PP_TC.class);
                i.putExtra("name","PP");
                startActivity(i);
            }
        });
        TC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AboutUs.this,EULA_PP_TC.class);
                i.putExtra("name","TC");
                startActivity(i);
            }
        });
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
}
