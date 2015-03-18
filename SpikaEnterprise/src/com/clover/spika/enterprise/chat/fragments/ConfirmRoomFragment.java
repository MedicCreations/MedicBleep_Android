package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.TextUtils;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.RoomsSpice;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class ConfirmRoomFragment extends CustomFragment implements OnCreateRoomListener {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteRemoveAdapter adapter;

	List<User> usersToAdd = new ArrayList<User>();

	private String userIds;
	private String groupIds;
	private String groupAllIds;
	private String roomIds;
	private String roomAllIds;
	private String roomThumbId;
	private String roomNameData;

	private List<GlobalModel> allData = new ArrayList<GlobalModel>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new InviteRemoveAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), null, null);
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_confirm_users, container, false);

		if (getArguments() != null)
			userIds = getArguments().getString(Const.USER_IDS, "");

		if (getArguments() != null)
			groupIds = getArguments().getString(Const.GROUP_IDS, "");

		if (getArguments() != null)
			groupAllIds = getArguments().getString(Const.GROUP_ALL_IDS, "");

		if (getArguments() != null)
			roomIds = getArguments().getString(Const.ROOM_IDS, "");

		if (getArguments() != null)
			roomAllIds = getArguments().getString(Const.ROOM_ALL_IDS, "");

		if (getArguments() != null)
			roomThumbId = getArguments().getString(Const.ROOM_THUMB_ID, "");

		if (getArguments() != null)
			roomNameData = getArguments().getString(Const.NAME, "");

		View header = fillHeader(inflater);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.main_list_view);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mainListView.getRefreshableView().addHeaderView(header);

		mainListView.setAdapter(adapter);

		getUsers();

		((CreateRoomActivity) getActivity()).setCreateRoom(this);

		return rootView;
	}

	private View fillHeader(LayoutInflater inflater) {
		View rootView = inflater.inflate(R.layout.pull_to_refresh_header_create_room, null, false);

		ImageView imgRoom = (ImageView) rootView.findViewById(R.id.img_room);
		TextView roomName = (TextView) rootView.findViewById(R.id.tv_room_name);
		rootView.findViewById(R.id.et_room_name).setVisibility(View.GONE);
		if (!TextUtils.isEmpty(roomThumbId)) {
			getImageLoader().displayImage(imgRoom, roomThumbId, ImageLoaderSpice.DEFAULT_GROUP_IMAGE);
		}
		roomName.setText(roomNameData);
		roomName.setVisibility(View.VISIBLE);

		Switch switchPrivate = (Switch) rootView.findViewById(R.id.switch_private_room);
		switchPrivate.setChecked(getArguments().getBoolean(Const.IS_PRIVATE, false));
		switchPrivate.setEnabled(false);
		
		boolean isPrivateRoomEnabled = getResources().getBoolean(R.bool.enable_private_room);

		if (!isPrivateRoomEnabled) {
			rootView.findViewById(R.id.layoutPrivate).setVisibility(View.GONE);
			rootView.findViewById(R.id.belowPrivateLayout).setVisibility(View.GONE);
		}else{
			rootView.findViewById(R.id.layoutPrivate).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.belowPrivateLayout).setVisibility(View.VISIBLE);
		}
		
		String passTxt = getArguments().getString(Const.PASSWORD, "");
		EditText password = (EditText) rootView.findViewById(R.id.etPassword);
		password.setEnabled(false);
		password.setText(passTxt);
		
		boolean isPasswordEnabled = getResources().getBoolean(R.bool.enable_room_password);

		if (!isPasswordEnabled || TextUtils.isEmpty(passTxt)) {
			rootView.findViewById(R.id.layoutPassword).setVisibility(View.GONE);
			rootView.findViewById(R.id.belowPasswordLayout).setVisibility(View.GONE);
		}else{
			rootView.findViewById(R.id.layoutPassword).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.belowPasswordLayout).setVisibility(View.VISIBLE);
		}

		rootView.findViewById(R.id.layoutPasswordRepeat).setVisibility(View.GONE);
		rootView.findViewById(R.id.belowPasswordRepeatLayout).setVisibility(View.GONE);

		String catName = getArguments().getString(Const.CATEGORY_NAME, "None");
		if(catName.equals(getString(R.string.select_category))){
			catName = "None";
		}
		TextView tvCategory = (TextView) rootView.findViewById(R.id.tvCategory);
		tvCategory.setText(catName);
		rootView.findViewById(R.id.arrowRightCategory).setVisibility(View.GONE);
		boolean isCategoriesEnabled = getResources().getBoolean(R.bool.enable_categories);
		if (!isCategoriesEnabled) {
			rootView.findViewById(R.id.layoutCategory).setVisibility(View.GONE);
			rootView.findViewById(R.id.belowCategoryLayout).setVisibility(View.GONE);
		}else{
			rootView.findViewById(R.id.layoutCategory).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.belowCategoryLayout).setVisibility(View.VISIBLE);
		}

		rootView.findViewById(R.id.txtUserNames).setVisibility(View.GONE);
		rootView.findViewById(R.id.belowUsersLayout).setVisibility(View.GONE);

		EditText etSearch = (EditText) rootView.findViewById(R.id.searchEt);
		etSearch.addTextChangedListener(textWatacher);

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

	private void setData(List<GlobalModel> data) {

		for (int i = 0; i < data.size(); i++) {
			((User) data.get(i).getModel()).setSelected(true);
		}

		for (int i = 0; i < data.size(); i++) {
			if (String.valueOf(((User) data.get(i).getModel()).getId()).equals(Helper.getUserId(getActivity()))) {
				data.remove(i);
			}
		}

		adapter.setData(data);

		for (int i = 0; i < data.size(); i++) {
			adapter.addToHelperArrays(data.get(i));
		}

		mainListView.onRefreshComplete();

		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}

		allData.clear();
		allData.addAll(adapter.getData());
	}

	public void getUsers() {

		handleProgress(true);
		RoomsSpice.GetDistinctUserOK get = new RoomsSpice.GetDistinctUserOK(userIds, groupIds, roomIds, groupAllIds, roomAllIds, getActivity());
		// RoomsSpice.GetDistinctUser getDistinctUser = new
		// RoomsSpice.GetDistinctUser(userIds, groupIds, roomIds, groupAllIds,
		// roomAllIds, getActivity());
		spiceManager.execute(get, new CustomSpiceListener<ConfirmUsersList>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}

			@Override
			public void onRequestSuccess(ConfirmUsersList list) {
				handleProgress(false);

				if (list != null && list.getCode() == Const.API_SUCCESS) {
					List<GlobalModel> globalList = new ArrayList<GlobalModel>();

					if (list.users_array != null) {
						for (User user : list.users_array) {

							GlobalModel model = new GlobalModel();
							model.type = Type.USER;
							model.user = user;
							globalList.add(model);
						}
					}

					setData(globalList);
				} else {
					Utils.onFailedUniversal(null, getActivity());
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener#onCreateRoom
	 * ()
	 */
	@Override
	public void onCreateRoom() {

		StringBuilder users_to_add = new StringBuilder();
		List<String> usersId = new ArrayList<String>();
		usersId.addAll(adapter.getUsersSelected());

		if (usersId.isEmpty()) {
			return;
		}

		String myUserId = Helper.getUserId(getActivity());
		users_to_add.append(myUserId + ",");

		for (int i = 0; i < usersId.size(); i++) {
			users_to_add.append(usersId.get(i));

			if (i != (usersId.size() - 1)) {
				users_to_add.append(",");
			}
		}

		if (!TextUtils.isEmpty(groupAllIds)) {
			groupIds = groupIds + "," + groupAllIds;
		}

		if (!TextUtils.isEmpty(roomAllIds)) {
			roomIds = roomIds + "," + roomAllIds;
		}

		((CreateRoomActivity) getActivity()).createRoomFinaly(users_to_add.toString(), groupIds, roomIds);
	}

	@Override
	public void onFilterClick() {
		
	}

}
