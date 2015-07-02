package com.medicbleep.app.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.medicbleep.app.chat.ChatActivity;
import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.ProfileOtherActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.adapters.PeopleAdapter;
import com.medicbleep.app.chat.caching.GlobalCaching.OnGlobalSearchDBChanged;
import com.medicbleep.app.chat.caching.GlobalCaching.OnGlobalSearchNetworkResult;
import com.medicbleep.app.chat.caching.robospice.GlobalCacheSpice;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.extendables.CustomFragment;
import com.medicbleep.app.chat.listeners.OnSearchListener;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.models.GlobalModel.Type;
import com.medicbleep.app.chat.models.User;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshBase;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshListView;

public class PeopleFragment extends CustomFragment implements OnItemClickListener, OnSearchListener, OnGlobalSearchDBChanged, OnGlobalSearchNetworkResult {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public PeopleAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private EditText etSearch;
	private List<GlobalModel> allData = new ArrayList<GlobalModel>();
	
	private boolean isDataFromNet = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new PeopleAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), R.drawable.default_user_image);
		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (adapter != null) {
			adapter.setSpiceManager(spiceManager);
		}
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

		etSearch = (EditText) rootView.findViewById(R.id.etSearchPeople);
		etSearch.setOnEditorActionListener(editorActionListener);

		etSearch.addTextChangedListener(textWatacher);

		if (allData.size() > 1) {
			adapter.addData(allData);
		} else {
			getUsers(mCurrentIndex, null, false);
		}

		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).disableCreateRoom();
		}

		return rootView;
	}

	private TextWatcher textWatacher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void afterTextChanged(final Editable s) {
			if(isDataFromNet){
				GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(getActivity(), spiceManager, 
						0, null, null, Type.USER, null, true, true, PeopleFragment.this, PeopleFragment.this);
				offlineSpiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

					@SuppressWarnings("unchecked")
					@Override
					public void onRequestSuccess(List result) {
						super.onRequestSuccess(result);
						allData.clear();
						allData.addAll(result);
						adapter.manageData(s.toString(), allData);
					}
				});
				isDataFromNet = false;
			}else {
				adapter.manageData(s.toString(), allData);
			}
		}
	};

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				((BaseActivity) getActivity()).hideKeyboard(etSearch);
				onSearch(v.getText().toString());
			}
			return false;
		}
	};

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

	private void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious) {
			currentCount = data.size();
		}

		adapter.setData(data);

		if (toClearPrevious) {
			mainListView.getRefreshableView().setSelection(0);
		}

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

		allData.clear();
		allData.addAll(adapter.getData());
	}

	@SuppressWarnings("rawtypes")
	public void getUsers(int page, String search, final boolean toClear) {
		
		if(!TextUtils.isEmpty(search)){
			isDataFromNet = true;
		}

		GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(getActivity(), spiceManager, page, null, null, Type.USER, search, toClear, this, this);
		offlineSpiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				setData(result, toClear);
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
		getUsers(mCurrentIndex, mSearchData, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			User user = (User) adapter.getItem(position).getModel();

			boolean isFirstUserProfile = getResources().getBoolean(R.bool.first_user_profile);

			if (isFirstUserProfile) {
				ProfileOtherActivity.openOtherProfileFromList(getActivity(), user.getId(), user.getImage(), user.getFirstName() + " " + user.getLastName(), user.getFirstName(),
						user.getLastName(), user);
			} else {
				ChatActivity.startWithUserId(getActivity(), String.valueOf(user.getId()), false, user.getFirstName(), user.getLastName(), user);
			}
		}
	}

	@Override
	public void onGlobalSearchNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}

	@Override
	public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear) {
		setData(usableData, isClear);
	}
}
