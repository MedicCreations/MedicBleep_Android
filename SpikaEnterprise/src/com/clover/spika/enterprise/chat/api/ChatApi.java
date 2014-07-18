package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class ChatApi {

	public void startChat(final String userId, final String firstname, final String lastname, boolean showProgressBar, Context ctx, final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, showProgressBar) {

			protected Chat doInBackground(Void... params) {
				HashMap<String, String> requestParams = new HashMap<String, String>();

				requestParams.put(Const.USER_ID, userId);
				requestParams.put(Const.FIRSTNAME, firstname);
				requestParams.put(Const.LASTNAME, lastname);

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_START_NEW_CHAT, requestParams, new JSONObject());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return new Gson().fromJson(String.valueOf(jsonObject), Chat.class);
			};

			protected void onPostExecute(Chat chat) {
				if (listener != null) {
					Result<Chat> apiResult;

					if (chat != null) {
						if (chat.getCode() == Const.API_SUCCESS) {
							apiResult = new Result<Chat>(Result.ApiResponseState.SUCCESS);
							apiResult.setResultData(chat);
						} else {
							apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
							apiResult.setResultData(chat);
						}
					} else {
						apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			};
		}.execute();
	}

	public void sendMessage(final String text, final int type, final String groupId, Context ctx, final ApiCallback<Integer> listener) {
		new BaseAsyncTask<Void, Void, Integer>(ctx, true) {

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.CHAT_ID, groupId);

					if (type == Const.MSG_TYPE_DEFAULT) {
						reqData.put(Const.FILE_ID, text);
					} else if (type == Const.MSG_TYPE_PHOTO) {
						reqData.put(Const.TEXT, text);
					}

					reqData.put(Const.MSG_TYPE, String.valueOf(type));

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);
				if (listener != null) {

					Result<Integer> apiResult;

					if (result.equals(Const.API_SUCCESS)) {
						apiResult = new Result<Integer>(Result.ApiResponseState.SUCCESS);
					} else {
						apiResult = new Result<Integer>(Result.ApiResponseState.FAILURE);
						apiResult.setResultData(result);
					}

					listener.onApiResponse(apiResult);
				}
			};

		}.execute();
	}

	public void getMessages(final boolean isClear, final boolean processing, final boolean isPagging, final boolean isNewMsg, final boolean isSend, final boolean isRefresh, final String chatId, final String msgId, final int adapterCount, Context ctx,
			final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, processing) {

			List<Message> tempMessage = new ArrayList<Message>();
			int totalItems = -1;

			protected Chat doInBackground(Void... params) {

				try {

					HashMap<String, String> requestParams = new HashMap<String, String>();
					requestParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());
					requestParams.put(Const.CHAT_ID, chatId);

					if (isPagging) {
						if (!isClear && adapterCount != -1 && adapterCount > 0) {
							requestParams.put(Const.LAST_MSG_ID, msgId);
						}
					} else if (isNewMsg) {
						if ((adapterCount - 1) >= 0) {
							requestParams.put(Const.FIRST_MSG_ID, msgId);
						}
					}

					JSONObject jsonObject = NetworkManagement.httpPostRequest(Const.F_GET_MESSAGES, requestParams, new JSONObject());

					return new Gson().fromJson(String.valueOf(jsonObject), Chat.class);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return null;
			};

			protected void onPostExecute(Chat result) {
				super.onPostExecute(result);

				if (listener != null) {

					Result<Chat> apiResult;

					if (result != null && result.equals(Const.API_SUCCESS)) {

						result.setMsgList(tempMessage);
						result.setNewMsg(isNewMsg);
						result.setRefresh(isRefresh);
						result.setClear(isClear);
						result.setSend(isSend);
						result.setAdapterCount(adapterCount);
						result.setPagging(isPagging);
						result.setTotalItems(totalItems);

						apiResult = new Result<Chat>(Result.ApiResponseState.SUCCESS);
						apiResult.setResultData(result);

					} else {
						apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			};

		}.execute();
	}

}
