package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.CreateRoomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class DeselectUsersInRoomActivity extends BaseActivity implements OnChangeListener<User> {

	private String roomName;
	private String roomId;
	private boolean isChecked = false;

	private List<User> mUsers;
	private List<String> mUsersToPass = new ArrayList<String>();

	public static void startActivity(String roomName, String roomId, boolean isChecked, ArrayList<String> ids, @NonNull Context context, int requestCode, CreateRoomFragment frag) {

		Intent intent = new Intent(context, DeselectUsersInRoomActivity.class);
		intent.putExtra(Const.ROOM_ID, roomId);
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

		new UserApi().getChatMembersWithPage(this, roomId, -1, false, new ApiCallback<UsersList>() {

			@Override
			public void onApiResponse(Result<UsersList> result) {
				if (result.isSuccess()) {
					mUsers = handleResult(result.getResultData().getMembersList());
					setListView();
				}
			}
		});
	}

	private List<User> handleResult(List<User> members) {

		List<String> usersIds = getIntent().getStringArrayListExtra(Const.USER_IDS);
		List<User> list = new ArrayList<User>();

		for (User item : members) {

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
		InviteUserAdapter adapter = new InviteUserAdapter(this, mUsers, this);
		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.main_list_view);
		listView.getRefreshableView().setAdapter(adapter);
	}

	@Override
	public void onChange(User obj, boolean isFromDetails) {
		boolean isFound = false;
		int j = 0;

		for (String item : mUsersToPass) {
			if (item.equals(String.valueOf(obj.getId()))) {
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
}
