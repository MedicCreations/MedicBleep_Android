package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InviteUsersFragment extends Fragment implements AdapterView.OnItemClickListener, OnChangeListener<User>, OnSearchListener {

    public interface Callbacks {
        void getUsers(int currentIndex, String search, final boolean toClear);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override public void getUsers(int currentIndex, String search, final boolean toClear) { }
    };
    private Callbacks mCallbacks = sDummyCallbacks;

    private PullToRefreshListView mainListView;
    private InviteUserAdapter adapter;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;
    private String mSearchData = null;
    
    private TextView noItems;

    public static InviteUsersFragment newInstance() {
        InviteUsersFragment fragment = new InviteUsersFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callbacks) {
            this.mCallbacks = (Callbacks) activity;
        } else {
            throw new IllegalArgumentException(activity.toString() +
                    " has to implement Callbacks interface in order to inflate this Fragment.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mCallbacks = sDummyCallbacks;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            adapter = new InviteUserAdapter(getActivity(), new ArrayList<User>(), this);

            noItems = (TextView) view.findViewById(R.id.noItems);
            
            mainListView = (PullToRefreshListView) view.findViewById(R.id.main_list_view);
            mainListView.setAdapter(adapter);
            mainListView.setOnRefreshListener(refreshListener2);
            mainListView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position = position - 1;

        if (position != -1 && position != adapter.getCount()) {
            User user = adapter.getItem(position);
            ProfileOtherActivity.openOtherProfile(getActivity(), user.getImage(), user.getFirstName() + " " + user.getLastName());
        }
    }

    @Override
    public void onChange(User obj) {

    }
    
    @Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		mCallbacks.getUsers(mCurrentIndex, mSearchData, true);
	}

    public void setData(List<User> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();
		if(toClearPrevious) currentCount = data.size();

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

    public void setTotalCount(int totalCount) {
        this.mTotalCount = totalCount;
    }

    PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            // mCurrentIndex--; don't need this for now
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        	mCurrentIndex++;
			mCallbacks.getUsers(mCurrentIndex, mSearchData, false);
        }
    };
    
    @Override
	public void onResume() {
		super.onResume();
		((ManageUsersActivity) getActivity()).setSearch(this);
	}
    
    @Override
	public void onPause() {
		super.onPause();
		((ManageUsersActivity) getActivity()).disableSearch();
	}
}
