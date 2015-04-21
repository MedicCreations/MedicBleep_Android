package com.clover.spika.enterprise.chat.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
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
    public static LocationUtility getInstance() {
        if (instance == null) {
            instance = new LocationUtility();
        }
        return instance;
    }

    private LocationUtility () {
    }

    public void start (Context appContext) {
        locationRequest = LocationRequest.create();

        client = new GoogleApiClient.Builder(appContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        client.connect();

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
    }

    public void stop () {
        if (client.isConnected()) {
            client.disconnect();
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
    }

    public void getLastLocation () {
        location = LocationServices.FusedLocationApi.getLastLocation(client);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("**** LOCATION ****", "LL: " + latitude + " " + longitude);
        }
        else {
            Log.e("**** LOCATION ****", "NULL");
        }
    }

    public String getCountryCode () {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Log.e("get adderess", addresses + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            countryCode = addresses.get(0).getCountryCode();
        }

        Log.e("HELLO", "Country code: " + countryCode);

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
}
