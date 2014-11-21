package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends CustomFragment implements OnItemClickListener, OnSearchListener {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public UserAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new UserAdapter(getActivity(), new ArrayList<User>());

		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).setSearch(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_users_list, container, false);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		getUsers(mCurrentIndex, null, false);

		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
		((MainActivity) getActivity()).disableSearch();
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
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();
		if (toClearPrevious)
			currentCount = data.size();

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);
		if (toClearPrevious)
			mainListView.getRefreshableView().setSelection(0);

		mainListView.onRefreshComplete();

		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}

		if (currentCount >= mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
	}

	public void getUsers(int page, String search, final boolean toClear) {

		UserApi api = new UserApi();

		if (search == null) {
			api.getUsersWithPage(getActivity(), mCurrentIndex, null, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mTotalCount = result.getResultData().getTotalCount();
						setData(result.getResultData().getUserList(), toClear);
					}
				}
			});
		} else {
			api.getUsersByName(mCurrentIndex, null, search, getActivity(), true, new ApiCallback<UsersList>() {

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

		if (position != -1 && position != adapter.getCount()) {
			User user = adapter.getItem(position);
			ChatActivity.startWithUserId(getActivity(), user.getId(), false, user.getFirstName(), user.getLastName());
		}
	}
}
