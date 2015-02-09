package com.clover.spika.enterprise.chat.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;

public class GlobalApi {

	public void globalSearch(Context ctx, final int page, final String chatId, final String categoryId, final int type, final String searchTerm, boolean showProgressBar,
			final ApiCallback<GlobalResponse> listener) {

		new BaseAsyncTask<Void, Void, GlobalResponse>(ctx, showProgressBar) {

			@Override
			protected GlobalResponse doInBackground(Void... params) {

				HashMap<String, String> requestParams = new HashMap<String, String>();

				requestParams.put(Const.PAGE, String.valueOf(page));
				requestParams.put(Const.TYPE, String.valueOf(type));

				if (!TextUtils.isEmpty(chatId)) {
					requestParams.put(Const.CHAT_ID, chatId);
				}

				if (!TextUtils.isEmpty(categoryId)) {
					requestParams.put(Const.CATEGORY_ID, categoryId);
				}

				if (!TextUtils.isEmpty(searchTerm)) {
					requestParams.put(Const.SEARCH, searchTerm);
				}

				try {

					JSONObject jsonObject = NetworkManagement.httpGetRequest(Const.F_GLOBAL_SEARCH_URL, requestParams, SpikaEnterpriseApp.getSharedPreferences(context)
							.getToken());

					int code = jsonObject.getInt(Const.CODE);
					if (code == Const.API_SUCCESS) {

						GlobalResponse response = new GlobalResponse();
						response.setCode(code);
						response.setPage(jsonObject.getInt(Const.PAGE));
						response.setTotalCount(jsonObject.getInt(Const.TOTAL_COUNT));

						List<GlobalModel> globalModels = new ArrayList<GlobalModel>();

						JSONArray jsonArray = jsonObject.getJSONArray(Const.SEARCH_RESULT);
						for (int i = 0; i < jsonArray.length(); i++) {
							GlobalModel item = new GlobalModel((JSONObject) jsonArray.get(i));
							globalModels.add(item);
						}

						response.setModelsList(globalModels);
						return response;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(GlobalResponse response) {
				super.onPostExecute(response);

				if (listener != null) {

					Result<GlobalResponse> result;

					if (response != null) {

						if (response.getCode() == Const.API_SUCCESS) {

							result = new Result<GlobalResponse>(Result.ApiResponseState.SUCCESS);
							result.setResultData(response);
						} else {

							result = new Result<GlobalResponse>(Result.ApiResponseState.FAILURE);
							result.setResultData(response);
						}
					} else {
						result = new Result<GlobalResponse>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}

		}.execute();
	}

	public void globalMembers(Context ctx, final int type, final String chatId, final String groupId, final int page, boolean showProgressBar,
			final ApiCallback<GlobalResponse> listener) {

		new BaseAsyncTask<Void, Void, GlobalResponse>(ctx, showProgressBar) {

			@Override
			protected GlobalResponse doInBackground(Void... params) {

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(Const.PAGE, String.valueOf(page));
				requestParams.put(Const.TYPE, String.valueOf(type));

				if (!TextUtils.isEmpty(chatId)) {
					requestParams.put(Const.CHAT_ID, chatId);
				}

				if (!TextUtils.isEmpty(groupId)) {
					requestParams.put(Const.GROUP_ID, groupId);
				}

				try {

					JSONObject jsonObject = NetworkManagement.httpGetRequest(Const.F_GLOBAL_MEMBERS_URL, requestParams, SpikaEnterpriseApp.getSharedPreferences(context)
							.getToken());

					int code = jsonObject.getInt(Const.CODE);
					if (code == Const.API_SUCCESS) {

						GlobalResponse response = new GlobalResponse();
						response.setCode(code);
						response.setPage(jsonObject.getInt(Const.PAGE));
						response.setTotalCount(jsonObject.getInt(Const.TOTAL_COUNT));

						List<GlobalModel> globalModels = new ArrayList<GlobalModel>();

						JSONArray jsonArray = jsonObject.getJSONArray(Const.MEMBERS_RESULT);
						for (int i = 0; i < jsonArray.length(); i++) {
							GlobalModel item = new GlobalModel((JSONObject) jsonArray.get(i));
							globalModels.add(item);
						}

						response.setModelsList(globalModels);
						return response;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(GlobalResponse response) {
				super.onPostExecute(response);

				if (listener != null) {

					Result<GlobalResponse> result;

					if (response != null) {
						if (response.getCode() == Const.API_SUCCESS) {
							result = new Result<GlobalResponse>(Result.ApiResponseState.SUCCESS);
							result.setResultData(response);
						} else {
							result = new Result<GlobalResponse>(Result.ApiResponseState.FAILURE);
							result.setResultData(response);
						}
					} else {
						result = new Result<GlobalResponse>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
}
