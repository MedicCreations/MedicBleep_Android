package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Collection;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class GroupsApi {

    public void getGroupsWithPage(final int page, Context ctx, boolean showProgressBar, final ApiCallback<Collection> listener) {
        new BaseAsyncTask<Void, Void, Collection>(ctx, showProgressBar) {

            @Override
            protected Collection doInBackground(Void... params) {

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
                return new Gson().fromJson(jsonObject.toString(), Collection.class);
            }

            @Override
            protected void onPostExecute(Collection collection) {
                super.onPostExecute(collection);

                if (listener != null) {
                    Result<Collection> result;

                    if (collection != null && collection.getCode() == Const.API_SUCCESS) {
                        result = new Result<Collection>(Result.ApiResponseState.SUCCESS);
                        result.setResultData(collection);
                    } else {
                        result = new Result<Collection>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(result);
                }
            }
        }.execute();
    }

}
