package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.CategoryList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class CategoryApi {

	public void getCategory(final Context ctx, boolean showProgressBar, final ApiCallback<CategoryList> listener) {
		new BaseAsyncTask<Void, Void, CategoryList>(ctx, showProgressBar) {

			@Override
			protected CategoryList doInBackground(Void... params) {

				try {
					String responseBody = NetworkManagement.httpGetRequest(Const.F_GET_CATEGORIES, null, SpikaEnterpriseApp.getSharedPreferences(ctx).getToken());

					ObjectMapper mapper = new ObjectMapper();

					if (responseBody == null) {
						return null;
					}

					return mapper.readValue(responseBody, CategoryList.class);

				} catch (IOException e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(CategoryList categoryModel) {
				super.onPostExecute(categoryModel);

				if (listener != null) {
					Result<CategoryList> result;

					if (categoryModel != null) {
						if (categoryModel.getCode() == Const.API_SUCCESS) {
							result = new Result<CategoryList>(Result.ApiResponseState.SUCCESS);
							result.setResultData(categoryModel);
						} else {
							result = new Result<CategoryList>(Result.ApiResponseState.FAILURE);
							result.setResultData(categoryModel);
						}
					} else {
						result = new Result<CategoryList>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}
			}
		}.execute();
	}
}
