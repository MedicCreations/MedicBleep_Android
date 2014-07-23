/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.clover.spika.enterprise.chat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LocationApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.GPSTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * LocationActivity
 * 
 * Shows user current location or other user's previous sent location.
 */

public class LocationActivity extends BaseActivity {

	private GoogleMap mMap;
	private GPSTracker mGpsTracker;

	private Bitmap mMapPinBlue;
	MarkerOptions markerOfUser;

	private double latitude = 0;
	private double longitude = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);

		if (checkGooglePlayServicesForUpdate()) {
			return;
		}

		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		mMapPinBlue = BitmapFactory.decodeResource(getResources(), R.drawable.location_more_icon_active);

		mGpsTracker = new GPSTracker(this);
		if (mGpsTracker.canGetLocation()) {
			latitude = mGpsTracker.getLatitude();
			longitude = mGpsTracker.getLongitude();

			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));

			final Marker myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));

			mMap.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng point) {
					latitude = point.latitude;
					longitude = point.longitude;
					mMap.clear();
					mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));
				}
			});

			mGpsTracker.setOnLocationChangedListener(new OnLocationChangedListener() {

				@Override
				public void onLocationChanged(Location location) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					myMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
				}
			});

		} else {
			mGpsTracker.showSettingsAlert();
		}
	}

	private boolean checkGooglePlayServicesForUpdate() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (status == ConnectionResult.SUCCESS) {
			return false;
		} else {
			Dialog d = GooglePlayServicesUtil.getErrorDialog(status, this, 1337);
			d.setCancelable(false);
			d.show();
			return true;
		}
	}

	private void setLocation(double lat, double lon) {

		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 16));

		markerOfUser = new MarkerOptions().position(new LatLng(lat, lon)).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue));

		mMap.addMarker(markerOfUser);

		new LocationApi().getAddress(lat, lon, this, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				// TODO Auto-generated method stub
			}
		});

		mGpsTracker = new GPSTracker(this);

		if (mGpsTracker.canGetLocation()) {
			double myLat = mGpsTracker.getLatitude();
			double myLon = mGpsTracker.getLongitude();

			mMap.addMarker(new MarkerOptions().position(new LatLng(myLat, myLon)).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_more_icon)));

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
		}
	}

}