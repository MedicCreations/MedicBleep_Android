package com.medicbleep.app.chat.api.robospice;

import java.util.HashMap;

import com.medicbleep.app.chat.models.LobbyModel;
import com.medicbleep.app.chat.networking.GetUrl;
import com.medicbleep.app.chat.services.robospice.CustomSpiceRequest;
import com.medicbleep.app.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicbleep.app.chat.utils.Logger;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class LobbySpice {
	
	public static class GetLobbyByType extends CustomSpiceRequest<LobbyModel> {
		
		private int page;
		private int type;

		public GetLobbyByType(int page, int type) {
			super(LobbyModel.class);
			
			this.page = page;
			this.type = type;
		}

		@Override
		public LobbyModel loadDataFromNetwork() throws Exception {
			
			HashMap<String, String> getParams = new HashMap<String, String>();
			getParams.put(Const.PAGE, String.valueOf(page));
			getParams.put(Const.TYPE, String.valueOf(type));
			
			GetUrl urlParams = new GetUrl(getParams);

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getGetHeaders())
				.url(Const.BASE_URL + Const.F_USER_GET_LOBBY + urlParams.toString())
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			Logger.custom("e", responseBody);
			
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, LobbyModel.class);
		}
	}
	
}
