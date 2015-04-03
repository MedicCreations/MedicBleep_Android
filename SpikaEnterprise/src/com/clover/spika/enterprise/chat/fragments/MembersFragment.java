package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class MembersFragment extends CustomFragment implements OnItemClickListener {

	public interface Callbacks {
		void getMembers(int index, final boolean toUpdateInviteMember);
	}

	private static Callbacks sDummyCallback = new Callbacks() {
		@Override
		public void getMembers(int index, final boolean toUpdateInviteMember) {
		}
	};
	protected Callbacks mCallbacks = sDummyCallback;

	protected InviteRemoveAdapter mUserAdapter;

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
			throw new IllegalArgumentException(activity.toString() + " has to implement Callbacks interface in order to inflate this Fragment.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mCallbacks = sDummyCallback;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_remove_users, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getListView() != null) {
			mUserAdapter = new InviteRemoveAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), null, null);
			getListView().setOnItemClickListener(this);
			getListView().setAdapter(mUserAdapter);
			mUserAdapter.setCheckBox(false);
		}
	}

	public void setMembers(List<GlobalModel> members) {

		try {

			int currentCount = getListView().getRefreshableView().getAdapter().getCount() - 2 + members.size();

			mUserAdapter.setData(members);

			getListView().setAdapter(mUserAdapter);
			getListView().setOnRefreshListener(refreshListener2);

			if (currentCount >= mTotalCount) {
				getListView().setMode(PullToRefreshBase.Mode.DISABLED);
			} else if (currentCount < mTotalCount) {
				getListView().setMode(PullToRefreshBase.Mode.PULL_FROM_END);
			}

		} catch (Exception ex) {
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != mUserAdapter.getCount()) {
			GlobalModel user = mUserAdapter.getItem(position);

			User userUser = null;
			if (user.type == GlobalModel.Type.USER)
				userUser = (User) user.getModel();

			ChatActivity.startWithUserId(getActivity(), String.valueOf(((User) user.getModel()).getId()), false,
					((User) user.getModel()).getFirstName(), ((User) user.getModel()).getLastName(), userUser);
		}
	}

}
