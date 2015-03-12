package com.clover.spika.enterprise.chat.api;

import android.content.Context;
import android.os.AsyncTask;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.LocalPush;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

public class LocalPushApi {

	public void getPush(final Context ctx, final ApiCallback<LocalPush> listener) {
		new AsyncTask<Void, Void, LocalPush>() {

			@Override
			protected LocalPush doInBackground(Void... params) {

				try {

					String responseBody = NetworkManagement.httpGetRequest(Const.F_USER_PUSH, new HashMap<String, String>(), SpikaEnterpriseApp.getSharedPreferences(ctx)
							.getToken());
					return new ObjectMapper().readValue(responseBody, LocalPush.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(LocalPush model) {
				super.onPostExecute(model);

				if (listener != null) {
					Result<LocalPush> result;

					if (model != null) {
						if (model.getCode() == Const.API_SUCCESS) {
							result = new Result<LocalPush>(Result.ApiResponseState.SUCCESS);
							result.setResultData(model);
						} else {
							result = new Result<LocalPush>(Result.ApiResponseState.FAILURE);
							result.setResultData(model);
						}
					} else {
						result = new Result<LocalPush>(Result.ApiResponseState.FAILURE);
						model = new LocalPush();
						model.setCode(Const.E_SOMETHING_WENT_WRONG);
						result.setResultData(model);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
