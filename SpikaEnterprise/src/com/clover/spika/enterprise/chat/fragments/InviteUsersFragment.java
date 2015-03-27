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

import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnInviteClickListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchManageUsersListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class InviteUsersFragment extends CustomFragment implements AdapterView.OnItemClickListener, OnChangeListener<GlobalModel>, OnSearchManageUsersListener,
		OnInviteClickListener {

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
	private InviteRemoveAdapter adapter;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private TextView noItems;
	private TextView txtUsers;

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
			adapter = new InviteRemoveAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), this, this);

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
			GlobalModel user = adapter.getItem(position);

			User userUser = null;

			if (user.type == GlobalModel.Type.USER)
				userUser = (User) user.getModel();

			ProfileOtherActivity.openOtherProfile(getActivity(), user.getId(), ((User) user.getModel()).getImage(),
					((User) user.getModel()).getFirstName() + " " + ((User) user.getModel()).getLastName(), userUser);
		}
	}

	@Override
	public void onChange(GlobalModel obj, boolean isFromDetails) {

		boolean isFound = false;
		int j = 0;

		for (GlobalModel user : adapter.getUsersForString()) {

			if (user.getId() == obj.getId()) {
				isFound = true;
				break;
			}

			j++;
		}

		if (isFound) {

			if (isFromDetails) {
				return;
			}

			adapter.removeFromUsersForString(j);
		} else {
			adapter.addUsersForString(obj);
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < adapter.getUsersForString().size(); i++) {

			if (adapter.getUsersForString().get(i).type == Type.GROUP) {
				builder.append(((Group) adapter.getUsersForString().get(i).getModel()).getGroupName());
			} else if (adapter.getUsersForString().get(i).type == Type.CHAT) {
				builder.append(((Chat) adapter.getUsersForString().get(i).getModel()).chat_name);
			} else if (adapter.getUsersForString().get(i).type == Type.USER) {
				builder.append(((User) adapter.getUsersForString().get(i).getModel()).getFirstName() + " " + ((User) adapter.getUsersForString().get(i).getModel()).getLastName());
			}

			if (i != (adapter.getUsersForString().size() - 1)) {
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

		if (adapter.getUsersSelected().isEmpty() && adapter.getGroupsSelected().isEmpty() && adapter.getRoomsSelected().isEmpty() && adapter.getRoomsAllSelected().isEmpty()
				&& adapter.getGroupsAllSelected().isEmpty()) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}

		List<String> usersId = new ArrayList<String>();
		usersId.addAll(adapter.getUsersSelected());

		List<String> groupsId = new ArrayList<String>();
		groupsId.addAll(adapter.getGroupsSelected());

		List<String> roomsId = new ArrayList<String>();
		roomsId.addAll(adapter.getRoomsSelected());

		List<String> allRoomsId = new ArrayList<String>();
		allRoomsId.addAll(adapter.getRoomsAllSelected());

		List<String> allGroupsId = new ArrayList<String>();
		allGroupsId.addAll(adapter.getGroupsAllSelected());

		SparseArray<List<String>> usersFromGroups = adapter.getUsersFromGroups();
		SparseArray<List<String>> usersFromRooms = adapter.getUsersFromRooms();

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

		// add all room ids
		StringBuilder rooms_to_add_all = new StringBuilder();
		for (int i = 0; i < allRoomsId.size(); i++) {
			rooms_to_add_all.append(allRoomsId.get(i));
			if (i != (allRoomsId.size() - 1)) {
				rooms_to_add_all.append(",");
			}
		}

		// add all group ids
		StringBuilder groups_to_add_all = new StringBuilder();
		for (int i = 0; i < allGroupsId.size(); i++) {
			groups_to_add_all.append(allGroupsId.get(i));
			if (i != (allGroupsId.size() - 1)) {
				groups_to_add_all.append(",");
			}
		}

		// add group ids
		StringBuilder group_to_add = new StringBuilder();
		for (int i = 0; i < groupsId.size(); i++) {

			if (allGroupsId.contains(groupsId.get(i))) {
				continue;
			}

			group_to_add.append(groupsId.get(i));
			if (i != (groupsId.size() - 1)) {
				group_to_add.append(",");
			}
		}

		// add room ids
		StringBuilder rooms_to_add = new StringBuilder();
		for (int i = 0; i < roomsId.size(); i++) {

			if (allRoomsId.contains(roomsId.get(i))) {
				continue;
			}

			rooms_to_add.append(roomsId.get(i));
			if (i != (roomsId.size() - 1)) {
				rooms_to_add.append(",");
			}
		}

		handleProgress(true);
		ChatSpice.AddUsersToRoom addUsersToRoom = new ChatSpice.AddUsersToRoom(users_to_add.toString(), group_to_add.toString(), rooms_to_add.toString(),
				groups_to_add_all.toString(), rooms_to_add_all.toString(), chatId, getActivity());
		spiceManager.execute(addUsersToRoom, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}

			@Override
			public void onRequestSuccess(Chat result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {

					if (getActivity() instanceof ManageUsersActivity) {
						((ManageUsersActivity) getActivity()).setNewChat(result.chat);
					}

					mCurrentIndex = 0;
					mCallbacks.getUsers(mCurrentIndex, null, true, true);
					setInitialTextToTxtUsers();
					adapter.resetSelected();
				} else {
					AppDialog dialog = new AppDialog(getActivity(), false);
					dialog.setFailed(result.getCode());
				}
			}
		});
	}

	public void setData(List<GlobalModel> data, boolean toClearPrevious) {

		try {
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == InviteRemoveAdapter.FROM_GROUP_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String groupId = data.getStringExtra(Const.GROUP_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromGroup(Integer.parseInt(groupId), false);
				} else {
					adapter.addFromGroup(Integer.parseInt(groupId), dataS, true);
				}

				adapter.removeAllGroup(Integer.parseInt(groupId));
			}
		} else if (requestCode == InviteRemoveAdapter.FROM_ROOM_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String roomId = data.getStringExtra(Const.ROOM_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromRoom(Integer.parseInt(roomId), false);
				} else {
					adapter.addFromRoom(Integer.parseInt(roomId), dataS, true);
				}

				adapter.removeAllRoom(Integer.parseInt(roomId));
			}
		}
	}

}
