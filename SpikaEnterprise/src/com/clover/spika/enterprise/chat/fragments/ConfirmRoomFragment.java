package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.TextUtils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.RoomsApi;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class ConfirmRoomFragment extends CustomFragment implements OnCreateRoomListener {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteRemoveAdapter adapter;

	private ImageView imgRoom;
	private RobotoRegularTextView roomName;

	List<User> usersToAdd = new ArrayList<User>();

	private String userIds;
	private String groupIds;
	private String groupAllIds;
	private String roomIds;
	private String roomAllIds;
	private String roomThumbId;
	private String roomNameData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new InviteRemoveAdapter(getActivity(), new ArrayList<GlobalModel>(), null, null);
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

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.main_list_view);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setMode(PullToRefreshBase.Mode.DISABLED);

		mainListView.setAdapter(adapter);

		imgRoom = (ImageView) rootView.findViewById(R.id.img_room);

		if (!TextUtils.isEmpty(roomThumbId)) {
			((CreateRoomActivity) getActivity()).getImageLoader().displayImage(getActivity(), roomThumbId, imgRoom);
		}

		roomName = (RobotoRegularTextView) rootView.findViewById(R.id.tv_room_name);
		roomName.setText(roomNameData);

		getUsers();

		((CreateRoomActivity) getActivity()).setCreateRoom(this);

		return rootView;
	}

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
	}

	public void getUsers() {

		new RoomsApi().getDistinctUser(userIds, groupIds, roomIds, groupAllIds, roomAllIds, getActivity(), true, new ApiCallback<ConfirmUsersList>() {

			@Override
			public void onApiResponse(Result<ConfirmUsersList> result) {

				List<GlobalModel> globalList = new ArrayList<GlobalModel>();

				for (User user : result.getResultData().getUserList()) {

					GlobalModel model = new GlobalModel();
					model.setType(Type.USER);
					model.setUser(user);
					globalList.add(model);
				}

				setData(globalList);
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

}
