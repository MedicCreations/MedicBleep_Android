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

import com.clover.spika.enterprise.chat.api.robospice.LocationSpice;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceManager;
import com.clover.spika.enterprise.chat.services.robospice.OkHttpService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.io.IOException;
import java.util.List;

/**
 * Created by mislav on 21/04/15.
 */
public class LocationUtility implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String COUNTRY_CODE_FIRST_TIME_OBTAINED = "COUNTRY_CODE_FIRST_TIME_OBTAINED";
    public static final String COUNTRY_CODE_UPDATED = "COUNTRY_CODE_UPDATED";
    public static final String LOCATION_UPDATED = "LOCATION_UPDATED";

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location location;
    private double latitude;
    private double longitude;
    private String countryCode;
    private OnLocationChangeListener onLocationChangeListener;
    private SpiceManager spiceManager = new CustomSpiceManager(OkHttpService.class);
    private Context appContext;

    private LocalBroadcastManager localBroadcastManager;

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

        localBroadcastManager = LocalBroadcastManager.getInstance(appContext);

        locationRequest = LocationRequest.create();

        client = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.appContext = appContext;

        IntentFilter intentFilter = new IntentFilter(ApplicationStateManager.APPLICATION_PAUSED);
        intentFilter.addAction(ApplicationStateManager.APPLICATION_RESUMED);
        LocalBroadcastManager.getInstance(appContext).registerReceiver(new BroadcastReceiverImplementation(), intentFilter);
    }

    public void start () {
        spiceManager.start(appContext);
        client.connect();
    }

    public void stop () {
        spiceManager.shouldStop();
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
            updateCountryCodeAsync();
        }
        else {
            Log.e("**** LOCATION ****", "NULL");
        }
    }

    public String getCountryCode () {
//        Log.e("GET", "Country code: " + countryCode);
//        return "HR";
//        return "US";
//        return "UK";
//        return "JP";
        return countryCode;
    }

    public Location getLocation () {
        return location;
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
            updateCountryCodeAsync();
//            if (onLocationChangeListener != null) {
//                onLocationChangeListener.onLocationChange(location);
//            }
            locationUpdated();
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

    public void setOnLocationChangeListener (OnLocationChangeListener onLocationChangeListener) {
        this.onLocationChangeListener = onLocationChangeListener;
    }

    public void removeOnLocationChangeListener () {
        this.onLocationChangeListener = null;
    }

    public interface OnLocationChangeListener {
        public void onLocationChange(Location location);
    }

    private void updateCountryCodeAsync(){

        LocationSpice.GetAddress getAddress = new LocationSpice.GetAddress(latitude, longitude, LocationSpice.RETURN_TYPE_COUNTRY_CODE, appContext);
        spiceManager.execute(getAddress, new CustomSpiceListener<String>(){
            @Override
            public void onRequestFailure(SpiceException arg0) {
                super.onRequestFailure(arg0);
            }

            @Override
            public void onRequestSuccess(String countryCode) {
                super.onRequestSuccess(countryCode);
                if (TextUtils.isEmpty(LocationUtility.this.countryCode) && !TextUtils.isEmpty(countryCode)) {
                    LocationUtility.this.countryCode = countryCode;
                    firstCountryCodeUpdate();
                }
                else if (!TextUtils.isEmpty(countryCode) && !countryCode.equals(LocationUtility.this.countryCode)) {
                    LocationUtility.this.countryCode = countryCode;
                    countryCodeUpdate();
                }
            }
        });
    }

    void locationUpdated () {
        Log.wtf("******************", "LOCATION UPDATE");
        Intent i = new Intent(LOCATION_UPDATED);
        localBroadcastManager.sendBroadcast(i);
    }

    void firstCountryCodeUpdate () {
        Log.wtf("******************", "COUNRY CODE FIRST: " + countryCode);
        Intent i = new Intent(COUNTRY_CODE_FIRST_TIME_OBTAINED);
        localBroadcastManager.sendBroadcast(i);
        countryCodeUpdate();
    }

    void countryCodeUpdate () {
        Log.wtf("******************", "COUNRY CODE: " + countryCode);
        Intent i = new Intent(COUNTRY_CODE_UPDATED);
        localBroadcastManager.sendBroadcast(i);
    }
}
