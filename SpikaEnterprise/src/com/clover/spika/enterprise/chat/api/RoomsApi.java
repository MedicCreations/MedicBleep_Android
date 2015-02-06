package com.clover.spika.enterprise.chat.api;

import android.content.Context;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class RoomsApi {

	public void getDistinctUser(final String userIds, final String groupIds, final String roomIds, final String groupAllIds, final String roomAllIds, Context ctx,
			boolean showProgressBar, final ApiCallback<ConfirmUsersList> listener) {
		new BaseAsyncTask<Void, Void, ConfirmUsersList>(ctx, showProgressBar) {

			@Override
			protected ConfirmUsersList doInBackground(Void... params) {

				JSONObject jsonObject = new JSONObject();

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.USER_IDS, userIds);
				getParams.put(Const.GROUP_IDS, groupIds);
				getParams.put(Const.GROUP_ALL_IDS, groupAllIds);
				getParams.put(Const.ROOM_IDS, roomIds);
				getParams.put(Const.ROOM_ALL_IDS, roomAllIds);

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_GET_DISTINC_USER, getParams, SpikaEnterpriseApp.getSharedPreferences().getToken());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return new Gson().fromJson(jsonObject.toString(), ConfirmUsersList.class);
			}

			@Override
			protected void onPostExecute(ConfirmUsersList users) {
				super.onPostExecute(users);

				if (listener != null) {
					Result<ConfirmUsersList> result;

					if (users != null) {
						if (users.getCode() == Const.API_SUCCESS) {
							result = new Result<ConfirmUsersList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(users);
						} else {
							result = new Result<ConfirmUsersList>(Result.ApiResponseState.FAILURE);
							result.setResultData(users);
						}

					} else {
						result = new Result<ConfirmUsersList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
