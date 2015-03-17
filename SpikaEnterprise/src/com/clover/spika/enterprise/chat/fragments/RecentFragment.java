package com.clover.spika.enterprise.chat.fragments;

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

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.RecentAdapter;
import com.clover.spika.enterprise.chat.api.robospice.LobbySpice;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.LobbyModel;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class RecentFragment extends CustomFragment implements OnItemClickListener {

	private final int CLEAR_ALL = 0;
	private final int DONT_CLEAR = 1;
	private final int CHECK_FOR_NEW_DATA = 2;

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
		if (allData.size() < 1) {
			getLobby(0, CLEAR_ALL);
		} else {
			getLobby(0, CHECK_FOR_NEW_DATA);
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
			getLobby(mCurrentIndex, DONT_CLEAR);
		}
	};

	private void setData(List<Chat> data, int toClearPrevious) {
		if (mainListView == null) {
			return;
		}

		for (Chat item : data) {
			item.last_message = Message.decryptContent(getActivity(), item.last_message);
		}

		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious == CLEAR_ALL)
			currentCount = data.size();

		if (toClearPrevious == CLEAR_ALL) {
			adapter.setData(data);
		} else if (toClearPrevious == CHECK_FOR_NEW_DATA) {
			adapter.setData(data);
		} else {
			adapter.addData(data);
		}

		if (toClearPrevious == CLEAR_ALL)
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

		allData.clear();
		allData.addAll(adapter.getData());
	}

	public void getLobby(int page, final int toClear) {
		
		boolean toShowProgress = true;
		
		if (toClear == CHECK_FOR_NEW_DATA){
			toShowProgress = false;
		}
		
		handleProgress(toShowProgress);
		
		LobbySpice.GetLobbyByType getLobbyByType = new LobbySpice.GetLobbyByType(page, Const.ALL_TOGETHER_TYPE, getActivity());
		spiceManager.execute(getLobbyByType, new CustomSpiceListener<LobbyModel>(){
			
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

					mTotalCount = result.all_chats.total_count;
					setData(result.all_chats.chats, toClear);

				} else {

					if(result != null && !TextUtils.isEmpty(result.getMessage())){
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
			ChatActivity.startWithChatId(getActivity(), String.valueOf(user.getId()), user.password, null);
		}
	}

	@Override
	public void handlePushNotificationInFragment(String chatId) {
		if (adapter != null) {
			boolean isFound = adapter.incrementUnread(chatId);
			if (!isFound) {
				mCurrentIndex = 0;
				getLobby(mCurrentIndex, CHECK_FOR_NEW_DATA);
			}
		}
	}
}
