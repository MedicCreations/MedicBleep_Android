package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Result;

public class LoginApi {

	public static void loginWithCredentials(String username, String password, final ApiCallback<Void> listener, Context ctx, boolean showProgressBar) {

		new BaseAsyncTask<Void, Void, Void>(ctx, showProgressBar) {

			protected Void doInBackground(Void... params) {
				return null;
			}

			protected void onPostExecute(Void result) {
				if (listener != null) {
                    Result<Void> apiResult;

                    if (result != null) {
                        apiResult = new Result<Void>(Result.ApiResponseState.SUCCESS);
                        apiResult.setResultData(result);
                    } else {
                        apiResult = new Result<Void>(Result.ApiResponseState.FAILURE);
                    }

                    listener.onApiResponse(apiResult);
				}
			}

		}.execute();
	}

}
