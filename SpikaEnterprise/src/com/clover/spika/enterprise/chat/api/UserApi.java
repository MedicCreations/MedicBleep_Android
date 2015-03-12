package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Information;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class UserApi {

	public void updateUserImage(final String image, final String thumb, final Context ctx, boolean showProgressBar, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, showProgressBar) {

			@Override
			protected BaseModel doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.IMAGE, image);
				postParams.put(Const.IMAGE_THUMB, thumb);

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_UPDATE_USER, postParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					ObjectMapper mapper = new ObjectMapper();
					BaseModel result = mapper.readValue(responseBody, BaseModel.class);

					return result;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
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
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, false) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.PUSH_TOKEN_LOCAL));

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_UPDATE_PUSH_TOKEN, postParams, SpikaEnterpriseApp.getSharedPreferences(ctx).getToken());

					ObjectMapper mapper = new ObjectMapper();
					BaseModel result = mapper.readValue(responseBody, BaseModel.class);

					return result;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
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

	public void logout(final Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, true) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, "");

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_LOGOUT_API, postParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					ObjectMapper mapper = new ObjectMapper();
					BaseModel result = mapper.readValue(responseBody, BaseModel.class);

					return result;

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
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

	public void getProfile(final Context context, final boolean getDetailValues, final String userId, final ApiCallback<UserWrapper> callback) {
		new BaseAsyncTask<Void, Void, UserWrapper>(context, true) {
			@Override
			protected UserWrapper doInBackground(Void... params) {

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.USER_ID, userId);

				if (getDetailValues) {
					getParams.put(Const.GET_DETAIL_VALUES, "1");
				}

				try {

					String responseBody = NetworkManagement.httpGetRequest(Const.F_USER_PROFILE, getParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());
					return new ObjectMapper().readValue(responseBody, UserWrapper.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(UserWrapper userWrapper) {
				super.onPostExecute(userWrapper);
				if (callback != null) {
					Result<UserWrapper> result;
					if (userWrapper != null) {
						if (userWrapper.getCode() == Const.API_SUCCESS) {
							result = new Result<UserWrapper>(userWrapper, Result.ApiResponseState.SUCCESS);
						} else {
							result = new Result<UserWrapper>(Result.ApiResponseState.FAILURE);
						}
					} else {
						result = new Result<UserWrapper>(Result.ApiResponseState.FAILURE);
					}
					callback.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void getInformation(final Context context, final ApiCallback<Information> callback) {
		new BaseAsyncTask<Void, Void, Information>(context, true) {
			@Override
			protected Information doInBackground(Void... params) {

				try {

					String responseBody = NetworkManagement.httpGetRequest(Const.F_USER_INFORMATION, new HashMap<String, String>(), SpikaEnterpriseApp
							.getSharedPreferences(context).getToken());
					return new ObjectMapper().readValue(responseBody, Information.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Information infomration) {
				super.onPostExecute(infomration);
				if (callback != null) {
					Result<Information> result;
					if (infomration != null) {
						if (infomration.getCode() == Const.API_SUCCESS) {
							result = new Result<Information>(infomration, Result.ApiResponseState.SUCCESS);
						} else {
							result = new Result<Information>(Result.ApiResponseState.FAILURE);
						}
					} else {
						result = new Result<Information>(Result.ApiResponseState.FAILURE);
					}
					callback.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void updateUserDetails(final List<UserDetail> list, final Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, true) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				JSONArray detailsArray = new JSONArray();

				for (UserDetail detail : list) {

					if (detail.getValue() != null) {

						try {
							JSONObject object = new JSONObject();

							object.put(detail.getKey(), detail.getValue());
							object.put(Const.PUBLIC, detail.isPublicValue());

							detailsArray.put(object);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.DETAILS, detailsArray.toString());

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_UPDATE_USER, postParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());
					return new ObjectMapper().readValue(responseBody, BaseModel.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
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
							result.setResultData(baseModel);
						}
					} else {
						result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
						baseModel = new BaseModel();
						baseModel.setCode(Const.E_SOMETHING_WENT_WRONG);
						result.setResultData(baseModel);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void forgotPassword(final String username, final Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, true) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.USERNAME, username);

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_FORGOT_PASSWORD, postParams, null);
					return new ObjectMapper().readValue(responseBody, BaseModel.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
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
							result.setResultData(baseModel);
						}
					} else {
						result = new Result<BaseModel>(Result.ApiResponseState.FAILURE);
						baseModel = new BaseModel();
						baseModel.setCode(Const.E_SOMETHING_WENT_WRONG);
						result.setResultData(baseModel);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void updateUserPassword(final boolean isUpdate, final String tempPassword, final String newPassword, final Context ctx, final ApiCallback<Login> listener) {
		new BaseAsyncTask<Void, Void, Login>(ctx, true) {
			@Override
			protected Login doInBackground(Void... params) {

				HashMap<String, String> postParams = new HashMap<String, String>();
				String hashPassword;
				try {
					hashPassword = Utils.getHexString(newPassword);
					postParams.put(Const.NEW_PASSWORD, hashPassword);
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				try {

					String responseBody;

					if (isUpdate) {
						responseBody = NetworkManagement.httpPostRequest(Const.F_UPDATE_USER_PASSWORD, postParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());
					} else {

						String hashTempPassword = Utils.getHexString(tempPassword);
						postParams.put(Const.TEMP_PASSWORD, hashTempPassword);

						responseBody = NetworkManagement.httpPostRequest(Const.F_CHANGE_USER_PASSWORD, postParams, null);
					}

					return new ObjectMapper().readValue(responseBody, Login.class);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Login baseModel) {
				super.onPostExecute(baseModel);

				if (listener != null) {
					Result<Login> result;

					if (baseModel != null) {
						if (baseModel.getCode() == Const.API_SUCCESS) {
							result = new Result<Login>(Result.ApiResponseState.SUCCESS);
							result.setResultData(baseModel);

							SpikaEnterpriseApp.getSharedPreferences(context).setUserTokenId(baseModel.getToken());
							if (SpikaEnterpriseApp.getSharedPreferences(context).getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
								SpikaEnterpriseApp.getSharedPreferences(context).setCustomString(Const.PASSWORD, newPassword);
							} else {
								SpikaEnterpriseApp.getSharedPreferences(context).setCustomString(Const.PASSWORD, "");
							}
						} else {
							result = new Result<Login>(Result.ApiResponseState.FAILURE);
							result.setResultData(baseModel);
						}
					} else {
						result = new Result<Login>(Result.ApiResponseState.FAILURE);
						baseModel = new Login();
						baseModel.setCode(Const.E_SOMETHING_WENT_WRONG);
						result.setResultData(baseModel);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

	public void inviteUsers(final String chatId, final String users, Context ctx, final ApiCallback<Chat> listener) {
		new BaseAsyncTask<Void, Void, Chat>(ctx, true) {

			@Override
			protected Chat doInBackground(Void... params) {

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.CHAT_ID, chatId);
				getParams.put(Const.USERS_TO_ADD, users);

				if (chatId != null) {
					getParams.put(Const.CHAT_ID, chatId);
				}

				try {

					String responseBody = NetworkManagement.httpPostRequest(Const.F_INVITE_USERS, getParams, SpikaEnterpriseApp.getSharedPreferences(context).getToken());
					return new ObjectMapper().readValue(responseBody, Chat.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Chat chat) {
				super.onPostExecute(chat);

				if (listener != null) {

					Result<Chat> result;

					if (chat != null) {
						if (chat.getCode() == Const.API_SUCCESS) {
							result = new Result<Chat>(Result.ApiResponseState.SUCCESS);
							result.setResultData(chat.chat);
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
