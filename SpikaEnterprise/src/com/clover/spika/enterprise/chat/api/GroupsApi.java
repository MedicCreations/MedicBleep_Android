package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GroupMembersList;
import com.clover.spika.enterprise.chat.models.GroupsList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class GroupsApi {

	public void getGroupsWithPage(final int page, final String cat, Context ctx, boolean showProgressBar,
			final ApiCallback<GroupsList> listener) {
		new BaseAsyncTask<Void, Void, GroupsList>(ctx, showProgressBar) {

			@Override
			protected GroupsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_GROUPS, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), GroupsList.class);
			}

			@Override
			protected void onPostExecute(GroupsList groupModel) {
				super.onPostExecute(groupModel);

				if (listener != null) {
					Result<GroupsList> result;

					if (groupModel != null) {
						if (groupModel.getCode() == Const.API_SUCCESS) {
							result = new Result<GroupsList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(groupModel);
						} else {
							result = new Result<GroupsList>(Result.ApiResponseState.FAILURE);
							result.setResultData(groupModel);
						}
					} else {
						result = new Result<GroupsList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void getGroupsByName(final int page, final String cat, final String data, Context ctx, boolean showProgressBar,
			final ApiCallback<GroupsList> listener) {
		new BaseAsyncTask<Void, Void, GroupsList>(ctx, showProgressBar) {

			@Override
			protected GroupsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.SEARCH, data);
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_GROUPS, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), GroupsList.class);
			}

			@Override
			protected void onPostExecute(GroupsList groups) {
				super.onPostExecute(groups);

				if (listener != null) {
					Result<GroupsList> result;

					if (groups != null) {
						if (groups.getCode() == Const.API_SUCCESS) {
							result = new Result<GroupsList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(groups);
						} else {
							result = new Result<GroupsList>(Result.ApiResponseState.FAILURE);
							result.setResultData(groups);
						}

					} else {
						result = new Result<GroupsList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
	
	public void getGroupMembers(final String page, final String groupId, Context ctx, boolean showProgressBar,
			final ApiCallback<GroupMembersList> listener) {
		new BaseAsyncTask<Void, Void, GroupMembersList>(ctx, showProgressBar) {

			@Override
			protected GroupMembersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.GROUP_ID, groupId);
				getParams.put(Const.PAGE, page);

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_GROUP_MEMBERS, getParams,
							SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), GroupMembersList.class);
			}

			@Override
			protected void onPostExecute(GroupMembersList groups) {
				super.onPostExecute(groups);

				if (listener != null) {
					Result<GroupMembersList> result;

					if (groups != null) {
						if (groups.getCode() == Const.API_SUCCESS) {
							result = new Result<GroupMembersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(groups);
						} else {
							result = new Result<GroupMembersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(groups);
						}

					} else {
						result = new Result<GroupMembersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
