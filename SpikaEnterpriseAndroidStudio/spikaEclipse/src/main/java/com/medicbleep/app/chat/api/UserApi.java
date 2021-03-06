package com.medicbleep.app.chat.api;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

import com.medicbleep.app.chat.extendables.BaseAsyncTask;
import com.medicbleep.app.chat.extendables.BaseModel;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.networking.NetworkManagement;
import com.medicbleep.app.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserApi {

	public void updateUserToken(final Context ctx, final ApiCallback<BaseModel> listener) {

		new BaseAsyncTask<Void, Void, BaseModel>(ctx, false) {

			@Override
			protected BaseModel doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, SpikaEnterpriseApp.getSharedPreferences().getCustomString(Const.PUSH_TOKEN_LOCAL));

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_UPDATE_PUSH_TOKEN, postParams, SpikaEnterpriseApp.getSharedPreferences().getToken());
					ObjectMapper mapper = new ObjectMapper();

					BaseModel result = mapper.readValue(responseBody, BaseModel.class);
					return result;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(BaseModel baseModel) {
				super.onPostExecute(baseModel);

				if (listener != null) {

					Result<BaseModel> result;

					if (baseModel != null) {

						if (baseModel.getCode() == Const.API_SUCCESS) {
							result = new Result<BaseModel>(Result.ApiResponseState.SUCCESS);
						} else {
							result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
						}
					} else {
						result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
