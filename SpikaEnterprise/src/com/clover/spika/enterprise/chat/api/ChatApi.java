package com.clover.spika.enterprise.chat.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
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
import com.google.gson.GsonBuilder;

public class ChatApi {

	public void startChat(Context ctx, boolean showProgressBar) {
		new BaseAsyncTask<Void, Void, Result>(ctx, showProgressBar) {
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
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.FUNCTION, Const.F_POST_MESSAGE);
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

					if (result.equals(Const.E_SUCCESS)) {
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

	public void getMessages(final boolean isClear, final boolean processing, final boolean isPagging, final boolean isNewMsg, final boolean isSend, final boolean isRefresh, final String groupId, final String msgId, final int adapterCount, Context ctx,
			final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Integer>(ctx, processing) {

			List<Message> tempMessage = new ArrayList<Message>();
			int totalItems = -1;

			protected Integer doInBackground(Void... params) {

				// start: Get messages
				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.CHAT_ID, groupId);

					if (isPagging) {
						getParams.put(Const.FUNCTION, Const.F_GET_MESSAGES);

						if (!isClear && adapterCount != -1 && adapterCount > 0) {
							reqData.put(Const.LAST_MSG_ID, msgId);
						}
					} else if (isNewMsg) {
						getParams.put(Const.FUNCTION, Const.F_GET_NEW_MESSAGES);

						if ((adapterCount - 1) >= 0) {
							reqData.put(Const.FIRST_MSG_ID, msgId);
						}
					}

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						totalItems = Integer.parseInt(result.getString(Const.TOTAL_ITEMS));

						JSONArray items = result.getJSONArray(Const.ITEMS);

						for (int i = 0; i < items.length(); i++) {
							JSONObject obj = (JSONObject) items.get(i);

							Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							Message msg = sGsonExpose.fromJson(obj.toString(), Message.class);

							if (msg != null) {
								tempMessage.add(msg);
							}
						}

						if (tempMessage.size() > 0) {
							return Const.E_SUCCESS;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// end: Get messages

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (listener != null) {

					Result<Chat> apiResult;

					if (result.equals(Const.E_SUCCESS)) {

						Chat chatData = new Chat();
						chatData.setMsgList(tempMessage);
						chatData.setNewMsg(isNewMsg);
						chatData.setRefresh(isRefresh);
						chatData.setClear(isClear);
						chatData.setSend(isSend);
						chatData.setAdapterCount(adapterCount);
						chatData.setPagging(isPagging);
						chatData.setTotalItems(totalItems);

						apiResult = new Result<Chat>(Result.ApiResponseState.SUCCESS);
						apiResult.setResultData(chatData);

					} else {
						apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			};

		}.execute();
	}

}
