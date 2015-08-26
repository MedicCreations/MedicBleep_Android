package com.medicbleep.app.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.medicbleep.app.chat.api.robospice.ChatSpice;
import com.medicbleep.app.chat.api.robospice.LocationSpice;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.models.SendMessageResponse;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.GoogleUtils;
import com.medicbleep.app.chat.utils.LocationUtility;
import com.medicbleep.app.chat.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
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

	private Bitmap mMapPinBlue;
	private Marker mapMarker;
	private MarkerOptions markerOfUser;

	private String chatId;
	private double latitude = 0;
	private double longitude = 0;

	private ImageButton goBack;
	private TextView locationAddress;
	private ImageButton sendLocation;

	boolean isShare = true;

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

		Bundle extras = getIntent().getExtras();

		chatId = extras.getString(Const.CHAT_ID);

		if (extras.containsKey(Const.LATITUDE)) {

			isShare = false;

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

				latitude = LocationUtility.getInstance().getLocation().getLatitude();
				longitude = LocationUtility.getInstance().getLocation().getLongitude();

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
		}

		getAddress();
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverImplementation);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isShare) {
			IntentFilter intentFilter = new IntentFilter(LocationUtility.LOCATION_UPDATED);
			LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverImplementation, intentFilter);
			updateLocation();
		}
	}

	private void sendMsg() {
		String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
		String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);

		handleProgress(true);
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(Const.MSG_TYPE_LOCATION, chatId, null, null, null, String.valueOf(longitude), String.valueOf(latitude),
				rootId, messageId);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, LocationActivity.this, ex);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				handleProgress(false);

				AppDialog dialog = new AppDialog(LocationActivity.this, true);

				if (result.getCode() == Const.API_SUCCESS) {
					dialog.setSucceed();
				} else {
					dialog.setFailed(result.getCode());
				}
			}
		});
	}
	
	private void getAddress(){
		
		LocationSpice.GetAddress getAddress = new LocationSpice.GetAddress(latitude, longitude, LocationSpice.RETURN_TYPE_ADRESS, this);
		spiceManager.execute(getAddress, new CustomSpiceListener<String>() {

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

	BroadcastReceiver broadcastReceiverImplementation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(LocationUtility.LOCATION_UPDATED)) {
				updateLocation();
			}
		}
	};

	void updateLocation () {
		Location location = LocationUtility.getInstance().getLocation();

		latitude = location.getLatitude();
		longitude = location.getLongitude();
		LatLng point = new LatLng(latitude, longitude);
		mapMarker.setPosition(point);
		mMap.moveCamera(CameraUpdateFactory.newLatLng(point));

		getAddress();
	}

}