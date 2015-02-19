package com.clover.spika.enterprise.chat.extendables;

import github.ankushsachdeva.emojicon.EmojiconGridView.OnEmojiconClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup;
import github.ankushsachdeva.emojicon.EmojiconsPopup.OnEmojiconBackspaceClickedListener;
import github.ankushsachdeva.emojicon.EmojiconsPopup.OnSoftKeyboardOpenCloseListener;
import github.ankushsachdeva.emojicon.emoji.Emojicon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;
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
import com.clover.spika.enterprise.chat.api.EmojiApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnNegativeButtonCLickListener;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.Stickers;
import com.clover.spika.enterprise.chat.models.StickersHolder;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.emoji.EmojiRelativeLayout;
import com.clover.spika.enterprise.chat.views.emoji.SelectEmojiListener;
import com.clover.spika.enterprise.chat.views.menu.FrameLayoutForMenuPager;
import com.clover.spika.enterprise.chat.views.menu.SelectImageListener;

public abstract class BaseChatActivity extends BaseActivity {

	protected static final int PICK_FILE_RESULT_CODE = 987;

	protected static final int SETTINGS_POSITION_FIRST = 0;
	protected static final int SETTINGS_POSITION_SECOND = 1;
	protected static final int SETTINGS_POSITION_THIRD = 2;
	protected static final int SETTINGS_POSITION_FOURTH = 3;
	
	private static final int MENU_OPEN = 1;
	private static final int STATIC_SMILEY_OPEN = 2;
	private static final int NONE_OPEN = 0;

	protected String chatImage = null;
	protected String chatImageThumb = null;
	protected String chatId = null;
	protected boolean isAdmin = false;
	protected int isActive = 1;
	protected int isPrivate = 0;
	protected String chatPassword = null;
	private int drawerDuration = 300;
	private int drawerHeight = 200;
	private int drawerNewHeight = 395;
	protected String chatName = null;
	protected String categoryName = null;
	protected String categoryId = null;
	protected int chatType = 0;

	private ListView settingsListView;
	private ImageButton footerMore;
	private ImageButton footerEmoji;
	protected RelativeLayout rlDrawerNew;
	protected RelativeLayout rlDrawerEmoji;
	protected EmojiconsPopup staticEmojiPopup;
	private RelativeLayout chatLayout;
	private RobotoThinTextView screenTitle;
	protected EditText etMessage;
	protected ListView chatListView;
	
	private View dimMenu;
	private View dimOther;

	private SettingsAdapter settingsAdapter;

	private Animation animShowSettings;
	private Animation animHideSettings;
	private Animation animHideSettingsHack;

	private ImageLoader imageLoader;
	
	private List<Stickers> stickersList = new ArrayList<Stickers>();
	
	private boolean isMenuInAnimation = false;
	
	private SelectEmojiListener mEmojiListener = null;
	private boolean isMenuSetted = false;

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
		
		findViewById(R.id.chooseLocation).setOnClickListener(thisClickListener);
		findViewById(R.id.choosePhoto).setOnClickListener(thisClickListener);
		findViewById(R.id.chooseVideo).setOnClickListener(thisClickListener);
		findViewById(R.id.chooseVoice).setOnClickListener(thisClickListener);
		findViewById(R.id.voiceCall).setOnClickListener(thisClickListener);
		findViewById(R.id.chooseFile).setOnClickListener(thisClickListener);
		findViewById(R.id.footerSend).setOnClickListener(thisClickListener);

		chatListView = (ListView) findViewById(R.id.main_list_view);

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
		etMessage.addTextChangedListener(thisTextChangeWatcher);

		rlDrawerNew = (RelativeLayout) findViewById(R.id.rlNewDrawer);
		rlDrawerNew.setSelected(false);
		
		dimMenu = findViewById(R.id.blackedTopMenu);
		dimOther = findViewById(R.id.blackedOther);
		
		rlDrawerEmoji = (RelativeLayout) findViewById(R.id.rlDrawerEmoji);
		rlDrawerEmoji.setSelected(false);

		footerMore = (ImageButton) findViewById(R.id.footerMore);
		footerMore.setOnClickListener(thisClickListener);
		
		footerEmoji = (ImageButton) findViewById(R.id.footerSmiley);
		footerEmoji.setOnClickListener(thisClickListener);

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
		
		dimOther.setOnClickListener(thisClickListener);
		dimMenu.setOnClickListener(thisClickListener);
		
		boolean isEmojiEnable = getResources().getBoolean(R.bool.enable_emoji);
		if(!isEmojiEnable){
			findViewById(R.id.footerSmiley).setVisibility(View.GONE);
		}
		
		boolean isStaticEmojiEnable = getResources().getBoolean(R.bool.enable_static_emoji);
		if(!isStaticEmojiEnable){
			findViewById(R.id.footerSmileyStatic).setVisibility(View.GONE);
		}
		
		staticEmojiPopup = new EmojiconsPopup(findViewById(R.id.rootView), this);
		staticEmojiPopup.setSizeForSoftKeyboard();
		
		final ImageButton staticSmileyButton = (ImageButton) findViewById(R.id.footerSmileyStatic);
		staticSmileyButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				staticEmojiManage(staticSmileyButton);
			}
		});
		
		// Set on emojicon click listener
		staticEmojiPopup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() {

			@Override
			public void onEmojiconClicked(Emojicon emojicon) {
				etMessage.append(emojicon.getEmoji());
			}
		});

		// Set on backspace click listener
		staticEmojiPopup.setOnEmojiconBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() {

			@Override
			public void onEmojiconBackspaceClicked(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				etMessage.dispatchKeyEvent(event);
			}
		});

		// If the emoji popup is dismissed, change emojiButton to smiley icon
		staticEmojiPopup.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				changeEmojiKeyboardIcon(staticSmileyButton, R.drawable.smiley_static);
			}
		});

		// If the text keyboard closes, also dismiss the emoji popup
		staticEmojiPopup.setOnSoftKeyboardOpenCloseListener(new OnSoftKeyboardOpenCloseListener() {

			@Override
			public void onKeyboardOpen(int keyBoardHeight) {

			}

			@Override
			public void onKeyboardClose() {
				if (staticEmojiPopup.isShowing())
					staticEmojiPopup.dismiss();
			}
		});

		// On emoji clicked, add it to edittext
		staticEmojiPopup.setOnEmojiconClickedListener(new OnEmojiconClickedListener() {

			@Override
			public void onEmojiconClicked(Emojicon emojicon) {
				etMessage.append(emojicon.getEmoji());
			}
		});

		// On backspace clicked, emulate the KEYCODE_DEL key event
		staticEmojiPopup.setOnEmojiconBackspaceClickedListener(new OnEmojiconBackspaceClickedListener() {

			@Override
			public void onEmojiconBackspaceClicked(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				etMessage.dispatchKeyEvent(event);
			}
		});
		
	}
	
	private void changeEmojiKeyboardIcon(ImageButton iconToBeChanged, int drawableResourceId){
		iconToBeChanged.setImageResource(drawableResourceId);
	}
	
	protected void setMenuByChatType () {
		if(!isMenuSetted){
			isMenuSetted = true;
			if(chatType != Const.C_PRIVATE){
				rlDrawerNew.removeView(rlDrawerNew.getChildAt(rlDrawerNew.getChildCount()-1)); // remove call
				rlDrawerNew.removeView(rlDrawerNew.getChildAt(rlDrawerNew.getChildCount()-1)); // remove divider above call
				rlDrawerNew.getLayoutParams().height = Helper.dpToPx(this, 344);
				drawerNewHeight = Helper.dpToPx(this, 344);
				rlDrawerNew.getChildAt(rlDrawerNew.getChildCount()-1).setBackgroundResource(R.drawable.trans_to_gray_with_bottom_corners);
			}
		}
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
	
	private void rlDrawerNewManage() {
		if(isMenuInAnimation) return;
		if(rlDrawerEmoji.isSelected()){
			rlDrawerEmojiManage(MENU_OPEN);
			return;
		}
		if (!rlDrawerNew.isSelected()) {
			isMenuInAnimation = true;
			rlDrawerNew.setVisibility(View.VISIBLE);
			dimMenu.setVisibility(View.VISIBLE);
			dimOther.setVisibility(View.VISIBLE);
			AnimUtils.translationY(rlDrawerNew, Helper.dpToPx(this, drawerNewHeight), 0, drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawerNew.setSelected(true);
					hideKeyboard(etMessage);
					
					new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							FrameLayout flForPager = (FrameLayout) findViewById(R.id.layoutForImagesPager);
							FrameLayoutForMenuPager pagerLayout = new FrameLayoutForMenuPager(BaseChatActivity.this);
							pagerLayout.setViews(new SelectImageListener() {

								@Override
								public void onSelectImage(String path) {
									if(path.equals("camera")){
										openCamera();
									}else{
										openPathCropActivity(path);
									}
								}
							});
							flForPager.addView(pagerLayout);

							findViewById(R.id.pbLoading).setVisibility(View.GONE);
							isMenuInAnimation = false;
						}
					}, 200);
					
				}
			});
			
			AnimUtils.fadeAnim(dimMenu, 0, 1, drawerDuration);
			AnimUtils.fadeAnim(dimOther, 0, 1, drawerDuration);
		} else {
			isMenuInAnimation = true;
			FrameLayout flForPager = (FrameLayout) findViewById(R.id.layoutForImagesPager);
			if(flForPager.getChildCount() > 1 && flForPager.getChildAt(1) instanceof FrameLayoutForMenuPager) ((FrameLayoutForMenuPager) flForPager.getChildAt(1)).clearAdapters();
			if(flForPager.getChildCount() > 1 ) flForPager.removeView(flForPager.getChildAt(1));
			
			findViewById(R.id.pbLoading).setVisibility(View.VISIBLE);
			
			AnimUtils.translationY(rlDrawerNew, 0, Helper.dpToPx(this, drawerNewHeight), drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawerNew.setVisibility(View.GONE);
					rlDrawerNew.setSelected(false);
					dimMenu.setVisibility(View.GONE);
					dimOther.setVisibility(View.GONE);
					isMenuInAnimation = false;

				}
			});
			AnimUtils.fadeAnim(dimMenu, 1, 0, drawerDuration);
			AnimUtils.fadeAnim(dimOther, 1, 0, drawerDuration);
		}
	}

	private void rlDrawerEmojiManage(final int openOther) {
		if(isMenuInAnimation) return;
		if(stickersList.size() == 0){
			new EmojiApi().getEmoji(this, new ApiCallback<StickersHolder>() {
				
				@Override
				public void onApiResponse(Result<StickersHolder> result) {
					stickersList.addAll(result.getResultData().getStickersList());
					EmojiRelativeLayout layout = (EmojiRelativeLayout) rlDrawerEmoji.getChildAt(0);
					layout.setStickersList(result.getResultData().getStickersList(), BaseChatActivity.this, mEmojiListener);
				}
			});
		}
		if (!rlDrawerEmoji.isSelected()) {
			isMenuInAnimation = true;
			rlDrawerEmoji.setVisibility(View.VISIBLE);
			AnimUtils.translationY(rlDrawerEmoji, Helper.dpToPx(this, drawerHeight), 0, drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawerEmoji.setSelected(true);
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
					params.bottomMargin = Helper.dpToPx(BaseChatActivity.this, drawerHeight);
					chatListView.setLayoutParams(params);

					hideKeyboard(etMessage);

					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							chatListView.setSelection(chatListView.getAdapter().getCount() - 1);
							EmojiRelativeLayout layout = (EmojiRelativeLayout) rlDrawerEmoji.getChildAt(0);
							layout.resetDotsIfNeed();
							isMenuInAnimation = false;
						}
					}, 100);
				}
			});
			AnimUtils.translationY(chatLayout, 0, -Helper.dpToPx(this, drawerHeight), drawerDuration, null);
		} else {
			isMenuInAnimation = true;
			AnimUtils.translationY(rlDrawerEmoji, 0, Helper.dpToPx(this, drawerHeight), drawerDuration, new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					rlDrawerEmoji.setVisibility(View.GONE);
					rlDrawerEmoji.setSelected(false);

					isMenuInAnimation = false;
					if(openOther == MENU_OPEN) rlDrawerNewManage();
					else if(openOther == STATIC_SMILEY_OPEN) staticEmojiManage((ImageButton) findViewById(R.id.footerSmileyStatic));
				}
			});
			AnimUtils.translationY(chatLayout, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chatListView.getLayoutParams();
			params.bottomMargin = 0;
			chatListView.setLayoutParams(params);
			AnimUtils.translationY(chatListView, -Helper.dpToPx(this, drawerHeight), 0, drawerDuration, null);
		}
	}
	
	private void staticEmojiManage(ImageButton buttonView){
		if(rlDrawerEmoji.isSelected()){
			rlDrawerEmojiManage(STATIC_SMILEY_OPEN);
			return;
		}
		if(!staticEmojiPopup.isShowing()){
			
			//If keyboard is visible, simply show the emoji popup
			if(staticEmojiPopup.isKeyBoardOpen()){
				staticEmojiPopup.showAtBottom();
				changeEmojiKeyboardIcon(buttonView, R.drawable.keyboard_icon);
			}
			
			//else, open the text keyboard first and immediately after that show the emoji popup
			else{
				etMessage.setFocusableInTouchMode(true);
				etMessage.requestFocus();
				staticEmojiPopup.showAtBottomPending();
				final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
				changeEmojiKeyboardIcon(buttonView, R.drawable.keyboard_icon);
			}
		}
		
		//If popup is showing, simply dismiss it to show the undelying text keyboard 
		else{
			staticEmojiPopup.dismiss();
		}
		
	}

	protected void forceClose() {
		if (rlDrawerNew.isSelected()) {
			rlDrawerNewManage();
			hideKeyboard(etMessage);
		}else if(rlDrawerEmoji.isSelected()){
			rlDrawerEmojiManage(NONE_OPEN);
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
		if (rlDrawerNew.isSelected()) {
			forceClose();
		} else if(rlDrawerEmoji.isSelected()){
			forceClose();
		}else {
			super.onBackPressed();
		}
	}
	
	private void openCamera() {
		boolean isChoiceEnabled = getResources().getBoolean(R.bool.enable_full_size_and_crop_image_choice);
		if(isChoiceEnabled){
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
					intent.putExtra(Const.IS_SQUARE, false);
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
		}else{
			Intent intent = new Intent(BaseChatActivity.this, CameraFullPhotoActivity.class);
			intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
			intent.putExtra(Const.FROM_WAll, true);
			intent.putExtra(Const.CHAT_ID, chatId);
			intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
			intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
			startActivity(intent);
		}
		
	}
	
	private TextWatcher thisTextChangeWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			if(TextUtils.isEmpty(s.toString())){
				findViewById(R.id.footerSend).setVisibility(View.GONE);
				findViewById(R.id.footerSmiley).setVisibility(View.VISIBLE);
			}else{
				findViewById(R.id.footerSend).setVisibility(View.VISIBLE);
				findViewById(R.id.footerSmiley).setVisibility(View.INVISIBLE);
			}
		}
	};
	
	View.OnClickListener thisClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();

			if (id == R.id.bntFile || id == R.id.chooseFile) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				startActivityForResult(intent, PICK_FILE_RESULT_CODE);

			} else if (id == R.id.btnGallery || id == R.id.choosePhoto) {
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
//					openCameraCropActivity();
					openCameraFullSizeActivity();
				}

			} else if (id == R.id.btnVideo || id == R.id.chooseVideo) {
				AppDialog dialog = new AppDialog(BaseChatActivity.this, false);
				dialog.choseCamGallery(chatId, getRootId(), getMessageId());
				hideSettings();

			} else if (id == R.id.btnLocation || id == R.id.chooseLocation) {
				Intent intent = new Intent(BaseChatActivity.this, LocationActivity.class);
				intent.putExtra(Const.CHAT_ID, chatId);
				intent.putExtra(Const.EXTRA_ROOT_ID, getRootId());
				intent.putExtra(Const.EXTRA_MESSAGE_ID, getMessageId());
				startActivity(intent);

			} else if (id == R.id.btnRecord  || id == R.id.chooseVoice) {
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
				rlDrawerNewManage();
				hideSettings();

			} else if (id == R.id.footerSmiley && isActive == 1) {
				rlDrawerEmojiManage(NONE_OPEN);
				hideSettings();

			}  else if (id == R.id.goBack) {
				kill();
			} else if (id == R.id.settingsBtn) {
				if (settingsListView.getVisibility() == View.GONE) {
					showSettings();
				} else {
					hideSettings();
				}
			} else if (id == R.id.blackedOther) {
				rlDrawerNewManage();
			} else if (id == R.id.blackedTopMenu) {
				rlDrawerNewManage();
			} else if (id == R.id.footerSend) {
				onEditorSendEvent(etMessage.getText().toString());
			} else if (id == R.id.voiceCall) {
				//make call
				Toast.makeText(BaseChatActivity.this, "VOICE CALL IS NOT IMPLEMENTED YES", 2000).show();
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
	
	void openPathCropActivity(String path) {
		Intent intent = new Intent(BaseChatActivity.this, CameraFullPhotoActivity.class);
		intent.putExtra(Const.INTENT_TYPE, Const.PATH_INTENT);
		intent.putExtra(Const.FROM_WAll, true);
		intent.putExtra(Const.EXTRA_PATH, path);
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
	
	public void setEmojiListener(SelectEmojiListener lis){
		mEmojiListener = lis;
	}
	
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
