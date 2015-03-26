package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.MessagesAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.api.robospice.LoginSpice;
import com.clover.spika.enterprise.chat.caching.ChatCaching.OnChatDBChanged;
import com.clover.spika.enterprise.chat.caching.ChatCaching.OnChatNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.ChatCacheSpice;
import com.clover.spika.enterprise.chat.caching.robospice.StickersCacheSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Login;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.GoogleUtils;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.clover.spika.enterprise.chat.views.emoji.SelectEmojiListener;

public class ChatActivity extends BaseChatActivity implements OnChatDBChanged, OnChatNetworkResult{

	private TextView noItems;

	public MessagesAdapter adapter;

	private int totalItems = 0;
	private String mUserId;

	private boolean isRunning = false;
	private boolean isResume = false;
	private boolean isOnCreate = false;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		noItems = (TextView) findViewById(R.id.noItems);

		adapter = new MessagesAdapter(spiceManager, this, new ArrayList<Message>());
		chatListView.setAdapter(adapter);
		chatListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getAdapter() != null) {
					Message message = (Message) parent.getAdapter().getItem(position);
					if (message.getType() != Const.MSG_TYPE_DELETED) {
						int rootId = message.getRootId() == 0 ? message.getIntegerId() : message.getRootId();
						ThreadsActivity.start(ChatActivity.this, String.valueOf(rootId), message.getChat_id(), message.getId(), chatImageThumb, chatImage, chatName, mUserId);
					}
				}
			}
		});

		chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (parent.getAdapter() != null) {
					Message message = (Message) parent.getAdapter().getItem(position);
					if (message.isMe()) {
						deleteMessage(message.getId());
					}
				}
				return true;
			}
		});

		adapter.setOnLongAndSimpleClickCustomListener(new MessagesAdapter.OnMessageLongAndSimpleClickCustomListener() {

			@Override
			public void onLongClick(Message message) {
				if (message.isMe()) {
					deleteMessage(message.getId());
				}
			}

			@Override
			public void onSimpleClick(Message message) {
				if (message.getType() != Const.MSG_TYPE_DELETED) {
					int rootId = message.getRootId() == 0 ? message.getIntegerId() : message.getRootId();
					ThreadsActivity.start(ChatActivity.this, String.valueOf(rootId), message.getChat_id(), message.getId(), chatImageThumb, chatImage, chatName, mUserId);
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

		if (isResume) {
			if (adapter.getCount() > 0) {
				getMessages(false, false, false, true, false, true);
			} else {
				getMessages(true, true, true, false, false, true);
			}
		} else {
			isResume = true;
		}

		adapter.notifyDataSetChanged();
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
			} else if (intent.getExtras().containsKey(Const.IS_UPDATE_PRIVATE_PASSWORD)) {

				if (intent.getExtras().containsKey(Const.IS_PRIVATE)) {
					isPrivate = intent.getExtras().getInt(Const.IS_PRIVATE);
				}

				if (intent.getExtras().containsKey(Const.PASSWORD)) {
					chatPassword = intent.getExtras().getString(Const.PASSWORD);
				}
			} else if (intent.getExtras().containsKey(Const.IS_UPDATE_CATEGORY)) {

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
	 * @param chatId
	 * @param password
	 */
	public static void startWithChatId(Context context, String chatId, String password, User user) {

		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.PASSWORD, password);
		intent.putExtra(Const.USER, user);
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
	 * @param intent
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
				try {
					Logger.d("organization_id: " + intent.getExtras().getString(Const.ORGANIZATION_ID));

					String hashPassword = Utils.getHexString(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.PASSWORD));

					handleProgress(true);
					LoginSpice.LoginWithCredentials loginWithCredentials = new LoginSpice.LoginWithCredentials(SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(
							Const.USERNAME), hashPassword, intent.getExtras().getString(Const.ORGANIZATION_ID), this);
					spiceManager.execute(loginWithCredentials, new CustomSpiceListener<Login>() {

						@Override
						public void onRequestFailure(SpiceException ex) {
							handleProgress(false);
							Utils.onFailedUniversal(null, ChatActivity.this);
						}

						@Override
						public void onRequestSuccess(Login result) {
							handleProgress(false);

							if (result.getCode() == Const.API_SUCCESS) {

								Helper.setUserProperties(getApplicationContext(), result.getUserId(), result.image, result.image_thumb, result.firstname, result.lastname,
										result.getToken());
								new GoogleUtils().getPushToken(ChatActivity.this);

								handleIntentSecondLevel(intent);

							} else {

								String message = "";
								if (result.getCode() == Const.E_INVALID_TOKEN) {
									Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
								} else if (result.getCode() == Const.E_LOGIN_WITH_TEMP_PASS) {
									Intent intent = new Intent(ChatActivity.this, ChangePasswordActivity.class);
									intent.putExtra(Const.TEMP_PASSWORD, SpikaEnterpriseApp.getSharedPreferences(ChatActivity.this).getCustomString(Const.PASSWORD));
									startActivity(intent);
									finish();
									return;
								} else {
									message = result.getMessage();
								}

								Utils.onFailedUniversal(message, ChatActivity.this);
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				handleIntentSecondLevel(intent);
			}
		}
	}

	private void handleIntentSecondLevel(final Intent intent) {

		isOnCreate = false;

		if (intent.getExtras().containsKey(Const.CHAT_ID)) {

			if (chatId != null && intent.getExtras().getString(Const.CHAT_ID) != null && !chatId.equals(intent.getExtras().getString(Const.CHAT_ID))) {
				adapter.clearItems();
			}

			chatId = intent.getExtras().getString(Const.CHAT_ID);
			chatPassword = intent.getExtras().getString(Const.PASSWORD);

			// adapter.clearItems();

			if (!TextUtils.isEmpty(chatPassword)) {

				if (Helper.getStoredChatPassword(ChatActivity.this, chatId) != null && Helper.getStoredChatPassword(ChatActivity.this, chatId).equals(chatPassword)) {
					getMessages(true, true, true, false, false, false);
				} else {
					AppDialog dialog = new AppDialog(this, true);
					dialog.setPasswordInput(getString(R.string.requires_password), getString(R.string.ok), getString(R.string.cancel_big), chatPassword);
					dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

						@Override
						public void onPositiveButtonClick(View v, Dialog d) {
							Helper.storeChatPassword(ChatActivity.this, chatPassword, chatId);
							getMessages(true, true, true, false, false, false);
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
				getMessages(true, true, true, false, false, false);
			}
		} else if (intent.getExtras().containsKey(Const.USER_ID)) {

			boolean isGroup = intent.getExtras().containsKey(Const.IS_GROUP);
			mUserId = intent.getExtras().getString(Const.USER_ID);

			handleProgress(true);
			ChatSpice.StartChat startChat = new ChatSpice.StartChat(isGroup, mUserId, intent.getExtras().getString(Const.FIRSTNAME), intent.getExtras().getString(Const.LASTNAME),
					this);
			spiceManager.execute(startChat, new CustomSpiceListener<Chat>() {

				@Override
				public void onRequestFailure(SpiceException ex) {
					handleProgress(false);
					Utils.onFailedUniversal(null, ChatActivity.this);
				}

				@Override
				public void onRequestSuccess(Chat result) {
					handleProgress(false);

					chatParams(result.chat);

					if (result.user != null) {
						currentUser = result.user;
					}
					if (getIntent().getSerializableExtra(Const.USER) != null && currentUser == null) {
						currentUser = (User) getIntent().getSerializableExtra(Const.USER);
					}

					chatParams(result.chat);

					chatId = String.valueOf(result.getId());
					chatName = result.chat_name;

					setTitle(chatName);
					adapter.clearItems();
					totalItems = Integer.valueOf(result.total_count);
					adapter.addItems(result.messages, true);
					adapter.setSeenBy(result.seen_by);
					adapter.setTotalCount(Integer.valueOf(result.total_count));
					if (adapter.getCount() > 0) {
						chatListView.setSelectionFromTop(adapter.getCount(), 0);
					}

					checkForLeaveVoiceMessage(intent);

					setNoItemsVisibility();
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
			isAdmin = Helper.getUserId(this).equals(chat.admin_id) ? true : false;
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

	private void callNewMsgs() {
		if (adapter.getCount() > 0) {
			getMessages(false, false, false, true, true, false);
		} else {
			getMessages(true, true, true, false, true, false);
		}
	}

	@Override
	protected void onFileSelected(int result, final String fileName, String filePath) {
		if (result == RESULT_OK) {
			new FileManageApi().uploadFile(filePath, this, true, new ApiCallback<UploadFileModel>() {

				@Override
				public void onApiResponse(Result<UploadFileModel> result) {
					if (result.isSuccess()) {
						sendMessage(Const.MSG_TYPE_FILE, chatId, fileName, result.getResultData().getFileId(), null, null, null);
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
		} else if (result == RESULT_CANCELED) {
		} else {
			AppDialog dialog = new AppDialog(this, false);
			dialog.setFailed(getResources().getString(R.string.e_while_encrypting));
		}
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
		return Integer.valueOf(mUserId);
	}

	public void sendMessage(final int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude) {

		handleProgress(true);
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(type, chatId, text, fileId, thumbId, longitude, latitude, null, null, this);
		spiceManager.execute(sendMessage, new CustomSpiceListener<Integer>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(Integer result) {
				handleProgress(false);

				if (result == Const.API_SUCCESS) {

					etMessage.setText("");
					if (type != Const.MSG_TYPE_DEFAULT)
						forceClose();

					callNewMsgs();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(result);
					if (result == Const.E_CHAT_INACTIVE) {
						isActive = 0;
						etMessage.setText("");
						etMessage.setFocusable(false);
					}
				}
			}
		});
	}

	@Override
	protected void onChatPushUpdated() {
		getMessages(false, false, false, true, false, true);
	}

	@Override
	protected void onMessageDeleted() {
		getMessages(false, false, false, true, false, true);
	}

	public void getMessages(final boolean isClear, final boolean processing, final boolean isPagging, final boolean isNewMsg, final boolean isSend, final boolean isRefresh) {

		if (!isRunning) {
			isRunning = true;

			if (isClear) {
				// adapter.clearItems();
				totalItems = 0;
			}
		} else {
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

//		handleProgress(processing);
		// TODO add caching
		ChatCacheSpice.GetData chatCacheSpice = new ChatCacheSpice.GetData(this, spiceManager, isClear, isPagging, 
				isNewMsg, isSend, isRefresh, chatId, msgId, adapterCount, this, this);
		
		spiceManager.execute(chatCacheSpice, new CustomSpiceListener<Chat>(){
			
			@Override
			public void onRequestFailure(SpiceException ex) {
				super.onRequestFailure(ex);
				Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
				super.onRequestSuccess(result);
				
				manageGetMessages(result, isNewMsg, isSend, isRefresh, isClear, isPagging);
				
			}
			
		});
		
//		ChatSpice.GetMessages getMessages = new ChatSpice.GetMessages(isClear, isPagging, isNewMsg, isSend, isRefresh, chatId, msgId, adapterCount, this);
//		spiceManager.execute(getMessages, new CustomSpiceListener<Chat>() {
//
//			@Override
//			public void onRequestFailure(SpiceException ex) {
//				handleProgress(false);
//				Utils.onFailedUniversal(null, ChatActivity.this);
//			}
//
//			@Override
//			public void onRequestSuccess(Chat chat) {
//				handleProgress(false);
//
//				isRunning = false;
//				
//				if(chat == null || chat.chat == null){
//					finish();
//				}
//
//				chatParams(chat.chat);
//
//				if (chat.user != null) {
//					currentUser = chat.user;
//				}
//
//				if (getIntent().getSerializableExtra(Const.USER) != null && currentUser == null) {
//					currentUser = (User) getIntent().getSerializableExtra(Const.USER);
//				}
//
//				setMenuByChatType();
//
//				if (TextUtils.isEmpty(mUserId)) {
//					mUserId = chat.user == null ? "" : String.valueOf(chat.user.getId());
//				}
//
//				adapter.addItems(chat.messages, isNewMsg);
//				for (int i = 0; i < chat.messages.size(); i++) {
//					if (chat.messages.get(i).getType() == Const.MSG_TYPE_DEFAULT) {
//						if (chat.messages.get(i).getText().startsWith("http") && chat.messages.get(i).getText().endsWith(".gif")) {
//							chat.messages.get(i).setType(Const.MSG_TYPE_GIF);
//						}
//					}
//				}
//				adapter.setSeenBy(chat.seen_by);
//
//				totalItems = Integer.valueOf(chat.total_count);
//				adapter.setTotalCount(totalItems);
//
//				if (!isRefresh) {
//					if (isClear || isSend) {
//						chatListView.setSelectionFromTop(adapter.getCount(), 0);
//					} else if (isPagging) {
//						chatListView.setSelection(chat.messages.size());
//					}
//				} else {
//					int visibleItem = chatListView.getFirstVisiblePosition();
//
//					boolean isScroll = false;
//
//					if ((adapter.getCount() - visibleItem) <= 15) {
//						isScroll = true;
//					}
//
//					if (isScroll && !isSend) {
//						chatListView.setSelectionFromTop(adapter.getCount(), 0);
//					}
//				}
//
//				setNoItemsVisibility();
//			}
//		});
	}

	List<Message> activeChat =  new ArrayList<Message>();
	protected void manageGetMessages(Chat chat, boolean isNewMsg, boolean isSend, boolean isRefresh, boolean isClear, boolean isPagging) {
		isRunning = false;
		
		Log.d("LOG", "SIZE OLD: " + activeChat.size()+", new suze: "+chat.messages.size());
		if(chat.messages.equals(activeChat)){
			Log.d("LOG", "same");
			return;
		}else{
			Log.d("LOG", "not same");
		}
		
		activeChat.clear();
		activeChat.addAll(chat.messages);
		
		if(chat == null || chat.chat == null){
			finish();
		}
		
		chatParams(chat.chat);

		if (chat.user != null) {
			currentUser = chat.user;
		}

		if (getIntent().getSerializableExtra(Const.USER) != null && currentUser == null) {
			currentUser = (User) getIntent().getSerializableExtra(Const.USER);
		}

		setMenuByChatType();

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
		ChatSpice.LeaveChat leaveChat = new ChatSpice.LeaveChat(chatId, this);
		spiceManager.execute(leaveChat, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(Chat result) {
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
		ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_DEACTIVATE, null, null, null, ChatActivity.this);
		spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(BaseModel result) {
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
				ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_DELETE, null, null, null, ChatActivity.this);
				spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

					@Override
					public void onRequestFailure(SpiceException ex) {
						handleProgress(false);
						Utils.onFailedUniversal(null, ChatActivity.this);
					}

					@Override
					public void onRequestSuccess(BaseModel result) {
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
		ChatSpice.UpdateChat updateChat = new ChatSpice.UpdateChat(chatId, Const.UPDATE_CHAT_ACTIVATE, null, null, null, this);
		spiceManager.execute(updateChat, new CustomSpiceListener<BaseModel>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, ChatActivity.this);
			}

			@Override
			public void onRequestSuccess(BaseModel result) {
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
		if(totalItems != totalCount){
			totalItems = totalCount;
			adapter.setTotalCount(totalItems);
		}
	}

	@Override
	public void onChatDBChanged(Chat usableData, boolean isClear, boolean isPagging, boolean isNewMsg, boolean isSend, boolean isRefresh) {
		Log.d("LOG", "DB CHANGE, size: " + + usableData.messages.size());
		manageGetMessages(usableData, isNewMsg, isSend, isRefresh, isClear, isPagging);
	}
}
