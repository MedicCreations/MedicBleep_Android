package com.clover.spika.enterprise.chat.api.robospice;

import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class LoginSpice {

	public static class PreLoginWithCredentials extends CustomSpiceRequest<PreLogin> {
		
		private String username;
		private String password;

		public PreLoginWithCredentials(String username, String password) {
			super(PreLogin.class);

			this.username = username;
			this.password = password;
		}

		@Override
		public PreLogin loadDataFromNetwork() throws Exception {

			RequestBody formBody = new FormEncodingBuilder()
				.add(Const.USERNAME, username)
				.add(Const.PASSWORD, password)
				.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders())
				.url(Const.BASE_URL + Const.F_PRELOGIN)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responsBody = resBody.string();

			return new ObjectMapper().readValue(responsBody, PreLogin.class);
		}
	}

	public static class LoginWithCredentials extends CustomSpiceRequest<Login> {

		private String username;
		private String password;
		private String organizationId;

		public LoginWithCredentials(String username, String password, String organizationId) {
			super(Login.class);

			this.username = username;
			this.password = password;
			this.organizationId = organizationId;
		}

		@Override
		public Login loadDataFromNetwork() throws Exception {

			RequestBody formBody = new FormEncodingBuilder().add(Const.USERNAME, username).add(Const.PASSWORD, password).add(Const.ORGANIZATION_ID, organizationId).build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_LOGIN).post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responsBody = resBody.string();
			
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responsBody, Login.class);
		}
	}

	public static class LoginWithCredentialsWithGet extends CustomSpiceRequest<Login> {

		private String username;
		private String password;

		public LoginWithCredentialsWithGet(String username, String password) {
			super(Login.class);

			this.username = username;
			this.password = password;
		}

		@Override
		public Login loadDataFromNetwork() throws Exception {

			RequestBody formBody = new FormEncodingBuilder().add(Const.USERNAME, username).add(Const.PASSWORD, password).build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_LOGIN).post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responsBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responsBody, Login.class);
		}
	}

}
