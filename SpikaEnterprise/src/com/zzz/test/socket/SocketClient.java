package com.zzz.test.socket;

import java.io.IOException;

import android.content.Context;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;

public class SocketClient {

	public void getSessionId(Context ctx, boolean showProgressBar, final ApiCallback<String> listener) {
		new BaseAsyncTask<Void, Void, String>(ctx, showProgressBar) {

			@Override
			protected String doInBackground(Void... params) {

				try {
					return NetworkManagement.httpGetRequestWithRawResponse("https://www.spikaent.com:32443/socket.io/1/");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(String session) {
				super.onPostExecute(session);

				if (listener != null) {
					Result<String> result;
					
					result = new Result<String>(Result.ApiResponseState.SUCCESS);
					result.setResultData(session);

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}

}
