package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;

import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InvitePeopleActivity extends BaseActivity implements OnItemClickListener, OnSearchListener {

	UsersApi api;

	PullToRefreshListView mainList;
	InviteUserAdapter adapter;

	private String chatId = "";
	private int chatType = 0;
	private int mCurrentIndex = 0;
	private String mSearchData = null;
	private int mTotalCount = 0;

	private ImageButton searchBtn;

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

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				invitePeople();
			}
		});

		adapter = new InviteUserAdapter(this, new ArrayList<User>());

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

	private void invitePeople() {
		api.inviteUsers(chatId, adapter.getData(), this, new ApiCallback<Chat>() {

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
}
