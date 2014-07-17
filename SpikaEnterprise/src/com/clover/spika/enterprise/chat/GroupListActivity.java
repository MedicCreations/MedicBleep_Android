package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GroupsApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.Groups;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class GroupListActivity extends BaseActivity implements OnClickListener, OnRefreshListener {

	public static GroupListActivity instance;

	TextView screenTitle;

	RelativeLayout noItemsLayout;

	PullToRefreshListView mainListView;
	public GroupAdapter adapter;
	
	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_group_list);

		instance = this;

		screenTitle = (TextView) findViewById(R.id.screenTitle);

		noItemsLayout = (RelativeLayout) findViewById(R.id.noItemsLayout);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		adapter = new GroupAdapter(this, new ArrayList<Group>());

		mainListView.setAdapter(adapter);
		
		getGroup(mCurrentIndex, null);
		
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
			getGroup(mCurrentIndex, null);
		}
	};
	
	private void setData(List<Group> data){
		noItemsLayout.setVisibility(View.GONE);
		
		if(data.size() >= mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}else if(data.size() < mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
		
		adapter.addItems(data);
		
		mainListView.onRefreshComplete();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void getGroup(int page, String search) {
		GroupsApi groupApi=new GroupsApi();
		if(search == null){
			groupApi.getGroupsWithPage(mCurrentIndex, this, true, new ApiCallback<Groups>() {
				
				@Override
				public void onApiResponse(Result<Groups> result) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getGroupList());
				}
			});
		}
	}

	@Override
	public void onClick(View view) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
	}

	@Override
	public void onRefresh() {
		Log.d("LOG", "go to next page");
	}

}
