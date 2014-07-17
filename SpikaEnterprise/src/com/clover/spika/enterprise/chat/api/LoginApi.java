package com.clover.spika.enterprise.chat.api;

import android.content.Context;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class LoginApi {

    public void loginWithCredentials(final String username, final String password, Context ctx,
                                     boolean showProgressBar, final ApiCallback<Login> listener) {

        new BaseAsyncTask<Void, Void, Login>(ctx, showProgressBar) {

            protected Login doInBackground(Void... params) {
                HashMap<String, String> requestParams = new HashMap<String, String>();

                // TODO: postaviti keyeve parametara negdje u konstante
                requestParams.put("username", username);
                requestParams.put("password", password);

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject = NetworkManagement.httpPostRequest(Const.F_LOGIN, requestParams, new JSONObject());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return new Gson().fromJson(String.valueOf(jsonObject), Login.class);
            }

            protected void onPostExecute(Login login) {
                super.onPostExecute(login);

                if (listener != null) {
                    Result<Login> apiResult;

                    if (login != null) {
                        if (login.getCode() == Const.API_SUCCESS) {
                            apiResult = new Result<Login>(Result.ApiResponseState.SUCCESS);
                            apiResult.setResultData(login);

                            if (!TextUtils.isEmpty(login.getToken())) {
                                SpikaEnterpriseApp.getSharedPreferences(getContext()).setUserTokenId(login.getToken());
                            }
                        } else {
                            apiResult = new Result<Login>(Result.ApiResponseState.FAILURE);
                            apiResult.setResultData(login);
                        }
                    } else {
                        apiResult = new Result<Login>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(apiResult);
                }
            }

        }.execute();
    }

}
