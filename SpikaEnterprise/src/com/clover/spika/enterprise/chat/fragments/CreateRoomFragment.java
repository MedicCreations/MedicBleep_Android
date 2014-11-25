package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.DeselectUsersInGroupActivity;
import com.clover.spika.enterprise.chat.DeselectUsersInRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUsersOrGroupsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.RoomsApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnGroupClickedListener;
import com.clover.spika.enterprise.chat.listeners.OnNextStepRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnRoomClickedListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserGroupRoom;
import com.clover.spika.enterprise.chat.models.UsersAndGroupsList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class CreateRoomFragment extends CustomFragment implements OnSearchListener, OnClickListener, OnNextStepRoomListener, OnChangeListener<UserGroupRoom>,
		OnGroupClickedListener, OnRoomClickedListener {

	private static final int FROM_CATEGORY = 11;
	private static final int FROM_GROUP_MEMBERS = 12;
	private static final int FROM_ROOM_MEMBERS = 13;

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteUsersOrGroupsAdapter adapter;

	private TextView txtUsers;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;

	private String room_file_id = "";
	private String room_thumb_id = "";

	private ImageView imgRoom;
	private RobotoThinEditText roomName;
	private ImageButton btnSearch;
	private EditText etSearch;

	List<UserGroupRoom> usersToAdd = new ArrayList<UserGroupRoom>();
	SparseArray<List<String>> usersFromGroups = new SparseArray<List<String>>();
	SparseArray<List<String>> usersFromRooms = new SparseArray<List<String>>();

	private String mCategoryId = "0";
	private String mCategoryName = "";
	private TextView mTvCategoryName;
	private Switch mSwitchPrivate;
	private RobotoThinEditText mEtPassword;
	private RobotoThinEditText mEtPasswordRepeat;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new InviteUsersOrGroupsAdapter(getActivity(), new ArrayList<UserGroupRoom>(), this, this, this);

		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
		SpikaEnterpriseApp.getInstance().deleteSamsungPathImage();
	}

	@SuppressLint("InflateParams")
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_create_room, container, false);

		View header = inflater.inflate(R.layout.pull_to_refresh_header_create_room, null);

		boolean isCategoriesEnabled = getResources().getBoolean(R.bool.enable_categories);

		if (!isCategoriesEnabled) {

			RelativeLayout categoryLayout = (RelativeLayout) header.findViewById(R.id.layoutCategory);
			categoryLayout.setVisibility(View.GONE);
			// View viewAboveCategory = (View)
			// header.findViewById(R.id.aboveCategoryLayout);
			// viewAboveCategory.setVisibility(View.GONE);
		}

		if (getArguments() != null)
			mCategoryId = getArguments().getString(Const.CATEGORY_ID, "0");
		if (getArguments() != null)
			mCategoryName = getArguments().getString(Const.CATEGORY_NAME, getString(R.string.select_category));

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		txtUsers = (TextView) header.findViewById(R.id.txtUserNames);
		txtUsers.setMovementMethod(new ScrollingMovementMethod());
		mTvCategoryName = (TextView) header.findViewById(R.id.tvCategory);
		setCategory(mCategoryName);
		imgRoom = (ImageView) header.findViewById(R.id.img_room);
		imgRoom.setOnClickListener(this);

		roomName = (RobotoThinEditText) header.findViewById(R.id.et_room_name);
		roomName.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(roomName.getWindowToken(), 0);
				}
				return false;
			}
		});

		mSwitchPrivate = (Switch) header.findViewById(R.id.switch_private_room);
		mEtPassword = (RobotoThinEditText) header.findViewById(R.id.etPassword);
		mEtPasswordRepeat = (RobotoThinEditText) header.findViewById(R.id.etPasswordRepeat);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.getRefreshableView().addHeaderView(header);
		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);

		btnSearch = (ImageButton) rootView.findViewById(R.id.searchBtn);
		btnSearch.setOnClickListener(this);

		etSearch = (EditText) rootView.findViewById(R.id.searchEt);
		etSearch.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					onSearch(etSearch.getText().toString());
				}
				return false;
			}
		});

		getUsers(mCurrentIndex, null, false);

		setInitialTextToTxtUsers();

		((CreateRoomActivity) getActivity()).setNext(this);

		rootView.findViewById(R.id.layoutCategory).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openChooseCategory();
			}
		});

		return rootView;
	}

	@Override
	public void onClosed() {
		super.onClosed();
		if (getActivity() instanceof CreateRoomActivity) {

			room_file_id = Helper.getRoomFileId(getActivity());
			room_thumb_id = Helper.getRoomThumbId(getActivity());

			if (room_file_id != "") {
				((CreateRoomActivity) getActivity()).getImageLoader().displayImage(getActivity(), room_thumb_id, imgRoom);
			}

			((CreateRoomActivity) getActivity()).setRoom_file_id(room_file_id);
			((CreateRoomActivity) getActivity()).setRoom_thumb_id(room_thumb_id);
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
			getUsers(mCurrentIndex, mSearchData, false);
		}
	};

	private void setData(List<UserGroupRoom> data, boolean toClearPrevious) {
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

	public void getUsers(int page, String search, final boolean toClear) {
		RoomsApi api = new RoomsApi();
		api.getUsersAndGroupsForRoomsByName(mCurrentIndex, search, getActivity(), true, new ApiCallback<UsersAndGroupsList>() {

			@Override
			public void onApiResponse(Result<UsersAndGroupsList> result) {
				mTotalCount = result.getResultData().getTotalCount();
				setData(result.getResultData().getUsersAndGroupsList(), toClear);
			}
		});

	}

	@Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		getUsers(mCurrentIndex, mSearchData, true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.img_room:
			showDialog();
			break;
		case R.id.searchBtn:
			mSearchData = etSearch.getText().toString();
			onSearch(mSearchData);
		}

	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryRoom();
	}

	@Override
	public void onChange(UserGroupRoom obj, boolean isFromDetails) {

		if (isFromDetails) {
			return;
		}

		boolean isFound = false;
		int j = 0;

		for (UserGroupRoom user : usersToAdd) {

			if (user.getId().equals(obj.getId())) {

				if (user.getIs_group() == obj.getIs_group()) {
					isFound = true;
					break;
				}
			}

			j++;
		}

		if (isFound) {
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
			} else if (usersToAdd.get(i).getIsUser()) {
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

	private void setInitialTextToTxtUsers() {
		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers);
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtUsers.setText(span);
	}

	private void openChooseCategory() {
		startActivityForResult(new Intent(getActivity(), ChooseCategoryActivity.class), FROM_CATEGORY);
	}

	private void setCategory(String catName) {
		if (mCategoryId != null && !mCategoryId.equals("0")) {
			mTvCategoryName.setText(catName);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == FROM_CATEGORY) {

			if (data != null) {
				mCategoryId = data.getStringExtra(Const.CATEGORY_ID);
				setCategory(data.getStringExtra(Const.CATEGORY_NAME));
			}
		} else if (requestCode == FROM_GROUP_MEMBERS) {

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

	private boolean checkIfItemInUserAdd(UserGroupRoom item) {

		for (UserGroupRoom item2 : usersToAdd) {

			if (item2.getIs_group() && item2.getId().equals(item.getId())) {
				return true;
			}
		}

		return false;
	}

	private UserGroupRoom getGroupById(String id) {
		for (UserGroupRoom item : adapter.getData()) {
			if (item.getId().equals(id)) {
				return item;
			}
		}
		return null;
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
	public void onNext() {

		String name = roomName.getText().toString();
		String password = mEtPassword.getText().toString();
		String passwordRepeat = mEtPasswordRepeat.getText().toString();
		String isPrivate = "0";

		if (mSwitchPrivate.isChecked()) {
			isPrivate = "1";
		}

		((CreateRoomActivity) getActivity()).setRoomName(name);
		((CreateRoomActivity) getActivity()).setCategoryId(mCategoryId);

		if (name.equals("")) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.room_name_empty));
			return;
		}

		if (!password.equals(passwordRepeat)) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.password_error));
			return;
		}

		if (usersToAdd.isEmpty()) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}

		List<String> usersId = adapter.getUsersSelected();
		List<String> groupsId = adapter.getGroupsSelected();
		List<String> roomsId = adapter.getRoomsSelected();

		// remove group duplicates
		for (int i = 0; i < usersFromGroups.size(); i++) {
			usersId.addAll(usersFromGroups.valueAt(i));
			if (groupsId.contains(String.valueOf(usersFromGroups.keyAt(i)))) {
				groupsId.remove(String.valueOf(usersFromGroups.keyAt(i)));
			}
		}

		if (usersId.isEmpty() && groupsId.isEmpty() && roomsId.isEmpty()) {
			return;
		}

		// remove room duplicates
		for (int i = 0; i < usersFromRooms.size(); i++) {
			roomsId.addAll(usersFromRooms.valueAt(i));
			if (roomsId.contains(String.valueOf(usersFromRooms.keyAt(i)))) {
				roomsId.remove(String.valueOf(usersFromRooms.keyAt(i)));
			}
		}

		// add user ids
		StringBuilder users_to_add = new StringBuilder();
		users_to_add.append(Helper.getUserId(getActivity()));

		boolean isIn = false;
		for (int i = 0; i < usersId.size(); i++) {

			if (!isIn) {
				users_to_add.append(",");
				isIn = true;
			}

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

		((CreateRoomActivity) getActivity()).setConfirmScreen(users_to_add.toString(), group_to_add.toString(), rooms_to_add.toString(), isPrivate, password);
	}

	@Override
	public void onDestroy() {
		Helper.setRoomThumbId(getActivity(), null);
		super.onDestroy();
	}
}
