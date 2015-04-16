package com.clover.spika.enterprise.chat.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import com.google.ads.AdRequest;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;

/**
 * GPSTracker
 * 
 * Tracks location updates based on a GPS location provider.
 */

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	// Declaring a Location Manager
	protected LocationManager mLocationManager;

	private OnLocationChangedListener mOnLocationChangedListener;

	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	public Location getLocation() {
		try {
			mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					if (mLocationManager != null) {
						location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				else if (isGPSEnabled) {
					if (location == null) {
						mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						if (mLocationManager != null) {
							location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public Location getCurrentLocation() {
		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(GPSTracker.this);
		}
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();

			if (mOnLocationChangedListener != null)
				mOnLocationChangedListener.onLocationChanged(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void setOnLocationChangedListener(OnLocationChangedListener listener) {
		mOnLocationChangedListener = listener;
	}

	public interface LocationChangeListener {
		public void onLocationChange(Location loc);
	}

	public String getCountryCode () {
		String countryCode = "";
		List<Address> addresses = null;
		Geocoder geoCoder = new Geocoder(mContext);
		try {
			addresses = geoCoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (addresses != null && addresses.size() > 0) {
			countryCode = addresses.get(0).getCountryCode();
		}
		return countryCode;
	}
}
