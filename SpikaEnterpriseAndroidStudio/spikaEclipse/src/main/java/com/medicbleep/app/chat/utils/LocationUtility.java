package com.medicbleep.app.chat.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.medicbleep.app.chat.api.robospice.LocationSpice;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.services.robospice.CustomSpiceManager;
import com.medicbleep.app.chat.services.robospice.OkHttpService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Created by mislav on 21/04/15.
 */
public class LocationUtility implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String COUNTRY_CODE_FIRST_TIME_OBTAINED = "COUNTRY_CODE_FIRST_TIME_OBTAINED";
    public static final String COUNTRY_CODE_UPDATED = "COUNTRY_CODE_UPDATED";
    public static final String LOCATION_UPDATED = "LOCATION_UPDATED";
    public static final String LOCATION_SETTINGS_ERROR = "LOCATION_SETTINGS_ERROR";

    public boolean isLocationSettingsStatusOK = false;

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location location;
    private double latitude;
    private double longitude;
    private String countryCode;
    private SpiceManager spiceManager = new CustomSpiceManager(OkHttpService.class);
    private Context appContext;

    private LocalBroadcastManager localBroadcastManager;

    private static LocationUtility instance;

    /**
     * Call this in Application class to instantiate
     * @param appContext
     */
    public static void createInstance (Context appContext) {
        Log.wtf("Instance", "Created");
        if (instance == null) {
            instance = new LocationUtility(appContext);
        }
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
        checkSettings();
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
            Log.i("**** LOCATION ****", "LL: " + latitude + " " + longitude);
            updateCountryCodeAsync();
        }
        else {
            Log.i("**** LOCATION ****", "NULL");
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
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.i("**** LOCATION ****", "CHANGE TO: " + latitude + " " + longitude);
            updateCountryCodeAsync();
            locationUpdated();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Logger.i("**** LOCATION **** API CONNECTED");
        getLastLocation();
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Logger.e("**** LOCATION **** API CONNECTED FAIL");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("**** LOCATION ****", "API SUSPENDED " + i);
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
        Log.i("******************", "LOCATION UPDATE");
        Intent i = new Intent(LOCATION_UPDATED);
        localBroadcastManager.sendBroadcast(i);
    }

    void firstCountryCodeUpdate () {
        Log.i("******************", "COUNRY CODE FIRST: " + countryCode);
        Intent i = new Intent(COUNTRY_CODE_FIRST_TIME_OBTAINED);
        localBroadcastManager.sendBroadcast(i);
        countryCodeUpdate();
    }

    void countryCodeUpdate () {
        Log.i("******************", "COUNTRY CODE: " + countryCode);
        Intent i = new Intent(COUNTRY_CODE_UPDATED);
        localBroadcastManager.sendBroadcast(i);
    }

    void checkSettings () {
        //******* CHECK SETTINGS *******
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                Log.i("**** SETTINGS ****", "SETIINGS RESULT: " + status);
//                final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.i("**** SETTINGS ****", "SETIINGS OK");

                        isLocationSettingsStatusOK = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        Log.i("**** SETTINGS ****", "SETIINGS CHANGE");
                        isLocationSettingsStatusOK = false;

                        Intent i = new Intent(LOCATION_SETTINGS_ERROR);
                        localBroadcastManager.sendBroadcast(i);

//                        Intent intent = new Intent(appContext, AlertDialogActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        appContext.startActivity(intent);

//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            status.startResolutionForResult(
//                                    OuterClass.this,
//                                    REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Logger.e("**** SETTINGS **** SETIINGS FAIL");
                        break;
                }
            }
        });
    }
}
