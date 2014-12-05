package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.adapters.GlobalModelAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GlobalApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class RoomsActivity extends BaseActivity implements AdapterView.OnItemClickListener, OnSearchListener {

	public static void startActivity(String categoryId, String categoryName, @NonNull Context context) {
		Intent intent = new Intent(context, RoomsActivity.class);
		intent.putExtra(Const.CATEGORY_ID, categoryId);
		intent.putExtra(Const.CATEGORY_NAME, categoryName);
		context.startActivity(intent);
	}

	private TextView noItems;

	private PullToRefreshListView mainListView;
	public GlobalModelAdapter adapter;

	private ImageButton searchBtn;
	private EditText searchEt;
	private ImageButton closeSearchBtn;

	private String mCategory = "0";
	private String mCategoryName = "";
	private String mSearchData;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private int screenWidth;
	private int speedSearchAnimation = 300;
	private OnSearchListener mSearchListener;

	private GlobalApi api = new GlobalApi();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rooms);

		adapter = new GlobalModelAdapter(this, new ArrayList<GlobalModel>(), R.drawable.default_group_image);
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
		mCategoryName = getIntent().getStringExtra(Const.CATEGORY_NAME);

		((TextView) findViewById(R.id.screenTitle)).setText(getString(R.string.rooms));

		getRooms(mCurrentIndex, null, false);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		closeSearchBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), screenWidth,
						speedSearchAnimation, (LinearLayout) findViewById(R.id.roomsOptions));
			}
		});

		findViewById(R.id.createRoomBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CreateRoomActivity.start(mCategory, mCategoryName, RoomsActivity.this);
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
		getRooms(mCurrentIndex, mSearchData, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			Chat room = (Chat) adapter.getItem(position).getModel();
			ChatActivity.startWithChatId(this, String.valueOf(room.getChat_id()), room.getPassword());
		}
	}

	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getRooms(mCurrentIndex, mSearchData, false);
		}
	};

	public void setSearch(OnSearchListener listener) {
		mSearchListener = listener;
		setSearch(searchBtn, searchOnClickListener, searchEt, editorActionListener);
	}

	public void disableSearch() {
		disableSearch(searchBtn, searchEt, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, (TextView) findViewById(R.id.screenTitle), screenWidth, speedSearchAnimation,
				(LinearLayout) findViewById(R.id.roomsOptions));
	}

	private OnClickListener searchOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (searchEt.getVisibility() == View.GONE) {
				openSearchAnimation(searchBtn, (ImageButton) findViewById(R.id.goBack), closeSearchBtn, searchEt, (TextView) findViewById(R.id.screenTitle), screenWidth,
						speedSearchAnimation, (LinearLayout) findViewById(R.id.roomsOptions));
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
					speedSearchAnimation, (LinearLayout) findViewById(R.id.roomsOptions));
			return;
		}

		finish();
	}

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

	private void getRooms(int page, String search, final boolean toClear) {

		api.globalSearch(this, page, null, mCategory, Type.CHAT, search, true, new ApiCallback<GlobalResponse>() {

			@Override
			public void onApiResponse(Result<GlobalResponse> result) {

				if (result.isSuccess()) {
					mTotalCount = result.getResultData().getTotalCount();
					setData(result.getResultData().getModelsList(), toClear);
				}
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
}
