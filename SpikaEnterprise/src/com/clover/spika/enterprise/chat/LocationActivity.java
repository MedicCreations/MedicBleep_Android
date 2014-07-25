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

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.LocationApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GPSTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.MapFragment;
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
	private Marker mapMarker;
	private MarkerOptions markerOfUser;

	private String chatId;
	private double latitude = 0;
	private double longitude = 0;

	private EditText locationAddress;
	private ImageButton sendLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		disableSidebar();

		if (checkGooglePlayServicesForUpdate()) {
			return;
		}

		locationAddress = (EditText) findViewById(R.id.locationAddress);
		locationAddress.setEnabled(false);
		sendLocation = (ImageButton) findViewById(R.id.sendLocation);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMapPinBlue = BitmapFactory.decodeResource(getResources(), R.drawable.location_more_icon_active);

		mGpsTracker = new GPSTracker(this);

		Bundle extras = getIntent().getExtras();

		chatId = extras.getString(Const.CHAT_ID);

		if (extras.containsKey(Const.LATITUDE)) {

			latitude = extras.getDouble(Const.LATITUDE);
			longitude = extras.getDouble(Const.LONGITUDE);

			sendLocation.setVisibility(View.GONE);

			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
			markerOfUser = new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue));
			mapMarker = mMap.addMarker(markerOfUser);
		} else {
			sendLocation.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					sendMsg();
				}
			});

			if (mGpsTracker.canGetLocation()) {

				latitude = mGpsTracker.getLatitude();
				longitude = mGpsTracker.getLongitude();

				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));

				markerOfUser = new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue));
				mapMarker = mMap.addMarker(markerOfUser);

				mMap.setOnMapClickListener(new OnMapClickListener() {

					@Override
					public void onMapClick(LatLng point) {
						latitude = point.latitude;
						longitude = point.longitude;
						mMap.clear();
						mapMarker = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromBitmap(mMapPinBlue)));

						new LocationApi().getAddress(latitude, longitude, LocationActivity.this, new ApiCallback<String>() {

							@Override
							public void onApiResponse(Result<String> result) {
								if (result.isSuccess()) {
									locationAddress.setText(result.getResultData());
								} else {

								}
							}
						});
					}
				});

				mGpsTracker.setOnLocationChangedListener(new OnLocationChangedListener() {

					@Override
					public void onLocationChanged(Location location) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
						mapMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
					}
				});
			} else {
				mGpsTracker.showSettingsAlert();
			}
		}

		new LocationApi().getAddress(latitude, longitude, this, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				if (result.isSuccess()) {
					locationAddress.setText(result.getResultData());
				} else {

				}
			}
		});
	}

	private void sendMsg() {
		new ChatApi().sendMessage(Const.MSG_TYPE_LOCATION, chatId, null, null, null, String.valueOf(longitude), String.valueOf(latitude), this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {

				AppDialog dialog = new AppDialog(LocationActivity.this, true);

				if (result.isSuccess()) {
					dialog.setSucceed();
				} else {
					dialog.setFailed(result.getResultData());
				}
			}
		});
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
		}
	}

}