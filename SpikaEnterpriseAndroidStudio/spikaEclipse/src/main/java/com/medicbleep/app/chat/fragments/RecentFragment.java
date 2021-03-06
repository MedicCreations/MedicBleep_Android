package com.medicbleep.app.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.medicbleep.app.chat.ChatActivity;
import com.medicbleep.app.chat.MainActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.adapters.RecentAdapter;
import com.medicbleep.app.chat.api.robospice.LobbySpice;
import com.medicbleep.app.chat.caching.LobbyCaching.HandleNewData;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyDBChanged;
import com.medicbleep.app.chat.caching.LobbyCaching.OnLobbyNetworkResult;
import com.medicbleep.app.chat.caching.robospice.LobbyCacheSpice;
import com.medicbleep.app.chat.extendables.CustomFragment;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.models.LobbyModel;
import com.medicbleep.app.chat.models.Message;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Logger;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshBase;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class RecentFragment extends CustomFragment implements OnItemClickListener, OnLobbyDBChanged, OnLobbyNetworkResult {

	private PullToRefreshListView mainListView;
	private RecentAdapter adapter;
	private TextView noItems;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	private List<Chat> allData = new ArrayList<Chat>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getLobby(0, false);

		if (adapter != null) {
			adapter.setSpiceManager(spiceManager);
		}
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

		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).disableCreateRoom();
		}

		if (allData.size() > 1) {
			adapter.addData(allData);
		}

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

		for (Chat item : data) {

			if (item.last_message != null) {
				item.last_message = Message.decryptContent(getActivity(), item.last_message);
			}
		}

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

		allData.clear();
		allData.addAll(adapter.getData());
	}

	@SuppressWarnings("rawtypes")
	public void getLobby(int page, final boolean toClear) {

		LobbyCacheSpice.GetData recentFragmentGetData = new LobbyCacheSpice.GetData(getActivity(), spiceManager, page, toClear, this, this);
		offlineSpiceManager.execute(recentFragmentGetData, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				setData(result, toClear);
			}

		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			final Chat chat = adapter.getItem(position);
			ChatActivity.startWithChatId(getActivity(), chat, null);
		}
	}

	@Override
	public void handlePushNotificationInFragment(String chatId) {
		if (adapter != null) {
			boolean isFound = adapter.incrementUnread(chatId);
			if (!isFound) {
				mCurrentIndex = 0;
				getLobby(mCurrentIndex, false);
			} else {
				LobbySpice.GetLobbyByType getLobbyByType = new LobbySpice.GetLobbyByType(mCurrentIndex, Const.ALL_TOGETHER_TYPE);
				spiceManager.execute(getLobbyByType, new CustomSpiceListener<LobbyModel>() {

					@Override
					public void onRequestFailure(SpiceException ex) {
						super.onRequestFailure(ex);
						Utils.onFailedUniversal(null, getActivity(), ex);
					}

					@Override
					public void onRequestSuccess(final LobbyModel result) {
						super.onRequestSuccess(result);

						String message = getActivity().getResources().getString(R.string.e_something_went_wrong);

						if (result.getCode() == Const.API_SUCCESS) {

							mTotalCount = result.all_chats.total_count;

							HandleNewData handleNewData = new HandleNewData(getActivity(), result.all_chats.chats, false, RecentFragment.this);
							spiceManager.execute(handleNewData, null);

						} else {

							if (result != null && !TextUtils.isEmpty(result.getMessage())) {
								message = result.getMessage();
							}

							Utils.onFailedUniversal(message, getActivity(), result.getCode(), false, null);
						}
					}
				});
			}
		}
	}

	@Override
	public void onRecentDBChanged(List<Chat> usableData, boolean isClear) {
		setData(usableData, isClear);
	}

	@Override
	public void onRecentNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}
}
