package com.appstone.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.slf4j.MarkerFactory;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int LOCATION_PERMISSION = 1093;

    private GoogleMap googleMap;

    private LocationManager lm;


    private double latitude;
    private double longitude;

    private AutoCompleteTextView mAtSearchPlaces;

    private Handler mThreadHandler;

    private PlacesAdapter placesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAtSearchPlaces = findViewById(R.id.act_places);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            iniatiteLocationListener();
        }

        placesAdapter = new PlacesAdapter(MainActivity.this, R.layout.cell_place);
        mAtSearchPlaces.setThreshold(3);
        mAtSearchPlaces.setAdapter(placesAdapter);

        if (mThreadHandler == null) {
            HandlerThread mHandlerThread = new HandlerThread("REGISTER", Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            mThreadHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayList<Place> results = placesAdapter.searchedPlaces;

                                if (results != null && results.size() > 0) {
                                    placesAdapter.notifyDataSetChanged();
                                } else {
                                    placesAdapter.notifyDataSetInvalidated();
                                }
                            }
                        });
                    }
                }
            };
        }

        mAtSearchPlaces.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                mThreadHandler.removeCallbacks(null);
                mThreadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        placesAdapter.searchedPlaces = placesAdapter.searchPlacesAPI.autocomplete(editable.toString());
                        mThreadHandler.sendEmptyMessage(1);
                    }
                }, 500);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void iniatiteLocationListener() {
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6000, 10, this);

        lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onResume() {
        super.onResume();
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            this.googleMap = googleMap;

            googleMap.setMyLocationEnabled(true);

            if (googleMap != null) {
                LatLng currentLocation = new LatLng(latitude, longitude);

                googleMap.addMarker(new MarkerOptions().position(currentLocation)
                        .anchor(0.5f, 0.5f)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_to)));

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 5.5f));
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniatiteLocationListener();
            } else {
                Toast.makeText(MainActivity.this, "Location Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}