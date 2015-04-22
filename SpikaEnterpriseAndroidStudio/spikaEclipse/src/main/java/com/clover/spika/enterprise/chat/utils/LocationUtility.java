package com.clover.spika.enterprise.chat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by mislav on 21/04/15.
 */
public class LocationUtility implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Geocoder geocoder;
    private Location location;
    private double latitude;
    private double longitude;
    private String countryCode;

    private static LocationUtility instance;

    /**
     * Call this in Application class to instantiate
     * @param appContext
     */
    public static void createInstance (Context appContext) {
        instance = new LocationUtility(appContext);
    }

    /**
     * Call createInstance(applicationContext) in Application class before use
     * @return instance
     */
    public static LocationUtility getInstance() {
        return instance;
    }

    private LocationUtility (Context appContext) {

        locationRequest = LocationRequest.create();

        client = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (Geocoder.isPresent()) {
            Log.e("Geocoder", "Present");
            geocoder = new Geocoder(appContext);
            if (geocoder == null) {
                Log.e("Geocoder", "null");
            }
            else {
                Log.e("Geocoder", "instantiated");
            }
        }
        else {
            Log.e("Geocoder", "Not Present");
        }

        IntentFilter intentFilter = new IntentFilter(ApplicationStateManager.APPLICATION_PAUSED);
        intentFilter.addAction(ApplicationStateManager.APPLICATION_RESUMED);
        LocalBroadcastManager.getInstance(appContext).registerReceiver(new BroadcastReceiverImplementation(), intentFilter);
    }

    public void start () {
        client.connect();
    }

    public void stop () {
        if (client.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            client.disconnect();
        }
    }

    public void getLastLocation () {
        location = LocationServices.FusedLocationApi.getLastLocation(client);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("**** LOCATION ****", "LL: " + latitude + " " + longitude);
            updateCountryCode();
        }
        else {
            Log.e("**** LOCATION ****", "NULL");
        }
    }

    private void updateCountryCode () {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Log.e("adderess", addresses + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            String countryCode = addresses.get(0).getCountryCode();
            if (!TextUtils.isEmpty(countryCode)) {
                this.countryCode = countryCode;
                Log.e("UPDATED", "Country code: " + countryCode);
            }
        }
    }

    public String getCountryCode () {
        Log.e("GET", "Country code: " + countryCode);
        return countryCode;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("**** LOCATION ****", "API CONNECTED FAIL");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("**** LOCATION ****", "CHANGE TO: " + latitude + " " + longitude);
            updateCountryCode();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("**** LOCATION ****", "API CONNECTED");
        getLastLocation();
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("**** LOCATION ****", "API SUSPENDED " + i);
    }

    private class BroadcastReceiverImplementation extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ApplicationStateManager.APPLICATION_PAUSED)) {
                stop();
            } else if (intent.getAction().equals(ApplicationStateManager.APPLICATION_RESUMED)) {
                start();
            }
        }
    }
}
