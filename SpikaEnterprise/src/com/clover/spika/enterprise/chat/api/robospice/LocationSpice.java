package com.clover.spika.enterprise.chat.api.robospice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;

public class LocationSpice {

	public static class GetAddress extends CustomSpiceRequest<String> {

		private Context ctx;
		private double latitude;
		private double longitude;

		public GetAddress(double latitude, double longitude, Context context) {
			super(String.class);

			this.ctx = context;
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		public String loadDataFromNetwork() throws Exception {

			String finalAddress = "";

			if (Geocoder.isPresent()) {
				Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
				List<Address> addresses = null;

				try {
					addresses = geocoder.getFromLocation(latitude, longitude, 1);

					if (addresses != null && addresses.size() > 0) {
						Address address = addresses.get(0);

						if (address.getMaxAddressLineIndex() > 0) {
							finalAddress = finalAddress + address.getAddressLine(0);
						}

						if (address.getLocality() != null) {
							finalAddress = finalAddress + ", " + address.getLocality();
						}

						if (address.getCountryName() != null) {
							finalAddress = finalAddress + ", " + address.getCountryName();
						}

						return finalAddress;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json";

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put("latlng", String.valueOf(latitude) + "," + String.valueOf(longitude));
				getParams.put("sensor", "false");

				JSONObject googleMapResponse = NetworkManagement.httpGetCustomUrlRequest(googleMapUrl, getParams);

				JSONArray results = (JSONArray) googleMapResponse.get("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);

					if (result.has("formatted_address")) {
						return result.getString("formatted_address");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

}
