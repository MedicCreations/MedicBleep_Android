package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GroupsApi;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.GroupsList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GroupsFragment extends CustomFragment implements OnSearchListener {

	TextView noItems;

	PullToRefreshListView mainListView;
	public GroupAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		adapter = new GroupAdapter(getActivity(), new ArrayList<Group>());
		
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

		View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		getGroup(mCurrentIndex, null, false);

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
			getGroup(mCurrentIndex, mSearchData, false);
		}
	};

	private void setData(List<Group> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious)
			adapter.clearItems();
		adapter.addItems(data);
		if (toClearPrevious)
			mainListView.getRefreshableView().setSelection(0);

		mainListView.onRefreshComplete();

		if (adapter.getCount() == 0 || adapter.getCount() == 1) {
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

	public void getGroup(int page, String search, final boolean toClear) {
		GroupsApi groupApi = new GroupsApi();
		if (search == null) {
			groupApi.getGroupsWithPage(mCurrentIndex, getActivity(), true, new ApiCallback<GroupsList>() {

				@Override
				public void onApiResponse(Result<GroupsList> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getGroupList(), toClear);
				}
			});
		} else {
			groupApi.getGroupsByName(mCurrentIndex, search, getActivity(), true, new ApiCallback<GroupsList>() {

				@Override
				public void onApiResponse(Result<GroupsList> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getGroupList(), toClear);
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
		getGroup(mCurrentIndex, mSearchData, true);
	}
}
