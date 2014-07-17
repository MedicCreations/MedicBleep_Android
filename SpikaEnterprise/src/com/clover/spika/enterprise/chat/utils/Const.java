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
    public static final String GCM_CLIENT_API_KEY = "AIzaSyATEhCZG3oXNhH-0C9gLPHxUvAdWQPagt8";

    // Timestamp
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
    public static final String APP_FILES_DIRECTORY = "VectorChat/";

    // Intent const
    public static final String INTENT_TYPE = "IntentType";
    public static final String PHOTO_INTENT = "PhotoIntent";
    public static final String VIDEO_INTENT = "VideoIntent";
    public static final String GALLERY_INTENT = "GalleryIntent";
    public static final String ANONYMOUS_INTENT = "AnnonymousIntent";
    public static final String FROM_WAll = "FromWall";
    public static final String FROM_NOTIFICATION = "FromNotification";

    // custom params key
    public static final String VECTOR_CHAT = "Vector Chat";
    public static final String MODULE = "module";
    public static final String FUNCTION = "function";
    public static final String TOKEN = "token";
    public static final String TOKEN_DEFAULT = "tokendefault";
    public static final String REQDATA = "reqdata";
    public static final String UUID_KEY = "uuid";
    public static final String USERNAME = "username";
    public static final String ANDROID_PUSH_TOKEN = "android_push_token_list";
    public static final String CODE = "code";
    public static final String CHARACTER_ID = "character_id";
    public static final String GROUP_NAME = "group_name";
    public static final String GROUP_ID = "group_id";
    public static final String LAST_MSG_ID = "last_msg_id";
    public static final String FIRST_MSG_ID = "first_msg_id";
    public static final String ITEMS = "items";
    public static final String NEW_GROUP_PERIOD = "new_group_period";
    public static final String MESSAGE_ID = "message_id";
    public static final String FILE_NAME = "file_name";
    public static final String FILE = "file";
    public static final String FILE_ID = "file_id";
    public static final String ADD_MEMBERS = "add_members";
    public static final String KICK_MEMBERS = "kick_members";
    public static final String ANONYMOUS_NICK = "anonymous";
    public static final String TEXT = "text";
    public static final String MSG_TYPE = "type";
    public static final String MSG_BUBBLE_TYPE = "bubble_type";
    public static final String TOTAL_ITEMS = "total_items";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_SIZE = "pageSize";

    public static final String IMAGE_NAME = "image_name";
    public static final String USER_ID = "user_id";
    public static final String USER_IMAGE_NAME = "user_image_name";
    public static final String USER_NICKNAME = "user_nickname";
    public static final String EMAIL = "email";
    public static final String MESSAGE = "message";

    // Message types
    public static final int MSG_BUBBLE_DEFAULT = 0;
    public static final int MSG_BUBBLE_ANONIMOUS = 1;
    public static final int MSG_BUBBLE_CLOUD = 2;
    public static final int MSG_BUBBLE_TRIANGLES = 3;
    public static final int MSG_BUBBLE_SQUARE = 4;

    public static final int MSG_TYPE_DEFAULT = 0;
    public static final int MSG_TYPE_PHOTO = 1;
    public static final int MSG_TYPE_VIDEO = 2;
    public static final int MSG_TYPE_STICKER = 3;

    // push type
    public static final int PT_MESSAGE = 1;
    public static final int PT_REPORT = 2;
    public static final int PT_GROUP_CREATED = 3;

    // Api urls
    public static final String BASE_URL = "http://local.clover-studio.com/SpikaAPI/wwwroot/v1";

    public static final int M_USERS = 1;
    public static final int M_CHAT = 4;
    public static final int M_PUSH = 5;

    public static final String F_LOGIN = "/user/login";
    public static final String F_USER_CREATE_CHARACTER = "create_character";
    public static final String F_USER_GET_ALL_CHARACTERS = "get_all_characters";
    public static final String F_USER_CREATE_GROUP = "create_group";
    public static final String F_USER_GET_GROUPS = "/groups/list";
    public static final String F_USER_DELETE_GROUP = "delete_group";
    public static final String F_USER_ADD_MEMBER = "add_member";
    public static final String F_USER_KICK_MEMBER = "kick_member";
    public static final String F_USER_GET_GROUP_MEMBERS = "get_group_members";
    public static final String F_USER_GET_FILE = "get_file";
    public static final String F_POST_MESSAGE = "post_message";
    public static final String F_GET_MESSAGES = "get_messages_paging";
    public static final String F_GET_NEW_MESSAGES = "get_messages_new";
    public static final String F_RATE_MESSAGE = "rate_message";
    public static final String F_DELETE_MESSAGE = "delete_message";
    public static final String F_REPORT_MESSAGE = "report_message";

    // TODO
    // define ('UPDATE_CHARACTER_PUSH_TOKEN', 'update_push_token');
    // define ('USER_UPLOAD_FILE', 'upload_file');

    public static final int LIKE_MSG = 1;
    public static final int REPORT_MSG = 2;

    // Error constants
    public static final int E_INVALID_TOKEN = 1000;
    public static final int E_EXPIRED_TOKEN = 1001;
    public static final int E_SOMETHING_WENT_WRONG = 1111;
    public static final int E_MESSAGE_ALLREADY_REPORTED = 1020;
    public static final int E_YOUR_ACC_IS_BLOCKED = 1021;
    public static final int E_GROUP_DELETED = 1022;
    public static final int E_NG_WORD_DETECTED = 1023;
    public static final int E_SUCCESS = 2000;
    public static final int E_FAILED = 999;

    // Ok Cancel types
    public static final int T_DELETE_MSG = 987;

    // Code values from web
    public static final int C_SUCCESS = 2000;

    // Get an UUID for this phone
    public static String getUUID(Context cntx) {
        String androidID = Settings.Secure.getString(cntx.getContentResolver(), Settings.Secure.ANDROID_ID);
        return UUID.nameUUIDFromBytes(androidID.getBytes()).toString();
    }
}