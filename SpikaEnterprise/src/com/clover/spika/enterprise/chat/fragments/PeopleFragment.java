package com.clover.spika.enterprise.chat.fragments;

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

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.PeopleAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GlobalApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class PeopleFragment extends CustomFragment implements OnItemClickListener, OnSearchListener {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public PeopleAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private GlobalApi api = new GlobalApi();
	
	private EditText etSearch;
	private List<GlobalModel> allData = new ArrayList<GlobalModel>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new PeopleAdapter(getActivity(), new ArrayList<GlobalModel>(), R.drawable.default_user_image);
		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
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
		
		if(allData.size() > 1){
			adapter.addData(allData);
		}else{
			getUsers(mCurrentIndex, null, false);
		}
		
		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).disableCreateRoom();
		}
		
		return rootView;
	}
	
	private TextWatcher textWatacher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {
			adapter.manageData(s.toString(), allData);
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
		
		allData.clear();
		allData.addAll(adapter.getData());
	}

	public void getUsers(int page, String search, final boolean toClear) {

		api.globalSearch(getActivity(), page, null, null, Type.USER, search, true, new ApiCallback<GlobalResponse>() {

			@Override
			public void onApiResponse(Result<GlobalResponse> result) {
				if (result.isSuccess()) {

					GlobalResponse response = result.getResultData();

					mTotalCount = response.getTotalCount();
					setData(response.getModelsList(), toClear);
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
		getUsers(mCurrentIndex, mSearchData, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			User user = (User) adapter.getItem(position).getModel();
			
			boolean isFirstUserProfile = getResources().getBoolean(R.bool.first_user_profile);
			
			if (isFirstUserProfile){
				ProfileOtherActivity.openOtherProfileFromList(getActivity(), user.getId(), user.getImage(), user.getFirstName() + " " + user.getLastName(), user.getFirstName(), user.getLastName(), user);
			} else {
				ChatActivity.startWithUserId(getActivity(), String.valueOf(user.getId()), false, user.getFirstName(), user.getLastName(), user);
			}
			
		}
	}
}
