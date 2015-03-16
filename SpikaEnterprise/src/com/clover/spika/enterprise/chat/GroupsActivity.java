package com.clover.spika.enterprise.chat;

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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.GlobalModelAdapter;
import com.clover.spika.enterprise.chat.api.robospice.GlobalSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends BaseActivity implements OnItemClickListener, OnSearchListener {

	TextView noItems;

	PullToRefreshListView mainListView;
	public GlobalModelAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private ImageButton searchBtn;
	private EditText searchEt;
	private ImageButton closeSearchBtn;

	int screenWidth;
	int speedSearchAnimation = 300;// android.R.integer.config_shortAnimTime;
	private OnSearchListener mSearchListener;

	private String mCategory = "0";

	public static void startActivity(String categoryId, Context context) {
		Intent intent = new Intent(context, GroupsActivity.class);
		intent.putExtra(Const.CATEGORY_ID, categoryId);
		context.startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_groups);
		// setSearch(this);

		adapter = new GlobalModelAdapter(spiceManager, this, new ArrayList<GlobalModel>(), R.drawable.default_group_image);
		mCurrentIndex = 0;

		noItems = (TextView) findViewById(R.id.noItems);
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		searchBtn = (ImageButton) findViewById(R.id.searchBtn);
		searchEt = (EditText) findViewById(R.id.searchEt);
		closeSearchBtn = (ImageButton) findViewById(R.id.close_search);

		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		mCategory = getIntent().getStringExtra(Const.CATEGORY_ID);

		((TextView) findViewById(R.id.screenTitle)).setText(getString(R.string.groups));

		getGroup(mCurrentIndex, null, false);

		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		closeSearchBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), screenWidth,
						speedSearchAnimation);
			}
		});
		
	}

	@Override
	public void onResume() {
		super.onResume();
		setSearch(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		disableSearch();
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

	private void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();
		if (toClearPrevious)
			currentCount = data.size();

		if (toClearPrevious) {
			adapter.setData(data);
		} else {
			adapter.addData(data);
		}

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

	public void getGroup(int page, String search, final boolean toClear) {
		
		handleProgress(true);

		GlobalSpice.GlobalSearch globalSearch = new GlobalSpice.GlobalSearch(page, null, mCategory, Type.GROUP, search, this);
		spiceManager.execute(globalSearch, new CustomSpiceListener<GlobalResponse>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, GroupsActivity.this);
			}

			@Override
			public void onRequestSuccess(GlobalResponse result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					mTotalCount = result.getTotalCount();
					setData(result.getModelsList(), toClear);

				} else {
					String message = getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, GroupsActivity.this);
				}
			}
		});
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			Group group = (Group) adapter.getItem(position).getModel();
			ChatActivity.startWithUserId(this, String.valueOf(group.getId()), true, group.getGroupName(), null, null);
		}
	}

	public void setSearch(OnSearchListener listener) {
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}

	public void disableSearch() {
		disableSearch(searchBtn, searchEt, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, (TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation);
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), screenWidth,
						speedSearchAnimation);
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

	@Override
	public void onBackPressed() {
		if (searchEt != null && searchEt.getVisibility() == View.VISIBLE) {
			closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), screenWidth,
					speedSearchAnimation);
			return;
		}

		finish();
	}

}
