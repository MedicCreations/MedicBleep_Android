package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.GlobalApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetAdminActivity extends BaseActivity implements OnItemClickListener {

	private PullToRefreshListView mainListView;
	private InviteRemoveAdapter adapter;
	private TextView noItems;

	private String chatId;
	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	private GlobalApi api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_admin);

		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Bundle extras = getIntent().getExtras();

		if (extras.containsKey(Const.CHAT_ID)) {
			chatId = extras.getString(Const.CHAT_ID);
		} else {
			finish();
		}

		noItems = (TextView) findViewById(R.id.noItems);
		mainListView = (PullToRefreshListView) findViewById(R.id.main_list_view);
		adapter = new InviteRemoveAdapter(this, new ArrayList<GlobalModel>(), null, null);
		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);
		mainListView.setOnItemClickListener(this);

		api = new GlobalApi();
		getUsers(true);
	}

	private void getUsers(final boolean clearPrevious) {

		api.globalSearch(this, mCurrentIndex, chatId, null, Type.USER, null, false, new ApiCallback<GlobalResponse>() {
			@Override
			public void onApiResponse(Result<GlobalResponse> result) {
				if (result.isSuccess()) {
					setData((List<GlobalModel>) result.getResultData().getModelsList(), clearPrevious);
				}
			}
		});
	}

	public void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
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

	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			getUsers(false);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			final User user = (User) adapter.getItem(position).getModel();

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Const.CHAT_ID, chatId);
			params.put(Const.ADMIN_ID, String.valueOf(user.getId()));
			new ChatApi().updateChatAll(params, true, SetAdminActivity.this, new ApiCallback<BaseModel>() {

				@Override
				public void onApiResponse(Result<BaseModel> result) {
					if (result.isSuccess()) {

						Intent intent = new Intent();

						if (String.valueOf(user.getId()).equals(SpikaEnterpriseApp.getSharedPreferences(SetAdminActivity.this).getCustomString(Const.USER_ID))) {
							intent.putExtra(Const.IS_ADMIN, true);
						} else {
							intent.putExtra(Const.IS_ADMIN, false);
						}

						setResult(RESULT_OK, intent);
						finish();
					} else {
						AppDialog dialog = new AppDialog(SetAdminActivity.this, false);
						dialog.setFailed(result.getResultData().getCode());
					}
				}
			});
		}
	}
}
