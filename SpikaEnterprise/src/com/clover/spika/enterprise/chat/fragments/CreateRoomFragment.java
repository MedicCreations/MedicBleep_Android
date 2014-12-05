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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUsersGroupsRoomsAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.RoomsApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnNextStepRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserGroupRoom;
import com.clover.spika.enterprise.chat.models.UsersAndGroupsList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class CreateRoomFragment extends CustomFragment implements OnSearchListener, OnClickListener, OnNextStepRoomListener, OnChangeListener<UserGroupRoom> {

	private static final int FROM_CATEGORY = 11;

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteUsersGroupsRoomsAdapter adapter;

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

	private String mCategoryId = "0";
	private String mCategoryName = "";
	private TextView mTvCategoryName;
	private Switch mSwitchPrivate;
	private RobotoThinEditText mEtPassword;
	private RobotoThinEditText mEtPasswordRepeat;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new InviteUsersGroupsRoomsAdapter(getActivity(), new ArrayList<UserGroupRoom>(), this, this);

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
		mainListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((CreateRoomActivity)getActivity()).hideKeyboard(roomName);
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
		});

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
		api.getUsersAndGroupsForRoomsByName(null, mCurrentIndex, search, getActivity(), true, new ApiCallback<UsersAndGroupsList>() {

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

		boolean isFound = false;
		int j = 0;

		for (UserGroupRoom user : adapter.getUsersForString()) {

			if (user.getId().equals(obj.getId())) {

				if (user.getIs_group() == obj.getIs_group()) {
					isFound = true;
					break;
				}
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

			if (adapter.getUsersForString().get(i).getIs_group()) {
				builder.append(adapter.getUsersForString().get(i).getGroupName());
			} else if (adapter.getUsersForString().get(i).getIsRoom()) {
				builder.append(adapter.getUsersForString().get(i).getRoomName());
			} else if (adapter.getUsersForString().get(i).getIsUser()) {
				builder.append(adapter.getUsersForString().get(i).getFirstName() + " " + adapter.getUsersForString().get(i).getLastName());
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
		} else if (requestCode == InviteUsersGroupsRoomsAdapter.FROM_GROUP_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String groupId = data.getStringExtra(Const.GROUP_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromGroup(groupId, false);
				} else {
					adapter.addFromGroup(groupId, dataS, true);
				}

				adapter.removeAllGroup(groupId);
			}
		} else if (requestCode == InviteUsersGroupsRoomsAdapter.FROM_ROOM_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String roomId = data.getStringExtra(Const.ROOM_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromRoom(roomId, false);
				} else {
					adapter.addFromRoom(roomId, dataS, true);
				}

				adapter.removeAllRoom(roomId);
			}
		}
	}

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

		if (adapter.getUsersForString().isEmpty()) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}

		if (adapter.getUsersSelected().size() == 0 && adapter.getGroupsSelected().size() == 0 && adapter.getRoomsSelected().size() == 0) {
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

		((CreateRoomActivity) getActivity()).setConfirmScreen(users_to_add.toString(), group_to_add.toString(), rooms_to_add.toString(), groups_to_add_all.toString(),
				rooms_to_add_all.toString(), isPrivate, password);
	}

	@Override
	public void onDestroy() {
		Helper.setRoomThumbId(getActivity(), null);
		super.onDestroy();
	}
}
