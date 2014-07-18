package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UserModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class UserListActivity extends BaseActivity implements OnClickListener, OnItemClickListener, OnSearchListener {

	private RelativeLayout noItemsLayout;

	PullToRefreshListView mainListView;
	public UserAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_character_list);

		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);
		adapter = new UserAdapter(this, new ArrayList<User>());

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		setSearch(this);

		setScreenTitle("Users");

		getUsers(mCurrentIndex, null, false);

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
		noItemsLayout.setVisibility(View.GONE);
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size(); // -2
																										// is
																										// because
																										// of
																										// header
																										// and
																										// footer
																										// view

		if (currentCount >= mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (data.size() < mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);
		if (toClearPrevious)
			mainListView.getRefreshableView().setSelection(0);

		mainListView.onRefreshComplete();
	}

	public void getUsers(int page, String search, final boolean toClear) {
		UsersApi api = new UsersApi();
		if (search == null) {
			api.getUsersWithPage(this, mCurrentIndex, true, new ApiCallback<UserModel>() {

				@Override
				public void onApiResponse(Result<UserModel> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getUserList(), toClear);
				}
			});
		} else {
			api.getUsersByName(mCurrentIndex, search, this, true, new ApiCallback<UserModel>() {

				@Override
				public void onApiResponse(Result<UserModel> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getUserList(), toClear);
				}
			});
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != 0 && position != adapter.getCount()) {
			User user = adapter.getItem(position);

			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(Const.USER_ID, user.getId());
			intent.putExtra(Const.FIRSTNAME, user.getFirstName());
			intent.putExtra(Const.LASTNAME, user.getLastName());
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}