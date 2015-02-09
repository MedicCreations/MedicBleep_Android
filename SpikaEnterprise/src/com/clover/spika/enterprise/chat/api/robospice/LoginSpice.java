package com.clover.spika.enterprise.chat.api.robospice;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import android.content.Context;

import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;

public class LoginSpice {

	public static class PreLoginWithCredentials extends CustomSpiceRequest<PreLogin> {

		private Context ctx;
		private String username;
		private String password;

		public PreLoginWithCredentials(String username, String password, Context context) {
			super(PreLogin.class);

			this.ctx = context;
			this.username = username;
			this.password = password;
		}

		@Override
		public PreLogin loadDataFromNetwork() throws Exception {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getPostHeader(ctx));

			return getRestTemplate().postForObject(Const.BASE_URL + Const.F_PRELOGIN, request, PreLogin.class);
		}
	}

	public static class LoginWithCredentials extends CustomSpiceRequest<Login> {

		private Context ctx;
		private String username;
		private String password;
		private String organizationId;

		public LoginWithCredentials(String username, String password, String organizationId, Context context) {
			super(Login.class);

			this.ctx = context;
			this.username = username;
			this.password = password;
			this.organizationId = organizationId;
		}

		@Override
		public Login loadDataFromNetwork() throws Exception {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);
			parameters.set(Const.ORGANIZATION_ID, organizationId);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getPostHeader(ctx));

			return getRestTemplate().postForObject(Const.BASE_URL + Const.F_LOGIN, request, Login.class);
		}
	}

	public static class LoginWithCredentialsWithGet extends CustomSpiceRequest<Login> {

		private Context ctx;
		private String username;
		private String password;

		public LoginWithCredentialsWithGet(String username, String password, Context context) {
			super(Login.class);

			this.ctx = context;
			this.username = username;
			this.password = password;
		}

		@Override
		public Login loadDataFromNetwork() throws Exception {

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getPostHeader(ctx));

			return getRestTemplate().getForObject(Const.BASE_URL + Const.F_LOGIN, Login.class, request);
		}
	}

}
