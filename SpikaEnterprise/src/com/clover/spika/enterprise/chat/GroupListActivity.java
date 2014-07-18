package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GroupsApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.GroupModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class GroupListActivity extends BaseActivity implements OnClickListener, OnSearchListener {

	RelativeLayout noItemsLayout;

	PullToRefreshListView mainListView;
	public GroupAdapter adapter;
	
	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_group_list);

		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		adapter = new GroupAdapter(this, new ArrayList<Group>());

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);
		
		setSearch(this);
		
		setScreenTitle("Groups");
		
		getGroup(mCurrentIndex, null, false);
		
	}
	
	@SuppressWarnings("rawtypes")
	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
//			mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getGroup(mCurrentIndex, mSearchData, false);
		}
	};
	
	private void setData(List<Group> data, boolean toClearPrevious){
		noItemsLayout.setVisibility(View.GONE);
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount()-2+data.size(); // -2 is because of header and footer view
		
		if(currentCount >= mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}else if(data.size() < mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
		
		if(toClearPrevious) adapter.clearItems();
		adapter.addItems(data);
		if(toClearPrevious) mainListView.getRefreshableView().setSelection(0);
		
		mainListView.onRefreshComplete();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public void getGroup(int page, String search, final boolean toClear) {
		GroupsApi groupApi=new GroupsApi();
		if(search == null){
			groupApi.getGroupsWithPage(mCurrentIndex, this, true, new ApiCallback<GroupModel>() {
				
				@Override
				public void onApiResponse(Result<GroupModel> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getGroupList(), toClear);
				}
			});
		}else{
			groupApi.getGroupsByName(mCurrentIndex, search, this, true, new ApiCallback<GroupModel>() {
				
				@Override
				public void onApiResponse(Result<GroupModel> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getGroupList(), toClear);
				}
			});
		}
	}

	@Override
	public void onClick(View view) {
	}

	@Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if(TextUtils.isEmpty(data)){
			mSearchData = null;
		}else{
			mSearchData = data;
		}
		getGroup(mCurrentIndex, mSearchData, true);
	}

}
