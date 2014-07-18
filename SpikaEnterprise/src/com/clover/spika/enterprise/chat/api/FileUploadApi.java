package com.clover.spika.enterprise.chat.api;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.dialogs.AppProgressDialogWithBar;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class FileUploadApi {
	
	private AppProgressDialogWithBar progressBar;
	
	public void uploadFile(final String path, final Context ctx, boolean showProgressBar, final ApiCallback<UploadFileModel> listener){
		new BaseAsyncTask<Void, Void, UploadFileModel>(ctx, showProgressBar) {
			
			protected void onPreExecute() {
				progressBar = new AppProgressDialogWithBar(ctx);
				progressBar.show();
			};

			protected UploadFileModel doInBackground(Void... params) {
				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.FILE, path);

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject = NetworkManagement.httpPostFileRequest(SpikaEnterpriseApp.getSharedPreferences(context), 
							postParams, new ProgressBarListeners() {
								
								@Override
								public void onSetMax(long total) {
									if(progressBar.getMaxBar() == 1) progressBar.setMaxBar((int) total);
								}
								
								@Override
								public void onProgress(long current) {
									progressBar.updateBar((int) current);
								}
							});
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return new Gson().fromJson(String.valueOf(jsonObject), UploadFileModel.class);
			}

			protected void onPostExecute(UploadFileModel upload) {
				super.onPostExecute(upload);

				if (listener != null) {
					Result<UploadFileModel> result;

					if (upload != null) {
						if (upload.getCode() == Const.API_SUCCESS) {
							result = new Result<UploadFileModel>(Result.ApiResponseState.SUCCESS);
							result.setResultData(upload);
						} else {
							result = new Result<UploadFileModel>(Result.ApiResponseState.FAILURE);
							result.setResultData(upload);
						}
					} else {
						result = new Result<UploadFileModel>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}

		}.execute();
	}

}
