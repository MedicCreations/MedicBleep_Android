package com.clover.spika.enterprise.chat.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching.OnGlobalMemberDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching.OnGlobalMemberNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.GlobalCacheSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class SetAdminDialog extends Dialog implements OnItemClickListener, OnGlobalMemberDBChanged, OnGlobalMemberNetworkResult {

	public SetAdminDialog(final Context context, String chatId) {
		super(context, R.style.Theme_Dialog);
		setOwnerActivity((Activity) context);

		this.chatId = chatId;
	}

	TextView noItems;

	PullToRefreshListView mainListView;
	public InviteRemoveAdapter adapter;

	private OnActionClick listener;

	private String chatId;
	private int mCurrentIndex = 0;
	private int mTotalCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_select_category);

		noItems = (TextView) findViewById(R.id.noItems);
		mainListView = (PullToRefreshListView) findViewById(R.id.mainListView);

		adapter = new InviteRemoveAdapter(((BaseActivity) getOwnerActivity()).spiceManager, getOwnerActivity(), new ArrayList<GlobalModel>(), null, null);
		mainListView.setAdapter(adapter);
		adapter.setCheckBox(false);
		adapter.disableNameClick(true);
		mainListView.setOnRefreshListener(refreshListener2);
		mainListView.setOnItemClickListener(this);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);

		getUsers(true);

		findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onCloseClick(SetAdminDialog.this);
				else
					dismiss();
			}
		});

		findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onAcceptClick(SetAdminDialog.this);
				else
					dismiss();
			}
		});

		((TextView) findViewById(R.id.screenTitle)).setText(getOwnerActivity().getString(R.string.set_a_new_admin));

	}

	public void setListener(OnActionClick lis) {
		listener = lis;
	}

	private void getUsers(final boolean clearPrevious) {

		GlobalCacheSpice.GlobalMember globalMembers = new GlobalCacheSpice.GlobalMember(getOwnerActivity(), ((BaseActivity) getOwnerActivity()).spiceManager, mCurrentIndex,
				chatId, null, Type.USER, clearPrevious, this, this);
		((BaseActivity) getOwnerActivity()).spiceManager.execute(globalMembers, new CustomSpiceListener<List>() {

			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				setData(result, clearPrevious);
			}
		});
	}

	public void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious) {
			currentCount = data.size();
		}

		if (toClearPrevious) {
			adapter.setData(data);
		} else {
			adapter.addData(data);
		}

		if (toClearPrevious) {
			mainListView.getRefreshableView().setSelection(0);
		}

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
		// header is 0 position
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			final User user = (User) adapter.getItem(position).getModel();

			if (user.isAdmin()) {
				return;
			}

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(Const.CHAT_ID, chatId);
			params.put(Const.ADMIN_ID, String.valueOf(user.getId()));

			((BaseActivity) getOwnerActivity()).handleProgress(true);
			ChatSpice.UpdateChatAll updateChatAll = new ChatSpice.UpdateChatAll(params, getOwnerActivity());
			((BaseActivity) getOwnerActivity()).spiceManager.execute(updateChatAll, new CustomSpiceListener<BaseModel>() {

				@Override
				public void onRequestFailure(SpiceException ex) {
					((BaseActivity) getOwnerActivity()).handleProgress(false);
					Utils.onFailedUniversal(null, getOwnerActivity());
				}

				@Override
				public void onRequestSuccess(BaseModel result) {
					((BaseActivity) getOwnerActivity()).handleProgress(false);

					if (result.getCode() == Const.API_SUCCESS) {

						boolean isAdmin = false;

						if (String.valueOf(user.getId()).equals(SpikaEnterpriseApp.getSharedPreferences(getOwnerActivity()).getCustomString(Const.USER_ID))) {
							isAdmin = true;
						}

						if (listener != null) {
							listener.onAdminSelect(isAdmin, SetAdminDialog.this);
						}
					} else {
						AppDialog dialog = new AppDialog(getOwnerActivity(), false);
						dialog.setFailed(result.getCode());
					}
				}
			});
		}
	}

	public interface OnActionClick {
		public void onAcceptClick(Dialog d);

		public void onCloseClick(Dialog d);

		public void onAdminSelect(boolean isAdmin, Dialog d);
	}

	@Override
	public void onGlobalMemberNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}

	@Override
	public void onGlobalMemberDBChanged(List<GlobalModel> usableData, boolean isClear) {
		setData(usableData, isClear);
	}

}