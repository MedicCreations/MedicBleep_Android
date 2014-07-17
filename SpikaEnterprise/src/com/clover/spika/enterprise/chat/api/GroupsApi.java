package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Logger;

import org.json.JSONException;

import java.io.IOException;

public class GroupsApi {

    public void getAllGroups(Context ctx, boolean showProgressBar, final ApiCallback<Void> listener) {
        new BaseAsyncTask<Void, Void, Void>(ctx, showProgressBar) {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Logger.custom("WATAFAK", String.valueOf(NetworkManagement.httpGetRequest("/groups/list", null,
                            SpikaEnterpriseApp.getSharedPreferences(getContext()).getToken())));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if (listener != null) {
                    Result<Void> result;

                    if (aVoid != null) {
                        result = new Result<Void>(Result.ApiResponseState.SUCCESS);
                        result.setResultData(aVoid);
                    } else {
                        result = new Result<Void>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(result);
                }
            }
        }.execute();
    }

}
