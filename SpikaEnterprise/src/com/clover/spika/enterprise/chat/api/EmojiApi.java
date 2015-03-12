package com.clover.spika.enterprise.chat.api;

import java.io.IOException;


import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmojiApi {

	public void getEmoji(final Context context, final ApiCallback<StickersHolder> callback) {
		new BaseAsyncTask<Void, Void, StickersHolder>(context, false) {
			@Override
			protected StickersHolder doInBackground(Void... params) {

				try {
					String responseBody = NetworkManagement.httpGetRequest(Const.F_STICKERS_URL, null, SpikaEnterpriseApp.getSharedPreferences(context)
							.getCustomString(Const.TOKEN));

					ObjectMapper mapper = new ObjectMapper();

					if (responseBody == null) {
						return null;
					}

					return mapper.readValue(responseBody, StickersHolder.class);

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
