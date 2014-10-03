package com.clover.spika.enterprise.chat.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    public interface Callbacks {
        void getMembers(int index);
    }
    private static Callbacks sDummyCallback = new Callbacks() {
        @Override public void getMembers(int index) { }
    };
    private Callbacks mCallbacks = sDummyCallback;

    private UserAdapter mUserAdapter;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;

    public static MembersFragment newInstance() {
        MembersFragment fragment = new MembersFragment();
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
        this.mCallbacks = sDummyCallback;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remove_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getListView() != null) {
            mUserAdapter = new UserAdapter(getActivity(), new ArrayList<User>());

            getListView().setAdapter(mUserAdapter);
        }
    }

    public void setMembers(List<User> members) {
        int currentCount = getListView().getRefreshableView().getAdapter().getCount() - 2 + members.size();

        mUserAdapter.addData(members);

        getListView().setAdapter(mUserAdapter);
        getListView().setOnRefreshListener(refreshListener2);

        if (currentCount >= mTotalCount) {
            getListView().setMode(PullToRefreshBase.Mode.DISABLED);
        } else if (currentCount < mTotalCount) {
            getListView().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        }
    }

    public void setTotalCount(int totalCount) {
        this.mTotalCount = totalCount;
    }

    private PullToRefreshListView getListView() {
        return (PullToRefreshListView) getView();
    }

    PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase refreshView) {
            // mCurrentIndex--; don't need this for now
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase refreshView) {
            mCurrentIndex++;
            mCallbacks.getMembers(mCurrentIndex);
        }
    };

}
