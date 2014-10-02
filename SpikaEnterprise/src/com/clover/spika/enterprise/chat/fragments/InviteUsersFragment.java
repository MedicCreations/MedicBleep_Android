package com.clover.spika.enterprise.chat.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class InviteUsersFragment extends Fragment implements AdapterView.OnItemClickListener, OnChangeListener<User> {

    public interface Callbacks {
        void getUsers(int currentIndex);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override public void getUsers(int currentIndex) { }
    };
    private Callbacks mCallbacks = sDummyCallbacks;

    private PullToRefreshListView mainList;
    private InviteUserAdapter adapter;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;

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

            mainList = (PullToRefreshListView) view.findViewById(R.id.main_list_view);
            mainList.setAdapter(adapter);
            mainList.setOnRefreshListener(refreshListener2);
            mainList.setOnItemClickListener(this);
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

    public void setData(List<User> data) {
        // -2 is because of header and footer view
        int currentCount = mainList.getRefreshableView().getAdapter().getCount() - 2 + data.size();

        adapter.addData(data);

        mainList.onRefreshComplete();

        if (currentCount >= mTotalCount) {
            mainList.setMode(PullToRefreshBase.Mode.DISABLED);
        } else if (currentCount < mTotalCount) {
            mainList.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
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
            mCallbacks.getUsers(mCurrentIndex);
        }
    };
}
