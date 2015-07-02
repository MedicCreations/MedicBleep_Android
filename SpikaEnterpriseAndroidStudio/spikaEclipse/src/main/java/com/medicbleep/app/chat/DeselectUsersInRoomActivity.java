package com.medicbleep.app.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.medicbleep.app.chat.adapters.InviteRemoveAdapter;
import com.medicbleep.app.chat.caching.ChatMembersCaching.OnChatMembersDBChanged;
import com.medicbleep.app.chat.caching.GlobalCaching.OnGlobalMemberDBChanged;
import com.medicbleep.app.chat.caching.robospice.ChatMembersCacheSpice;
import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.extendables.CustomFragment;
import com.medicbleep.app.chat.listeners.OnChangeListener;
import com.medicbleep.app.chat.models.GlobalModel;
import com.medicbleep.app.chat.services.robospice.CustomSpiceListener;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshBase;
import com.medicbleep.app.chat.views.pulltorefresh.PullToRefreshListView;

public class DeselectUsersInRoomActivity extends BaseActivity implements OnChangeListener<GlobalModel>, OnGlobalMemberDBChanged, OnChatMembersDBChanged {

	private String roomName;
	private String roomId;
	private boolean isChecked = false;

	private List<GlobalModel> mUsers;
	private List<String> mUsersToPass = new ArrayList<String>();

	public static void startActivity(String roomName, int roomId, boolean isChecked, ArrayList<String> ids, @NonNull Context context, int requestCode, CustomFragment frag) {
		
		Intent intent = new Intent(context, DeselectUsersInRoomActivity.class);
		intent.putExtra(Const.ROOM_ID, String.valueOf(roomId));
		intent.putExtra(Const.ROOM_NAME, roomName);
		intent.putExtra(Const.IS_ACTIVE, isChecked);
		intent.putStringArrayListExtra(Const.USER_IDS, ids);
		frag.startActivityForResult(intent, requestCode);
		if(context instanceof BaseActivity) ((BaseActivity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deselect_users_in_room);

		roomName = getIntent().getStringExtra(Const.ROOM_NAME);
		roomId = getIntent().getStringExtra(Const.ROOM_ID);
		isChecked = getIntent().getBooleanExtra(Const.IS_ACTIVE, false);

		((TextView) findViewById(R.id.screenTitle)).setText(roomName);

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// finish and pass data to activity
				String[] array = new String[mUsersToPass.size()];

				int i = 0;
				for (String itemString : mUsersToPass) {
					array[i] = itemString;
					i++;
				}

				setResult(RESULT_OK, new Intent().putExtra(Const.USER_IDS, array).putExtra(Const.ROOM_ID, roomId));
				finish();
			}
		});
		
		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.main_list_view);
		listView.setMode(PullToRefreshBase.Mode.DISABLED);

		getUsersFromRoom();
	}

	@SuppressWarnings("rawtypes")
	private void getUsersFromRoom() {
		
		ChatMembersCacheSpice.GetChatMembers chatMembers = new ChatMembersCacheSpice.GetChatMembers(this, spiceManager, roomId, this);
		spiceManager.execute(chatMembers, new CustomSpiceListener<List>() {

			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);
				mUsers = handleResult(result);
				setListView();
			}
		});

	}

	private List<GlobalModel> handleResult(List<GlobalModel> members) {

		List<String> usersIds = getIntent().getStringArrayListExtra(Const.USER_IDS);
		List<GlobalModel> list = new ArrayList<GlobalModel>();

		for (GlobalModel item : members) {

			if (isChecked) {
				if (usersIds != null) {

					if (usersIds.contains(String.valueOf(item.getId()))) {

						mUsersToPass.add(String.valueOf(item.getId()));
						item.setSelected(true);
					} else {
						item.setSelected(false);
					}
				} else {

					mUsersToPass.add(String.valueOf(item.getId()));
					item.setSelected(true);
				}
			}

			list.add(item);
		}

		return list;
	}

	private void setListView() {
		InviteRemoveAdapter adapter = new InviteRemoveAdapter(spiceManager, this, mUsers, this, null);
		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.main_list_view);
		listView.getRefreshableView().setAdapter(adapter);
	}

	@Override
	public void onChange(GlobalModel obj, boolean isFromDetails) {

		boolean isFound = false;
		int j = 0;

		for (String item : mUsersToPass) {

			if (Integer.parseInt(item) == obj.getId()) {
				isFound = true;
				break;
			}

			j++;
		}

		if (isFound) {
			mUsersToPass.remove(j);
		} else {
			mUsersToPass.add(String.valueOf(obj.getId()));
		}
	}

	@Override
	public void onGlobalMemberDBChanged(List<GlobalModel> usableData, boolean isClear) {
		mUsers = handleResult(usableData);
		setListView();
	}

	@Override
	public void onChatMembersDBChanged(List<GlobalModel> usableData) {
		mUsers = handleResult(usableData);
		setListView();
	}
}
