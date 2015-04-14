package com.clover.spika.enterprise.chat.api.robospice;

import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class StickersSpice {
	
	public static class GetEmoji extends CustomSpiceRequest<StickersHolder> {

		public GetEmoji() {
			super(StickersHolder.class);
		}

		@Override
		public StickersHolder loadDataFromNetwork() throws Exception {
			
			Request.Builder requestBuilder = new Request.Builder()
				.headers(getGetHeaders())
				.url(Const.BASE_URL + Const.F_STICKERS_URL)
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, StickersHolder.class);
		}
	}
	
}
