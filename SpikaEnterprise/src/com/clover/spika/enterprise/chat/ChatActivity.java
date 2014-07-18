package com.clover.spika.enterprise.chat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class ChatActivity extends BaseActivity implements OnClickListener {

	private static final int OPENED = 1003;
	private static final int CLOSED = 1004;

	ImageLoader imageLoader;

	RobotoThinTextView screenTitle;
	ImageView headerBack;
	RelativeLayout headerPerson;
	ImageView meIcon;

	TextView photo;
	TextView gallery;

	ListView main_list_view;
	public MessagesAdapter adapter;

	EditText etMessage;

	String fromProfileId = null;
	String myProfileImg = null;
	String chatId = null;
	String chatName = null;

	int totalItems = 0;

	boolean isRunning = false;
	boolean isResume = false;

	ImageButton footerMore;
	RelativeLayout chatLayout;
	SlidingDrawer mSlidingDrawer;
	RelativeLayout.LayoutParams mParamsOpened;
	RelativeLayout.LayoutParams mParamsClosed;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_chat);

		imageLoader = new ImageLoader(this);

		screenTitle = (RobotoThinTextView) findViewById(R.id.screenTitle);
		meIcon = (ImageView) findViewById(R.id.meIcon);

		footerMore = (ImageButton) findViewById(R.id.footerMore);
		footerMore.setOnClickListener(this);

		photo = (TextView) findViewById(R.id.photo);
		photo.setOnClickListener(this);
		gallery = (TextView) findViewById(R.id.gallery);
		gallery.setOnClickListener(this);

		mSlidingDrawer = (SlidingDrawer) findViewById(R.id.slDrawer);
		chatLayout = (RelativeLayout) findViewById(R.id.chatLayout);

		mParamsClosed = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.menu_height));
		mParamsClosed.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		mParamsOpened = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.menu_height));
		mParamsOpened.addRule(RelativeLayout.ABOVE, mSlidingDrawer.getId());

		main_list_view = (ListView) findViewById(R.id.main_list_view);
		adapter = new MessagesAdapter(this, new ArrayList<Message>());
		main_list_view.setAdapter(adapter);

		main_list_view.setOnScrollListener(new OnScrollListener() {
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					adapter.setScrolling(false);
					adapter.notifyDataSetChanged();
				} else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					adapter.setScrolling(true);
				}
			}
		});

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

		if (!TextUtils.isEmpty(myProfileImg)) {
			imageLoader.displayImage(this, myProfileImg, meIcon, true);
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
			hideKeyboard(etMessage);
			mSlidingDrawer.open();
			chatLayout.setLayoutParams(mParamsOpened);
			break;
		case CLOSED:
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

				fromProfileId = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USER_ID);
				myProfileImg = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.IMAGE);

				chatId = intent.getExtras().getString(Const.CHAT_ID);
				chatName = intent.getExtras().getString(Const.CHAT_NAME);

				screenTitle.setText(chatName);

				adapter.setGroupId(chatId);
				adapter.clearItems();
				getMessages(true, true, true, false, false, false);
			} else if (intent.getExtras().containsKey(Const.USER_ID)) {

				fromProfileId = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.USER_ID);
				myProfileImg = SpikaEnterpriseApp.getSharedPreferences(this).getCustomString(Const.IMAGE);

				new ChatApi().startChat(intent.getExtras().getString(Const.USER_ID), intent.getExtras().getString(Const.FIRSTNAME), intent.getExtras().getString(Const.LASTNAME), true, this, new ApiCallback<Chat>() {

					@Override
					public void onApiResponse(Result<Chat> result) {

						if (result.isSuccess()) {

							chatId = result.getResultData().getChat_id();
							chatName = result.getResultData().getChat_name();

							screenTitle.setText(chatName);

							adapter.setGroupId(chatId);
							adapter.clearItems();
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
			setSlidingDrawer(OPENED);
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

					adapter.addItems(chat.getMsgList(), isNewMsg);

					totalItems = chat.getTotalItems();
					adapter.setTotalItem(totalItems);

					if (!isRefresh) {
						if (isClear || isSend) {
							main_list_view.setSelectionFromTop(adapter.getCount(), 0);
						} else if (isPagging) {
							main_list_view.setSelection(chat.getMsgList().size());
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

					adapter.setScrolling(false);
				}
			}
		});
	}

}
