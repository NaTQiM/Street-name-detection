package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class GPSManager implements LocationListener {
    private Context context;
    private List<onRequestGPSPermission> list_callback;
    private int request_code;
    public boolean isGPSEnable = false;
    private boolean locationAvailable = false;
    private Location location;
    private OnGetLocation onGetLocation = null;

    public GPSManager(Context ctx, int request_code) {
        this.context = ctx;
        list_callback = new ArrayList<onRequestGPSPermission>();
        this.request_code = request_code;
    }

    public boolean isGPSPermissionGranted() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestGPSPermission() {
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                request_code);
    }

    @SuppressLint("MissingPermission")
    private void requestForLocation() {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    protected void onGPSPermissionGranted(boolean result) {
        if (result)
        {
            requestForLocation();
            InvokeGranted();
        }
        else {
            InvokeDenied();
            if (onGetLocation!=null)
                onGetLocation.Failure();
        }
    }

    public void AddRequestGPSPermissionListener(onRequestGPSPermission callback) {
        list_callback.add(callback);
    }

    private void InvokeGranted() {
        for (onRequestGPSPermission callback:
             list_callback) {
            callback.Granted();
        }
    }

    private void InvokeDenied() {
        for (onRequestGPSPermission callback:
                list_callback) {
            callback.Denied();
        }
    }

    public boolean getStatus() {
        return isGPSEnable;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        locationAvailable = true;
        this.location = location;

        if (onGetLocation!=null)
            onGetLocation.Success(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        isGPSEnable = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        isGPSEnable = false;
    }

    public void GetLocation(OnGetLocation callback) {
        if (isGPSEnable)
        {
            if (isGPSPermissionGranted()){
                if (locationAvailable)
                    callback.Success(location);
                else
                {
                    requestForLocation();
                    onGetLocation = callback;
                }
            }
            else {
                onGetLocation = callback;
                requestGPSPermission();
            }
        }
        else
        {
            callback.Failure();
        }

    }

    protected interface OnGetLocation {
        void Success(Location location);
        void Failure();
    }

    protected interface onRequestGPSPermission {
        void Granted();
        void Denied();
    }
}
