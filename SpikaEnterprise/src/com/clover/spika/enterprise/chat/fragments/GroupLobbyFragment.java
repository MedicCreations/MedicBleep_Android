package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.LobbyActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.LobbyAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.LobbyApi;
import com.clover.spika.enterprise.chat.listeners.LobbyChangedListener;
import com.clover.spika.enterprise.chat.models.ChatsLobby;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class GroupLobbyFragment extends Fragment implements LobbyChangedListener, OnItemClickListener {

	private PullToRefreshListView mainListView;
	private LobbyAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lobby, container, false);

		((LobbyActivity) getActivity()).getLobby(this);

		mainListView = (PullToRefreshListView) view.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);
		adapter = new LobbyAdapter(getActivity(), new ArrayList<ChatsLobby>(), false);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		return view;
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
			getLobby(mCurrentIndex, false);
		}
	};

	private void setData(List<ChatsLobby> data, boolean toClearPrevious) {
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (currentCount >= mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		} else {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);
		if (toClearPrevious)
			mainListView.getRefreshableView().setSelection(0);

		mainListView.onRefreshComplete();
	}

	public void getLobby(int page, final boolean toClear) {
		new LobbyApi().getLobbyByType(page, Const.GROUPS_TYPE, getActivity(), true, new ApiCallback<LobbyModel>() {

			@Override
			public void onApiResponse(Result<LobbyModel> result) {
				if (result.isSuccess()) {
					mTotalCount = result.getResultData().getUsersLoby().getTotalCount();
					setData(result.getResultData().getUsersLoby().getChatsList(), toClear);
				}
			}
		});
	}

	@Override
	public void onChangeAll(LobbyModel model) {
		mTotalCount = model.getGroupsLobby().getTotalCount();
		setData(model.getGroupsLobby().getChatsList(), true);
	}

	@Override
	public void onChangeGroup(LobbyModel model) {

	}

	@Override
	public void onChangeUser(LobbyModel model) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			ChatsLobby user = adapter.getItem(position);

			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra(Const.CHAT_ID, String.valueOf(user.getChatId()));
			intent.putExtra(Const.CHAT_NAME, user.getChatName());
			startActivity(intent);
		}
	}
}
