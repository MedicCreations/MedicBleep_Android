package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InvitePeopleActivity extends BaseActivity implements OnItemClickListener {

	UsersApi api;

	PullToRefreshListView mainList;
	UserAdapter adapter;

	private String chatId = "";
	private int chatType = 0;
	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	public static void startActivity(String chatId, int type, Context context) {
		Intent intent = new Intent(context, InvitePeopleActivity.class);
		intent.putExtra(Const.CHAT_ID, chatId);
		intent.putExtra(Const.TYPE, type);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_people);

		api = new UsersApi();

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		adapter = new UserAdapter(this, new ArrayList<User>());

		mainList = (PullToRefreshListView) findViewById(R.id.main_list_view);
		mainList.setAdapter(adapter);
		mainList.setOnRefreshListener(refreshListener2);
		mainList.setOnItemClickListener(this);

		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			chatType = intent.getExtras().getInt(Const.TYPE);
			getUsers(0, false);
		}
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
			getUsers(mCurrentIndex, false);
		}
	};

	private void setData(List<User> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainList.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious)
			adapter.setData(data);
		else
			adapter.addData(data);

		if (toClearPrevious)
			mainList.getRefreshableView().setSelection(0);

		mainList.onRefreshComplete();

		if (currentCount >= mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.DISABLED);
		} else if (currentCount < mTotalCount) {
			mainList.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		}
	}

	private void getUsers(int page, final boolean toClear) {
		api.getChatMembersWithPage(this, chatId, mCurrentIndex, true, new ApiCallback<UsersList>() {

			@Override
			public void onApiResponse(Result<UsersList> result) {
				mTotalCount = result.getResultData().getTotalCount();
				setData(result.getResultData().getMembersList(), toClear);
			}
		});

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			User user = adapter.getItem(position);
			ProfileOtherActivity.openOtherProfile(this, user.getImage(), user.getFirstName() + " " + user.getLastName());
		}
	}
}
