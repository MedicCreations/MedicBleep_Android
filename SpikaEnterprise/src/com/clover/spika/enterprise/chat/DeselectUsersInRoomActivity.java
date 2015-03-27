package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.caching.GlobalSearchCaching.OnGlobalMemberDBChanged;
import com.clover.spika.enterprise.chat.caching.robospice.GlobalCachingSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class DeselectUsersInRoomActivity extends BaseActivity implements OnChangeListener<GlobalModel>, OnGlobalMemberDBChanged {

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

		getUsersFromRoom();
	}

	private void getUsersFromRoom() {

		GlobalCachingSpice.GlobalMember globalMembers = new GlobalCachingSpice.GlobalMember(this, spiceManager, -1, null, roomId, Type.USER, false, this, null);
		spiceManager.execute(globalMembers, new CustomSpiceListener<List>() {

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
}
