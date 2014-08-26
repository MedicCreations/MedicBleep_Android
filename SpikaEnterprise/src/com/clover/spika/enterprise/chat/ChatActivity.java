package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.spika.enterprise.chat.adapters.MessagesAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class ChatActivity extends BaseChatActivity {

	private RobotoThinTextView screenTitle;
	private TextView noItems;

	public MessagesAdapter adapter;

	private int totalItems = 0;

	private boolean isRunning = false;
	private boolean isResume = false;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
		noItems = (TextView) findViewById(R.id.noItems);

		adapter = new MessagesAdapter(this, new ArrayList<Message>());
		chatListView.setAdapter(adapter);
        // TODO: elegantnije riješiti click listener
        chatListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter() != null) {
                    Message message = (Message) parent.getAdapter().getItem(position);
                    ThreadsActivity.start(ChatActivity.this, message.getIntegerId());
                }
            }
        });
        chatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getAdapter() != null) {
                    Message message = (Message) parent.getAdapter().getItem(position);
                    if (message.isMe()) {
                        Toast.makeText(ChatActivity.this, message.getCreated(), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

		getIntentData(getIntent());
	}

	@Override
	public void pushCall(String msg, String chatIdPush, String chatName, String chatImage, String pushType) {
		getFromPush(msg, chatIdPush, chatName, chatImage, pushType);
	}

	@Override
	protected void onResume() {
		super.onResume();

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
		getIntentData(intent);
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {

			if (intent.getExtras().containsKey(Const.CHAT_ID)) {

				chatId = intent.getExtras().getString(Const.CHAT_ID);
				chatName = intent.getExtras().getString(Const.CHAT_NAME);
				chatImage = intent.getExtras().getString(Const.IMAGE);

				screenTitle.setText(chatName);

				adapter.clearItems();
				getMessages(true, true, true, false, false, false);
			} else if (intent.getExtras().containsKey(Const.USER_ID)) {

				chatImage = intent.getExtras().getString(Const.IMAGE);

				boolean isGroup = intent.getExtras().containsKey(Const.IS_GROUP);
				String userId = intent.getExtras().getString(Const.USER_ID);

				new ChatApi().startChat(isGroup, userId, intent.getExtras().getString(Const.FIRSTNAME), intent.getExtras().getString(Const.LASTNAME), true, this,
						new ApiCallback<Chat>() {

							@Override
							public void onApiResponse(Result<Chat> result) {

								if (result.isSuccess()) {

									chatId = result.getResultData().getChat_id();
									chatName = result.getResultData().getChat_name();

									screenTitle.setText(chatName);

									adapter.clearItems();
									totalItems = Integer.valueOf(result.getResultData().getTotal_count());
									adapter.addItems(result.getResultData().getMessagesList(), true);
									adapter.setSeenBy(result.getResultData().getSeen_by());
									adapter.setTotalCount(Integer.valueOf(result.getResultData().getTotal_count()));
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
				chatType = Integer.valueOf(intent.getExtras().getString(Const.TYPE));
				disableSettingsItems(chatType);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_FILE_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				Uri fileUri = data.getData();

				String fileName = null;
				String filePath = null;

				if (fileUri.getScheme().equals("content")) {

					String[] proj = { MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DISPLAY_NAME };
					Cursor cursor = getContentResolver().query(fileUri, proj, null, null, null);

					int column_index_name = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
					int column_index_path = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
					cursor.moveToFirst();

					fileName = cursor.getString(column_index_name);
					filePath = cursor.getString(column_index_path);

				} else if (fileUri.getScheme().equals("file")) {

					File file = new File(URI.create(fileUri.toString()));
					fileName = file.getName();
					filePath = file.getAbsolutePath();
				}

				final String finalFileName = fileName;

				final String filePathTemp = Utils.handleFileEncryption(filePath, ChatActivity.this);

				if (filePathTemp == null) {
					AppDialog dialog = new AppDialog(ChatActivity.this, false);
					dialog.setFailed(getResources().getString(R.string.e_while_encrypting_file));
					return;
				}

				new FileManageApi().uploadFile(filePathTemp, this, true, new ApiCallback<UploadFileModel>() {

					@Override
					public void onApiResponse(Result<UploadFileModel> result) {
						if (result.isSuccess()) {
							sendMessage(Const.MSG_TYPE_FILE, chatId, finalFileName, result.getResultData().getFileId(), null, null, null);
						} else {
							AppDialog dialog = new AppDialog(ChatActivity.this, false);
							if (result.hasResultData()) {
								dialog.setFailed(result.getResultData().getMessage());
							} else {
								dialog.setFailed("");
							}
						}
					}
				});
			}
		}
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
				}
			}
		});
	}

	private void getFromPush(String msg, String chatIdPush, String chatName, String chatImage, String pushType) {
		if (chatIdPush.equals(chatId)) {
			getMessages(false, false, false, true, false, true);
		} else {
			showPopUp(msg, chatIdPush, chatName, chatImage);
		}
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

}
