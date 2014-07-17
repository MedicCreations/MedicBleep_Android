package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.GroupAdapter;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
			getGroup();
		}
	};
	
	private void setData(List<Group> data, boolean toPullDown){
		noItemsLayout.setVisibility(View.GONE);
		
		if(data.size() >= mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}else if(data.size() < mTotalCount){
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
		
		adapter.clearItems();
		adapter.addItems(data);
		
		if (toPullDown) {
			mainListView.getRefreshableView().setSelection(0);
		} else {
			mainListView.getRefreshableView().setSelection(data.size());
		}
		mainListView.onRefreshComplete();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getList();
	}
	
	private void getGroup() {
		
	}

	public void getList() {
		
		List<Group> tempDiscussion = new ArrayList<Group>();
		for(int i=0;i<20;i++){
			Group group = new Group();
			group.setGroup_name("group name");
			group.setGroupId("1");
			group.setImage_name(null);
			tempDiscussion.add(group);
		}
		Group group = new Group();
		group.setGroup_name("group name");
		group.setGroupId("1");
		group.setImage_name(null);
		tempDiscussion.add(group);
		tempDiscussion.add(group);
		tempDiscussion.add(group);
		
		noItemsLayout.setVisibility(View.GONE);
		adapter.clearItems();
		adapter.addItems(tempDiscussion);
		adapter.setNewGroupPeriod("1200");
		
		if(true) return;
		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			List<Group> tempDiscussion = new ArrayList<Group>();
			Integer code = 0;

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {
					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_USERS));
					getParams.put(Const.FUNCTION, Const.F_USER_GET_GROUPS);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject result = NetworkManagement.httpPostRequest(getParams, new JSONObject());

					if (result != null) {

						code = result.getInt(Const.CODE);

						if (code == Const.E_SUCCESS) {

							JSONArray items = result.getJSONArray(Const.ITEMS);

							for (int i = 0; i < items.length(); i++) {
								JSONObject obj = (JSONObject) items.get(i);

								Gson sGsonExpose = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
								Group discussion = sGsonExpose.fromJson(obj.toString(), Group.class);

								if (discussion != null) {
									tempDiscussion.add(discussion);
								}
							}

							if (tempDiscussion.size() > 0) {
								return Const.E_SUCCESS;
							}
						} else {
							return code;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					setData(tempDiscussion, true);
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
					// noItemsLayout.setVisibility(View.VISIBLE);
				}
			};

		}.execute();
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
