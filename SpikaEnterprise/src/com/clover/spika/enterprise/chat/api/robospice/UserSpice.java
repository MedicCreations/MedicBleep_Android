package com.clover.spika.enterprise.chat.api.robospice;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Information;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.networking.GetUrl;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class UserSpice {
	
	public static class UpdateUserImage extends CustomSpiceRequest<BaseModel> {

		private Context ctx;
		
		private String image;
		private String thumb;

		public UpdateUserImage(String image, String thumb, Context context) {
			super(BaseModel.class);

			this.ctx = context;
			
			this.image = image;
			this.thumb = thumb;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {
			
			RequestBody formBody = new FormEncodingBuilder()
			.add(Const.IMAGE, image)
			.add(Const.IMAGE_THUMB, thumb)
			.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_UPDATE_USER)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}
	
	public static class UpdateUserToken extends CustomSpiceRequest<BaseModel> {

		private Context ctx;
		
		public UpdateUserToken(Context context) {
			super(BaseModel.class);

			this.ctx = context;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {
			
			RequestBody formBody = new FormEncodingBuilder()
			.add(Const.PUSH_TOKEN, SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomString(Const.PUSH_TOKEN_LOCAL))
			.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_UPDATE_PUSH_TOKEN)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}
	
	public static class Logout extends CustomSpiceRequest<BaseModel> {

		private Context ctx;
		
		public Logout(Context context) {
			super(BaseModel.class);

			this.ctx = context;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {
			
			RequestBody formBody = new FormEncodingBuilder()
			.add(Const.PUSH_TOKEN, "")
			.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_LOGOUT_API)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}
	
	public static class GetProfile extends CustomSpiceRequest<UserWrapper> {

		private Context ctx;
		
		private String userId;
		private boolean getDetailValues;
		
		public GetProfile(String userId, boolean getDetailValues, Context context) {
			super(UserWrapper.class);

			this.ctx = context;
			this.userId = userId;
			this.getDetailValues = getDetailValues;
		}

		@Override
		public UserWrapper loadDataFromNetwork() throws Exception {
			
			HashMap<String, String> getParams = new HashMap<String, String>();
			getParams.put(Const.USER_ID, userId);

			if (getDetailValues) {
				getParams.put(Const.GET_DETAIL_VALUES, "1");
			}
			
			GetUrl getParameters = new GetUrl(getParams);

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_USER_PROFILE + getParameters.toString())
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();
			
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, UserWrapper.class);
		}
	}
	
	public static class GetInformation extends CustomSpiceRequest<Information> {

		private Context ctx;
		
		public GetInformation(Context context) {
			super(Information.class);

			this.ctx = context;
		}

		@Override
		public Information loadDataFromNetwork() throws Exception {
			
			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_USER_INFORMATION)
				.get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Information.class);
		}
	}
	
	public static class UpdateUserDetails extends CustomSpiceRequest<BaseModel> {

		private Context ctx;
		
		private List<UserDetail> userDetails;
		
		public UpdateUserDetails(List<UserDetail> userDetails, Context context) {
			super(BaseModel.class);

			this.ctx = context;
			
			this.userDetails = userDetails;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {
			
			JSONArray detailsArray = new JSONArray();

			for (UserDetail detail : userDetails) {
				
				if (detail.getValue() != null) {
					JSONObject object = new JSONObject();
					object.put(detail.getKey(), detail.getValue());
					object.put(Const.PUBLIC, detail.isPublicValue());
					detailsArray.put(object);
				}
			}

			RequestBody formBody = new FormEncodingBuilder()
				.add(Const.DETAILS, detailsArray.toString())
				.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_UPDATE_USER)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}
	
	public static class UpdateUserPassword extends CustomSpiceRequest<Login> {

		private Context ctx;
		
		private String newPassword;
		private String tempPassword;
		boolean isUpdate;
		
		public UpdateUserPassword(boolean isUpdate, String tempPassword, String newPassword, Context context) {
			super(Login.class);

			this.ctx = context;
			
			this.newPassword = newPassword;
			this.tempPassword = tempPassword;
			this.isUpdate = isUpdate;
		}

		@Override
		public Login loadDataFromNetwork() throws Exception {
			
			FormEncodingBuilder formBodyBuilder = new FormEncodingBuilder();
			
			try {
				
				String hashPassword = Utils.getHexString(newPassword);
				formBodyBuilder.add(Const.NEW_PASSWORD, hashPassword);
				
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			
			Request.Builder requestBuilder = new Request.Builder()
			.headers(getPostHeaders(ctx));
			
			if (isUpdate) {
				
				requestBuilder.url(Const.BASE_URL + Const.F_UPDATE_USER_PASSWORD)
				.post(formBodyBuilder.build());
				
			} else {

				String hashTempPassword = Utils.getHexString(tempPassword);
				
				formBodyBuilder.add(Const.TEMP_PASSWORD, hashTempPassword);
				
				requestBuilder.url(Const.BASE_URL + Const.F_CHANGE_USER_PASSWORD)
				.post(formBodyBuilder.build());
			}
			
			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			Login login = mapper.readValue(responseBody, Login.class);
			
			if (login != null) {
				if (login.getCode() == Const.API_SUCCESS) {

					SpikaEnterpriseApp.getSharedPreferences(ctx).setUserTokenId(login.getToken());
					if (SpikaEnterpriseApp.getSharedPreferences(ctx).getCustomBoolean(Const.REMEMBER_CREDENTIALS)) {
						SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.PASSWORD, newPassword);
					} else {
						SpikaEnterpriseApp.getSharedPreferences(ctx).setCustomString(Const.PASSWORD, "");
					}
				}
			}
			
			return login;
		}
	}
	
	public static class InviteUsers extends CustomSpiceRequest<Chat> {

		private Context ctx;
		
		private String chatId;
		private String users;
		
		public InviteUsers(String chatId, String users, Context context) {
			super(Chat.class);

			this.ctx = context;
			
			this.chatId = chatId;
			this.users = users;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {
			
			RequestBody formBody = new FormEncodingBuilder()
				.add(Const.CHAT_ID, chatId)
				.add(Const.USERS_TO_ADD, users)
				.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_INVITE_USERS)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}
	
	public static class ForgotPassword extends CustomSpiceRequest<BaseModel> {

		private Context ctx;
		
		private String username;
		
		public ForgotPassword(String username, Context context) {
			super(BaseModel.class);

			this.ctx = context;
			
			this.username = username;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {
			
			RequestBody formBody = new FormEncodingBuilder()
				.add(Const.USERNAME, username)
				.build();

			Request.Builder requestBuilder = new Request.Builder()
				.headers(getPostHeaders(ctx))
				.url(Const.BASE_URL + Const.F_FORGOT_PASSWORD)
				.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}
	
}
