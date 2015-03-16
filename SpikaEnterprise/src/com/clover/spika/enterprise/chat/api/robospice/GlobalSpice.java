package com.clover.spika.enterprise.chat.api.robospice;

import java.util.HashMap;

import org.apache.http.util.TextUtils;

import android.content.Context;

import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.networking.GetUrl;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class GlobalSpice {
	
	public static class GlobalSearch extends CustomSpiceRequest<GlobalResponse> {

		private Context ctx;
		
		private int page;
		private String chatId;
		private String categoryId;
		private int type;
		private String searchTerm;
		
		public GlobalSearch(int page, String chatId, String categoryId, int type, String searchTerm, Context context) {
			super(GlobalResponse.class);

			this.ctx = context;
			
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
			
			Request.Builder requestBuilder = new Request.Builder()
				.headers(getGetHeaders(ctx))
				.url(Const.BASE_URL + Const.F_GLOBAL_SEARCH_URL + getParameters.toString())
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, GlobalResponse.class);
		}
	}
	
	public static class GlobalMembers extends CustomSpiceRequest<GlobalResponse> {

		private Context ctx;
		
		private int page;
		private String chatId;
		private String groupId;
		private int type;
		
		public GlobalMembers(int page, String chatId, String groupId, int type, Context context) {
			super(GlobalResponse.class);

			this.ctx = context;
			
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
			
			Request.Builder requestBuilder = new Request.Builder()
				.headers(getGetHeaders(ctx))
				.url(Const.BASE_URL + Const.F_GLOBAL_MEMBERS_URL + getParameters.toString())
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, GlobalResponse.class);
		}
	}
	
}
