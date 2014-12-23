package com.clover.spika.enterprise.chat.extendables;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CameraCropActivity;
import com.clover.spika.enterprise.chat.CameraFullPhotoActivity;
import com.clover.spika.enterprise.chat.InvitePeopleActivity;
import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.ProfileGroupActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.RecordAudioActivity;
import com.clover.spika.enterprise.chat.adapters.SettingsAdapter;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public abstract class BaseChatActivity extends BaseActivity {

	protected static final int PICK_FILE_RESULT_CODE = 987;

	protected static final int SETTINGS_POSITION_FIRST = 0;
	protected static final int SETTINGS_POSITION_SECOND = 1;
	protected static final int SETTINGS_POSITION_THIRD = 2;
	protected static final int SETTINGS_POSITION_FOURTH = 3;

	protected String chatImage = null;
	protected String chatImageThumb = null;
	protected String chatId = null;
	protected boolean isAdmin = false;
	protected int isActive = 1;
	protected int isPrivate = 0;
	protected String chatPassword = null;
	private int drawerDuration = 300;
	private int drawerHeight = 200;
	protected String chatName = null;
	protected String categoryName = null;
	protected String categoryId = null;
	protected int chatType = 0;

	private ListView settingsListView;
	private ImageButton footerMore;
	protected RelativeLayout rlDrawer;
	private RelativeLayout chatLayout;
	private ImageView partnerIcon;
	private RobotoThinTextView screenTitle;
	protected EditText etMessage;
	protected ListView chatListView;

	private SettingsAdapter settingsAdapter;

	private Animation animShowSettings;
	private Animation animHideSettings;
	private Animation animHideSettingsHack;

	private ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		imageLoader = ImageLoader.getInstance(this);

		Button file = (Button) findViewById(R.id.bntFile);
		file.setOnClickListener(thisClickListener);
		Button photo = (Button) findViewById(R.id.btnPhoto);
		photo.setOnClickListener(thisClickListener);
		Button gallery = (Button) findViewById(R.id.btnGallery);
		gallery.setOnClickListener(thisClickListener);
		Button video = (Button) findViewById(R.id.btnVideo);
		video.setOnClickListener(thisClickListener);
		Button location = (Button) findViewById(R.id.btnLocation);
		location.setOnClickListener(thisClickListener);
		Button record = (Button) findViewById(R.id.btnRecord);
		record.setOnClickListener(thisClickListener);

		chatListView = (ListView) findViewById(R.id.main_list_view);

		partnerIcon = (ImageView) findViewById(R.id.partnerIcon);
		screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
		ImageButton goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(thisClickListener);
		ImageButton settingsBtn = (ImageButton) findViewById(R.id.settingsBtn);
		settingsBtn.setOnClickListener(thisClickListener);

		settingsListView = (ListView) findViewById(R.id.settings_list_view);
		settingsAdapter = new SettingsAdapter(this);
		settingsListView.setAdapter(settingsAdapter);
		settingsListView.setOnItemClickListener(thisItemClickListener);

		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.setOnClickListener(thisClickListener);
		setEditTextEditorAction();

		rlDrawer = (RelativeLayout) findViewById(R.id.rlDrawer);
		rlDrawer.setSelected(false);

		footerMore = (ImageButton) findViewById(R.id.footerMore);
		footerMore.setOnClickListener(thisClickListener);

		chatLayout = (RelativeLayout) findViewById(R.id.chatLayout);

		animShowSettings = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		animShowSettings.setAnimationListener(new Animation.AnimationListener() {

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
		animHideSettings.setAnimationListener(new Animation.AnimationListener() {

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
		animHideSettingsHack.setAnimationListener(new Animation.AnimationListener() {

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

		final View activityRootView = findViewById(android.R.id.content);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {

				int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();

				if (heightDiff > 200) {
					chatListView.setSelection(chatListView.getAdapter().getCount() - 1);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		settingsAnimationHack();
		forceClose();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String fileName = null;
		String filePath = null;

		if (requestCode == PICK_FILE_RESULT_CODE) {
			if (resultCode == RESULT_OK) {
				Uri fileUri = data.getData();

				if (fileUri.getScheme().equals("content")) {

					String proj[];
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						proj = new String[] { MediaStore.Files.FileColumns.DISPLAY_NAME };
					} else {
						proj = new String[] { MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DISPLAY_NAME };
					}
					Cursor cursor = getContentResolver().query(fileUri, proj, null, null, null);
					cursor.moveToFirst();

					int column_index_name = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
					int column_index_path = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

					fileName = cursor.getString(column_index_name);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						try {
							new BuildTempFileAsync(this, fileName, new OnTempFileCreatedListener() {
								@Override
								public void onTempFileCreated(String path, String name) {
									if (TextUtils.isEmpty(path)) {
										onFileSelected(RESULT_CANCELED, null, null);
									} else {
										onFileSelected(RESULT_OK, name, path);
									}
								}
							}).execute(getContentResolver().openInputStream(fileUri));
							// async task initialized, exit
							return;
						} catch (FileNotFoundException ignored) {
							filePath = "";
						}
					} else {
						filePath = cursor.getString(column_index_path);
					}

				} else if (fileUri.getScheme().equals("file")) {

					File file = new File(URI.create(fileUri.toString()));
					fileName = file.getName();
					filePath = file.getAbsolutePath();
				}

			}

			if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(filePath)) {
				onFileSelected(RESULT_OK, fileName, filePath);
			} else {
				onFileSelected(RESULT_CANCELED, null, null);
			}
		}

	}

	protected void setTitle(String title) {
		screenTitle.setText(title);
	}

	protected ImageLoader getImageLoader() {
		return imageLoader;
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

	private void rlDrawerManage() {
		if (!rlDrawer.isSelected()) {
			rlDrawer.setVisibility(View.VISIBLE);
			AnimUtils.translationY(rlDrawer, Helper.dpToPx(this, drawerHeight), 0, drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawer.setSelected(true);
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
					params.bottomMargin = Helper.dpToPx(BaseChatActivity.this, drawerHeight);
					chatListView.setLayoutParams(params);

					footerMore.setImageDrawable(getResources().getDrawable(R.drawable.hide_more_btn_off));
					hideKeyboard(etMessage);

					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							chatListView.setSelection(chatListView.getAdapter().getCount() - 1);
						}
					}, 100);
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
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
			params.bottomMargin = 0;
			chatListView.setLayoutParams(params);
			AnimUtils.translationY(chatListView, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
		}
	}

	protected void forceClose() {
		if (rlDrawer.isSelected()) {
			rlDrawerManage();
			hideKeyboard(etMessage);
		}
	}

	private void setEditTextEditorAction() {
		etMessage.setHorizontallyScrolling(false);
		etMessage.setMaxLines(4);
		etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if ((event != null  && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) {
					if (!TextUtils.isEmpty(etMessage.getText().toString())) {
						onEditorSendEvent(etMessage.getText().toString());
						return true;
					}
				}
				return false;
			}
		});
		
	}

	protected void setSettingsItems(int chatType) {
		settingsAdapter.setSettings(chatType);
	}

	protected void loadImage() {
		if (!TextUtils.isEmpty(chatImage)) {
			partnerIcon.setVisibility(View.VISIBLE);
			partnerIcon.setOnClickListener(thisClickListener);
			imageLoader.displayImage(this, chatImageThumb, partnerIcon);
		} else {
			partnerIcon.setVisibility(View.INVISIBLE);
			partnerIcon.setOnClickListener(null);
		}
	}

	private void getFromPush(String msg, String chatIdPush, String pushType, String password) {
		if (chatIdPush.equals(chatId)) {
			onChatPushUpdated();
		} else {
			if (Integer.parseInt(pushType) != Const.PUSH_TYPE_SEEN) {
				showPopUp(msg, chatIdPush, password);
			}
		}
	}

	protected void deleteMessage(final String messageId) {
		AppDialog deleteDialog = new AppDialog(this, false);
		deleteDialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {
			@Override
			public void onPositiveButtonClick(View v) {
				new ChatApi().deleteMessage(messageId, BaseChatActivity.this, new ApiCallback<BaseModel>() {
					@Override
					public void onApiResponse(Result<BaseModel> result) {
						if (result.isSuccess()) {
							onMessageDeleted();
						}
					}
				});
			}
		});
		deleteDialog.setYesNo(getString(R.string.delete_message_confirmation));
	}

	@Override
	public void pushCall(String msg, String chatIdPush, String pushType, String password) {
		getFromPush(msg, chatIdPush, pushType, password);
	}

	@Override
	public void onBackPressed() {
		if (rlDrawer.isSelected()) {
			forceClose();
		} else {
			super.onBackPressed();
		}
	}

	View.OnClickListener thisClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();

			if (id == R.id.bntFile) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				startActivityForResult(intent, PICK_FILE_RESULT_CODE);

			} else if (id == R.id.btnPhoto) {
				final AppDialog dialog = new AppDialog(BaseChatActivity.this, false);
				dialog.setYesNo(getString(R.string.enableEditPhoto), getString(R.string.choiceCroppedImage), getString(R.string.choiceFullSizeImage));
				dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

					@Override
					public void onPositiveButtonClick(View v) {
						Intent intent = new Intent(BaseChatActivity.this, CameraCropActivity.class);
						intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
						intent.putExtra(Const.FROM_WAll, true);
						intent.putExtra(Const.CHAT_ID, chatId);
						intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
						intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
						startActivity(intent);
						dialog.dismiss();
					}
				});

				dialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {

					@Override
					public void onNegativeButtonClick(View v) {
						Intent intent = new Intent(BaseChatActivity.this, CameraFullPhotoActivity.class);
						intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
						intent.putExtra(Const.FROM_WAll, true);
						intent.putExtra(Const.CHAT_ID, chatId);
						intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
						intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
						startActivity(intent);
						dialog.dismiss();
					}
				});

				dialog.show();

			} else if (id == R.id.btnGallery) {
				boolean isChoiceEnabled = getResources().getBoolean(R.bool.enable_full_size_and_crop_image_choice);
				if (isChoiceEnabled) {
					final AppDialog cropImageConfirmationDialog = new AppDialog(BaseChatActivity.this, false);
					cropImageConfirmationDialog.setYesNo(getString(R.string.enableEditPhoto), getString(R.string.choiceCroppedImage), getString(R.string.choiceFullSizeImage));
					cropImageConfirmationDialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {
						@Override
						public void onPositiveButtonClick(View v) {
							openCameraCropActivity();
							cropImageConfirmationDialog.dismiss();
						}
					});

					cropImageConfirmationDialog.setOnNegativeButtonClick(new OnNegativeButtonCLickListener() {
						@Override
						public void onNegativeButtonClick(View v) {
							openCameraFullSizeActivity();
							cropImageConfirmationDialog.dismiss();
						}
					});

					cropImageConfirmationDialog.show();
				} else {
					openCameraCropActivity();
				}

			} else if (id == R.id.btnVideo) {
				AppDialog dialog = new AppDialog(BaseChatActivity.this, false);
				dialog.choseCamGallery(chatId, getRootId(), getMessageId());
				hideSettings();

			} else if (id == R.id.btnLocation) {
				Intent intent = new Intent(BaseChatActivity.this, LocationActivity.class);
				intent.putExtra(Const.CHAT_ID, chatId);
				intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
				intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
				startActivity(intent);

			} else if (id == R.id.btnRecord) {
				Intent intent = new Intent(BaseChatActivity.this, RecordAudioActivity.class);
				intent.putExtra(Const.CHAT_ID, chatId);
				intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
				intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
				startActivity(intent);

			} else if (id == R.id.etMessage && isActive == 1) {
				showKeyboard(etMessage);
				forceClose();
				hideSettings();

			} else if (id == R.id.footerMore && isActive == 1) {
				rlDrawerManage();
				hideSettings();

			} else if (id == R.id.partnerIcon) {
				if (chatType == Const.C_PRIVATE) {
					ProfileOtherActivity.openOtherProfile(BaseChatActivity.this, getUserId(), chatImage, chatName);
				} else if ((chatType == Const.C_GROUP) || (chatType == Const.C_ROOM)) {
					ProfileGroupActivity.openProfile(BaseChatActivity.this, chatImage, chatName, chatId, false, categoryId, categoryName, chatPassword);
				} else {
					ProfileGroupActivity.openProfile(BaseChatActivity.this, chatImage, chatName, chatId, true, true, isPrivate, chatPassword, categoryId, categoryName);
				}

			} else if (id == R.id.goBack) {
				kill();
			} else if (id == R.id.settingsBtn) {
				if (settingsListView.getVisibility() == View.GONE) {
					showSettings();
				} else {
					hideSettings();
				}
			}
		}
	};

	protected void kill() {
		finish();
	}

	void openCameraCropActivity() {
		Intent intent = new Intent(BaseChatActivity.this, CameraCropActivity.class);
		intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
		intent.putExtra(Const.FROM_WAll, true);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
		intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
		startActivity(intent);
	}

	void openCameraFullSizeActivity() {
		Intent intent = new Intent(BaseChatActivity.this, CameraFullPhotoActivity.class);
		intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
		intent.putExtra(Const.FROM_WAll, true);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
		intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
		startActivity(intent);
	}

	AdapterView.OnItemClickListener thisItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (parent.getAdapter() != null) {

				// SettingsItem item = (SettingsItem)
				// parent.getAdapter().getItem(position);

				if (position == SETTINGS_POSITION_FIRST) {
					if (chatType == Const.C_PRIVATE) {
						ProfileOtherActivity.openOtherProfile(BaseChatActivity.this, getUserId(), chatImage, chatName);
					} else if ((chatType == Const.C_GROUP) || (chatType == Const.C_ROOM)) {
						ProfileGroupActivity.openProfile(BaseChatActivity.this, chatImage, chatName, chatId, false, categoryId, categoryName, chatPassword);
					} else {
						ProfileGroupActivity.openProfile(BaseChatActivity.this, chatImage, chatName, chatId, true, true, isPrivate, chatPassword, categoryId, categoryName);
					}
				} else if (position == SETTINGS_POSITION_SECOND) {
					if (chatType == Const.C_PRIVATE) {
						InvitePeopleActivity.startActivity(chatId, chatType, isAdmin, BaseChatActivity.this);
					} else if (chatType == Const.C_ROOM){
						leaveChat();
					} else if (chatType == Const.C_ROOM_ADMIN_ACTIVE) {
						ManageUsersActivity.startActivity(chatId, BaseChatActivity.this);
					}
				} else if (position == SETTINGS_POSITION_THIRD) {
					if (chatType == Const.C_ROOM_ADMIN_ACTIVE) {
						// deactivate chat
						deactivateChat();
					} else if (chatType == Const.C_ROOM_ADMIN_INACTIVE) {
						// deactivate chat
						activateChat();
					}
				} else if (position == SETTINGS_POSITION_FOURTH) {
					// delete chat
					deleteChat();
				}
			}
		}
	};

	/**
	 * Called when admin wants to activate chat
	 */
	protected abstract void activateChat();

	/**
	 * Called when admin wants to deactivate chat
	 */
	protected abstract void deactivateChat();

	/**
	 * Called when admin wants to delete chat
	 */
	protected abstract void deleteChat();

	/**
	 * Called when user wants to leave chat
	 */
	protected abstract void leaveChat();

	/**
	 * Called when "enter" key has been pressed on keyboard
	 * 
	 * @param text
	 *            text provided from EditText
	 */
	protected abstract void onEditorSendEvent(String text);

	/**
	 * Called when push (related to this chat) has been received
	 */
	protected abstract void onChatPushUpdated();

	protected abstract void onMessageDeleted();

	/**
	 * Called as a callback method after user has selected a file.
	 * 
	 * @param result
	 *            same as result available in {@link android.app.Activity},
	 *            {@link android.app.Activity#RESULT_OK} or
	 *            {@link android.app.Activity#RESULT_CANCELED}
	 * @param fileName
	 *            name of the file selected by user, may be null oif result
	 *            isn't successful
	 * @param filePath
	 *            a path to the file which user has selected, may be null if
	 *            result isn't successful
	 */
	protected abstract void onFileSelected(int result, String fileName, String filePath);

	protected abstract String getRootId();

	protected abstract String getMessageId();

	/**
	 * Required to return user id when chat with a single user is opened. This
	 * method can return any value when chat involves more than two people.
	 */
	protected abstract int getUserId();

	public static interface OnTempFileCreatedListener {
		void onTempFileCreated(String path, String name);
	}

	public static class BuildTempFileAsync extends BaseAsyncTask<InputStream, Void, String> {

		private String mFileName;
		private OnTempFileCreatedListener mListener;

		public BuildTempFileAsync(Context ctx, String fileName, OnTempFileCreatedListener listener) {
			super(ctx, true);
			this.mFileName = fileName;
			this.mListener = listener;
		}

		@Override
		protected String doInBackground(InputStream... params) {
			try {
				InputStream in = params[0];

				File tempFile = Utils.getTempFile(getContext(), mFileName);
				OutputStream out = new FileOutputStream(tempFile);

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();

				return tempFile.getAbsolutePath();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return "";
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if (mListener != null) {
				mListener.onTempFileCreated(s, mFileName);
			}
		}
	}
}
