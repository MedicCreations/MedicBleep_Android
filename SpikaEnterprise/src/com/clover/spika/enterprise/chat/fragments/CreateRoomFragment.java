package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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

import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.GlobalSpice;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchDBChanged;
import com.clover.spika.enterprise.chat.caching.GlobalCaching.OnGlobalSearchNetworkResult;
import com.clover.spika.enterprise.chat.caching.robospice.GlobalCacheSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.ChooseCategoryDialog;
import com.clover.spika.enterprise.chat.dialogs.ChooseCategoryDialog.UseType;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnNextStepRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.GlobalResponse;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class CreateRoomFragment extends CustomFragment implements OnSearchListener, OnClickListener, OnNextStepRoomListener, OnChangeListener<GlobalModel>,
		OnGlobalSearchDBChanged, OnGlobalSearchNetworkResult {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteRemoveAdapter adapter;

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

	private TextView filterAll;
	private TextView filterUsers;
	private TextView filterGroups;
	private TextView filterRooms;

	private int currentFilter = GlobalModel.Type.ALL;

	private List<GlobalModel> allData = new ArrayList<GlobalModel>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new InviteRemoveAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), this, this);
		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
		SpikaEnterpriseApp.deleteSamsungPathImage();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_create_room, container, false);
		View header = inflater.inflate(R.layout.pull_to_refresh_header_create_room, null);

		boolean isCategoriesEnabled = getResources().getBoolean(R.bool.enable_categories);

		RelativeLayout categoryLayout = (RelativeLayout) header.findViewById(R.id.layoutCategory);
		if (!isCategoriesEnabled) {
			header.findViewById(R.id.belowCategoryLayout).setVisibility(View.GONE);
			categoryLayout.setVisibility(View.GONE);
		} else {
			header.findViewById(R.id.belowCategoryLayout).setVisibility(View.VISIBLE);
			categoryLayout.setVisibility(View.VISIBLE);
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

		filterAll = (TextView) header.findViewById(R.id.filter_all);
		filterAll.setOnClickListener(this);

		filterUsers = (TextView) header.findViewById(R.id.filter_users);
		filterUsers.setOnClickListener(this);

		filterGroups = (TextView) header.findViewById(R.id.filter_groups);
		filterGroups.setOnClickListener(this);

		filterRooms = (TextView) header.findViewById(R.id.filter_rooms);
		filterRooms.setOnClickListener(this);

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

		boolean isPrivateRoomEnabled = getResources().getBoolean(R.bool.enable_private_room);

		if (!isPrivateRoomEnabled) {
			header.findViewById(R.id.layoutPrivate).setVisibility(View.GONE);
			header.findViewById(R.id.belowPrivateLayout).setVisibility(View.GONE);
		} else {
			header.findViewById(R.id.layoutPrivate).setVisibility(View.VISIBLE);
			header.findViewById(R.id.belowPrivateLayout).setVisibility(View.VISIBLE);
		}

		mEtPassword = (RobotoThinEditText) header.findViewById(R.id.etPassword);
		mEtPasswordRepeat = (RobotoThinEditText) header.findViewById(R.id.etPasswordRepeat);

		boolean isPasswordEnabled = getResources().getBoolean(R.bool.enable_room_password);

		if (!isPasswordEnabled) {
			header.findViewById(R.id.layoutPassword).setVisibility(View.GONE);
			header.findViewById(R.id.layoutPasswordRepeat).setVisibility(View.GONE);
			header.findViewById(R.id.belowPasswordLayout).setVisibility(View.GONE);
			header.findViewById(R.id.belowPasswordRepeatLayout).setVisibility(View.GONE);
		} else {
			header.findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);
			header.findViewById(R.id.layoutPasswordRepeat).setVisibility(View.VISIBLE);
			header.findViewById(R.id.belowPasswordLayout).setVisibility(View.VISIBLE);
			header.findViewById(R.id.belowPasswordRepeatLayout).setVisibility(View.VISIBLE);
		}

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.getRefreshableView().addHeaderView(header);
		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);
		mainListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((CreateRoomActivity) getActivity()).hideKeyboard(roomName);

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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

		etSearch.addTextChangedListener(textWatacher);

		getListItems(mCurrentIndex, null, false, currentFilter);

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

	private TextWatcher textWatacher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			adapter.manageData(s.toString(), allData);
		}
	};

	@Override
	public void onClosed() {
		super.onClosed();
		if (getActivity() instanceof CreateRoomActivity) {

			room_file_id = Helper.getRoomFileId(getActivity());
			room_thumb_id = Helper.getRoomThumbId(getActivity());

			if (room_file_id != "") {
				getImageLoader().displayImage(imgRoom, room_thumb_id, ImageLoaderSpice.DEFAULT_GROUP_IMAGE);
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
			getListItems(mCurrentIndex, mSearchData, false, currentFilter);
		}
	};

	private void setData(List<GlobalModel> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

		if (toClearPrevious) {
			currentCount = data.size();
		}

		adapter.setData(data);

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

		allData.clear();
		allData.addAll(adapter.getData());
	}

	public void getListItems(int page, String search, final boolean toClear, int type) {
		
		if(TextUtils.isEmpty(search)){ // get data from database
			GlobalCacheSpice.GlobalSearch globalSearch = new GlobalCacheSpice.GlobalSearch(getActivity(), spiceManager, page, null, null, type, search, toClear, this, this);
			spiceManager.execute(globalSearch, new CustomSpiceListener<List>() {

				@SuppressWarnings("unchecked")
				@Override
				public void onRequestSuccess(List result) {
					super.onRequestSuccess(result);
					setData(result, toClear);
				}
			});
		}else{
			handleProgress(true);
			GlobalSpice.GlobalSearch globalSearch = new GlobalSpice.GlobalSearch(page, null, null, type, search, getActivity());
			spiceManager.execute(globalSearch, new CustomSpiceListener<GlobalResponse>() {

				@Override
				public void onRequestFailure(SpiceException arg0) {
					handleProgress(false);
					super.onRequestFailure(arg0);
					Utils.onFailedUniversal(null, getActivity(), 0 , false, arg0, null);
				}

				@Override
				public void onRequestSuccess(GlobalResponse result) {
					handleProgress(false);
					super.onRequestSuccess(result);

					if (result.getCode() == Const.API_SUCCESS) {
						
						mTotalCount = result.total_count;
						setData(result.search_result, toClear);

					} else {
						String message = getActivity().getString(R.string.e_something_went_wrong);
						Utils.onFailedUniversal(message, getActivity());
					}
				}
			});
		}
		
	}

	@Override
	public void onSearch(String data) {
		mCurrentIndex = 0;
		if (TextUtils.isEmpty(data)) {
			mSearchData = null;
		} else {
			mSearchData = data;
		}
		getListItems(mCurrentIndex, mSearchData, true, currentFilter);
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

		case R.id.filter_all:
			filterAllGo();
			break;

		case R.id.filter_users:
			filterUsersGo();
			break;

		case R.id.filter_groups:
			filterGroupsGo();
			break;

		case R.id.filter_rooms:
			filterRoomsGo();
			break;

		}

	}

	private void filterAllGo() {
		
		currentFilter = GlobalModel.Type.ALL;

		filterAll.setTextColor(getActivity().getResources().getColor(R.color.text_blue));
		filterAll.setTextSize(15);

		filterUsers.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterUsers.setTextSize(12);

		filterGroups.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterGroups.setTextSize(12);

		filterRooms.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterRooms.setTextSize(12);

		mCurrentIndex = 0;
		getListItems(mCurrentIndex, mSearchData, true, currentFilter);
	}

	private void filterUsersGo() {

		currentFilter = GlobalModel.Type.USER;

		filterAll.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterAll.setTextSize(12);

		filterUsers.setTextColor(getActivity().getResources().getColor(R.color.text_blue));
		filterUsers.setTextSize(15);

		filterGroups.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterGroups.setTextSize(12);

		filterRooms.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterRooms.setTextSize(12);

		mCurrentIndex = 0;
		getListItems(mCurrentIndex, mSearchData, true, currentFilter);
	}

	private void filterGroupsGo() {

		currentFilter = GlobalModel.Type.GROUP;

		filterAll.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterAll.setTextSize(12);

		filterUsers.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterUsers.setTextSize(12);

		filterGroups.setTextColor(getActivity().getResources().getColor(R.color.text_blue));
		filterGroups.setTextSize(15);

		filterRooms.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterRooms.setTextSize(12);

		mCurrentIndex = 0;
		getListItems(mCurrentIndex, mSearchData, true, currentFilter);
	}

	private void filterRoomsGo() {

		currentFilter = GlobalModel.Type.CHAT;

		filterAll.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterAll.setTextSize(12);

		filterUsers.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterUsers.setTextSize(12);

		filterGroups.setTextColor(getActivity().getResources().getColor(R.color.baloon_blue));
		filterGroups.setTextSize(12);

		filterRooms.setTextColor(getActivity().getResources().getColor(R.color.text_blue));
		filterRooms.setTextSize(15);

		mCurrentIndex = 0;
		getListItems(mCurrentIndex, mSearchData, true, currentFilter);
	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryRoom();
	}

	@Override
	public void onChange(GlobalModel obj, boolean isFromDetails) {

		boolean isFound = false;
		int j = 0;

		for (GlobalModel item : adapter.getUsersForString()) {

			if (item.getId() == obj.getId()) {

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

	private void setInitialTextToTxtUsers() {
		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers);
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		txtUsers.setText(span);
	}

	private void openChooseCategory() {
		ChooseCategoryDialog dialog = new ChooseCategoryDialog(getActivity(), UseType.CHOOSE_CATEGORY, Integer.parseInt(mCategoryId));
		dialog.show();
		dialog.setListener(new ChooseCategoryDialog.OnActionClick() {

			@Override
			public void onCloseClick(Dialog d) {
				d.dismiss();
			}

			@Override
			public void onCategorySelect(String categoryId, String categoryName, Dialog d) {
				mCategoryId = categoryId;
				setCategory(categoryName);
				d.dismiss();
			}

			@Override
			public void onAcceptClick(Dialog d) {
				d.dismiss();
			}
		});
	}

	private void setCategory(String catName) {
		if (mCategoryId != null && !mCategoryId.equals("0")) {
			mTvCategoryName.setText(catName);
			mTvCategoryName.setTextColor(getResources().getColor(R.color.default_blue));
		} else {
			mTvCategoryName.setText(getString(R.string.select_category));
			mTvCategoryName.setTextColor(getResources().getColor(R.color.devil_gray));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == InviteRemoveAdapter.FROM_GROUP_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String groupId = data.getStringExtra(Const.GROUP_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromGroup(Integer.valueOf(groupId), false);
				} else {
					adapter.addFromGroup(Integer.valueOf(groupId), dataS, true);
				}

				adapter.removeAllGroup(Integer.valueOf(groupId));
			}
		} else if (requestCode == InviteRemoveAdapter.FROM_ROOM_MEMBERS) {

			if (data != null) {
				String[] dataS = data.getStringArrayExtra(Const.USER_IDS);
				String roomId = data.getStringExtra(Const.ROOM_ID);

				if (dataS == null || dataS.length < 1) {
					adapter.removeFromRoom(Integer.valueOf(roomId), false);
				} else {
					adapter.addFromRoom(Integer.valueOf(roomId), dataS, true);
				}

				adapter.removeAllRoom(Integer.valueOf(roomId));
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
		((CreateRoomActivity) getActivity()).setCategoryName(mTvCategoryName.getText().toString());

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
		Helper.setRoomThumbId(getActivity(), "");
		super.onDestroy();
	}

	@Override
	public void onGlobalSearchNetworkResult(int totalCount) {
		mTotalCount = totalCount;
	}

	@Override
	public void onGlobalSearchDBChanged(List<GlobalModel> usableData, boolean isClear) {
		setData(usableData, isClear);
	}
}
