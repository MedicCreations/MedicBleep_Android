package com.clover.spika.enterprise.chat;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.MessagesAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class ChatActivity extends BaseActivity implements OnClickListener {

	private static final int OPENED = 1003;
	private static final int CLOSED = 1004;

	private ImageLoader imageLoader;

	private RobotoThinTextView screenTitle;
	private RoundImageView partnerIcon;

	private Button photo;
	private Button gallery;
	private Button video;
	private Button location;
	private Button record;

	private ListView main_list_view;
	public MessagesAdapter adapter;

	private EditText etMessage;

	private String myUserId = null;
	private String chatImage = null;
	private String chatId = null;
	private String chatName = null;

	private int totalItems = 0;

	private boolean isRunning = false;
	private boolean isResume = false;

	private ImageButton footerMore;
	private RelativeLayout chatLayout;
	private SlidingDrawer mSlidingDrawer;
	private RelativeLayout.LayoutParams mParamsOpened;
	private RelativeLayout.LayoutParams mParamsClosed;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_chat);

		imageLoader = new ImageLoader(this);

		screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
		partnerIcon = (RoundImageView) findViewById(R.id.partnerIcon);

		footerMore = (ImageButton) findViewById(R.id.footerMore);
		footerMore.setOnClickListener(this);

		photo = (Button) findViewById(R.id.photo);
		photo.setOnClickListener(this);
		gallery = (Button) findViewById(R.id.gallery);
		gallery.setOnClickListener(this);
		video = (Button) findViewById(R.id.video);
		video.setOnClickListener(this);
		location = (Button) findViewById(R.id.location);
		location.setOnClickListener(this);
		record = (Button) findViewById(R.id.record);
		record.setOnClickListener(this);

		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slDrawer);
		chatLayout = (RelativeLayout) findViewById(R.id.chatLayout);

		mParamsClosed = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.menu_height));
		mParamsClosed.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		mParamsOpened = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.menu_height));
		mParamsOpened.addRule(RelativeLayout.ABOVE, mSlidingDrawer.getId());

		main_list_view = (ListView) findViewById(R.id.main_list_view);
		adapter = new MessagesAdapter(this, new ArrayList<Message>());
		main_list_view.setAdapter(adapter);

		etMessage = (EditText) findViewById(R.id.etMessage);
		etMessage.setOnClickListener(this);

		etMessage.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

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

					sendMessage(text, Const.MSG_TYPE_DEFAULT);
				}
				return true;
			}
		});

		getIntentData(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);
		}

		if (!TextUtils.isEmpty(chatImage)) {
			partnerIcon.setVisibility(View.VISIBLE);
			imageLoader.displayImage(this, chatImage, partnerIcon, false);
		} else {
			partnerIcon.setVisibility(View.INVISIBLE);
		}

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

	private void setSlidingDrawer(int state) {

		switch (state) {
		case OPENED:
			footerMore.setImageDrawable(getResources().getDrawable(R.drawable.hide_more_btn_off));
			hideKeyboard(etMessage);
			mSlidingDrawer.open();
			chatLayout.setLayoutParams(mParamsOpened);
			break;
		case CLOSED:
			footerMore.setImageDrawable(getResources().getDrawable(R.drawable.more_button_selector));
			mSlidingDrawer.close();
			chatLayout.setLayoutParams(mParamsClosed);
			break;
		default:
			break;
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
			if (intent.getExtras().containsKey(Const.CHAT_ID)) {

				myUserId = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USER_ID);

				chatId = intent.getExtras().getString(Const.CHAT_ID);
				chatName = intent.getExtras().getString(Const.CHAT_NAME);
				chatImage = intent.getExtras().getString(Const.IMAGE);

				screenTitle.setText(chatName);

				adapter.clearItems();
				getMessages(true, true, true, false, false, false);
			} else if (intent.getExtras().containsKey(Const.USER_ID)) {

				myUserId = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USER_ID);
				chatImage = intent.getExtras().getString(Const.IMAGE);

				boolean isGroup = intent.getExtras().containsKey(Const.IS_GROUP);

				new ChatApi().startChat(isGroup, intent.getExtras().getString(Const.USER_ID), intent.getExtras().getString(Const.FIRSTNAME), intent.getExtras().getString(Const.LASTNAME), true, this, new ApiCallback<Chat>() {

					@Override
					public void onApiResponse(Result<Chat> result) {

						if (result.isSuccess()) {

							chatId = result.getResultData().getChat_id();
							chatName = result.getResultData().getChat_name();

							screenTitle.setText(chatName);

							adapter.clearItems();
							totalItems = Integer.valueOf(result.getResultData().getTotal_count());
							adapter.addItems(result.getResultData().getMessagesList(), true);
							adapter.setTotalCount(Integer.valueOf(result.getResultData().getTotal_count()));
						} else {
							AppDialog dialog = new AppDialog(ChatActivity.this, false);

							if (result.getResultData() != null) {
								dialog.setFailed(Helper.errorDescriptions(ChatActivity.this, result.getResultData().getCode()));
							} else {
								dialog.setFailed("");
							}
						}
					}
				});
			}
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

		if (mSlidingDrawer.isOpened()) {
			setSlidingDrawer(CLOSED);

			return;
		} else {
			super.onBackPressed();
		}
	}

	public void callAfterPush(String disId, String msg, int type) {
		if (disId.equals(chatId)) {
			getMessages(false, false, false, true, false, true);
		} else {
			showPopUp(msg, disId, type);
		}
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.etMessage) {
			showKeyboard(etMessage);
			setSlidingDrawer(CLOSED);
		} else if (id == R.id.footerMore) {

			if (mSlidingDrawer.isOpened()) {
				setSlidingDrawer(CLOSED);
			} else {
				setSlidingDrawer(OPENED);
			}

		} else if (id == R.id.photo) {
			Intent intent = new Intent(this, CameraCropActivity.class);
			intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
			intent.putExtra(Const.FROM_WAll, true);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.gallery) {
			Intent intent = new Intent(this, CameraCropActivity.class);
			intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
			intent.putExtra(Const.FROM_WAll, true);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.video) {
			AppDialog dialog = new AppDialog(this, false);
			dialog.choseCamGallery(chatId);
		} else if (id == R.id.location) {
			Intent intent = new Intent(this, LocationActivity.class);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivity(intent);
		} else if (id == R.id.record) {
			// TODO
			// Intent intent = new Intent(this, CameraCropActivity.class);
			// intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
			// intent.putExtra(Const.FROM_WAll, true);
			// intent.putExtra(Const.CHAT_ID, chatId);
			// startActivity(intent);
		}
	}

	public void sendMessage(final String text, final int type) {
		new ChatApi().sendMessage(type, chatId, text, null, null, null, this, new ApiCallback<Integer>() {

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

				// res
				isRunning = false;

				if (result.isSuccess()) {

					Chat chat = result.getResultData();

					adapter.addItems(chat.getMessagesList(), isNewMsg);

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
			}
		});
	}

}
