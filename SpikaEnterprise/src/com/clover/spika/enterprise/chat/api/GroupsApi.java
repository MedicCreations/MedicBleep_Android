package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GroupModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class GroupsApi {

    public void getGroupsWithPage(final int page, Context ctx, boolean showProgressBar, final ApiCallback<GroupModel> listener) {
        new BaseAsyncTask<Void, Void, GroupModel>(ctx, showProgressBar) {

            @Override
            protected GroupModel doInBackground(Void... params) {

                JSONObject jsonObject = new JSONObject();
                
                HashMap<String, String> getParams = new HashMap<String, String>();
                getParams.put(Const.PAGE, String.valueOf(page));

                try {

                    jsonObject = NetworkManagement.httpGetRequest(Const.F_USER_GET_GROUPS, getParams,
                            SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new Gson().fromJson(jsonObject.toString(), GroupModel.class);
            }

            @Override
            protected void onPostExecute(GroupModel groupModel) {
                super.onPostExecute(groupModel);

                if (listener != null) {
                    Result<GroupModel> result;

                    if (groupModel != null) {
                        if (groupModel.getCode() == Const.API_SUCCESS) {
                            result = new Result<GroupModel>(Result.ApiResponseState.SUCCESS);
                            result.setResultData(groupModel);
                        } else {
                            result = new Result<GroupModel>(Result.ApiResponseState.FAILURE);
                            result.setResultData(groupModel);
                        }
                    } else {
                        result = new Result<GroupModel>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(result);
                }
            }
        }.execute();
    }

}
