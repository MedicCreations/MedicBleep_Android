package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Result;

public class LocationApi {

	public void getAddress(final double latitude, final double longitude, Context ctx, final ApiCallback<String> listener) {
		new BaseAsyncTask<Void, Void, String>(ctx, false) {

			protected String doInBackground(Void... params) {

				Geocoder geocoder = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = null;
				String finalAddress = "";

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

				return null;
			};

			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				if (listener != null) {
					Result<String> apiResult;

					if (result != null) {
						apiResult = new Result<String>(Result.ApiResponseState.SUCCESS);
						apiResult.setResultData(result);
					} else {
						apiResult = new Result<String>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			};
		}.execute();
	}

}
