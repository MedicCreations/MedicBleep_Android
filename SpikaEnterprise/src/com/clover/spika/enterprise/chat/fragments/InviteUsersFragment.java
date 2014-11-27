package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.DeselectUsersInGroupActivity;
import com.clover.spika.enterprise.chat.DeselectUsersInRoomActivity;
import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUsersGroupsRoomsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnGroupClickedListener;
import com.clover.spika.enterprise.chat.listeners.OnInviteClickListener;
import com.clover.spika.enterprise.chat.listeners.OnRoomClickedListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchManageUsersListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserGroupRoom;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InviteUsersFragment extends CustomFragment implements AdapterView.OnItemClickListener, OnChangeListener<UserGroupRoom>, OnSearchManageUsersListener,
		OnInviteClickListener, OnGroupClickedListener, OnRoomClickedListener {

	public interface Callbacks {
		void getUsers(int currentIndex, String search, final boolean toClear, final boolean toUpdateMember);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void getUsers(int currentIndex, String search, final boolean toClear, final boolean toUpdateMember) {
		}
	};
	private Callbacks mCallbacks = sDummyCallbacks;

	private PullToRefreshListView mainListView;
	private InviteUsersGroupsRoomsAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private TextView noItems;
	private List<UserGroupRoom> usersToAdd = new ArrayList<UserGroupRoom>();
	SparseArray<List<String>> usersFromGroups = new SparseArray<List<String>>();
	SparseArray<List<String>> usersFromRooms = new SparseArray<List<String>>();
	private TextView txtUsers;

	private static final int FROM_GROUP_MEMBERS = 12;
	private static final int FROM_ROOM_MEMBERS = 13;

	public static InviteUsersFragment newInstance() {
		InviteUsersFragment fragment = new InviteUsersFragment();
		Bundle arguments = new Bundle();
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof Callbacks) {
			this.mCallbacks = (Callbacks) activity;
		} else {
			throw new IllegalArgumentException(activity.toString() + " has to implement Callbacks interface in order to inflate this Fragment.");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		this.mCallbacks = sDummyCallbacks;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_invite_users, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (view != null) {
			adapter = new InviteUsersGroupsRoomsAdapter(getActivity(), new ArrayList<UserGroupRoom>(), this, this, this);

			noItems = (TextView) view.findViewById(R.id.noItems);
			txtUsers = (TextView) view.findViewById(R.id.invitedPeople);
			txtUsers.setMovementMethod(new ScrollingMovementMethod());

			mainListView = (PullToRefreshListView) view.findViewById(R.id.main_list_view);
			mainListView.setAdapter(adapter);
			mainListView.setOnRefreshListener(refreshListener2);
			mainListView.setOnItemClickListener(this);

			if (getActivity() instanceof ManageUsersActivity) {
				((ManageUsersActivity) getActivity()).setOnInviteClickListener(this);
			}

			setInitialTextToTxtUsers();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			UserGroupRoom user = adapter.getItem(position);
			ProfileOtherActivity.openOtherProfile(getActivity(), user.getId(), user.getImage(), user.getFirstName() + " " + user.getLastName());
		}
	}

	@Override
	public void onChange(UserGroupRoom obj, boolean isFromDetails) {

		boolean isFound = false;
		int j = 0;

		for (UserGroupRoom user : usersToAdd) {
			if (user.getId().equals(obj.getId())) {
				isFound = true;
				break;
			}
			j++;
		}

		if (isFound) {
			
			if (isFromDetails) {
				return;
			}
			
			usersToAdd.remove(j);
		} else {
			usersToAdd.add(obj);
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < usersToAdd.size(); i++) {

			if (usersToAdd.get(i).getIs_group()) {
				builder.append(usersToAdd.get(i).getGroupName());
			} else if (usersToAdd.get(i).getIsRoom()) {
				builder.append(usersToAdd.get(i).getRoomName());
			} else {
				builder.append(usersToAdd.get(i).getFirstName() + " " + usersToAdd.get(i).getLastName());
			}

			if (i != (usersToAdd.size() - 1)) {
				builder.append(", ");
			}
		}

		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers + builder.toString());
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtUsers.setText(span);
	}

	@Override
	public void onSearchInInvite(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		mCallbacks.getUsers(mCurrentIndex, mSearchData, true, false);
	}

	@Override
	public void onInvite(final String chatId) {

		if (adapter.getUsersSelected().size() == 0 && adapter.getGroupsSelected().size() == 0 && adapter.getRoomsSelected().size() == 0) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}

		List<String> usersId = adapter.getUsersSelected();
		List<String> groupsId = adapter.getGroupsSelected();
		List<String> roomsId = adapter.getRoomsSelected();

		for (int i = 0; i < usersFromGroups.size(); i++) {

			List<String> userList = usersFromGroups.valueAt(i);
			for (String user : userList) {
				if (!usersId.contains(user)) {
					usersId.add(user);
				}
			}
		}

		for (int i = 0; i < usersFromRooms.size(); i++) {

			List<String> userList = usersFromRooms.valueAt(i);
			for (String user : userList) {
				if (!usersId.contains(user)) {
					usersId.add(user);
				}
			}
		}

		// add user ids
		StringBuilder users_to_add = new StringBuilder();
		for (int i = 0; i < usersId.size(); i++) {
			users_to_add.append(usersId.get(i));
			if (i != (usersId.size() - 1)) {
				users_to_add.append(",");
			}
		}

		// add group ids
		StringBuilder group_to_add = new StringBuilder();

		for (int i = 0; i < groupsId.size(); i++) {
			group_to_add.append(groupsId.get(i));

			if (i != (groupsId.size() - 1)) {
				group_to_add.append(",");
			}
		}

		// add room ids
		StringBuilder rooms_to_add = new StringBuilder();

		for (int i = 0; i < roomsId.size(); i++) {
			rooms_to_add.append(roomsId.get(i));

			if (i != (roomsId.size() - 1)) {
				rooms_to_add.append(",");
			}
		}

		new ChatApi().addUsersToRoom(users_to_add.toString(), group_to_add.toString(), rooms_to_add.toString(), chatId, getActivity(), new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {

				if (result.isSuccess()) {

					if (getActivity() instanceof ManageUsersActivity) {
						((ManageUsersActivity) getActivity()).setNewChat(result.getResultData().getChat());
					}

					mCurrentIndex = 0;
					mCallbacks.getUsers(mCurrentIndex, null, true, true);
					setInitialTextToTxtUsers();
					adapter.resetSelected();
					usersToAdd.clear();
				}
			}
		});
	}

	public void setData(List<UserGroupRoom> data, boolean toClearPrevious) {

		try {
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
		} catch (Exception ignore) {
		}
	}

	public void setTotalCount(int totalCount) {
		this.mTotalCount = totalCount;
	}

	PullToRefreshBase.OnRefreshListener2 refreshListener2 = new PullToRefreshBase.OnRefreshListener2() {
		@Override
		public void onPullDownToRefresh(PullToRefreshBase refreshView) {
			// mCurrentIndex--; don't need this for now
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase refreshView) {
			mCurrentIndex++;
			mCallbacks.getUsers(mCurrentIndex, mSearchData, false, false);
		}
	};

	private void setInitialTextToTxtUsers() {
		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers);
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtUsers.setText(span);
	}

	@Override
	public void onResume() {
		super.onResume();
		((ManageUsersActivity) getActivity()).setSearch(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		((ManageUsersActivity) getActivity()).disableSearch();
	}

	@Override
	public void onGroupClicked(String groupId, String groupName, boolean isChecked) {
		ArrayList<String> ids = null;

		if (usersFromGroups.get(Integer.parseInt(groupId)) != null) {

			List<String> idsList = usersFromGroups.get(Integer.parseInt(groupId));
			ids = new ArrayList<String>();

			for (String item : idsList) {
				ids.add(item);
			}
		}

		DeselectUsersInGroupActivity.startActivity(groupName, groupId, isChecked, ids, getActivity(), FROM_GROUP_MEMBERS, this);
	}

	@Override
	public void onRoomClicked(String roomId, String roomName, boolean isChecked) {
		ArrayList<String> ids = null;

		if (usersFromRooms.get(Integer.parseInt(roomId)) != null) {

			List<String> idsList = usersFromRooms.get(Integer.parseInt(roomId));
			ids = new ArrayList<String>();

			for (String item : idsList) {
				ids.add(item);
			}
		}

		DeselectUsersInRoomActivity.startActivity(roomName, roomId, isChecked, ids, getActivity(), FROM_ROOM_MEMBERS, this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == FROM_GROUP_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String groupId = data.getStringExtra(Const.GROUP_ID);

				if (dataS == null || dataS.length < 1) {
					removeGroup(groupId, false);
				} else {
					addGroup(groupId, dataS, true);
				}
			}
		} else if (requestCode == FROM_ROOM_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String roomId = data.getStringExtra(Const.ROOM_ID);

				if (dataS == null || dataS.length < 1) {
					removeRoom(roomId, false);
				} else {
					addRoom(roomId, dataS, true);
				}
			}
		}
	}

	private void addGroup(String groupId, String[] users, boolean isFromDetails) {

		List<String> list = new ArrayList<String>();

		for (String item : users) {
			list.add(item);
		}

		usersFromGroups.remove(Integer.parseInt(groupId));
		usersFromGroups.put(Integer.parseInt(groupId), list);

		adapter.addGroup(groupId);
		adapter.notifyDataSetChanged();

		UserGroupRoom item = getGroupById(groupId);
		if (item != null) {
			if (!checkIfItemInUserAdd(item)) {
				onChange(item, isFromDetails);
			}
		}
	}

	private void addRoom(String roomId, String[] users, boolean isFromDetails) {

		List<String> list = new ArrayList<String>();

		for (String item : users) {
			list.add(item);
		}

		usersFromRooms.remove(Integer.parseInt(roomId));
		usersFromRooms.put(Integer.parseInt(roomId), list);

		adapter.addRoom(roomId);
		adapter.notifyDataSetChanged();

		UserGroupRoom item = getGroupById(roomId);
		if (item != null) {
			if (!checkIfItemInUserAdd(item)) {
				onChange(item, isFromDetails);
			}
		}
	}

	private void removeGroup(String groupId, boolean isFromDetails) {

		usersFromGroups.remove(Integer.parseInt(groupId));
		adapter.removeGroup(groupId);
		adapter.notifyDataSetChanged();

		UserGroupRoom item = getGroupById(groupId);
		if (item != null) {
			if (checkIfItemInUserAdd(item)) {
				onChange(item, isFromDetails);
			}
		}
	}

	private void removeRoom(String roomId, boolean isFromDetails) {

		usersFromRooms.remove(Integer.parseInt(roomId));
		adapter.removeRoom(roomId);
		adapter.notifyDataSetChanged();

		UserGroupRoom item = getGroupById(roomId);
		if (item != null) {
			if (checkIfItemInUserAdd(item)) {
				onChange(item, isFromDetails);
			}
		}
	}

	private UserGroupRoom getGroupById(String id) {

		for (UserGroupRoom item : adapter.getData()) {
			if (item.getId().equals(id)) {
				return item;
			}
		}

		return null;
	}

	private boolean checkIfItemInUserAdd(UserGroupRoom item) {

		for (UserGroupRoom item2 : usersToAdd) {

			if (item2.getIs_group() && item2.getId().equals(item.getId())) {
				return true;
			}
		}

		return false;
	}

}
