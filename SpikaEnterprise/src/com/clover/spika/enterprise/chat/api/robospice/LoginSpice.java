package com.clover.spika.enterprise.chat.api.robospice;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.PreLogin;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;

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

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getHeader());

			return getRestTemplate().postForObject(Const.BASE_URL + Const.F_PRELOGIN, request, PreLogin.class);
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

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);
			parameters.set(Const.ORGANIZATION_ID, organizationId);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getHeader());

			return getRestTemplate().postForObject(Const.BASE_URL + Const.F_LOGIN, request, Login.class);
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

			MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
			parameters.set(Const.USERNAME, username);
			parameters.set(Const.PASSWORD, password);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(parameters, getHeader());

			return getRestTemplate().getForObject(Const.BASE_URL + Const.F_LOGIN, Login.class, request);
		}
	}

}
