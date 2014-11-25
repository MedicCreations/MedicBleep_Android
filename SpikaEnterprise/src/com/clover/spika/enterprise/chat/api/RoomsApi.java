package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.RoomsList;
import com.clover.spika.enterprise.chat.models.UsersAndGroupsList;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class RoomsApi {

	public void getRoomsWithPage(final int page, final String cat, Context ctx, boolean showProgressBar, final ApiCallback<RoomsList> listener) {
		new BaseAsyncTask<Void, Void, RoomsList>(ctx, showProgressBar) {

			@Override
			protected RoomsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ROOMS, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
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

	public void getRoomsByName(final int page, final String cat, final String data, Context ctx, boolean showProgressBar, final ApiCallback<RoomsList> listener) {
		new BaseAsyncTask<Void, Void, RoomsList>(ctx, showProgressBar) {

			@Override
			protected RoomsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.SEARCH, data);
				getParams.put(Const.CATEGORY_ID, String.valueOf(cat));

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ROOMS, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
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

	public void getUsersAndGroupsForRoomsByName(final int page, final String data, Context ctx, boolean showProgressBar, final ApiCallback<UsersAndGroupsList> listener) {
		new BaseAsyncTask<Void, Void, UsersAndGroupsList>(ctx, showProgressBar) {

			@Override
			protected UsersAndGroupsList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));

				if (!TextUtils.isEmpty(data)) {
					getParams.put(Const.SEARCH, data);
				}

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USERS_AND_GROUPS_FOR_ROOMS, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), UsersAndGroupsList.class);
			}

			@Override
			protected void onPostExecute(UsersAndGroupsList usersAndGroups) {
				super.onPostExecute(usersAndGroups);

				if (listener != null) {
					Result<UsersAndGroupsList> result;

					if (usersAndGroups != null) {
						if (usersAndGroups.getCode() == Const.API_SUCCESS) {
							result = new Result<UsersAndGroupsList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(usersAndGroups);
						} else {
							result = new Result<UsersAndGroupsList>(Result.ApiResponseState.FAILURE);
							result.setResultData(usersAndGroups);
						}

					} else {
						result = new Result<UsersAndGroupsList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void getDistinctUser(final String userIds, final String groupIds, final String roomIds, Context ctx, boolean showProgressBar,
			final ApiCallback<ConfirmUsersList> listener) {
		new BaseAsyncTask<Void, Void, ConfirmUsersList>(ctx, showProgressBar) {

			@Override
			protected ConfirmUsersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.USER_IDS, userIds);
				getParams.put(Const.GROUP_IDS, groupIds);
				getParams.put(Const.ROOM_IDS, roomIds);

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_GET_DISTINC_USER, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), ConfirmUsersList.class);
			}

			@Override
			protected void onPostExecute(ConfirmUsersList users) {
				super.onPostExecute(users);

				if (listener != null) {
					Result<ConfirmUsersList> result;

					if (users != null) {
						if (users.getCode() == Const.API_SUCCESS) {
							result = new Result<ConfirmUsersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(users);
						} else {
							result = new Result<ConfirmUsersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(users);
						}

					} else {
						result = new Result<ConfirmUsersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
