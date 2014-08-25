package com.clover.spika.enterprise.chat;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.MessagesAdapter;
import com.clover.spika.enterprise.chat.adapters.SettingsAdapter;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class ChatActivity extends BaseActivity implements OnClickListener, OnItemClickListener {

	private static final int PICK_FILE_RESULT_CODE = 987;

	private ImageLoader imageLoader;

	private RobotoThinTextView screenTitle;
	private RoundImageView partnerIcon;
	private ImageButton settingsBtn;
	private ListView settingsListView;
	private SettingsAdapter settingsAdapter;
	private Animation animShowSettings;
	private Animation animHideSettings;
	private Animation animHideSettingsHack;
	private TextView noItems;

	private Button file;
	private Button photo;
	private Button gallery;
	private Button video;
	private Button location;
	private Button record;

	private ListView main_list_view;
	public MessagesAdapter adapter;

	private EditText etMessage;

	private ImageButton goBack;

	private String chatImage = null;
	private String chatId = null;
	private String chatName = null;
	private int chatType = 0;

	private int totalItems = 0;

	private boolean isRunning = false;
	private boolean isResume = false;

	private ImageButton footerMore;
	private RelativeLayout chatLayout;

	private RelativeLayout rlDrawer;
	private int drawerDuration = 300;
	private int drawerHeight = 200;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_chat);

		imageLoader = new ImageLoader(this);

		screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
		partnerIcon = (RoundImageView) findViewById(R.id.partnerIcon);
		settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
		settingsBtn.setOnClickListener(this);

		animShowSettings = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		animShowSettings.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				settingsListView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});

		animHideSettings = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
		animHideSettings.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				settingsListView.setVisibility(View.GONE);
			}
		});

		animHideSettingsHack = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_hack);
		animHideSettingsHack.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				settingsListView.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});

		settingsListView = (ListView) findViewById(R.id.settings_list_view);
		settingsAdapter = new SettingsAdapter(this);
		settingsListView.setAdapter(settingsAdapter);
		settingsListView.setOnItemClickListener(this);

		noItems = (TextView) findViewById(R.id.noItems);

		footerMore = (ImageButton) findViewById(R.id.footerMore);
		footerMore.setOnClickListener(this);

		file = (Button) findViewById(R.id.bntFile);
		file.setOnClickListener(this);
		photo = (Button) findViewById(R.id.btnPhoto);
		photo.setOnClickListener(this);
		gallery = (Button) findViewById(R.id.btnGallery);
		gallery.setOnClickListener(this);
		video = (Button) findViewById(R.id.btnVideo);
		video.setOnClickListener(this);
		location = (Button) findViewById(R.id.btnLocation);
		location.setOnClickListener(this);
		record = (Button) findViewById(R.id.btnRecord);
		record.setOnClickListener(this);

		chatLayout = (RelativeLayout) findViewById(R.id.chatLayout);
		rlDrawer = (RelativeLayout) findViewById(R.id.rlDrawer);
		rlDrawer.setSelected(false);

		main_list_view = (ListView) findViewById(R.id.main_list_view);
		adapter = new MessagesAdapter(this, new ArrayList<Message>());
		main_list_view.setAdapter(adapter);

		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.setOnClickListener(this);

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(this);

		etMessage.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

				Log.d("Vida", "Editor");
				Log.d("Vida", "Editor: " + event);
				Log.d("Vida", "Editor: " + event.getKeyCode());
				Log.d("Vida", "const: " + KeyEvent.KEYCODE_ENTER);
				Log.d("Vida", "actionId: " + actionId);
				Log.d("Vida", "actionIdConst: " + EditorInfo.IME_ACTION_DONE);

				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
					String text;

					if (!TextUtils.isEmpty(etMessage.getText().toString())) {
						text = etMessage.getText().toString();
					} else {
						return false;
					}

					if (TextUtils.isEmpty(chatId)) {
						return false;
					}

					sendMessage(Const.MSG_TYPE_DEFAULT, chatId, text, null, null, null, null);
				}
				return true;
			}
		});

		getIntentData(getIntent());
	}

	@Override
	public void pushCall(String msg, String chatIdPush, String chatName, String chatImage, int pushType) {
		getFromPush(msg, chatIdPush, chatName, chatImage, pushType);
	}

	@Override
	protected void onResume() {
		super.onResume();

		settingsAnimationHack();

		forceClose();

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

	private void loadImage() {
		if (!TextUtils.isEmpty(chatImage)) {
			partnerIcon.setVisibility(View.VISIBLE);
			partnerIcon.setOnClickListener(this);
			imageLoader.displayImage(this, chatImage, partnerIcon);
		} else {
			partnerIcon.setVisibility(View.INVISIBLE);
			partnerIcon.setOnClickListener(null);
		}
	}

	private void forceClose() {
		if (rlDrawer.isSelected()) {
			rlDrawerManage();
			hideKeyboard(etMessage);
		}
	}

	private void rlDrawerManage() {
		if (!rlDrawer.isSelected()) {
			rlDrawer.setVisibility(View.VISIBLE);
			AnimUtils.translationY(rlDrawer, Helper.dpToPx(this, drawerHeight), 0, drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawer.setSelected(true);
					LayoutParams params = (LayoutParams) main_list_view.getLayoutParams();
					params.bottomMargin = Helper.dpToPx(ChatActivity.this, drawerHeight);
					main_list_view.setLayoutParams(params);
					// main_list_view.smoothScrollToPosition(main_list_view.getAdapter().getCount());

					footerMore.setImageDrawable(getResources().getDrawable(R.drawable.hide_more_btn_off));
					hideKeyboard(etMessage);
				}
			});
			AnimUtils.translationY(chatLayout, 0, -Helper.dpToPx(this, drawerHeight), drawerDuration, null);
		} else {
			AnimUtils.translationY(rlDrawer, 0, Helper.dpToPx(this, drawerHeight), drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawer.setVisibility(View.GONE);
					rlDrawer.setSelected(false);

					footerMore.setImageDrawable(getResources().getDrawable(R.drawable.more_button_selector));
				}
			});
			AnimUtils.translationY(chatLayout, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
			// main_list_view.smoothScrollToPosition(main_list_view.getAdapter().getCount());
			LayoutParams params = (LayoutParams) main_list_view.getLayoutParams();
			params.bottomMargin = 0;
			main_list_view.setLayoutParams(params);
			AnimUtils.translationY(main_list_view, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		isResume = false;
		getIntentData(intent);
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {

			if (intent.getExtras().containsKey(Const.PUSH_TYPE)) {
				// TODO
			}

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

				new ChatApi().startChat(isGroup, intent.getExtras().getString(Const.USER_ID), intent.getExtras().getString(Const.FIRSTNAME),
						intent.getExtras().getString(Const.LASTNAME), true, this, new ApiCallback<Chat>() {

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
				settingsAdapter.disableItem(chatType);
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
	public void onBackPressed() {

		if (rlDrawer.isSelected()) {
			forceClose();

			return;
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.etMessage) {
			showKeyboard(etMessage);
			forceClose();
			hideSettings();
		} else if (id == R.id.footerMore) {

			rlDrawerManage();
			hideSettings();
		} else if (id == R.id.bntFile) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			startActivityForResult(intent, PICK_FILE_RESULT_CODE);
		} else if (id == R.id.btnPhoto) {
			Intent intent = new Intent(this, CameraCropActivity.class);
			intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
			intent.putExtra(Const.FROM_WAll, true);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.btnGallery) {
			Intent intent = new Intent(this, CameraCropActivity.class);
			intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
			intent.putExtra(Const.FROM_WAll, true);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.btnVideo) {
			AppDialog dialog = new AppDialog(this, false);
			dialog.choseCamGallery(chatId);
			hideSettings();
		} else if (id == R.id.btnLocation) {
			Intent intent = new Intent(this, LocationActivity.class);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.btnRecord) {
			Intent intent = new Intent(this, RecordAudioActivity.class);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.partnerIcon) {
			ProfileOtherActivity.openOtherProfile(this, chatImage, chatName);
		} else if (id == R.id.goBack) {
			finish();
		} else if (id == R.id.settingsBtn) {
			if (settingsListView.getVisibility() == View.GONE) {
				showSettings();
			} else {
				hideSettings();
			}
		}
	}

	/* Animation expand started form 0,0 */
	private void settingsAnimationHack() {
		settingsListView.startAnimation(animHideSettingsHack);
	}

	private void showSettings() {
		settingsListView.startAnimation(animShowSettings);
	}

	private void hideSettings() {
		if (settingsListView.getVisibility() == View.VISIBLE) {
			settingsListView.startAnimation(animHideSettings);
		}
	}

	/**
	 * Chat settings item click
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (settingsAdapter.getItem(position).isDisabled()) {
			return;
		}

		if (position == 0) {
			if (chatType == Const.C_PRIVATE) {
				ProfileOtherActivity.openOtherProfile(this, chatImage, chatName);
			} else if (chatType == Const.C_GROUP || chatType == Const.C_TEAM) {
				ChatMembersActivity.startActivity(chatId, this);
			}
		} else if (position == 1) {
			if (chatType == Const.C_GROUP || chatType == Const.C_PRIVATE) {
				InvitePeopleActivity.startActivity(chatId, chatType, this);
			} else {
				// This options is disabled for chat type C_TEAM
				return;
			}
		} else if (position == 2) {
			if (chatType == Const.C_GROUP) {
				leaveChat();
			} else {
				// This option is disabled got chat type C_TEAM and C_PRIVATE
			}
		} else if (position == 3) {
			// TODO add other settings items
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == PICK_FILE_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				Uri fileUri = (Uri) data.getData();

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

	private void getFromPush(String msg, String chatIdPush, String chatName, String chatImage, int pushType) {
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
							main_list_view.setSelectionFromTop(adapter.getCount(), 0);
						} else if (isPagging) {
							main_list_view.setSelection(chat.getMessagesList().size());
						}
					} else {
						int visibleItem = main_list_view.getFirstVisiblePosition();

						boolean isScroll = false;

						if ((adapter.getCount() - visibleItem) <= 15) {
							isScroll = true;
						}

						if (isScroll && !isSend) {
							main_list_view.setSelectionFromTop(adapter.getCount(), 0);
						}
					}
				}

				setNoItemsVisibility();
			}
		});
	}

	private void leaveChat() {
		// TODO implement leave chat
	}

	private void setNoItemsVisibility() {
		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}
	}

}
