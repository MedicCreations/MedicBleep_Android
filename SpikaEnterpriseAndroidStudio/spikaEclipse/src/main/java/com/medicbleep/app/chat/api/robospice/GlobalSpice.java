package com.medicbleep.app.chat.api.robospice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.models.GlobalResponse;
import com.medicbleep.app.chat.networking.GetUrl;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class GlobalSpice {

	public static class GlobalSearch extends CustomSpiceRequest<GlobalResponse> {

		private int page;
		private String chatId;
		private String categoryId;
		private int type;
		private String searchTerm;

		public GlobalSearch(int page, String chatId, String categoryId, int type, String searchTerm) {
			super(GlobalResponse.class);

			this.page = page;
			this.chatId = chatId;
			this.categoryId = categoryId;
			this.type = type;
			this.searchTerm = searchTerm;
		}

		@Override
		public GlobalResponse loadDataFromNetwork() throws Exception {

			HashMap<String, String> requestParams = new HashMap<String, String>();

			requestParams.put(Const.PAGE, String.valueOf(page));
			requestParams.put(Const.TYPE, String.valueOf(type));

			if (!TextUtils.isEmpty(chatId)) {
				requestParams.put(Const.CHAT_ID, chatId);
			}

			if (!TextUtils.isEmpty(categoryId)) {
				requestParams.put(Const.CATEGORY_ID, categoryId);
			}

			if (!TextUtils.isEmpty(searchTerm)) {
				requestParams.put(Const.SEARCH, searchTerm);
			}

			GetUrl getParameters = new GetUrl(requestParams);

			Request.Builder requestBuilder = new Request.Builder().headers(getGetHeaders())
					.url(Const.BASE_URL + Const.F_GLOBAL_SEARCH_URL + getParameters.toString()).get();

            Logger.custom("e", "LOG", Const.BASE_URL + Const.F_GLOBAL_SEARCH_URL + getParameters.toString());

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

            Logger.custom("i", "LOG", "SEARCH: " + responseBody);
			
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, GlobalResponse.class);
		}
	}

	public static class GlobalMembers extends CustomSpiceRequest<GlobalResponse> {

		private int page;
		private String chatId;
		private String groupId;
		private int type;

		public GlobalMembers(int page, String chatId, String groupId, int type) {
			super(GlobalResponse.class);

			this.page = page;
			this.chatId = chatId;
			this.groupId = groupId;
			this.type = type;
		}

		@Override
		public GlobalResponse loadDataFromNetwork() throws Exception {

			HashMap<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(Const.PAGE, String.valueOf(page));
			requestParams.put(Const.TYPE, String.valueOf(type));

			if (!TextUtils.isEmpty(chatId)) {
				requestParams.put(Const.CHAT_ID, chatId);
			}

			if (!TextUtils.isEmpty(groupId)) {
				requestParams.put(Const.GROUP_ID, groupId);
			}

			GetUrl getParameters = new GetUrl(requestParams);

			Request.Builder requestBuilder = new Request.Builder().headers(getGetHeaders())
					.url(Const.BASE_URL + Const.F_GLOBAL_MEMBERS_URL + getParameters.toString()).get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();
			
			JSONObject jsonObject = new JSONObject(responseBody);
			int code = jsonObject.getInt(Const.CODE);

			GlobalResponse response = new GlobalResponse();

			if (code == Const.API_SUCCESS) {

				response.setCode(code);
				response.setPage(jsonObject.getInt(Const.PAGE));
				response.setTotalCount(jsonObject.getInt(Const.TOTAL_COUNT));

				List<GlobalModel> globalModels = new ArrayList<GlobalModel>();
				JSONArray jsonArray = jsonObject.getJSONArray(Const.MEMBERS_RESULT);

				for (int i = 0; i < jsonArray.length(); i++) {
					GlobalModel item = new GlobalModel((JSONObject) jsonArray.get(i));
					globalModels.add(item);
				}

				response.setModelsList(globalModels);

				return response;
			} else {
				response.setCode(Const.E_SOMETHING_WENT_WRONG);
			}

			return null;
		}
	}

}
