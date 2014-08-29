package com.clover.spika.enterprise.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.LocationApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GPSTracker;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
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

	private ImageButton goBack;
	private TextView locationAddress;
	private ImageButton sendLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);

		if (new GoogleUtils().checkGooglePlayServicesForUpdate(this)) {
			return;
		}

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		locationAddress = (TextView) findViewById(R.id.locationAddress);
		sendLocation = (ImageButton) findViewById(R.id.sendLocation);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMapPinBlue = BitmapFactory.decodeResource(getResources(), R.drawable.location_more_icon_active);

		mGpsTracker = new GPSTracker(this);

		Bundle extras = getIntent().getExtras();

		chatId = extras.getString(Const.CHAT_ID);

		if (extras.containsKey(Const.LATITUDE)) {

			latitude = extras.getDouble(Const.LATITUDE);
			longitude = extras.getDouble(Const.LONGITUDE);

			sendLocation.setVisibility(View.INVISIBLE);

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
        String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
        String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);
		new ChatApi().sendMessage(Const.MSG_TYPE_LOCATION, chatId, null, null, null,
                String.valueOf(longitude), String.valueOf(latitude), rootId, messageId, this, new ApiCallback<Integer>() {

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
		}
	}

}