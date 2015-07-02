package com.medicbleep.app.chat.webrtc.socket;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

import com.medicbleep.app.chat.api.ApiCallback;
import com.medicbleep.app.chat.extendables.BaseAsyncTask;
import com.medicbleep.app.chat.models.PreLogin;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.networking.NetworkManagement;
import com.medicbleep.app.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketClient {

	public void getSessionId(boolean showProgressBar, final ApiCallback<String> listener) {
		new BaseAsyncTask<Void, Void, String>(null, false) {

			@Override
			protected String doInBackground(Void... params) {

				try {
					return NetworkManagement.httpGetRequestWithRawResponse(Const.WS_GET_SESSION_URL);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String session) {
				super.onPostExecute(session);

				if (listener != null) {
					Result<String> result;
					
					result = new Result<String>(Result.ApiResponseState.SUCCESS);
					result.setResultData(session);

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
	
	public void fakeApiForSSL(final  Context ctx, final ApiCallback<PreLogin> listener) {

		new BaseAsyncTask<Void, Void, PreLogin>(ctx, false) {

			protected PreLogin doInBackground(Void... params) {
				try {
					HashMap<String, String> requestParams = new HashMap<String, String>();

					requestParams.put(Const.USERNAME, "FAKE");
					requestParams.put(Const.PASSWORD, "FAKE");

					PreLogin preLogin = new ObjectMapper().readValue(NetworkManagement.httpPostRequest(Const.F_PRELOGIN, requestParams, null), PreLogin.class);

					return preLogin;
				} catch (IOException e) {
					e.printStackTrace();
				} 

				return null;
			}

			protected void onPostExecute(PreLogin preLogin) {
				super.onPostExecute(preLogin);

				if (listener != null) {
					Result<PreLogin> apiResult;

					if (preLogin != null) {
						if (preLogin.getCode() == Const.API_SUCCESS) {
							apiResult = new Result<PreLogin>(Result.ApiResponseState.SUCCESS);
							apiResult.setResultData(preLogin);
							
						} else {
							apiResult = new Result<PreLogin>(Result.ApiResponseState.FAILURE);
							apiResult.setResultData(preLogin);
						}
					} else {
						apiResult = new Result<PreLogin>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			}

		}.execute();
	}

}
