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
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalMemberDBChanged;
import com.clover.spika.enterprise.chat.caching.robospice.GlobalCacheSpice;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class DeselectUsersInGroupActivity extends BaseActivity implements OnChangeListener<GlobalModel>, OnGlobalMemberDBChanged {

	private String groupName;
	private String groupId;
	private boolean isChecked = false;

	private List<GlobalModel> mUsers;
	private List<String> mUsersToPass = new ArrayList<String>();

	public static void startActivity(String groupName, int groupId, boolean isChecked, ArrayList<String> ids, @NonNull Context context, int requestCode, CustomFragment frag) {
		Intent intent = new Intent(context, DeselectUsersInGroupActivity.class);
		intent.putExtra(Const.GROUP_ID, String.valueOf(groupId));
		intent.putExtra(Const.GROUP_NAME, groupName);
		intent.putExtra(Const.IS_ACTIVE, isChecked);
		intent.putStringArrayListExtra(Const.USER_IDS, ids);
		frag.startActivityForResult(intent, requestCode);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deselect_users_in_group);

		groupName = getIntent().getStringExtra(Const.GROUP_NAME);
		groupId = getIntent().getStringExtra(Const.GROUP_ID);
		isChecked = getIntent().getBooleanExtra(Const.IS_ACTIVE, false);

		((TextView) findViewById(R.id.screenTitle)).setText(groupName);

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
				setResult(RESULT_OK, new Intent().putExtra(Const.USER_IDS, array).putExtra(Const.GROUP_ID, groupId));
				finish();
			}
		});

		getUsersFromGroup();
	}

	private void getUsersFromGroup() {

		GlobalCacheSpice.GlobalMember globalMembers = new GlobalCacheSpice.GlobalMember(this, spiceManager, -1, null, groupId, Type.USER, false, this, null);
		spiceManager.execute(globalMembers, new CustomSpiceListener<List>() {

			@Override
			public void onRequestSuccess(List result) {
				super.onRequestSuccess(result);

				mUsers = generateUserList(result);
				setListView();
			}
		});
	}

	private void setListView() {
		InviteRemoveAdapter adapter = new InviteRemoveAdapter(spiceManager, this, mUsers, this, null);
		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.main_list_view);
		listView.getRefreshableView().setAdapter(adapter);
	}

	private List<GlobalModel> generateUserList(List<GlobalModel> members) {
		List<String> usersIds = getIntent().getStringArrayListExtra(Const.USER_IDS);
		List<GlobalModel> list = new ArrayList<GlobalModel>();
		for (GlobalModel globalModel : members) {

			User item = (User) globalModel.getModel();

			boolean toCheck = false;
			if (isChecked) {
				if (usersIds != null) {
					if (usersIds.contains(String.valueOf(item.getId()))) {
						mUsersToPass.add(String.valueOf(item.getId()));
						toCheck = true;
					}
				} else {
					mUsersToPass.add(String.valueOf(item.getId()));
					toCheck = true;
				}
			}

			User finalUser = new User(item.getId(), item.getFirstName(), item.getLastName(), null, item.getImage(), item.getImageThumb(), false, null, toCheck,
					item.getOrganization());

			GlobalModel finalModel = new GlobalModel();
			finalModel.type = Type.USER;
			finalModel.user = finalUser;

			list.add(finalModel);
		}

		return list;
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
			mUsersToPass.add(String.valueOf(((User) obj.getModel()).getId()));
		}
	}

	@Override
	public void onGlobalMemberDBChanged(List<GlobalModel> usableData, boolean isClear) {
		mUsers = generateUserList(usableData);
		setListView();
	}

}
