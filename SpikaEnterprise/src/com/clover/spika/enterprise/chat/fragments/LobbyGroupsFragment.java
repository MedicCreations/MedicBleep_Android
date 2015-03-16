package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.RecentAdapter;
import com.clover.spika.enterprise.chat.api.robospice.LobbySpice;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;
import java.util.List;

public class LobbyGroupsFragment extends CustomFragment implements OnItemClickListener {

	private PullToRefreshListView mainListView;
	private RecentAdapter adapter;
	private TextView noItems;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLobby(0, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_lobby_child, container, false);

		mCurrentIndex = 0;

		noItems = (TextView) view.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) view.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		adapter = new RecentAdapter(spiceManager, getActivity(), new ArrayList<Chat>(), false);

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

	private void setData(List<Chat> data, boolean toClearPrevious) {
		if (mainListView == null) {
			return;
		}
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
			mainListView.setVisibility(View.INVISIBLE);
			noItems.setVisibility(View.VISIBLE);
		} else {
			mainListView.setVisibility(View.VISIBLE);
			noItems.setVisibility(View.GONE);
		}

		if (currentCount >= mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		} else {
			mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}

	public void getLobby(int page, final boolean toClear) {

		handleProgress(true);

		LobbySpice.GetLobbyByType getLobbyByType = new LobbySpice.GetLobbyByType(page, Const.GROUPS_TYPE, getActivity());
		spiceManager.execute(getLobbyByType, new CustomSpiceListener<LobbyModel>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}

			@Override
			public void onRequestSuccess(LobbyModel result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				String message = getResources().getString(R.string.e_something_went_wrong);

				if (result.getCode() == Const.API_SUCCESS) {

					mTotalCount = result.groups.getTotalCount();
					setData(result.groups.getChatsList(), toClear);

				} else {

					if (result != null && !TextUtils.isEmpty(result.getMessage())) {
						message = result.getMessage();
					}

					Utils.onFailedUniversal(message, getActivity());
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			final Chat user = adapter.getItem(position);
			ChatActivity.startWithChatId(getActivity(), String.valueOf(user.getId()), user.password, user.user);
		}
	}

	@Override
	public void handlePushNotificationInFragment(String chatId) {
		if (adapter != null) {
			adapter.incrementUnread(chatId);
		}
	}
}
