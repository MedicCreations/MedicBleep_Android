package com.clover.spika.enterprise.chat.api;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.google.gson.Gson;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class ChatApi {

	public void startChat(final boolean isGroup, final String userId, final String firstname, final String lastname, boolean showProgressBar, Context ctx,
			final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, showProgressBar) {

			protected Chat doInBackground(Void... params) {
				HashMap<String, String> requestParams = new HashMap<String, String>();

				JSONObject jsonObject = new JSONObject();
				try {

					String url = Const.F_START_NEW_CHAT;

					if (isGroup) {
						url = Const.F_START_NEW_GROUP;

						requestParams.put(Const.GROUP_ID, userId);
						requestParams.put(Const.GROUP_NAME, firstname);
					} else {
						requestParams.put(Const.USER_ID, userId);
						requestParams.put(Const.FIRSTNAME, firstname);
						requestParams.put(Const.LASTNAME, lastname);
					}

					jsonObject = NetworkManagement.httpPostRequest(url, requestParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					return new Gson().fromJson(String.valueOf(jsonObject), Chat.class);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return null;
			};

			protected void onPostExecute(Chat chat) {
				super.onPostExecute(chat);

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

	public void getMessages(final boolean isClear, final boolean processing, final boolean isPagging, final boolean isNewMsg, final boolean isSend, final boolean isRefresh,
			final String chatId, final String msgId, final int adapterCount, Context ctx, final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, processing) {

			protected Chat doInBackground(Void... params) {

				try {

					HashMap<String, String> requestParams = new HashMap<String, String>();
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

					JSONObject jsonObject = NetworkManagement.httpGetRequest(Const.F_GET_MESSAGES, requestParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

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

					if (result != null && result.getCode() == Const.API_SUCCESS) {

						result.setNewMsg(isNewMsg);
						result.setRefresh(isRefresh);
						result.setClear(isClear);
						result.setSend(isSend);
						result.setPagging(isPagging);

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

    public void sendMessage(final int type, final String chatId, final String text, final String fileId, final String thumbId, final String longitude, final String latitude,
                            Context ctx, final ApiCallback<Integer> listener) {
        sendMessage(type, chatId, text, fileId, thumbId, longitude, latitude, null, null, ctx, listener);
    }

	public void sendMessage(final int type, final String chatId, final String text, final String fileId, final String thumbId, final String longitude, final String latitude,
			final String rootId, final String parentId, Context ctx, final ApiCallback<Integer> listener) {
		new BaseAsyncTask<Void, Void, Integer>(ctx, true) {

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {
					HashMap<String, String> requestParams = new HashMap<String, String>();

					requestParams.put(Const.CHAT_ID, chatId);
					requestParams.put(Const.MSG_TYPE, String.valueOf(type));

					if (!TextUtils.isEmpty(text)) {
						requestParams.put(Const.TEXT, JNAesCrypto.encryptJN(text));
					}

					if (!TextUtils.isEmpty(fileId)) {
						requestParams.put(Const.FILE_ID, fileId);
					}

					if (!TextUtils.isEmpty(thumbId)) {
						requestParams.put(Const.THUMB_ID, thumbId);
					}

					if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
						requestParams.put(Const.LONGITUDE, JNAesCrypto.encryptJN(longitude));
						requestParams.put(Const.LATITUDE, JNAesCrypto.encryptJN(latitude));
					}

                    requestParams.put(Const.ROOT_ID, String.valueOf(rootId));
                    requestParams.put(Const.PARENT_ID, String.valueOf(parentId));

					JSONObject jsonObject = NetworkManagement.httpPostRequest(Const.F_SEND_MESSAGE, requestParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					if (jsonObject != null) {
						return jsonObject.getInt(Const.CODE);
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

	public void leaveChat(final String chatId, boolean showProgressBar, Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, showProgressBar) {

			protected BaseModel doInBackground(Void... params) {
				HashMap<String, String> requestParams = new HashMap<String, String>();

				JSONObject jsonObject = new JSONObject();
				requestParams.put(Const.CHAT_ID, chatId);

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_LEAVE_CHAT, requestParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					return new Gson().fromJson(String.valueOf(jsonObject), BaseModel.class);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return null;
			};

			protected void onPostExecute(BaseModel model) {
				super.onPostExecute(model);

				if (listener != null) {
					Result<BaseModel> apiResult;

					if (model != null) {
						if (model.getCode() == Const.API_SUCCESS) {
							apiResult = new Result<BaseModel>(Result.ApiResponseState.SUCCESS);
						} else {
							apiResult = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
						}
					} else {
						apiResult = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(apiResult);
				}
			};
		}.execute();
	}

    public void getThreads(final String messageId, boolean showProgressBar, Context context, final ApiCallback<Chat> listener) {
        new BaseAsyncTask<Void, Void, Chat>(context, showProgressBar) {

            @Override
            protected Chat doInBackground(Void... params) {
                HashMap<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(Const.ROOT_ID, String.valueOf(messageId));

                try {
                    JSONObject jsonObject = NetworkManagement.httpGetRequest(Const.F_GET_THREADS, requestParams,
                            SpikaEnterpriseApp.getSharedPreferences(context).getToken());
                    return new Gson().fromJson(String.valueOf(jsonObject), Chat.class);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Chat model) {
                super.onPostExecute(model);

                if (listener != null) {
                    Result<Chat> apiResult;

                    if (model != null) {
                        if (model.getCode() == Const.API_SUCCESS) {
                            apiResult = new Result<Chat>(model, Result.ApiResponseState.SUCCESS);
                        } else {
                            apiResult = new Result<Chat>(model, Result.ApiResponseState.FAILURE);
                        }
                    } else {
                        apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
                    }
                    listener.onApiResponse(apiResult);
                }
            }

        }.execute();
    }

    public void deleteMessage(final String messageId, Context context, final ApiCallback<BaseModel> listener) {
        new BaseAsyncTask<Void, Void, BaseModel>(context, true) {

            @Override
            protected BaseModel doInBackground(Void... params) {
                HashMap<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(Const.MESSAGE_ID, messageId);

                try {
                    JSONObject jsonObject = NetworkManagement.httpPostRequest(Const.F_DELETE_MESSAGE, requestParams,
                            SpikaEnterpriseApp.getSharedPreferences(context).getToken());
                    return new Gson().fromJson(jsonObject.toString(), BaseModel.class);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(BaseModel model) {
                super.onPostExecute(model);

                if (listener != null) {
                    Result<BaseModel> apiResult;

                    if (model != null) {
                        if (model.getCode() == Const.API_SUCCESS) {
                            apiResult = new Result<BaseModel>(model, Result.ApiResponseState.SUCCESS);
                        } else {
                            apiResult = new Result<BaseModel>(model, Result.ApiResponseState.FAILURE);
                        }
                    } else {
                        apiResult = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
                    }
                    listener.onApiResponse(apiResult);
                }
            }
        }.execute();
    }
    
    
    public void createRoom(final String name, final String image, final String image_thumb, final String users_to_add, Context context, final ApiCallback<Chat> listener) {
        new BaseAsyncTask<Void, Void, Chat>(context, true) {

            @Override
            protected Chat doInBackground(Void... params) {
                HashMap<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(Const.NAME, name);
                requestParams.put(Const.IMAGE, image);
                requestParams.put(Const.IMAGE_THUMB, image_thumb);
                requestParams.put(Const.USERS_TO_ADD, users_to_add);

                try {
                    JSONObject jsonObject = NetworkManagement.httpPostRequest(Const.F_CREATE_ROOM, requestParams,
                            SpikaEnterpriseApp.getSharedPreferences(context).getToken());
                    return new Gson().fromJson(String.valueOf(jsonObject), Chat.class);
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Chat chat) {
                super.onPostExecute(chat);

                if (listener != null) {
                    Result<Chat> apiResult;

                    if (chat != null) {
                        if (chat.getCode() == Const.API_SUCCESS) {
                            apiResult = new Result<Chat>(chat, Result.ApiResponseState.SUCCESS);
                            apiResult.setResultData(chat);
                        } else {
                            apiResult = new Result<Chat>(chat, Result.ApiResponseState.FAILURE);
                            apiResult.setResultData(chat);
                        }
                    } else {
                        apiResult = new Result<Chat>(Result.ApiResponseState.FAILURE);
                    }
                    listener.onApiResponse(apiResult);
                }
            }
        }.execute();
    }

}
