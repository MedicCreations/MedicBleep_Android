package com.clover.spika.enterprise.chat.api.robospice;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.SendMessageResponse;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceRequest;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

public class ChatSpice {

	public static class AddUsersToRoom extends CustomSpiceRequest<Chat> {

		private String userToAdd;
		private String groupIds;
		private String roomIds;
		private String groupsAll;
		private String roomsAll;
		private String chatId;

		public AddUsersToRoom(final String userToAdd, final String groupIds, final String roomIds, final String groupsAll, final String roomsAll,
				final String chatId) {
			super(Chat.class);

			this.userToAdd = userToAdd;
			this.groupIds = groupIds;
			this.roomIds = roomIds;
			this.groupsAll = groupsAll;
			this.roomsAll = roomsAll;
			this.chatId = chatId;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			RequestBody formBody = new FormEncodingBuilder().add(Const.USERS_TO_ADD, userToAdd).add(Const.CHAT_ID, chatId)
					.add(Const.GROUP_IDS, groupIds).add(Const.GROUP_ALL_IDS, groupsAll).add(Const.ROOM_IDS, roomIds)
					.add(Const.ROOM_ALL_IDS, roomsAll).build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_INVITE_USERS)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class UpdateChatAll extends CustomSpiceRequest<Chat> {

		private HashMap<String, String> requestParams;

		public UpdateChatAll(HashMap<String, String> requestParams) {
			super(Chat.class);
			this.requestParams = requestParams;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();

			for (Map.Entry<String, String> param : requestParams.entrySet()) {
				formBuilder.add(param.getKey(), param.getValue());
			}

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_UPDATE_CHAT)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();
			
			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class UpdateChat extends CustomSpiceRequest<BaseModel> {

		private String chatId;
		private int type;
		private String image;
		private String imageThumb;
		private String name;

		public UpdateChat(String chatId, int type, String image, String image_thumb, String name) {
			super(BaseModel.class);
			this.chatId = chatId;
			this.type = type;
			this.image = image;
			this.imageThumb = image_thumb;
			this.name = name;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();

			formBuilder.add(Const.CHAT_ID, chatId);

			switch (type) {
			case Const.UPDATE_CHAT_EDIT:
				formBuilder.add(Const.NAME, name);
				formBuilder.add(Const.IMAGE, image);
				formBuilder.add(Const.IMAGE_THUMB, imageThumb);
				break;
			case Const.UPDATE_CHAT_DEACTIVATE:
				formBuilder.add(Const.IS_ACTIVE, "0");
				break;
			case Const.UPDATE_CHAT_DELETE:
				formBuilder.add(Const.IS_DELETED, "1");
				break;
			case Const.UPDATE_CHAT_ACTIVATE:
				formBuilder.add(Const.IS_ACTIVE, "1");
				break;
			default:
				break;
			}

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_UPDATE_CHAT)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}

	public static class CreateRoom extends CustomSpiceRequest<Chat> {

		private String name;
		private String image;
		private String imageThumb;
		private String usersToAdd;
		private String groupToAdd;
		private String roomToAdd;
		private String categoryId;
		private String isPrivate;
		private String password;

		public CreateRoom(String name, String image, String image_thumb, String users_to_add, String group_to_add, String room_to_add,
				String categoryId, String is_private, String password) {
			super(Chat.class);
			this.name = name;
			this.image = image;
			this.imageThumb = image_thumb;
			this.usersToAdd = users_to_add;
			this.groupToAdd = group_to_add;
			this.roomToAdd = room_to_add;
			this.categoryId = categoryId;
			this.isPrivate = is_private;
			this.password = password;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();

			formBuilder.add(Const.NAME, name);
			formBuilder.add(Const.IMAGE, image);
			formBuilder.add(Const.IMAGE_THUMB, imageThumb);
			formBuilder.add(Const.USERS_TO_ADD, usersToAdd);
			formBuilder.add(Const.GROUP_IDS, groupToAdd);
			formBuilder.add(Const.ROOM_IDS, roomToAdd);
			formBuilder.add(Const.CATEGORY_ID, categoryId);
			formBuilder.add(Const.IS_PRIVATE, isPrivate);
			formBuilder.add(Const.PASSWORD, password);

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_CREATE_ROOM)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class DeleteMessage extends CustomSpiceRequest<BaseModel> {

		private String messageId;

		public DeleteMessage(String messageId) {
			super(BaseModel.class);
			this.messageId = messageId;
		}

		@Override
		public BaseModel loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();
			formBuilder.add(Const.MESSAGE_ID, messageId);

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_DELETE_MESSAGE)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, BaseModel.class);
		}
	}

	public static class GetThreads extends CustomSpiceRequest<Chat> {

		private String messageId;

		public GetThreads(String messageId) {
			super(Chat.class);
			this.messageId = messageId;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			Request.Builder requestBuilder = new Request.Builder().headers(getGetHeaders())
					.url(Const.BASE_URL + Const.F_GET_THREADS + "?" + Const.ROOT_ID + "=" + messageId).get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class LeaveChatAdmin extends CustomSpiceRequest<Chat> {

		private String chatId;
		private String userIds;
		private String groupIds;
		private String roomIds;

		public LeaveChatAdmin(String chatId, String userIds, String groupIds, String roomIds) {
			super(Chat.class);
			this.chatId = chatId;
			this.userIds = userIds;
			this.groupIds = groupIds;
			this.roomIds = roomIds;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();
			formBuilder.add(Const.CHAT_ID, chatId);
			formBuilder.add(Const.USER_IDS, userIds);
			formBuilder.add(Const.GROUP_IDS, groupIds);
			formBuilder.add(Const.ROOM_IDS, roomIds);

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_LEAVE_CHAT_ADMIN)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class LeaveChat extends CustomSpiceRequest<Chat> {

		private String chatId;

		public LeaveChat(String chatId) {
			super(Chat.class);
			this.chatId = chatId;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();
			formBuilder.add(Const.CHAT_ID, chatId);

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_LEAVE_CHAT)
					.post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}

	public static class SendMessage extends CustomSpiceRequest<SendMessageResponse> {

		private int type;
		private String chatId;
		private String text;
		private String fileId;
		private String thumbId;
		private String longitude;
		private String latitude;
		private String rootId;
		private String parentId;
		private String attributes = null;

		public SendMessage(int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude, String rootId,
				String parentId) {
			super(SendMessageResponse.class);

			this.type = type;
			this.chatId = chatId;
			this.text = text;
			this.fileId = fileId;
			this.thumbId = thumbId;
			this.longitude = longitude;
			this.latitude = latitude;
			this.rootId = rootId;
			this.parentId = parentId;
		}
		
		public SendMessage(String attributes, int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude, String rootId,
				String parentId) {
			super(SendMessageResponse.class);

			this.type = type;
			this.chatId = chatId;
			this.text = text;
			this.fileId = fileId;
			this.thumbId = thumbId;
			this.longitude = longitude;
			this.latitude = latitude;
			this.rootId = rootId;
			this.parentId = parentId;
			this.attributes = attributes;
		}

		@Override
		public SendMessageResponse loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();

			formBuilder.add(Const.CHAT_ID, chatId);
			formBuilder.add(Const.MSG_TYPE, String.valueOf(type));

			if (!TextUtils.isEmpty(text)) {
				formBuilder.add(Const.TEXT, JNAesCrypto.encryptJN(text));
			}

			if (!TextUtils.isEmpty(fileId)) {
				formBuilder.add(Const.FILE_ID, fileId);
			}

			if (!TextUtils.isEmpty(thumbId)) {
				formBuilder.add(Const.THUMB_ID, thumbId);
			}

			if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
				formBuilder.add(Const.LONGITUDE, JNAesCrypto.encryptJN(longitude));
				formBuilder.add(Const.LATITUDE, JNAesCrypto.encryptJN(latitude));
			}

			if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
				formBuilder.add(Const.LONGITUDE, JNAesCrypto.encryptJN(longitude));
				formBuilder.add(Const.LATITUDE, JNAesCrypto.encryptJN(latitude));
			}

			if (!TextUtils.isEmpty(rootId)) {
				formBuilder.add(Const.ROOT_ID, rootId);
			}

			if (!TextUtils.isEmpty(parentId)) {
				formBuilder.add(Const.PARENT_ID, parentId);
			}
			
			if (!TextUtils.isEmpty(attributes)) {
				formBuilder.add(Const.ATTRIBUTES, attributes);
			}
			
			RequestBody formBody = formBuilder.build();
			
			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + Const.F_SEND_MESSAGE)
					.post(formBody);
			
			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, SendMessageResponse.class);

		}
	}

	public static class GetMessages extends CustomSpiceRequest<Chat> {

		private boolean isClear;
		private boolean isPagging;
		private boolean isNewMessage;
		private boolean isSend;
		private boolean isRefresh;
		private String chatId;
		private String msgId;
		private int adapterCount;

		public GetMessages(boolean isClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh, String chatId, String msgId,
				int adapterCount) {
			super(Chat.class);

			this.isClear = isClear;
			this.isPagging = isPagging;
			this.isNewMessage = isNewMsg;
			this.isSend = isSend;
			this.isRefresh = isRefresh;
			this.chatId = chatId;
			this.msgId = msgId;
			this.adapterCount = adapterCount;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			String url = Const.BASE_URL + Const.F_GET_MESSAGES + "?" + Const.CHAT_ID + "=" + chatId;

			if (isPagging) {
				if (!isClear && adapterCount != -1 && adapterCount > 0) {
					url = url + "&" + Const.LAST_MSG_ID + "=" + msgId;
				}
			} else if (isNewMessage) {
				if ((adapterCount - 1) >= 0) {
					url = url + "&" + Const.FIRST_MSG_ID + "=" + msgId;
				}
			}

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(url).get();

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();
			
			ObjectMapper mapper = new ObjectMapper();

            Logger.i("get message: " + responseBody);

			Chat result = mapper.readValue(responseBody, Chat.class);

			if (result != null && result.getCode() == Const.API_SUCCESS) {

				result.isNewMsg = isNewMessage;
				result.isRefresh = isRefresh;
				result.isClear = isClear;
				result.isSend = isSend;
				result.isPagging = isPagging;

				return result;
			} else {
				return result;
			}
		}
	}

	public static class StartChat extends CustomSpiceRequest<Chat> {

		private boolean isGroup;
		private String userId;
		private String firstName;
		private String lastName;

		public StartChat(boolean isGroup, String userId, String firstname, String lastname) {
			super(Chat.class);
			
			this.isGroup = isGroup;
			this.userId = userId;
			this.firstName = firstname;
			this.lastName = lastname;
		}

		@Override
		public Chat loadDataFromNetwork() throws Exception {

			FormEncodingBuilder formBuilder = new FormEncodingBuilder();

			String url = Const.F_START_NEW_CHAT;

			if (isGroup) {
				url = Const.F_START_NEW_GROUP;

				formBuilder.add(Const.GROUP_ID, userId);
				formBuilder.add(Const.GROUP_NAME, firstName);
			} else {
				formBuilder.add(Const.USER_ID, userId);
				formBuilder.add(Const.FIRSTNAME, firstName);
				formBuilder.add(Const.LASTNAME, lastName);
			}

			RequestBody formBody = formBuilder.build();

			Request.Builder requestBuilder = new Request.Builder().headers(getPostHeaders()).url(Const.BASE_URL + url).post(formBody);

			Call connection = getOkHttpClient().newCall(requestBuilder.build());

			Response res = connection.execute();
			ResponseBody resBody = res.body();
			String responseBody = resBody.string();

            Logger.i("start chat: " + responseBody);

			ObjectMapper mapper = new ObjectMapper();

			return mapper.readValue(responseBody, Chat.class);
		}
	}
}
