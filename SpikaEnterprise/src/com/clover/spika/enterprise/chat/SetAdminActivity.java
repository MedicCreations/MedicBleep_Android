package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.api.robospice.GlobalSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

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
		adapter = new InviteRemoveAdapter(spiceManager, this, new ArrayList<GlobalModel>(), null, null);
		mainListView.setAdapter(adapter);
		adapter.setCheckBox(false);
		adapter.disableNameClick(true);
		mainListView.setOnRefreshListener(refreshListener2);
		mainListView.setOnItemClickListener(this);

		getUsers(true);
	}

	private void getUsers(final boolean clearPrevious) {
		
		handleProgress(true);

		GlobalSpice.GlobalMembers globalMembers = new GlobalSpice.GlobalMembers(mCurrentIndex, chatId, null, Type.USER, this);
		spiceManager.execute(globalMembers, new CustomSpiceListener<GlobalResponse>() {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				super.onRequestFailure(arg0);
				handleProgress(false);
				Utils.onFailedUniversal(null, SetAdminActivity.this);
			}

			@Override
			public void onRequestSuccess(GlobalResponse result) {
				super.onRequestSuccess(result);
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					setData((List<GlobalModel>) result.getModelsList(), clearPrevious);

				} else {
					String message = getString(R.string.e_something_went_wrong);
					Utils.onFailedUniversal(message, SetAdminActivity.this);
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
			
			handleProgress(true);
			ChatSpice.UpdateChatAll updateChatAll = new ChatSpice.UpdateChatAll(params, this);
			spiceManager.execute(updateChatAll, new CustomSpiceListener<BaseModel>() {

				@Override
				public void onRequestFailure(SpiceException ex) {
					handleProgress(false);
					Utils.onFailedUniversal(null, SetAdminActivity.this);
				}

				@Override
				public void onRequestSuccess(BaseModel result) {
					handleProgress(false);
					
					if (result.getCode() == Const.API_SUCCESS) {

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
						dialog.setFailed(result.getCode());
					}
				}
			});
		}
	}
}
