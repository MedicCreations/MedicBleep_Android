package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.api.robospice.LocationSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GPSTracker;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.octo.android.robospice.persistence.exception.SpiceException;

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

			sendLocation.setImageResource(android.R.drawable.ic_menu_directions);

			sendLocation.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					getRoute(latitude, longitude);
				}
			});

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

						getAddress();
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

		getAddress();
	}

	private void sendMsg() {
		String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
		String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);

		handleProgress(true);
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(Const.MSG_TYPE_LOCATION, chatId, null, null, null, String.valueOf(longitude), String.valueOf(latitude),
				rootId, messageId, this);
		spiceManager.execute(sendMessage, new CustomSpiceListener<Integer>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, LocationActivity.this);
			}

			@Override
			public void onRequestSuccess(Integer result) {
				handleProgress(false);

				AppDialog dialog = new AppDialog(LocationActivity.this, true);

				if (result == Const.API_SUCCESS) {
					dialog.setSucceed();
				} else {
					dialog.setFailed(result);
				}
			}
		});
	}
	
	private void getAddress(){
		
		LocationSpice.GetAddress getAddress = new LocationSpice.GetAddress(latitude, longitude, this);
		spiceManager.execute(getAddress, new CustomSpiceListener<String>(){
			
			@Override
			public void onRequestSuccess(String address) {
				super.onRequestSuccess(address);
				
				if (address != null) {
					locationAddress.setText(address);
				}
			}
		});
	}

	private void getRoute(double latitude, double longintude) {

		String url = "http://maps.google.com/maps?daddr=" + latitude + "," + longintude + "&hl=zh&t=m&dirflg=d";

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mGpsTracker != null) {
			mGpsTracker.stopUsingGPS();
		}
	}

}