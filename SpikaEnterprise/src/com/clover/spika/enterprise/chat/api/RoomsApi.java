package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.RoomsList;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class RoomsApi {

	public void getRoomsWithPage(final int page, final String cat, Context ctx, boolean showProgressBar,
			final ApiCallback<RoomsList> listener) {
		new BaseAsyncTask<Void, Void, RoomsList>(ctx, showProgressBar) {

			@Override
			protected RoomsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ROOMS, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), RoomsList.class);
			}

			@Override
			protected void onPostExecute(RoomsList roomModel) {
				super.onPostExecute(roomModel);

				if (listener != null) {
					Result<RoomsList> result;

					if (roomModel != null) {
						if (roomModel.getCode() == Const.API_SUCCESS) {
							result = new Result<RoomsList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(roomModel);
						} else {
							result = new Result<RoomsList>(Result.ApiResponseState.FAILURE);
							result.setResultData(roomModel);
						}
					} else {
						result = new Result<RoomsList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void getRoomsByName(final int page, final String cat, final String data, Context ctx, boolean showProgressBar,
			final ApiCallback<RoomsList> listener) {
		new BaseAsyncTask<Void, Void, RoomsList>(ctx, showProgressBar) {

			@Override
			protected RoomsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.SEARCH, data);
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ROOMS, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), RoomsList.class);
			}

			@Override
			protected void onPostExecute(RoomsList rooms) {
				super.onPostExecute(rooms);

				if (listener != null) {
					Result<RoomsList> result;

					if (rooms != null) {
						if (rooms.getCode() == Const.API_SUCCESS) {
							result = new Result<RoomsList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(rooms);
						} else {
							result = new Result<RoomsList>(Result.ApiResponseState.FAILURE);
							result.setResultData(rooms);
						}

					} else {
						result = new Result<RoomsList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
