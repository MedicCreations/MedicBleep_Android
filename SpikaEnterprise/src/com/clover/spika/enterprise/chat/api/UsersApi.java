package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class UsersApi {

	public void getUsersWithPage(Context ctx, final int page, final String chatId, boolean showProgressBar, final ApiCallback<UsersList> listener) {
		new BaseAsyncTask<Void, Void, UsersList>(ctx, showProgressBar) {

			@Override
			protected UsersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(Const.PAGE, String.valueOf(page));

				if (chatId != null) {
					requestParams.put(Const.CHAT_ID, chatId);
				}

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ALL_CHARACTERS, requestParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), UsersList.class);
			}

			@Override
			protected void onPostExecute(UsersList userModel) {
				super.onPostExecute(userModel);

				if (listener != null) {
					Result<UsersList> result;

					if (userModel != null) {
						if (userModel.getCode() == Const.API_SUCCESS) {
							result = new Result<UsersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(userModel);
						} else {
							result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(userModel);
						}
					} else {
						result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void getUsersByName(final int page, final String chatId, final String data, Context ctx, boolean showProgressBar, final ApiCallback<UsersList> listener) {
		new BaseAsyncTask<Void, Void, UsersList>(ctx, showProgressBar) {

			@Override
			protected UsersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.PAGE, String.valueOf(page));
				getParams.put(Const.SEARCH, data);

				if (chatId != null) {
					getParams.put(Const.CHAT_ID, chatId);
				}

				try {

					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ALL_CHARACTERS, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), UsersList.class);
			}

			@Override
			protected void onPostExecute(UsersList userModel) {
				super.onPostExecute(userModel);

				if (listener != null) {

					Result<UsersList> result;

					if (userModel != null) {
						if (userModel.getCode() == Const.API_SUCCESS) {
							result = new Result<UsersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(userModel);
						} else {
							result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(userModel);
						}
					} else {
						result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);

				}
			}
		}.execute();
	}

	public void getChatMembersWithPage(Context ctx, final String chatId, final int page, boolean showProgressBar, final ApiCallback<UsersList> listener) {
		new BaseAsyncTask<Void, Void, UsersList>(ctx, showProgressBar) {

			@Override
			protected UsersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(Const.PAGE, String.valueOf(page));
				requestParams.put(Const.CHAT_ID, chatId);

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_CHAT_MEMBERS, requestParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), UsersList.class);
			}

			@Override
			protected void onPostExecute(UsersList userModel) {
				super.onPostExecute(userModel);

				if (listener != null) {
					Result<UsersList> result;

					if (userModel != null) {
						if (userModel.getCode() == Const.API_SUCCESS) {
							result = new Result<UsersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(userModel);
						} else {
							result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(userModel);
						}
					} else {
						result = new Result<UsersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void inviteUsers(final String chatId, final List<User> data, Context ctx, final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, true) {

			@Override
			protected Chat doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.CHAT_ID, chatId);

				StringBuilder users = new StringBuilder();

				boolean isComma = false;
				for (User user : data) {

					if (isComma) {
						users.append(",");
					}

					if (!user.isMember() && user.isSelected()) {
						users.append(user.getId());
						isComma = true;
					} else {
						isComma = false;
					}

				}

				getParams.put(Const.USERS_TO_ADD, users.toString());

				if (chatId != null) {
					getParams.put(Const.CHAT_ID, chatId);
				}

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_INVITE_USERS, getParams, SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), Chat.class);
			}

			@Override
			protected void onPostExecute(Chat chat) {
				super.onPostExecute(chat);

				if (listener != null) {

					Result<Chat> result;

					if (chat != null) {
						if (chat.getCode() == Const.API_SUCCESS) {
							result = new Result<Chat>(Result.ApiResponseState.SUCCESS);
							result.setResultData(chat.getChat());
						} else {
							result = new Result<Chat>(Result.ApiResponseState.FAILURE);
						}
					} else {
						result = new Result<Chat>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);

				}
			}
		}.execute();
	}

}
