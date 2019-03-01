package net.mzi.trackengine;

import android.app.DatePickerDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import net.mzi.trackengine.adapter.VoucherTabsPagerAdapter;

import java.util.Calendar;

public class VouchersActivity extends AppCompatActivity  implements ActionBar.TabListener {
    private ViewPager viewPager;
    private VoucherTabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private int mYear, mMonth, mDay;
    Button bFromDate,bToDate,bProceed;
    private String[] icons ={"Pending","Paid"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vouchers);
        bFromDate= findViewById(R.id.IdFromdate);
        bToDate= findViewById(R.id.idToDate);
        bProceed= findViewById(R.id.idProceed);

        getSupportActionBar().setTitle("Voucher Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        viewPager = findViewById(R.id.Voucherpager);
        bFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(VouchersActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                bFromDate.setText((year + "-" + (monthOfYear + 1) + "-" + dayOfMonth));
                                Log.e("fgfgfgfh",(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year));
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        bToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(VouchersActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                bToDate.setText((year + "-" + (monthOfYear + 1) + "-" + dayOfMonth));
                                Log.e("Voucher:",(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year));
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        bProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionBar = getSupportActionBar();
                mAdapter = new VoucherTabsPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(mAdapter);
            }
        });
        actionBar = getSupportActionBar();
      /*  mAdapter = new VoucherTabsPagerAdapter(getSupportFragmentManager());*/
        //viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Adding Tabs
        for (String tab_name : icons) {
            actionBar.addTab(actionBar.newTab()
                    .setText(tab_name)
                    .setTabListener(this));
        }
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

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
