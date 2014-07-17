package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Groups;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GroupsApi {

    public void getAllGroups(Context ctx, boolean showProgressBar, final ApiCallback<Groups> listener) {
        new BaseAsyncTask<Void, Void, Groups>(ctx, showProgressBar) {

            @Override
            protected Groups doInBackground(Void... params) {

                JSONObject jsonObject = new JSONObject();

                try {

                    // TODO: postaviti konstantu za link umjesto hardcodiranog Stringa
                    jsonObject = NetworkManagement.httpGetRequest("/groups/list", null,
                            SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new Gson().fromJson(jsonObject.toString(), Groups.class);
            }

            @Override
            protected void onPostExecute(Groups groups) {
                super.onPostExecute(groups);

                if (listener != null) {
                    Result<Groups> result;

                    if (groups != null) {
                        result = new Result<Groups>(Result.ApiResponseState.SUCCESS);
                        result.setResultData(groups);
                    } else {
                        result = new Result<Groups>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(result);
                }
            }
        }.execute();
    }

}
