package com.medicbleep.app.chat.api.robospice;

import android.content.Context;

import com.medicbleep.app.chat.models.CategoryList;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class CategorySpice {
	
	public static class GetCategory extends CustomSpiceRequest<CategoryList> {

		public GetCategory(Context context) {
			super(CategoryList.class);
		}

		@Override
		public CategoryList loadDataFromNetwork() throws Exception {
			
			Request.Builder requestBuilder = new Request.Builder()
				.headers(getGetHeaders())
				.url(Const.BASE_URL + Const.F_GET_CATEGORIES)
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, CategoryList.class);
		}
	}

}
