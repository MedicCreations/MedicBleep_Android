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
import android.util.Log;

import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;

public class LocationSpice {

	public static final int RETURN_TYPE_ADRESS = 0;
	public static final int RETURN_TYPE_COUNTRY_CODE = 1;

	public static class GetAddress extends CustomSpiceRequest<String> {

		private Context ctx;
		private double latitude;
		private double longitude;
		private int returnType;

		public GetAddress(double latitude, double longitude, int returnType, Context context) {
			super(String.class);

			this.ctx = context;
			this.latitude = latitude;
			this.longitude = longitude;
			this.returnType = returnType;
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

						if (returnType == RETURN_TYPE_ADRESS) {
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
						else {
							Log.wtf("GEOCODER", "**************** WORKS ********************");
							return address.getCountryCode();
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Log.wtf("GEOCODER", "**************** FALLBACK ********************");

				String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json";

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put("latlng", String.valueOf(latitude) + "," + String.valueOf(longitude));
				getParams.put("sensor", "false");

				JSONObject googleMapResponse = NetworkManagement.httpGetCustomUrlRequest(googleMapUrl, getParams);
				JSONArray results = (JSONArray) googleMapResponse.get("results");

				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);

					if (returnType == RETURN_TYPE_ADRESS) {
						if (result.has("formatted_address")) {
							return result.getString("formatted_address");
						}
					}
					else {
						if (result.has("address_components")) {
							JSONArray addressComponents = result.getJSONArray("address_components");
							for (int j = 0; j < addressComponents.length(); j++) {
								JSONObject component = addressComponents.getJSONObject(j);
								if (component.has("types")) {
									JSONArray types = component.getJSONArray("types");
									if (types.length() == 2
											&& (types.getString(0).equals("country") || types.getString(1).equals("country"))
											&& (types.getString(0).equals("political") || types.getString(1).equals("political"))
									) {
										if (component.has("short_name")) {
											return component.getString("short_name");
										}
									}
								}
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}

}
