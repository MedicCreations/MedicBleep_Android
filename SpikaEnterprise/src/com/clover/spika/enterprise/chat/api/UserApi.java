package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class UserApi {

	public void updateUserImage(final String image, final String thumb, final Context ctx, boolean showProgressBar, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, showProgressBar) {

			@Override
			protected BaseModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.IMAGE, image);
				postParams.put(Const.IMAGE_THUMB, thumb);

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_UPDATE_USER, postParams, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.TOKEN));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), BaseModel.class);
			}

			@Override
			protected void onPostExecute(BaseModel baseModel) {
				super.onPostExecute(baseModel);

				if (listener != null) {
					Result<BaseModel> result;

					if (baseModel != null) {
						if (baseModel.getCode() == Const.API_SUCCESS) {
							result = new Result<BaseModel>(Result.ApiResponseState.SUCCESS);
							result.setResultData(baseModel);
						} else {
							result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
							result.setResultData(baseModel);
						}
					} else {
						result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void updateUserToken(final Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, true) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, "");

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_UPDATE_PUSH_TOKEN, postParams, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.TOKEN));
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), BaseModel.class);
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
