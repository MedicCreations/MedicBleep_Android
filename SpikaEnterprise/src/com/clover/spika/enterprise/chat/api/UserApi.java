package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UpdateUserModel;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class UserApi {

	public void updateUserImage(final String image, final String thumb, final Context ctx, boolean showProgressBar,
			final ApiCallback<UpdateUserModel> listener) {
		new BaseAsyncTask<Void, Void, UpdateUserModel>(ctx, showProgressBar) {

			@Override
			protected UpdateUserModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.IMAGE, image);
				getParams.put(Const.IMAGE_THUMB, thumb);

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_UPDATE_USER, getParams, 
							SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.TOKEN));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), UpdateUserModel.class);
			}

			@Override
			protected void onPostExecute(UpdateUserModel baseModel) {
				super.onPostExecute(baseModel);

				if (listener != null) {
					Result<UpdateUserModel> result;

					if (baseModel != null) {
						if (baseModel.getCode() == Const.API_SUCCESS) {
							result = new Result<UpdateUserModel>(Result.ApiResponseState.SUCCESS);
							result.setResultData(baseModel);
						} else {
							result = new Result<UpdateUserModel>(Result.ApiResponseState.FAILURE);
							result.setResultData(baseModel);
						}
					} else {
						result = new Result<UpdateUserModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
}
