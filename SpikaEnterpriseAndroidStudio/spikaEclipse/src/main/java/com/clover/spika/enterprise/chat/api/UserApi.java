package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
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
