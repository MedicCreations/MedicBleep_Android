package com.medicbleep.app.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.medicbleep.app.chat.adapters.MessagesAdapter;
import com.medicbleep.app.chat.api.ApiCallback;
import com.medicbleep.app.chat.api.FileManageApi;
import com.medicbleep.app.chat.api.robospice.ChatSpice;
import com.medicbleep.app.chat.caching.ChatCaching.OnChatDBChanged;
import com.medicbleep.app.chat.caching.ChatCaching.OnChatNetworkResult;
import com.medicbleep.app.chat.caching.robospice.ChatCacheSpice;
import com.medicbleep.app.chat.caching.robospice.ChatCacheSpice.StartChat;
import com.medicbleep.app.chat.caching.robospice.EntryUtilsCaching;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.medicbleep.app.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.medicbleep.app.chat.extendables.BaseChatActivity;
import com.medicbleep.app.chat.extendables.BaseModel;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.listeners.OnInternetErrorListener;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.models.Message;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.models.SendMessageResponse;
import com.medicbleep.app.chat.models.Stickers;
import com.medicbleep.app.chat.models.UploadFileModel;
import com.medicbleep.app.chat.models.User;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.LocationUtility;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.emoji.SelectEmojiListener;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ChatActivity extends BaseChatActivity implements OnChatDBChanged, OnChatNetworkResult, OnInternetErrorListener {

	private TextView noItems;

	public MessagesAdapter adapter;

	private int totalItems = 0;
	private String mUserId;

	private boolean isRunning = false;
	private boolean isResume = false;
	private boolean isOnCreate = false;

    private SwipeRefreshLayout swipeControll;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		noItems = (TextView) findViewById(R.id.noItems);
        swipeControll = (SwipeRefreshLayout) findViewById(R.id.swipeControll);
        swipeControll.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
				boolean isClear = false, processing = false, isPagging = true, isNewMsg = false, isSend = false, isRefresh = false, isFirstTime = false;
				getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
            }
        });

		adapter = new MessagesAdapter(spiceManager, this, new ArrayList<Message>());
		chatListView.setAdapter(adapter);
		chatListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getAdapter() != null) {
					Message message = (Message) parent.getAdapter().getItem(position);
					if (message.getType() == Const.MSG_TYPE_TEMP_MESS_ERROR) {
						/* resend message */
						showResendDialog(message);
					} else if (message.getType() != Const.MSG_TYPE_DELETED && message.getType() != Const.MSG_TYPE_TEMP_MESS) {
						int rootId = message.getRootId() == 0 ? message.getIntegerId() : message.getRootId();
//						ThreadsActivity.start(ChatActivity.this, String.valueOf(rootId), message.getChat_id(), message.getId(), chatImageThumb,
//								chatImage, chatName, mUserId);
					}
				}
			}
		});

		chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getAdapter() != null) {
					Message message = (Message) parent.getAdapter().getItem(position);
					if (message.isMe() && message.type != Const.MSG_TYPE_DELETED) {
						deleteMessage(message);
					}
				}
				return true;
			}
		});

		adapter.setOnLongAndSimpleClickCustomListener(new MessagesAdapter.OnMessageLongAndSimpleClickCustomListener() {

			@Override
			public void onLongClick(Message message) {
				if (message.isMe() && message.type != Const.MSG_TYPE_DELETED) {
					deleteMessage(message);
				}
			}

			@Override
			public void onSimpleClick(Message message) {
				if (message.getType() == Const.MSG_TYPE_TEMP_MESS_ERROR) {
					/* resend message */
					showResendDialog(message);
				} else if (message.getType() != Const.MSG_TYPE_DELETED && message.getType() != Const.MSG_TYPE_TEMP_MESS) {
					int rootId = message.getRootId() == 0 ? message.getIntegerId() : message.getRootId();
//					ThreadsActivity.start(ChatActivity.this, String.valueOf(rootId), message.getChat_id(), message.getId(), chatImageThumb,
//							chatImage, chatName, mUserId);
				}
			}
		});

		isOnCreate = true;

		LocalBroadcastManager.getInstance(this).registerReceiver(adminBroadCast, adminFilter);
		getIntentData(getIntent());

		setEmojiListener(new SelectEmojiListener() {

			@Override
			public void onEmojiSelect(Stickers selectedStickers) {
				sendMessage(Const.MSG_TYPE_GIF, chatId, selectedStickers.getUrl(), null, null, null, null);
			}
		});

		if (SpikaEnterpriseApp.isCallInBackground()) {
			setViewWhenCallIsInBackground(R.id.rootView, R.id.actionBarLayout, false);
		}

		setActiveClass(ChatActivity.class.getName());
	}

	public void setIsResume(boolean isResume) {
		this.isResume = isResume;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// if activity restart after calling camera intent (SAMSUNG DEVICES)
		SpikaEnterpriseApp.setCheckForRestartVideoActivity(false);
		SpikaEnterpriseApp.setVideoPath(null);
		SpikaEnterpriseApp.deleteSamsungPathImage();

		if(chatId != null){
			EntryUtilsCaching.GetEntry getEntry = new EntryUtilsCaching.GetEntry(this, Integer.valueOf(chatId), GlobalModel.Type.CHAT);
			offlineSpiceManager.execute(getEntry, new CustomSpiceListener<GlobalModel>() {

				@Override
				public void onRequestSuccess(GlobalModel res) {
					super.onRequestSuccess(res);

					if (res.chat != null) {
						if (!TextUtils.isEmpty(res.chat.admin_id)) {
							isAdmin = Helper.getUserId().equals(res.chat.admin_id) ? true : false;
						} else {
							isAdmin = false;
						}

						if (!isAdmin) {
							chatType = Const.C_ROOM;
						}

						setSettingsItems(chatType);
					}
				}
			});
		}

		if (isResume) {
			if (adapter.getCount() > 0) {
				boolean isClear = false, processing = false, isPagging = false, isNewMsg = true, isSend = false, isRefresh = true, isFirstTime = false;
				getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
			} else {
				boolean isClear = true, processing = true, isPagging = true, isNewMsg = false, isSend = false, isRefresh = true, isFirstTime = false;
				getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
			}
		} else {
			isResume = true;
		}

		adapter.notifyDataSetChanged();

		if(!TextUtils.isEmpty(chatId)){
			removeNotificationForChat(chatId);
		}
	}

	IntentFilter adminFilter = new IntentFilter(Const.IS_ADMIN);
	BroadcastReceiver adminBroadCast = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getExtras().containsKey(Const.IS_UPDATE_ADMIN)) {

				isAdmin = intent.getExtras().getBoolean(Const.IS_ADMIN, isAdmin);

				if (chatType == Const.C_ROOM) {
					if (isAdmin && isActive == 1) {
						chatType = Const.C_ROOM_ADMIN_ACTIVE;
					}
					if (isAdmin && isActive == 0) {
						chatType = Const.C_ROOM_ADMIN_INACTIVE;
					}
				}

				setSettingsItems(chatType);
			}

			if (intent.getExtras().containsKey(Const.IS_UPDATE_PRIVATE_PASSWORD)) {

				if (intent.getExtras().containsKey(Const.IS_PRIVATE)) {
					isPrivate = intent.getExtras().getInt(Const.IS_PRIVATE);
				}

				if (intent.getExtras().containsKey(Const.PASSWORD)) {
					chatPassword = intent.getExtras().getString(Const.PASSWORD);
				}
			}

			if (intent.getExtras().containsKey(Const.IS_UPDATE_CATEGORY)) {

				if (intent.getExtras().containsKey(Const.CATEGORY_ID)) {
					categoryId = intent.getExtras().getString(Const.CATEGORY_ID, null);
				}

				if (intent.getExtras().containsKey(Const.CATEGORY_NAME)) {
					categoryName = intent.getExtras().getString(Const.CATEGORY_NAME, null);
				}
			}
		}
	};

	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(adminBroadCast);
	};

	protected void kill() {

		hideKeyboard(etMessage);

		finish();

		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);

	}

	@Override
	public void onBackPressed() {
		if (rlDrawerNew.isSelected()) {
			forceClose();
		} else if (rlDrawerEmoji.isSelected()) {
			forceClose();
		} else {
			kill();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		setIntent(intent);

		isResume = false;

		if (intent.getBooleanExtra(Const.UPDATE_PICTURE, false)) {
			chatImage = intent.getExtras().getString(Const.IMAGE, chatImage);
			chatImageThumb = intent.getExtras().getString(Const.IMAGE_THUMB, chatImageThumb);
		} else {
			if (!isOnCreate) {
				getIntentData(intent);
			}
		}

		if (intent.getBooleanExtra(Const.IS_CALL_ACTIVE, false)) {
			setViewWhenCallIsInBackground(R.id.rootView, R.id.actionBarLayout, true);
		}
	}

	/**
	 * Used to start chat activity with chat id
	 * 
	 * @param context
	 */
	public static void startWithChatId(Context context, Chat chat, User user) {

		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(Const.CHAT_ID, String.valueOf(chat.getId()));
		intent.putExtra(Const.PASSWORD, chat.password);

		if (chat.category != null && chat.category.id != null) {
			intent.putExtra(Const.CATEGORY_ID, chat.category.id);
			intent.putExtra(Const.CATEGORY_NAME, chat.category.name);
		} else if (chat.chat != null) {
			if (chat.chat != null && chat.chat.category != null && chat.chat.category.id != null) {
				intent.putExtra(Const.CATEGORY_ID, chat.chat.category.id);
				intent.putExtra(Const.CATEGORY_NAME, chat.chat.category.name);
			}
		}

		intent.putExtra(Const.CHAT, chat);
		intent.putExtra(Const.USER, user);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public static void startWithChatIdNoModel(Context context, String chatId, String password) {

		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.PASSWORD, password);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	/**
	 * Used to start chat activity with user id
	 * 
	 * @param context
	 * @param userId
	 * @param isGroup
	 * @param firstname
	 * @param lastname
	 */
	public static void startWithUserId(Context context, String userId, boolean isGroup, String firstname, String lastname, User user) {

		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(Const.USER_ID, userId);
		intent.putExtra(Const.FIRSTNAME, firstname);
		intent.putExtra(Const.USER, user);
		intent.putExtra(Const.LASTNAME, lastname);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		if (isGroup) {
			intent.putExtra(Const.IS_GROUP, true);
		}

		context.startActivity(intent);
	}

	public static void startWithUserIdWithLeaveMessage(Context context, User user) {

		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(Const.USER_ID, String.valueOf(user.getId()));
		intent.putExtra(Const.FIRSTNAME, user.getFirstName());
		intent.putExtra(Const.USER, user);
		intent.putExtra(Const.LASTNAME, user.getLastName());
		intent.putExtra(Const.TO_LEAVE_MESSAGE, user.getLastName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		context.startActivity(intent);
	}

	/**
	 * Used to start chat activity from notification
	 * 
	 * @param context
	 * @param intent
	 */
	public static void startFromNotification(Context context, Intent intent) {

		Intent intentFinal = new Intent(context, ChatActivity.class);
		intentFinal.putExtras(intent.getExtras());
		intentFinal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intentFinal);
	}

	/**
	 * Start chat activity from update profile picture activity
	 * 
	 * @param context
	 * @param newImage
	 * @param newThumbImage
	 */
	public static void startUpdateImage(Context context, String newImage, String newThumbImage, User user) {

		Intent intentFinal = new Intent(context, ChatActivity.class);
		intentFinal.putExtra(Const.IMAGE, newImage);
		intentFinal.putExtra(Const.IMAGE_THUMB, newThumbImage);
		intentFinal.putExtra(Const.UPDATE_PICTURE, true);
		intentFinal.putExtra(Const.USER, user);
		intentFinal.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intentFinal);
	}

	private void getIntentData(final Intent intent) {

		if (intent != null && intent.getExtras() != null) {

			if (intent.getExtras().containsKey(Const.FROM_NOTIFICATION) && intent.getExtras().getBoolean(Const.FROM_NOTIFICATION, false)) {
				intent.getExtras().remove(Const.FROM_NOTIFICATION);
				handleIntentSecondLevel(intent);
				// try {
				// Logger.d("organization_id: " +
				// intent.getExtras().getString(Const.ORGANIZATION_ID));
				//
				// String hashPassword =
				// Utils.getHexString(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD));
				//
				// handleProgress(true);
				// LoginSpice.LoginWithCredentials loginWithCredentials = new
				// LoginSpice.LoginWithCredentials(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(
				// Const.USERNAME), hashPassword,
				// intent.getExtras().getString(Const.ORGANIZATION_ID), this);
				// spiceManager.execute(loginWithCredentials, new
				// CustomSpiceListener<Login>() {
				//
				// @Override
				// public void onRequestFailure(SpiceException ex) {
				// handleProgress(false);
				// Utils.onFailedUniversal(null, ChatActivity.this);
				// }
				//
				// @Override
				// public void onRequestSuccess(Login result) {
				// handleProgress(false);
				//
				// if (result.getCode() == Const.API_SUCCESS) {
				//
				// Helper.setUserProperties(getApplicationContext(),
				// result.getUserId(), result.image, result.image_thumb,
				// result.firstname, result.lastname,
				// result.getToken());
				// new GoogleUtils().getPushToken(ChatActivity.this);
				//
				// handleIntentSecondLevel(intent);
				//
				// } else {
				//
				// String message = "";
				// if (result.getCode() == Const.E_INVALID_TOKEN) {
				// Intent intent = new Intent(ChatActivity.this,
				// LoginActivity.class);
				// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// startActivity(intent);
				// } else if (result.getCode() == Const.E_LOGIN_WITH_TEMP_PASS)
				// {
				// Intent intent = new Intent(ChatActivity.this,
				// ChangePasswordActivity.class);
				// intent.putExtra(Const.TEMP_PASSWORD,
				// SpikaEnterpriseApp.getSharedPreferences(ChatActivity.this).getCustomString(Const.PASSWORD));
				// startActivity(intent);
				// finish();
				// return;
				// } else {
				// message = result.getMessage();
				// }
				//
				// Utils.onFailedUniversal(message, ChatActivity.this);
				// }
				// }
				// });
				//
				// } catch (Exception e) {
				// e.printStackTrace();
				// }

			} else {
				handleIntentSecondLevel(intent);
			}
		}
	}

	private void handleIntentSecondLevel(final Intent intent) {

		isOnCreate = false;

		if (intent.getExtras().containsKey(Const.CHAT_ID)) {
			
			if (intent.getExtras().containsKey(Const.CATEGORY_ID)) {
				categoryId = intent.getExtras().getString(Const.CATEGORY_ID, null);
			}

            Log.i("LOG", "category id: " + categoryId);

			if (intent.getExtras().containsKey(Const.CATEGORY_NAME)) {
				categoryName = intent.getExtras().getString(Const.CATEGORY_NAME, null);
			}

			if (chatId != null && intent.getExtras().getString(Const.CHAT_ID) != null && !chatId.equals(intent.getExtras().getString(Const.CHAT_ID))) {
				adapter.clearItems();
			}

			removeNotificationForChat(chatId);

			chatId = intent.getExtras().getString(Const.CHAT_ID);
			chatPassword = intent.getExtras().getString(Const.PASSWORD);

			//Check for connection
			Chat chat = intent.getExtras().getParcelable(Const.CHAT);

			if (chat == null){
				return;
			}

			if(chat.is_connection == -1){
				etMessage.setEnabled(false);
				etMessage.setClickable(false);
				footerMore.setClickable(false);
				findViewById(R.id.settingsBtn).setClickable(false);
				findViewById(R.id.footerSmiley).setClickable(false);
			}else{
				etMessage.setEnabled(true);
				etMessage.setClickable(true);
				footerMore.setClickable(true);
				findViewById(R.id.settingsBtn).setClickable(true);
				findViewById(R.id.footerSmiley).setClickable(true);
			}
			//*********************

			// adapter.clearItems();

			if (!TextUtils.isEmpty(chatPassword)) {

				if (Helper.getStoredChatPassword(ChatActivity.this, chatId) != null
						&& Helper.getStoredChatPassword(ChatActivity.this, chatId).equals(chatPassword)) {
					boolean isClear = true, processing = true, isPagging = true, isNewMsg = false, isSend = false, isRefresh = false, isFirstTime = true;
					getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
				} else {
					AppDialog dialog = new AppDialog(this, true);
					dialog.setPasswordInput(getString(R.string.requires_password), getString(R.string.ok), getString(R.string.cancel_big),
							chatPassword);
					dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

						@Override
						public void onPositiveButtonClick(View v, Dialog d) {
							Helper.storeChatPassword(ChatActivity.this, chatPassword, chatId);
							boolean isClear = true, processing = true, isPagging = true, isNewMsg = false, isSend = false, isRefresh = false, isFirstTime = true;
							getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
							d.dismiss();
						}
					});
					dialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {

						@Override
						public void onNegativeButtonClick(View v, Dialog d) {
							finish();
						}
					});
					dialog.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
				}

			} else {
				boolean isClear = true, processing = true, isPagging = true, isNewMsg = false, isSend = false, isRefresh = false, isFirstTime = true;
				getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
			}
		} else if (intent.getExtras().containsKey(Const.USER_ID)) {

			boolean isGroup = intent.getExtras().containsKey(Const.IS_GROUP);
			mUserId = intent.getExtras().getString(Const.USER_ID);

			handleProgress(true);
			ChatSpice.StartChat startChat = new ChatSpice.StartChat(isGroup, mUserId, intent.getExtras().getString(Const.FIRSTNAME), intent
					.getExtras().getString(Const.LASTNAME));
			spiceManager.execute(startChat, new CustomSpiceListener<Chat>() {

				@Override
				public void onRequestFailure(SpiceException ex) {
					handleProgress(false);
					Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
				}

				@Override
				public void onRequestSuccess(Chat result) {
					handleProgress(false);

					if (result.user != null) {
						currentUser = result.user;
					}
					if (getIntent().getSerializableExtra(Const.USER) != null && currentUser == null) {
						currentUser = (User) getIntent().getSerializableExtra(Const.USER);
					}

					chatParams(result.chat);

					chatId = String.valueOf(result.getId());
					chatName = result.chat_name;

					removeNotificationForChat(chatId);

					setTitle(chatName);

					startChat(result);

					checkForLeaveVoiceMessage(intent);
				}
			});
		}
	}

	protected void checkForLeaveVoiceMessage(Intent intent) {
		if (intent.hasExtra(Const.TO_LEAVE_MESSAGE)) {
			if (currentUser != null)
				openRecordActivity(currentUser);
		}
	}

	private void chatParams(Chat chat) {
		
		if (chat == null && TextUtils.isEmpty(chatName)) {
			AppDialog dialog = new AppDialog(this, true);
			dialog.setFailed(Const.E_SOMETHING_WENT_WRONG);
			return;
		} else if (chat.chat_name == null && !TextUtils.isEmpty(chatName)) {
			return;
		}

		chatName = chat.chat_name;
		setTitle(chatName);
		chatImage = chat.image;
		chatImageThumb = chat.image_thumb;

		if (!TextUtils.isEmpty(chat.admin_id)) {
			isAdmin = Helper.getUserId().equals(chat.admin_id) ? true : false;
		} else {
			isAdmin = false;
		}

		isActive = chat.is_active;
		if (isActive == 0) {
			etMessage.setFocusable(false);
		}
		isPrivate = chat.is_private;

		if (chat.category != null) {
			categoryId = String.valueOf(chat.category.id);
			categoryName = chat.category.name;
		}

		chatType = chat.type;

		if (chatType == Const.C_ROOM) {
			if (isAdmin && isActive == 1) {
				chatType = Const.C_ROOM_ADMIN_ACTIVE;
			}
			if (isAdmin && isActive == 0) {
				chatType = Const.C_ROOM_ADMIN_INACTIVE;
			}
		}

		setSettingsItems(chatType);

	}

	private void replaceTempMessWithRealMess(Message mess, Message tempMess) {
		mess.isMe = true;
		totalItems++;
		com.medicbleep.app.chat.models.greendao.Message messDao = new com.medicbleep.app.chat.models.greendao.Message(
				Long.valueOf(mess.id), Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text,
				mess.file_id, mess.thumb_id, mess.longitude, mess.latitude, mess.created, mess.modified, mess.child_list, mess.image_thumb,
				mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed, mess.attributes, mess.country_code, mess.seen_timestamp,
				Long.valueOf(mess.getChat_id()));
		getDaoSession().getMessageDao().insertOrReplace(messDao);
		adapter.addNewMessage(mess, tempMess);

		setNoItemsVisibility();

		chatListView.setSelectionFromTop(adapter.getCount(), 0);
	}

	protected void showResendDialog(final Message message) {
		AppDialog dialog = new AppDialog(this, false);
		dialog.setYesNo(getString(R.string.resend_message), getString(R.string.resend), getString(R.string.cancel));
		dialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {

			@Override
			public void onPositiveButtonClick(View v, Dialog d) {
				resendMessage(message);
			}
		});
	}

	protected void resendMessage(Message message) {
		adapter.prepareResend(message);
		sendMessage(Const.MSG_TYPE_DEFAULT, chatId, message.text, null, null, null, null);
	}

	private void addRealMess(Message mess) {
		mess.isMe = true;
		totalItems++;
		com.medicbleep.app.chat.models.greendao.Message messDao = new com.medicbleep.app.chat.models.greendao.Message(
				Long.valueOf(mess.id), Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text,
				mess.file_id, mess.thumb_id, mess.longitude, mess.latitude, mess.created, mess.modified, mess.child_list, mess.image_thumb,
				mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed, mess.attributes, mess.country_code, mess.seen_timestamp,
				Long.valueOf(mess.getChat_id()));
		getDaoSession().getMessageDao().insert(messDao);
		adapter.addNewMessage(mess);

		setNoItemsVisibility();

		chatListView.setSelectionFromTop(adapter.getCount(), 0);

	}

	@Override
	protected void onFileSelected(int result, final String fileName, String filePath) {
		if (result == RESULT_OK) {
			checkForEncryption(filePath, fileName);
		} else if (result == RESULT_CANCELED) {
		} else {
			AppDialog dialog = new AppDialog(this, false);
			dialog.setFailed(getResources().getString(R.string.e_while_encrypting));
		}
	}
	
	private void checkForEncryption(final String mFilePath2, final String fileName) {
        //always crypt
        uploadFile(mFilePath2, fileName, true);
//		Utils.checkForEncryption(this, mFilePath2, new OnCheckEncryptionListener() {
//
//			@Override
//			public void onCheckFinish(String path, boolean toCrypt) {
//				uploadFile(mFilePath2, fileName, toCrypt);
//			}
//		});
	}
	
	private void uploadFile(final String filePath, final String fileName, final boolean toCrypt){
		new FileManageApi().uploadFile(toCrypt, filePath, this, true, new ApiCallback<UploadFileModel>() {

			@Override
			public void onApiResponse(Result<UploadFileModel> result) {
				if (result.isSuccess()) {
					sendMessage(toCrypt, Const.MSG_TYPE_FILE, chatId, fileName, result.getResultData().getFileId(), null, null, null);
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					if (result.hasResultData()) {
						dialog.setFailed(result.getResultData().getMessage());
					} else {
						dialog.setFailed(Helper.errorDescriptions(getApplicationContext(), result.getResultData().getCode()));
					}
				}
			}
		});
	}

	@Override
	protected String getRootId() {
		return null;
	}

	@Override
	protected String getMessageId() {
		return null;
	}

	@Override
	protected int getUserId() {
		int userId;
		try {
			userId = Integer.valueOf(mUserId);
		} catch (NumberFormatException e) {
			userId = 0;
		}
		return userId;
	}

	public void sendMessage(final int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude) {	
		sendMessage(true, type, chatId, text, fileId, thumbId, longitude, latitude);
	}
	
	public void sendMessage(final boolean toCrypt, final int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude) {

		/* if message type is deafult add temp message to adapter (see method in adapter) */
		final Message tempMessage = adapter.addTempMessage(text, type);
		if (tempMessage != null)
			setNoItemsVisibility();

		if (type == Const.MSG_TYPE_DEFAULT){
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    chatListView.setSelection(adapter.getCount());
                }
            }, 100);
            adapter.setSeenBy("");
        }

		etMessage.setText("");
		
		String attributes = null;
		if(!toCrypt){
			attributes = "{\"encrypted\":\"0\"}";
		}

		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(attributes, type, chatId, text, fileId, thumbId, longitude, latitude, null, null);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				/* if message type is deafult replace temp message with type error else show dialog */
				if (type == Const.MSG_TYPE_DEFAULT) {
					adapter.tempMessageError(tempMessage);
					if (ex instanceof NoNetworkException) {
						setViewNoInternetConnection(R.id.rootView, R.id.actionBarLayout);
					}
				} else {
					Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
				}
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {

				if (result.getCode() == Const.API_SUCCESS) {

					if (type != Const.MSG_TYPE_DEFAULT)
						forceClose();

					/* if message type is deafult replace temp message with real message else add real message */
					adapter.setSeenBy("");
					if (type == Const.MSG_TYPE_DEFAULT && tempMessage != null) {
						replaceTempMessWithRealMess(result.message_model, tempMessage);
					} else {
						addRealMess(result.message_model);
					}
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(result.getCode());
					if (result.getCode() == Const.E_CHAT_INACTIVE) {
						isActive = 0;
						etMessage.setFocusable(false);
						adapter.deleteAllTempChat();
					}
				}
			}
		});
	}

	@Override
	protected void onChatPushUpdated() {
		boolean isClear = false, processing = false, isPagging = false, isNewMsg = true, isSend = false, isRefresh = true, isFirstTime = false;
		getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, isFirstTime);
	}

	@Override
	protected void onMessageDeleted(Message mess) {
		mess.setType(Const.MSG_TYPE_DELETED);
		adapter.setMessageDelted(mess.getId());
		com.medicbleep.app.chat.models.greendao.Message messDao = new com.medicbleep.app.chat.models.greendao.Message(
				Long.valueOf(mess.id), Long.valueOf(mess.chat_id), Long.valueOf(mess.user_id), mess.firstname, mess.lastname, mess.image, mess.text,
				mess.file_id, mess.thumb_id, mess.longitude, mess.latitude, mess.created, mess.modified, mess.child_list, mess.image_thumb,
				mess.type, mess.root_id, mess.parent_id, mess.isMe, mess.isFailed, mess.attributes, mess.country_code, mess.seen_timestamp,
				Long.valueOf(mess.getChat_id()));

		getDaoSession().getMessageDao().update(messDao);
	}

	private void startChat(Chat result) {
		if (!isRunning) {
			isRunning = true;
		} else {
			return;
		}

		ChatCacheSpice.StartChat startChatSpice = new StartChat(this, spiceManager, chatId, "-1", this, this, result);
		spiceManager.execute(startChatSpice, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				// Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				super.onRequestSuccess(result);
				manageGetMessages(result, false, false, false, true, false);
			}

		});
	}

	public void getMessages(final boolean isClear, final boolean processing, final boolean isPagging, final boolean isNewMsg, final boolean isSend,
			final boolean isRefresh, final boolean isFirstTime) {

		if (!isRunning) {
			isRunning = true;

			if (isClear) {
				// adapter.clearItems();
				totalItems = 0;
			}
		} else {
            swipeControll.setRefreshing(false);
			return;
		}

		String msgId = "";
		int adapterCount = -1;

		if (isPagging) {

			adapterCount = adapter.getCount();

			if (!isClear && !adapter.getData().isEmpty() && adapter.getCount() > 0) {
				msgId = adapter.getData().get(0).getId();
			}
		} else if (isNewMsg) {

			adapterCount = adapter.getCount();

			if ((adapter.getCount() - 1) >= 0) {
				msgId = adapter.getData().get(adapter.getCount() - 1).getId();
			}
		}

		ChatCacheSpice.GetData chatCacheSpice = new ChatCacheSpice.GetData(this, spiceManager, isClear, isPagging, isNewMsg, isSend, isRefresh, isFirstTime,
				chatId, msgId, adapterCount, this, this);

		offlineSpiceManager.execute(chatCacheSpice, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
                super.onRequestFailure(arg0);
                swipeControll.setRefreshing(false);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				super.onRequestSuccess(result);
                swipeControll.setRefreshing(false);
				adapter.setSpiceManager(spiceManager);
				manageGetMessages(result, isNewMsg, isSend, isRefresh, isClear, isPagging);
			}
		});
	}

	List<Message> activeChat = new ArrayList<Message>();

	protected void manageGetMessages(Chat chat, boolean isNewMsg, boolean isSend, boolean isRefresh, boolean isClear, boolean isPagging) {
		isRunning = false;

		if (chat == null) {
			setNoItemsVisibility();
			if(getIntent().hasExtra(Const.CHAT)){
				chat = (Chat) getIntent().getParcelableExtra(Const.CHAT);
				chatParams(chat);
			}
			return;
		}

		if (chat == null || chat.chat == null) {
			finish();
		}

        Iterator<Message> iterator = chat.messages.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (!TextUtils.isEmpty(message.country_code) &&
					!message.country_code.equals(LocationUtility.getInstance().getCountryCode())) {
                iterator.remove();
            }
        }

		Log.d("LOG", "SIZE OLD: " + activeChat.size() + ", new size: " + chat.messages.size());
		if (chat.messages.equals(activeChat) && chat.messages.size() != 0) {
			Log.d("LOG", "same");
			return;
		} else {
			Log.d("LOG", "not same");
		}

		for (Message item : chat.messages){
			item.setIsCodeTextStyle();
		}

		activeChat.clear();
		activeChat.addAll(chat.messages);

		if (chat.chat != null) {
			chatParams(chat.chat);
		} else {
			chatParams(chat);
		}

		if (chat.user != null) {
			currentUser = chat.user;
		}

		if (getIntent().getSerializableExtra(Const.USER) != null && currentUser == null) {
			currentUser = (User) getIntent().getSerializableExtra(Const.USER);
		}

		setMenuByChatType(false);

		if (TextUtils.isEmpty(mUserId)) {
			mUserId = chat.user == null ? "" : String.valueOf(chat.user.getId());
		}

		adapter.addItems(chat.messages, isNewMsg);
		for (int i = 0; i < chat.messages.size(); i++) {
			if (chat.messages.get(i).getType() == Const.MSG_TYPE_DEFAULT) {
				if (chat.messages.get(i).getText().startsWith("http") && chat.messages.get(i).getText().endsWith(".gif")) {
					chat.messages.get(i).setType(Const.MSG_TYPE_GIF);
				}
			}
		}
		adapter.setSeenBy(chat.seen_by);


		totalItems = Integer.valueOf(chat.total_count);
        Log.d("LOG", "MANAGE, TOTAL COUNT: " + totalItems);
		adapter.setTotalCount(totalItems);

		if (!isRefresh) {
			if (isClear || isSend) {
				chatListView.setSelectionFromTop(adapter.getCount(), 0);
			} else if (isPagging) {
				chatListView.setSelection(chat.messages.size());
			}
		} else {
			int visibleItem = chatListView.getFirstVisiblePosition();

			boolean isScroll = false;

			if ((adapter.getCount() - visibleItem) <= 15) {
				isScroll = true;
			}

			if (isScroll && !isSend) {
				chatListView.setSelectionFromTop(adapter.getCount(), 0);
			}
		}

		setNoItemsVisibility();
	}

	@Override
	protected void leaveChat() {

		handleProgress(true);
		ChatSpice.LeaveChat leaveChat = new ChatSpice.LeaveChat(chatId);
		spiceManager.execute(leaveChat, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					EntryUtilsCaching.DeleteEntry deleteEntry = new EntryUtilsCaching.DeleteEntry(ChatActivity.this, Integer.valueOf(chatId),
							GlobalModel.Type.CHAT);
					spiceManager.execute(deleteEntry, null);

					AppDialog dialog = new AppDialog(ChatActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

	@Override
	protected void onEditorSendEvent(final String text) {
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				sendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null);
			}
		});
	}

	private void setNoItemsVisibility() {
		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
			findViewById(R.id.mainContent).setBackgroundColor(getResources().getColor(R.color.default_blue_light));
		} else {
			noItems.setVisibility(View.GONE);
			findViewById(R.id.mainContent).setBackgroundColor(Color.WHITE);
		}
	}

	@Override
	protected void deactivateChat() {

		handleProgress(true);
		ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_DEACTIVATE, null, null, null);
		spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(BaseModel result) {
                Intent inBroadcast = new Intent();
                inBroadcast.setAction(Const.ACTION_REFRESH_ROOMS);
                LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(inBroadcast);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					AppDialog dialog = new AppDialog(ChatActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

	@Override
	protected void deleteChat() {
		final AppDialog dialog = new AppDialog(this, false);
		dialog.setYesNo(getString(R.string.are_you_sure_), getString(R.string.yes), getString(R.string.no));
		dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

			@Override
			public void onPositiveButtonClick(View v, Dialog d) {

				handleProgress(true);
				ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_DELETE, null, null, null);
				spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

					@Override
					public void onRequestFailure(SpiceException ex) {
						handleProgress(false);
						Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
					}

					@Override
					public void onRequestSuccess(BaseModel result) {
						handleProgress(false);

						if (result.getCode() == Const.API_SUCCESS) {

							EntryUtilsCaching.DeleteEntry deleteEntry = new EntryUtilsCaching.DeleteEntry(ChatActivity.this, Integer.valueOf(chatId),
									GlobalModel.Type.CHAT);
							spiceManager.execute(deleteEntry, null);

							AppDialog dialog = new AppDialog(ChatActivity.this, true);
							dialog.setSucceed();
						} else {
							AppDialog dialog = new AppDialog(ChatActivity.this, false);
							dialog.setFailed(null);
						}
					}
				});
			}
		});

		dialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {

			@Override
			public void onNegativeButtonClick(View v, Dialog d) {
				dialog.dismiss();
			}
		});

		Intent inBroadcast = new Intent();
		inBroadcast.setAction(Const.ACTION_REFRESH_ROOMS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(inBroadcast);

	}

	@Override
	protected void activateChat() {

		handleProgress(true);
		ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_ACTIVATE, null, null, null);
		spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this, 0, false, ex, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(BaseModel result) {
				Intent inBroadcast = new Intent();
				inBroadcast.setAction(Const.ACTION_REFRESH_ROOMS);
				LocalBroadcastManager.getInstance(ChatActivity.this).sendBroadcast(inBroadcast);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					AppDialog dialog = new AppDialog(ChatActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

	@Override
	public void onChatNetworkResult(int totalCount) {
		Log.d("LOG", "NETWORK, TOTAL COUNT: " + totalCount);
		if (totalItems != totalCount) {
			totalItems = totalCount;
			adapter.setTotalCount(totalItems);
		}
	}

	@Override
	public void onChatDBChanged(Chat usableData, boolean isClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh) {
		Log.d("LOG", "DB CHANGE, size: " + (usableData != null ? usableData.messages.size() : "NULL JE"));
		manageGetMessages(usableData, isNewMsg, isSend, isRefresh, isClear, isPagging);
	}

	@Override
	public void onInternetError() {
		setViewNoInternetConnection(R.id.rootView, R.id.actionBarLayout);
	}

	private void removeNotificationForChat(String chatId){
		try {
			int chatIdInt = Integer.valueOf(chatId);
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(chatIdInt);
		}catch (Exception e){

		}
	}
}
