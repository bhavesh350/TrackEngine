package net.mzi.trackengine;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class FullScreenMap extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    Gps gps;
    Double lat, lon;
    SQLiteDatabase sql;
    Cursor cquery;
    private ArrayList<LatLng> listLatLng = new ArrayList<LatLng>();
    private ArrayList<String> corporatename = new ArrayList<String>();
    private ArrayList<String> issueId = new ArrayList<String>();
    private ArrayList<String> issueAddress = new ArrayList<String>();
    Button exit;
    private Marker mLoc;
    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_map);
        gps = new Gps(getApplicationContext());
        exit = (Button) findViewById(R.id.exitFromMap);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                sql = openOrCreateDatabase("MZI.sqlite", MODE_PRIVATE, null);
                cquery = sql.rawQuery("select IssueId,Address,Latitude,Longitude,CorporateName from Issue_Detail", null);
                for (cquery.moveToFirst(); !cquery.isAfterLast(); cquery.moveToNext()) {

                    issueId.add(cquery.getString(0).toString());
                    corporatename.add(cquery.getString(4).toString());
                    issueAddress.add(cquery.getString(1).toString());
                    if (cquery.getDouble(2) == 0.0) {
                        Geocoder coder = new Geocoder(FullScreenMap.this);
                        List<Address> address;

                        try {
                            address = coder.getFromLocationName(cquery.getString(1).toString(), 5);
                            if (address == null) {
                            }
                            Address location = address.get(0);
                            lat = location.getLatitude();
                            lon = location.getLongitude();


                        } catch (Exception e) {
                            Log.e("FullScreenMap", "exception" + e.getMessage());
                            lat = cquery.getDouble(2);
                            lon = cquery.getDouble(3);
                        }

                    } else {
                        lat = cquery.getDouble(2);
                        lon = cquery.getDouble(3);
                    }

                    listLatLng.add(new LatLng(lat, lon));
                    Log.e("TAG", "onCreate: FullScreenMap" + lat);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SupportMapFragment mapFragment =
                                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(FullScreenMap.this);
                        } catch (Exception e) {
                        }
                    }
                });
            }
        }).start();
        MyApp.showMassage(FullScreenMap.this, "Loading data...");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(FullScreenMap.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        mMap = googleMap;
        // Add some markers to the map, and add a data object to each marker.
        for (int i = 0; i < listLatLng.size(); i++) {
            Log.e("TAG", "onMapReady: " + i + listLatLng.get(i).toString());
            mLoc = mMap.addMarker(new MarkerOptions()
                    .position(listLatLng.get(i))
                    .title("Ticket:" + issueId.get(i))
                    .snippet(issueAddress.get(i)));
            mLoc.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.taskicon));
            mLoc.setTag(0);
            builder.include(mLoc.getPosition());
        }
        LatLng lCurrentUserLocation;
        lCurrentUserLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(lCurrentUserLocation)
                .title("User Info")
                .snippet("kjfg"))
                .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.newicon_blue));
        builder.include(lCurrentUserLocation);
       /* mPerth = mMap.addMarker(new MarkerOptions()
                .position(PERTH)
                .title("Perth"));
        mPerth.setTag(0);
        mSydney = mMap.addMarker(new MarkerOptions()
                .position(SYDNEY)
                .title("Sydney"));
        mSydney.setTag(0);
        mBrisbane = mMap.addMarker(new MarkerOptions()
                .position(BRISBANE)
                .title("Brisbane"));
        mBrisbane.setTag(0);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mPerth.getPosition());
        builder.include(mSydney.getPosition());
        builder.include(mBrisbane.getPosition());*/
        LatLngBounds bounds = builder.build();
        int padding = 0; // offset from edges of the map in pixels
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        //end of new code
        googleMap.animateCamera(cu);

        //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
        /*CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);*/
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Integer clickCount = (Integer) marker.getTag();
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            try {
                Toast.makeText(this,
                        marker.getTitle() +
                                " has been clicked " + clickCount + " times.",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
            }
        }
        return false;
    }
}
