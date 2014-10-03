package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class MembersFragment extends CustomFragment {

    public interface Callbacks {
        void getMembers(int index, final boolean toUpdateInviteMember);
    }
    private static Callbacks sDummyCallback = new Callbacks() {
        @Override public void getMembers(int index, final boolean toUpdateInviteMember) { }
    };
    protected Callbacks mCallbacks = sDummyCallback;

    protected InviteUserAdapter mUserAdapter;

    protected int mCurrentIndex = 0;
    protected int mTotalCount = 0;

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
            mUserAdapter = new InviteUserAdapter(getActivity(), new ArrayList<User>());

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
    
    public void resetMembers() {
        this.mCurrentIndex = 0;
        mUserAdapter.clearData();
    }

    protected PullToRefreshListView getListView() {
        if (getView() != null) {
            return (PullToRefreshListView) getView().findViewById(R.id.main_list_view);
        } else {
            return null;
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
            mCallbacks.getMembers(mCurrentIndex, false);
        }
    };

}
