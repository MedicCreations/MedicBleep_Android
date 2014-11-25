package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LobbyApi {

	public void getLobbyByType(final int page, final int type, Context ctx, boolean showProgressBar,
			final ApiCallback<LobbyModel> listener) {
		new BaseAsyncTask<Void, Void, LobbyModel>(ctx, showProgressBar) {

			@Override
			protected LobbyModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.TYPE, String.valueOf(type));

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_LOBBY, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				LobbyModel lobbyModel = new LobbyModel();
				if (jsonObject != null) {
					lobbyModel = new Gson().fromJson(jsonObject.toString(), LobbyModel.class);
				}
				
				return lobbyModel;
			}

			@Override
			protected void onPostExecute(LobbyModel model) {
				super.onPostExecute(model);

				if (listener != null) {
					Result<LobbyModel> result;

					if (model != null) {
						if (model.getCode() == Const.API_SUCCESS) {
							result = new Result<LobbyModel>(Result.ApiResponseState.SUCCESS);
							result.setResultData(model);
						} else {
							result = new Result<LobbyModel>(Result.ApiResponseState.FAILURE);
							result.setResultData(model);
						}
					} else {
						result = new Result<LobbyModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
	
}
