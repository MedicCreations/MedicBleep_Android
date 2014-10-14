package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Information;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
				} catch (JsonSyntaxException e) {
                    e.printStackTrace();
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
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, false) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.PUSH_TOKEN_LOCAL));

				try {
					jsonObject = NetworkManagement
							.httpPostRequest(Const.F_UPDATE_PUSH_TOKEN, postParams, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.TOKEN));
				} catch (JsonSyntaxException e) {
                    e.printStackTrace();
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

	public void logout(final Context ctx, final ApiCallback<BaseModel> listener) {
		new BaseAsyncTask<Void, Void, BaseModel>(ctx, true) {
			@Override
			protected BaseModel doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.PUSH_TOKEN, "");

				try {
					jsonObject = NetworkManagement.httpPostRequest(Const.F_LOGOUT_API, postParams, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.TOKEN));
					return new Gson().fromJson(jsonObject.toString(), BaseModel.class);
				} catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
					e.printStackTrace();
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

    public void getProfile(final Context context, final String userId, final ApiCallback<UserWrapper> callback) {
        new BaseAsyncTask<Void, Void, UserWrapper>(context, false) {
            @Override
            protected UserWrapper doInBackground(Void... params) {
                JSONObject jsonObject = new JSONObject();

                HashMap<String, String> getParams = new HashMap<String, String>();
                getParams.put(Const.USER_ID, userId);

                try {
                    jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_PROFILE, getParams,
                            SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.TOKEN));
                    return new Gson().fromJson(jsonObject.toString(), UserWrapper.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
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
                JSONObject jsonObject = new JSONObject();

                HashMap<String, String> getParams = new HashMap<String, String>();

                try {
                    jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_INFORMATION, getParams,
                            SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.TOKEN));
                    return new Gson().fromJson(jsonObject.toString(), Information.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
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
}
