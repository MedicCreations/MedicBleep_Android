package com.clover.spika.enterprise.chat.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.provider.Settings;

import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.User;

public class Const {
	
	// Version constants
	public static final String BASE_URL = "https://www.spikaent.com/dev/server/v1";
	public static final String GCM_SENDER_ID = "772714193583";
	public static final String HTTP_USER_AGENT = "SpikaEnterprise Android v1.0";
	public static final String S_PASSWORD = "jFglBLpOJQ4RLlVTl5EulWS2NLrTgHzB";
	public static final int S_ITERATIONS = 3;

	// Socket and WebRtc Constants
	public static final String WS_URL = "wss://www.spikaent.com";
	public static final String WS_PORT = "32443";
	public static final String WS_SUFIX_URL = "/socket.io/1/websocket/";
	public static final String WS_GET_SESSION_URL = "https://www.spikaent.com:32443/socket.io/1/";
	public static final String STUN_SERVER = "stun:spikaent.com:3478";
	public static final String TURN_SERVER = "turn:spikaent.com:3478";
	public static final String TURN_USER = "turn"; // same user for stun server
	public static final String TURN_PASS = "turn"; // same pass for stun server

	// Activity for result constants
	public static final int PASSCODE_ENTRY_VALIDATION_REQUEST = 21000;
	public static final int REQUEST_NEW_PASSCODE = 9001;
	public static final int REQUEST_REMOVE_PASSCODE = 9002;

	// Preferences keys
	public static final String CLIENT_TOKEN_EXPIRES = "ClientTokenExpires";
	public static final String CURRENT_APP_VERSION = "CurrentAppVersion";
	public static final String PUSH_TOKEN_LOCAL = "PushTokenLocal";
	public static final int ADMIN_REQUEST = 900;

	public static final String PREFERENCES_STORED_PASSCODE = "preferences_stored_passcode";

	// Timestamp
	public static final String DEFAULT_DATE_FORMAT = "MM.dd. - EEEE";
	public static final String DATE_SEPARATOR_FORMAT = "EEE d MMM HH:mm";
	public static final long WEEK = 604800;
	public static final long DAY = 86400;

	// ORM Lite DB
	public static final String DATABASE_NAME = "CacheDBSpice";
	public static final int DATABASE_VERSION = 1;
	public static final List<Class<?>> DATABASE_CLASSES = new ArrayList<Class<?>>() {

		private static final long serialVersionUID = -6134154163797941545L;

		{
			add(Message.class);
			add(Chat.class);
			add(User.class);
			add(Category.class);
		}
	};

	// Change this in production to falsename
	public static final boolean IS_DEBUG = true;

	public static final boolean DEBUG_CRYPTO = false;

	// Logger const values
	public static final String ERROR = "ERROR";
	public static final String DEBUG = "DEBUG";
	public static final String INFO = "INFO";
	public static final String VERBOSE = "VERBOSE";

	// App directiry
	public static final String APP_FILES_DIRECTORY = "SpikaEnterprise/";
	public static final String APP_LAZY_LIST = "LazyList";
	public static final String APP_FILED_DOWNLOADS = "Downloads";
	public static final String APP_FILED_FILES = "Files";
	public static final String APP_SPEN_FILE = "spen.txt";
	public static final String APP_SPEN_TEMP_FILE = "spentemp.txt";

	// Intent const
	public static final String PROFILE_INTENT = "profileIntent";
	public static final String INTENT_TYPE = "IntentType";
	public static final String PHOTO_INTENT = "PhotoIntent";
	public static final String VIDEO_INTENT = "VideoIntent";
	public static final String GALLERY_INTENT = "GalleryIntent";
	public static final String SHARE_INTENT = "ShareIntent";
	public static final String PATH_INTENT = "PathIntent";
	public static final String EXTRA_PATH = "ExtraPath";
	public static final String ROOM_INTENT = "RoomIntent";
	public static final String CHAT_INTENT = "ChatIntent";
	public static final int VIDEO_INTENT_INT = 9000;
	public static final int GALLERY_INTENT_INT = 9001;
	public static final int SHARE_INTENT_INT = 9002;
	public static final String ANONYMOUS_INTENT = "AnnonymousIntent";
	public static final String FROM_WAll = "FromWall";
	public static final String FROM_NOTIFICATION = "FromNotification";
	public static final String PUSH_INTENT_ACTION = "PushIntentAction";
	public static final String CHANGE_PASSCODE_INTENT = "ChangePasscodeIntent";
	public static final String IS_UPDATE_ADMIN = "IsUpdateAdmin";
	public static final String IS_UPDATE_PRIVATE_PASSWORD = "IsUpdatePrivatePassword";
	public static final String IS_UPDATE_PASSWORD = "IsUpdatePassword";
	public static final String IS_UPDATE_CATEGORY = "IsUpdateCategory";

	// Recording constants
	public static final long MAX_RECORDING_TIME_VIDEO = 30; // seconds
	public static final long MAX_RECORDING_TIME_VOICE = 180000; // milliseconds

	// File upload constant
	public static final long MAX_FILE_SIZE = 8 * 1024 * 1024;

	// custom params key
	public static final String TOKEN_BIG_T = "Token";
	public static final String TOKEN = "token";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String ANDROID_PUSH_TOKEN = "android_push_token";
	public static final String CODE = "code";
	public static final String CHAT_NAME = "chat_name";
	public static final String CHAT_ID = "chat_id";
	public static final String USER_GROUP_ROOMS = "user_group_rooms";
	public static final String USERS_TO_ADD = "user_ids";
	public static final String LAST_MSG_ID = "last_msg_id";
	public static final String FIRST_MSG_ID = "first_msg_id";
	public static final String MESSAGES = "messages";
	public static final String MESSAGE_ID = "message_id";
	public static final String PUSH_MESSAGE = "PushMessage";
	public static final String FILE_ID = "file_id";
	public static final String THUMB_ID = "thumb_id";
	public static final String PUSH_CHAT_THUMB = "chat_thumb";
	public static final String PUSH_CHAT_IMAGE = "chat_image";
	public static final String PUSH_CHAT_TYPE = "chat_type";
	public static final String LONGITUDE = "longitude";
	public static final String LATITUDE = "latitude";
	public static final String FILE = "file";
	public static final String TEXT = "text";
	public static final String MSG_TYPE = "type";
	public static final String TOTAL_COUNT = "total_count";
	public static final String USER_IMAGE_NAME = "user_image_name";
	public static final String USER_THUMB_IMAGE_NAME = "user_thumb_image_name";
	public static final String IS_GROUP = "isGroup";
	public static final String PUSH_TOKEN = "push_token";
	public static final String ROOM_FILE_ID = "room_file_id";
	public static final String ROOM_THUMB_ID = "room_thumb_id";
	public static final String NAME = "name";
	public static final String IS_ADMIN = "is_admin";
	public static final String IS_DELETED = "is_deleted";
	public static final String IS_ACTIVE = "is_active";
	public static final String USER_IDS = "user_ids";
	public static final String ADMIN_ID = "admin_id";
	public static final String GROUP_IDS = "group_ids";
	public static final String GROUP_ALL_IDS = "group_all_ids";
	public static final String ROOM_IDS = "room_ids";
	public static final String ROOM_ALL_IDS = "room_all_ids";
	public static final String APP_VERSION = "app_version";
	public static final String PLATFORM = "platform";
	public static final String GET_DETAIL_VALUES = "get_detail_values";
	public static final String DETAILS = "details";
	public static final String NEW_PASSWORD = "new_password";
	public static final String TEMP_PASSWORD = "temp_password";
	public static final String IS_PRIVATE = "is_private";
	public static final String PUSH_CHAT_PASSWORD = "chat_password";
	public static final String PUBLIC = "public";
	public static final String SEARCH_RESULT = "search_result";
	public static final String MEMBERS_RESULT = "members_result";

	public static final String IS_SQUARE = "is_square";

	public static final String USER = "user";
	public static final String GROUP = "group";
	public static final String CHAT = "chat";

	public static final String ROOT_ID = "root_id";
	public static final String PARENT_ID = "parent_id";

	public static final String PAGE = "page";
	public static final String SEARCH = "search";

	public static final String IMAGE = "image";
	public static final String IMAGE_THUMB = "image_thumb";
	public static final String USER_ID = "user_id";
	public static final String GROUP_ID = "group_id";
	public static final String GROUP_NAME = "groupname";
	public static final String ROOM_ID = "room_id";
	public static final String ROOM_NAME = "room_name";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String SCREEN_TITLE = "ScreenTitle";
	public static final String REMEMBER_CREDENTIALS = "remember_credentials";
	public static final String USER_WRAPPER = "user_wrapper";

	public static final String CATEGORY_ID = "category_id";
	public static final String CATEGORY_NAME = "category_name";
	public static final String URL = "url";

	public static final String ORGANIZATION_ID = "organization_id";
	public static final String ORGANIZATION_NAME = "organization_name";
	public static final String ORGANIZATIONS = "organizations";

	public static final int MSG_TYPE_DEFAULT = 1;
	public static final int MSG_TYPE_PHOTO = 2;
	public static final int MSG_TYPE_VIDEO = 3;
	public static final int MSG_TYPE_LOCATION = 4;
	public static final int MSG_TYPE_VOICE = 5;
	public static final int MSG_TYPE_FILE = 6;
	public static final int MSG_TYPE_DELETED = 7;
	public static final int MSG_TYPE_GIF = 9;
	public static final int MSG_TYPE_TEMP_MESS = 999;
	public static final int MSG_TYPE_TEMP_MESS_ERROR = 9999;

	public static final String PUSH_TYPE = "PushType";
	public static final int PUSH_TYPE_MSG = 1;
	public static final int PUSH_TYPE_SEEN = 2;

	// Lobby
	public static final int ALL_TYPE = 0;
	public static final int USERS_TYPE = 1;
	public static final int GROUPS_TYPE = 2;
	public static final int ALL_TOGETHER_TYPE = 3;
	public static final String TYPE = "type";

	// Chat type
	public static final int C_PRIVATE = 1;
	public static final int C_GROUP = 2;
	public static final int C_ROOM = 3;
	public static final int C_ROOM_ADMIN_ACTIVE = 4;
	public static final int C_ROOM_ADMIN_INACTIVE = 5;

	// update chat type
	public static final int UPDATE_CHAT_EDIT = 1;
	public static final int UPDATE_CHAT_DEACTIVATE = 2;
	public static final int UPDATE_CHAT_DELETE = 3;
	public static final int UPDATE_CHAT_ACTIVATE = 4;

	// Api urls
	public static final String F_PRELOGIN = "/user/prelogin";
	public static final String F_LOGIN = "/user/login";
	public static final String F_USER_GET_GROUPS = "/groups/list";
	public static final String F_INVITE_USERS = "/chat/member/add";
	public static final String F_USER_GET_FILE = "/file/download";
	public static final String F_POST_MESSAGE = "post_message";
	public static final String F_GET_MESSAGES = "/message/paging";
	public static final String F_LEAVE_CHAT = "/chat/leave";
	public static final String F_LEAVE_CHAT_ADMIN = "/chat/member/remove";
	public static final String F_START_NEW_CHAT = "/user/chat/start";
	public static final String F_START_NEW_GROUP = "/groups/chat/start";
	public static final String F_SEND_MESSAGE = "/message/send";
	public static final String F_USER_UPLOAD_FILE = "/file/upload";
	public static final String F_UPDATE_USER = "/user/update";
	public static final String F_UPDATE_USER_PASSWORD = "/user/password/update";
	public static final String F_CHANGE_USER_PASSWORD = "/user/password/change";
	public static final String F_UPDATE_PUSH_TOKEN = "/user/pushtoken/android/update";
	public static final String F_LOGOUT_API = "/user/logout";
	public static final String F_USER_GET_LOBBY = "/lobby/list";
	public static final String F_DELETE_MESSAGE = "/message/delete";
	public static final String F_GET_THREADS = "/message/child/list";
	public static final String F_CREATE_ROOM = "/room/create";
	public static final String F_UPDATE_CHAT = "/chat/update";
	public static final String F_GET_CATEGORIES = "/category/list";
	public static final String F_USER_PROFILE = "/user/profile";
	public static final String F_USER_PUSH = "/user/push";
	public static final String F_USER_GET_ROOMS = "/room/list";
	public static final String F_USER_INFORMATION = "/user/information";
	public static final String F_GROUP_MEMBERS = "/groups/members";
	public static final String F_USERS_AND_GROUPS_FOR_ROOMS = "/room/search/all";
	public static final String F_GET_DISTINC_USER = "/room/add/users";
	public static final String F_FORGOT_PASSWORD = "/user/password/forgot";
	public static final String F_CHANGE_PASSWORD = "/user/password/change";
	public static final String F_GLOBAL_SEARCH_URL = "/search/list";
	public static final String F_GLOBAL_MEMBERS_URL = "/member/list";
	public static final String F_STICKERS_URL = "/message/stickers";

	public static final int API_SUCCESS = 2000;

	// Error constants
	public static final int E_FAILED 						= 999;
	public static final int E_INVALID_TOKEN 				= 1000;
	public static final int E_EXPIRED_TOKEN 				= 1001;
	public static final int E_DIR_NOT_WRITABLE 				= 1002;
	public static final int E_INVALID_LOGIN 				= 1003;
	public static final int E_NO_CHILD_MSGS 				= 1004;
	public static final int E_NOT_CHAT_MEMBER 				= 1005;
	public static final int E_CHAT_INACTIVE 				= 1006;
	public static final int E_CHAT_DELETED 					= 1007;
	public static final int E_PAGE_NOT_FOUND 				= 1008;
	public static final int E_NOT_GROUP_ADMIN 				= 1009;
	public static final int E_EMAIL_MISSING 				= 1010;
	public static final int E_TEMP_PASSWORD_NOT_VALID 		= 1011;
	public static final int E_LOGIN_WITH_TEMP_PASS 			= 1012;
	public static final int E_USERNAME_NOT_EXIST 			= 1013;
	public static final int E_INVALID_TEMP_PASSWORD			= 1014;
	public static final int E_PASSWORD_EXIST 				= 1015;
	public static final int E_ROOM_LIMIT_EXCEEDED	 		= 1016;
	public static final int E_FILE_LIMIT_EXCEEDED 			= 1017;
	public static final int E_ORGANIZATION_SUSPENDED 		= 1018;
	public static final int E_ORGANIZATION_DELETED 			= 1019;
	public static final int E_SOMETHING_WENT_WRONG 			= 1111;

	// extras
	public static final String EXTRA_ROOT_ID = "com.clover.spika.enterprise.root_id";
	public static final String EXTRA_MESSAGE_ID = "com.clover.spika.enterprise.message_id";

	// Get an UUID for this phone
	public static String getUUID(Context cntx) {
		String androidID = Settings.Secure.getString(cntx.getContentResolver(), Settings.Secure.ANDROID_ID);
		return UUID.nameUUIDFromBytes(androidID.getBytes()).toString();
	}

	/* Get encryption password */
	public static char[] getPassword() {
		StringBuilder builder = new StringBuilder();
		builder.append(S_PASSWORD);
		return builder.toString().toCharArray();
	}

	public static final String UPDATE_PICTURE = "update_picture";
	public static final String DEFAULT_IMAGE_USER = "default_user_image";
	public static final String DEFAULT_IMAGE_GROUP = "default_group_image";
	public static final String FROM_CHAT = "from_chat";
	public static final String POSITION = "position";
	public static final String STICEKRS_HOLDER = "stickersHolder";
	public static final String ACTION_REFRESH_ROOMS = "actionRefreshRoom";

	public static final String SOCKET_ACTION = "socketCheckAvailableAction";
	public static final String CALL_ACTION = "callAction";
	public static final String SESSION_ID = "SessionId";
	public static final String AVAILABLE_TYPE = "AvailableType";
	public static final int ACTION_IDLE = 0;
	public static final int ACTION_CHECK = 1;
	public static final int ACTION_CALL = 2;
	public static final int ACTION_CALL_CANCEL = 3;
	public static final int ACTION_CALL_DECLINE = 4;
	public static final int ACTION_CALL_RINGING = 5;
	public static final int ACTION_CALL_ACCEPT = 6;
	public static final int ACTION_LEAVE_MY_ROOM = 7;
	public static final int ACTION_JOIN_OTHER_ROOM = 8;
	public static final int ACTION_WAIT_FOR_CANDIDATE = 9;
	public static final int ACTION_CALL_END = 10;
	public static final int ACTION_LEAVE_OTHER_ROOM = 11;
	public static final String USER_AVAILABLE = "UserAvailable";
	public static final String USER_BUSY = "USerBusy";
	public static final String USER_NOT_CONNECTED = "UserNotConnected";
	public static final String TYPE_OF_SOCKET_RECEIVER = "TypeOfSocketReceiver";
	public static final int CHECK_USER_AVAILABLE = 1;
	public static final int CALL_USER = 2;
	public static final int CALL_ENDED = 3;
	public static final int CALL_RECEIVE = 4;
	public static final int CALL_ANSWER = 5;
	public static final int CALL_CONNECT = 6;
	public static final int CALL_ACCEPTED = 7;
	public static final int CALL_RINGING = 8;
	public static final int CALL_CANCELED = 9;
	public static final int WEB_SOCKET_OPENED = 10;
	public static final String CANDIDATE = "Candidate";
	public static final String IS_VIDEO_ACCEPT = "VideoAccept";
	public static final String TO_LEAVE_MESSAGE = "toLeaveMessage";

	public static final int TIMEOUT_FOR_CALL = 30000;
	public static final int CALL_ACTIVITY_REQUEST = 99;
	public static final String IS_APLICATION_OPEN = "IsAppOpened";
	public static final String IS_CALL_ACTIVE = "IsCAllACtive";
	public static final String ACTIVE_CLASS = "ClassActive";
	public static final String INTERNET_CONNECTION_CHANGE_ACTION = "internetConnectionChangeAction";
	public static final String INTERNET_STATE = "internetState";
	public static final int HAS_INTERNET = 1;
	public static final int HAS_NOT_INTERNET = -1;

	public static final class SoundControl {
		public static final int PLAY_BUTTON = 0;
		public static final int DOWNLOAD_PROGRESS = 1;
		public static final int CHRONOMETER = 2;
		public static final int PERCENT_TV = 3;
		public static final int SEEKBAR = 4;
		public static final int PROGREEBAR = 5;
	}

	public static final class WebRTCCall {
		public static final String CALL_OFFER = "callOffer";
		public static final String CALL_CANCEL = "callCancel";
		public static final String CALL_DECLINE = "callDecline";
		public static final String CALL_END = "callEnd";
		public static final String CALL_ANSWER = "callAnswer";
		public static final String CALL_RINGING = "callRinging";
		public static final String CALL_MUTE = "mute";
		public static final String CALL_UNMUTE = "unmute";
		public static final String CALL_MUTE_REMOTE_VIDEO = "muteRemoteVideo";
	}

}