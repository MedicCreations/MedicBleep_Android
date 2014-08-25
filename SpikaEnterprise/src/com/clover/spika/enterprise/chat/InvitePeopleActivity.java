package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InvitePeopleActivity extends BaseActivity implements OnItemClickListener, OnSearchListener, OnChangeListener<User> {

	UsersApi api;

	PullToRefreshListView mainList;
	InviteUserAdapter adapter;

	private String chatId = "";
	@SuppressWarnings("unused")
	private int chatType = 0;
	private int mCurrentIndex = 0;
	private String mSearchData = null;
	private int mTotalCount = 0;

	ImageButton goBack;
	TextView screenTitle;

	LinearLayout invitationOptions;
	ImageButton inviteBtn;

	/* Search bar */
	ImageButton searchBtn;
	EditText searchEt;
	ImageButton closeSearchBtn;
	boolean isOpenSearch = false;

	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	OnSearchListener mSearchListener;

	TextView invitedPeople;

	List<User> usersToAdd = new ArrayList<User>();

	public static void startActivity(String chatId, int type, Context context) {
		Intent intent = new Intent(context, InvitePeopleActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.TYPE, type);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_people);
		// setSearch(this);

		api = new UsersApi();

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		invitationOptions = (LinearLayout) findViewById(R.id.invitationOptions);
		inviteBtn = (ImageButton) findViewById(R.id.inviteBtn);
		inviteBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				invitePeople();
			}
		});

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation();
			}
		});

		mSearchListener = this;

		searchBtn.setOnClickListener(searchOnClickListener);

		searchEt.setOnEditorActionListener(editorActionListener);
		searchEt.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);

		invitedPeople = (TextView) findViewById(R.id.invitedPeople);

		adapter = new InviteUserAdapter(this, new ArrayList<User>(), this);

		mainList = (PullToRefreshListView) findViewById(R.id.main_list_view);
		mainList.setAdapter(adapter);
		mainList.setOnRefreshListener(refreshListener2);
		mainList.setOnItemClickListener(this);

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			chatType = intent.getExtras().getInt(Const.TYPE);
			getUsers(0, mSearchData, false);
		}
	}

	@SuppressWarnings("rawtypes")
	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getUsers(mCurrentIndex, mSearchData, false);
		}
	};

	private void setData(List<User> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainList.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);

		if (toClearPrevious)
			mainList.getRefreshableView().setSelection(0);

		mainList.onRefreshComplete();

		if (currentCount >= mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
	}

	private void getUsers(int page, String search, final boolean toClear) {
		if (search == null) {
			api.getUsersWithPage(this, mCurrentIndex, chatId, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mTotalCount = result.getResultData().getTotalCount();
						setData(result.getResultData().getUserList(), toClear);
					}
				}
			});
		} else {
			api.getUsersByName(mCurrentIndex, chatId, search, this, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mTotalCount = result.getResultData().getTotalCount();
						setData(result.getResultData().getUserList(), toClear);
					}
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			User user = adapter.getItem(position);
			ProfileOtherActivity.openOtherProfile(this, user.getImage(), user.getFirstName() + " " + user.getLastName());
		}
	}

	@Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		getUsers(mCurrentIndex, mSearchData, true);
	}

	/**
	 * Set search bar
	 * 
	 * @param listener
	 */
	public void setSearch(OnSearchListener listener) {

		searchBtn.setVisibility(View.VISIBLE);
		mSearchListener = listener;

		searchBtn.setOnClickListener(searchOnClickListener);

		searchEt.setOnEditorActionListener(editorActionListener);
		searchEt.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
	}

	/**
	 * Disable search bar
	 */
	public void disableSearch() {

		if (isOpenSearch) {
			closeSearchAnimation();
		}

		searchBtn.setVisibility(View.GONE);
		searchEt.setVisibility(View.GONE);
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation();
			} else {
				if (mSearchListener != null) {
					String data = searchEt.getText().toString();
					hideKeyboard(searchEt);
					mSearchListener.onSearch(data);
				}
			}
		}
	};

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				hideKeyboard(searchEt);
				if (mSearchListener != null)
					mSearchListener.onSearch(v.getText().toString());
			}
			return false;
		}
	};

	private void openSearchAnimation() {
		searchBtn.setClickable(false);
		goBack.setClickable(false);
		searchEt.setVisibility(View.VISIBLE);

		AnimUtils.translationX(searchEt, screenWidth, 0f, speedSearchAnimation, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				searchBtn.setClickable(true);
				goBack.setClickable(true);
				closeSearchBtn.setVisibility(View.VISIBLE);
				showKeyboardForced(searchEt);
				isOpenSearch = true;
			}
		});

		AnimUtils.translationX(invitationOptions, 0, -(screenWidth - invitationOptions.getWidth()), speedSearchAnimation, null);
		AnimUtils.fadeAnim(goBack, 1, 0, speedSearchAnimation);
		AnimUtils.translationX(screenTitle, 0, -screenWidth, speedSearchAnimation, null);
	}

	private void closeSearchAnimation() {
		searchBtn.setClickable(false);
		goBack.setClickable(false);
		hideKeyboard(searchEt);
		closeSearchBtn.setVisibility(View.GONE);

		AnimUtils.translationX(searchEt, 0f, screenWidth, speedSearchAnimation, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				searchEt.setVisibility(View.GONE);
				searchEt.setText("");
				super.onAnimationEnd(animation);
				searchBtn.setClickable(true);
				goBack.setClickable(true);
				isOpenSearch = false;
			}
		});
		AnimUtils.translationX(invitationOptions, -(screenWidth - invitationOptions.getWidth()), 0, speedSearchAnimation, null);
		AnimUtils.fadeAnim(goBack, 0, 1, speedSearchAnimation);
		AnimUtils.translationX(screenTitle, -screenWidth, 0, speedSearchAnimation, null);

	}

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation();
			return;
		}

		finish();
	}

	private void invitePeople() {

		StringBuilder users = new StringBuilder();

		List<String> usersId = adapter.getSelected();

		if (usersId.isEmpty()) {
			return;
		}

		for (int i = 0; i < usersId.size(); i++) {
			users.append(usersId.get(i));

			if (i != (usersId.size() - 1)) {
				users.append(",");
			}
		}

		api.inviteUsers(chatId, users.toString(), this, new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {
				if (result.isSuccess()) {
					Chat chat = result.getResultData();

					Intent intent = new Intent(InvitePeopleActivity.this, ChatActivity.class);
					intent.putExtra(Const.CHAT_ID, String.valueOf(chat.getChat_id()));
					intent.putExtra(Const.CHAT_NAME, chat.getChat_name());
					intent.putExtra(Const.TYPE, String.valueOf(Const.C_GROUP));
					startActivity(intent);
					finish();
				} else {
					AppDialog dialog = new AppDialog(InvitePeopleActivity.this, false);
					dialog.setFailed("");
				}
			}
		});
	}

	@Override
	public void onChange(User obj) {
		// TODO Auto-generated method stub

		boolean isFound = false;
		int j = 0;

		for (User user : usersToAdd) {
			if (user.getId().equals(obj.getId())) {
				isFound = true;
				break;
			}
			j++;
		}

		if (isFound) {
			usersToAdd.remove(j);
		} else {
			usersToAdd.add(obj);
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < usersToAdd.size(); i++) {
			builder.append(usersToAdd.get(i).getFirstName() + " " + usersToAdd.get(i).getLastName());
			if (i != (usersToAdd.size() - 1)) {
				builder.append(", ");
			}
		}

		invitedPeople.setText(builder.toString());
	}
}
