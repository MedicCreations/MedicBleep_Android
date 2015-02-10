package com.clover.spika.enterprise.chat.api;

import java.io.IOException;

import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class EmojiApi {

	public void getEmoji(final Context context, final ApiCallback<StickersHolder> callback) {
		new BaseAsyncTask<Void, Void, StickersHolder>(context, false) {
			@Override
			protected StickersHolder doInBackground(Void... params) {
				JSONObject jsonObject = new JSONObject();

				try {
					jsonObject = NetworkManagement.httpGetRequest(Const.F_STICKERS_URL, null, SpikaEnterpriseApp.getSharedPreferences(context).getCustomString(Const.TOKEN));
					return new Gson().fromJson(jsonObject.toString(), StickersHolder.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(StickersHolder data) {
				super.onPostExecute(data);
				if (callback != null) {
					Result<StickersHolder> result;
					if (data != null) {
						if (data.getCode() == Const.API_SUCCESS) {
							result = new Result<StickersHolder>(data, Result.ApiResponseState.SUCCESS);
						} else {
							result = new Result<StickersHolder>(Result.ApiResponseState.FAILURE);
						}
					} else {
						result = new Result<StickersHolder>(Result.ApiResponseState.FAILURE);
					}
					callback.onApiResponse(result);
				}
			}
		}.execute();
	}

}
