package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserModel;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class UsersApi {

    public void getUsersWithPage(Context ctx, final int page, boolean showProgressBar, final ApiCallback<UserModel> listener) {
        new BaseAsyncTask<Void, Void, UserModel>(ctx, showProgressBar) {

            @Override
            protected UserModel doInBackground(Void... params) {

                JSONObject jsonObject = new JSONObject();

                HashMap<String, String> requestParams = new HashMap<String, String>();
                requestParams.put(Const.PAGE, String.valueOf(page));

                try {
                    jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_ALL_CHARACTERS, requestParams,
                            SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new Gson().fromJson(jsonObject.toString(), UserModel.class);
            }

            @Override
            protected void onPostExecute(UserModel userModel) {
                super.onPostExecute(userModel);

                if (listener != null) {
                    Result<UserModel> result;

                    if (userModel != null) {
                        if (userModel.getCode() == Const.API_SUCCESS) {
                            result = new Result<UserModel>(Result.ApiResponseState.SUCCESS);
                            result.setResultData(userModel);
                        } else {
                            result = new Result<UserModel>(Result.ApiResponseState.FAILURE);
                            result.setResultData(userModel);
                        }
                    } else {
                        result = new Result<UserModel>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(result);
                }
            }
        }.execute();
    }

}
