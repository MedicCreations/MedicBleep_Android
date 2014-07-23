package com.clover.spika.enterprise.chat.utils;

import android.content.Context;
import android.provider.Settings;

import java.util.UUID;

public class Const {

	// Preferences keys
	public static final String CLIENT_TOKEN_EXPIRES = "ClientTokenExpires";
	public static final String CURRENT_APP_VERSION = "CurrentAppVersion";
	public static final String REGISTRATION_ID = "RegistrationId";

	public static final String GCM_SENDER_ID = "772714193583";
	public static final String GCM_SERVER_API_KEY = "AIzaSyDccNnfu5tpyQASDT4xNV8BfI-fOGkOOeI";
//	public static final String GCM_CLIENT_API_KEY = "AIzaSyATEhCZG3oXNhH-0C9gLPHxUvAdWQPagt8";

    public static final String PREFERENCES_IS_PASSCODE_ENABLED = "preferences_is_passcode_enabled";
    public static final String PREFERENCES_STORED_PASSCODE = "preferences_stored_passcode";

	// Timestamp
	public static final String DEFAULT_DATE_FORMAT = "MM.dd. - EEEE";
	public static final long WEEK = 604800;
	public static final long DAY = 86400;

	// Change this in production to false
	public static final boolean IS_DEBUG = true;

	// Logger const values
	public static final String ERROR = "ERROR";
	public static final String DEBUG = "DEBUG";
	public static final String INFO = "INFO";
	public static final String VERBOSE = "VERBOSE";

	// App directiry
	public static final String APP_FILES_DIRECTORY = "SpikaEnterprise/";

	// Intent const
	public static final String PROFILE_INTENT = "profileIntent";
	public static final String INTENT_TYPE = "IntentType";
	public static final String PHOTO_INTENT = "PhotoIntent";
	public static final String VIDEO_INTENT = "VideoIntent";
	public static final String GALLERY_INTENT = "GalleryIntent";
	public static final String ANONYMOUS_INTENT = "AnnonymousIntent";
	public static final String FROM_WAll = "FromWall";
	public static final String FROM_NOTIFICATION = "FromNotification";

	// custom params key
	public static final String TOKEN = "token";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String ANDROID_PUSH_TOKEN = "android_push_token";
	public static final String CODE = "code";
	public static final String CHAT_NAME = "chat_name";
	public static final String CHAT_ID = "chat_id";
	public static final String LAST_MSG_ID = "last_msg_id";
	public static final String FIRST_MSG_ID = "first_msg_id";
	public static final String MESSAGES = "messages";
	public static final String MESSAGE_ID = "message_id";
	public static final String FILE_ID = "file_id";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	public static final String FILE = "file";
	public static final String TEXT = "text";
	public static final String MSG_TYPE = "type";
	public static final String TOTAL_COUNT = "total_count";
	public static final String USER_IMAGE_NAME = "user_image_name";
	public static final String IS_GROUP = "isGroup";

	public static final String PAGE = "page";
	public static final String SEARCH = "search";

	public static final String IMAGE = "image";
	public static final String USER_ID = "user_id";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String REMEMBER_CREDENTIALS = "remember_credentials";

	public static final int MSG_TYPE_DEFAULT = 1;
	public static final int MSG_TYPE_PHOTO = 2;
	public static final int MSG_TYPE_VIDEO = 3;
	public static final int MSG_TYPE_LOCATION = 4;
	public static final int MSG_TYPE_VOICE = 5;

	// Lobby
	public static final int ALL_TYPE = 0;
	public static final int USERS_TYPE = 1;
	public static final int GROUPS_TYPE = 2;
	public static final String TYPE = "type";

	// push type
	public static final int PT_MESSAGE = 1;

	// Api urls
	public static final String BASE_URL = "http://local.clover-studio.com/SpikaAPI/wwwroot/v1";
	
	public static final String F_LOGIN = "/user/login";
	public static final String F_USER_GET_GROUPS = "/groups/list";
	public static final String F_USER_GET_ALL_CHARACTERS = "/user/list";
	public static final String F_USER_GET_FILE = "/file/download";
	public static final String F_POST_MESSAGE = "post_message";
	public static final String F_GET_MESSAGES = "/message/paging";
	public static final String F_START_NEW_CHAT = "/user/chat/start";
	public static final String F_START_NEW_GROUP = "/groups/chat/start";
	public static final String F_SEND_MESSAGE = "/message/send";
	public static final String F_USER_UPLOAD_FILE = "/file/upload";
	public static final String F_UPDATE_USER = "/user/update";
	public static final String F_USER_GET_LOBBY = "/lobby/list";

	public static final int API_SUCCESS = 2000;

	// Error constants
	public static final int E_INVALID_TOKEN = 1000;
	public static final int E_EXPIRED_TOKEN = 1001;
	public static final int E_SOMETHING_WENT_WRONG = 1111;
	public static final int E_FAILED = 999;

	// Get an UUID for this phone
	public static String getUUID(Context cntx) {
		String androidID = Settings.Secure.getString(cntx.getContentResolver(), Settings.Secure.ANDROID_ID);
		return UUID.nameUUIDFromBytes(androidID.getBytes()).toString();
	}
}