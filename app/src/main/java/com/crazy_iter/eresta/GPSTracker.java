package com.crazy_iter.eresta;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

@SuppressLint("Registered")
public class GPSTracker extends Service implements LocationListener {

    private Context context;
    boolean isGPSEnables = false;
    boolean isNetworkEnable = false;
    public boolean canGetLocation = false;

    Location location;
    double lat;
    double lng;

    long DIS = 10;
    long TIME = 1000 * 60;

    protected LocationManager locationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

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
}
