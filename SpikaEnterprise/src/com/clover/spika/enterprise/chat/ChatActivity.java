package com.clover.spika.enterprise.chat;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.MessagesAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;

public class ChatActivity extends BaseChatActivity {

	private TextView noItems;

	public MessagesAdapter adapter;

	private int totalItems = 0;
	private String mUserId;

	private boolean isRunning = false;
	private boolean isResume = false;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		noItems = (TextView) findViewById(R.id.noItems);

		adapter = new MessagesAdapter(this, new ArrayList<Message>());
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

		getIntentData(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();

		// if activity restart after calling camera intent (SAMSUNG DEVICES)
		SpikaEnterpriseApp.getInstance().setCheckForRestartVideoActivity(false);
		SpikaEnterpriseApp.getInstance().setVideoPath(null);
		SpikaEnterpriseApp.getInstance().deleteSamsungPathImage();

		loadImage();

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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		isResume = false;
		if (intent.getBooleanExtra(Const.UPDATE_PICTURE, false)) {
			chatImage = intent.getExtras().getString(Const.IMAGE, chatImage);
			chatImageThumb = intent.getExtras().getString(Const.IMAGE_THUMB, chatImageThumb);
			loadImage();
		} else {
			getIntentData(intent);
		}
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {

			if (intent.getExtras().containsKey(Const.CHAT_ID)) {

				chatId = intent.getExtras().getString(Const.CHAT_ID);
				chatName = intent.getExtras().getString(Const.CHAT_NAME);
				chatImage = intent.getExtras().getString(Const.IMAGE);
				chatImageThumb = intent.getExtras().getString(Const.IMAGE_THUMB);
				if (intent.getExtras().containsKey(Const.ADMIN_ID)) {
					isAdmin = Helper.getUserId(this).equals(intent.getExtras().getString(Const.ADMIN_ID, "")) ? true : false;
				} else {
					isAdmin = intent.getExtras().getBoolean(Const.IS_ADMIN, false);
				}
				isActive = intent.getExtras().getInt(Const.IS_ACTIVE);
				if (isActive == 0) {
					etMessage.setFocusable(false);
				}
				isPrivate = intent.getExtras().getInt(Const.IS_PRIVATE);
				chatPassword = intent.getExtras().getString(Const.PASSWORD);

				Logger.d("ovo je password: " + isPrivate);

				setTitle(chatName);

				adapter.clearItems();

				if (!chatPassword.equals("")) {
					AppDialog dialog = new AppDialog(this, true);
					dialog.setPasswordInput(getString(R.string.requires_password), getString(R.string.ok), getString(R.string.cancel_big), chatPassword);
					dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

						@Override
						public void onPositiveButtonClick(View v) {
							getMessages(true, true, true, false, false, false);
						}
					});
					dialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {

						@Override
						public void onNegativeButtonClick(View v) {
							finish();
						}
					});
					dialog.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
					// dialog.setOnDismissListener(new OnDismissListener() {
					//
					// @Override
					// public void onDismiss(DialogInterface dialog) {
					// finish();
					// }
					// });
				} else {
					getMessages(true, true, true, false, false, false);
				}
			} else if (intent.getExtras().containsKey(Const.USER_ID)) {

				chatImage = intent.getExtras().getString(Const.IMAGE);
				chatImageThumb = intent.getExtras().getString(Const.IMAGE_THUMB);

				boolean isGroup = intent.getExtras().containsKey(Const.IS_GROUP);
				mUserId = intent.getExtras().getString(Const.USER_ID);

				new ChatApi().startChat(isGroup, mUserId, intent.getExtras().getString(Const.FIRSTNAME), intent.getExtras().getString(Const.LASTNAME), true, this,
						new ApiCallback<Chat>() {

							@Override
							public void onApiResponse(Result<Chat> result) {

								if (result.isSuccess()) {

									chatId = result.getResultData().getChat_id();
									chatName = result.getResultData().getChat_name();

									setTitle(chatName);

									adapter.clearItems();
									totalItems = Integer.valueOf(result.getResultData().getTotal_count());
									adapter.addItems(result.getResultData().getMessagesList(), true);
									adapter.setSeenBy(result.getResultData().getSeen_by());
									adapter.setTotalCount(Integer.valueOf(result.getResultData().getTotal_count()));
									if (adapter.getCount() > 0) {
										chatListView.setSelectionFromTop(adapter.getCount(), 0);
									}
								} else {
									AppDialog dialog = new AppDialog(ChatActivity.this, false);

									if (result.getResultData() != null) {
										dialog.setFailed(Helper.errorDescriptions(ChatActivity.this, result.getResultData().getCode()));
									} else {
										dialog.setFailed("");
									}
								}

								setNoItemsVisibility();
							}
						});
			}

			if (intent.getExtras().containsKey(Const.TYPE)) {

				try {
					chatType = Integer.valueOf(intent.getExtras().getString(Const.TYPE));
				} catch (Exception e) {
					AppDialog dialog = new AppDialog(this, true);
					dialog.setFailed(getString(R.string.e_something_went_wrong));
					return;
				}

				if (isAdmin && isActive == 1) {
					chatType = Const.C_ROOM_ADMIN_ACTIVE;
				}
				if (isAdmin && isActive == 0) {
					chatType = Const.C_ROOM_ADMIN_INACTIVE;
				}
				setSettingsItems(chatType);
			}

			loadImage();
		}
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
	protected String getUserId() {
		return mUserId;
	}

	public void sendMessage(int type, String chatId, String text, String fileId, String thumbId, String longitude, String latitude) {
		new ChatApi().sendMessage(type, chatId, text, fileId, thumbId, longitude, latitude, this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {
				if (result.isSuccess()) {
					etMessage.setText("");
					hideKeyboard(etMessage);

					callNewMsgs();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(result.getResultData());
					if (result.getResultData() == Const.E_CHAT_INACTIVE) {
						isActive = 0;
						etMessage.setText("");
						hideKeyboard(etMessage);
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
				adapter.clearItems();
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

		new ChatApi().getMessages(isClear, processing, isPagging, isNewMsg, isSend, isRefresh, chatId, msgId, adapterCount, this, new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {

				isRunning = false;

				if (result.isSuccess()) {

					Chat chat = result.getResultData();

					if (TextUtils.isEmpty(mUserId)) {
						mUserId = chat.getUser() == null ? "" : chat.getUser().getId();
					}

					adapter.addItems(chat.getMessagesList(), isNewMsg);
					adapter.setSeenBy(chat.getSeen_by());

					totalItems = Integer.valueOf(chat.getTotal_count());
					adapter.setTotalCount(totalItems);

					if (!isRefresh) {
						if (isClear || isSend) {
							chatListView.setSelectionFromTop(adapter.getCount(), 0);
						} else if (isPagging) {
							chatListView.setSelection(chat.getMessagesList().size());
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
				}

				setNoItemsVisibility();
			}
		});
	}

	@Override
	protected void leaveChat() {
		new ChatApi().leaveChat(chatId, true, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
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
	protected void onEditorSendEvent(String text) {
		sendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null);
	}

	private void setNoItemsVisibility() {
		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}
	}

	@Override
	protected void deactivateChat() {
		new ChatApi().updateChat(chatId, Const.UPDATE_CHAT_DEACTIVATE, "", "", "", true, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
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
			public void onPositiveButtonClick(View v) {
				new ChatApi().updateChat(chatId, Const.UPDATE_CHAT_DELETE, "", "", "", true, ChatActivity.this, new ApiCallback<BaseModel>() {

					@Override
					public void onApiResponse(Result<BaseModel> result) {
						if (result.isSuccess()) {
							dialog.dismiss();
							AppDialog dialogSS = new AppDialog(ChatActivity.this, true);
							dialogSS.setSucceed();
						} else {
							AppDialog dialogSS = new AppDialog(ChatActivity.this, false);
							dialogSS.setFailed(null);
						}
					}
				});
			}
		});

		dialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {

			@Override
			public void onNegativeButtonClick(View v) {
				dialog.dismiss();
			}
		});

	}

	@Override
	protected void activateChat() {
		new ChatApi().updateChat(chatId, Const.UPDATE_CHAT_ACTIVATE, "", "", "", true, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					AppDialog dialog = new AppDialog(ChatActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

}
