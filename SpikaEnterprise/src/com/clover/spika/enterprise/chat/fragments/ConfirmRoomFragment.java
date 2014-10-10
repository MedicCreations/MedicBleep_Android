package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.RoomsApi;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.models.ConfirmUsersList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class ConfirmRoomFragment extends CustomFragment implements OnCreateRoomListener, OnChangeListener<User> {
	
	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteUserAdapter adapter;
	
	private ImageView imgRoom;
	private RobotoRegularTextView roomName;

	List<User> usersToAdd = new ArrayList<User>();
	
	private String userIds;
	private String groupIds;
	private String roomThumbId;
	private String roomNameData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new InviteUserAdapter(getActivity(), new ArrayList<User>(), this);

	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_confirm_users, container, false);
		
		if(getArguments() != null) userIds = getArguments().getString(Const.USER_IDS, "");
		if(getArguments() != null) groupIds = getArguments().getString(Const.GROUP_IDS, "");
		if(getArguments() != null) roomThumbId = getArguments().getString(Const.ROOM_THUMB_ID, "");
		if(getArguments() != null) roomNameData = getArguments().getString(Const.NAME, "");

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.main_list_view);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setMode(PullToRefreshBase.Mode.DISABLED);

		mainListView.setAdapter(adapter);
		
		imgRoom = (ImageView) rootView.findViewById(R.id.img_room);
		
		if (roomThumbId != ""){
			((CreateRoomActivity) getActivity()).getImageLoader().displayImage(getActivity(), roomThumbId, imgRoom);
		}
		
		roomName = (RobotoRegularTextView) rootView.findViewById(R.id.tv_room_name);
		roomName.setText(roomNameData);
		
		getUsers();
		
		((CreateRoomActivity) getActivity()).setCreateRoom(this);
		
		return rootView;
	}
	
	private void setData(List<User> data) {
		
		for(int i = 0; i < data.size(); i++){
			data.get(i).setSelected(true);
		}

		adapter.setData(data);
		
		for(int i = 0; i < data.size(); i++){
			adapter.setId(data.get(i).getId());
		}
		
		mainListView.onRefreshComplete();

		if (adapter.getCount() == 0) {
			noItems.setVisibility(View.VISIBLE);
		} else {
			noItems.setVisibility(View.GONE);
		}
		
	}

	public void getUsers() {
		new RoomsApi().getDistinctUser(userIds, groupIds, getActivity(), true, new ApiCallback<ConfirmUsersList>() {
			
			@Override
			public void onApiResponse(Result<ConfirmUsersList> result) {
				setData(result.getResultData().getUserList());
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener#onCreateRoom()
	 */
	@Override
	public void onCreateRoom() {
		
		Log.d("LOG", "on created on room");
		
		StringBuilder users_to_add = new StringBuilder();
		List<String> usersId = new ArrayList<String>();
		usersId.addAll(adapter.getSelected());
		
		if (usersId.isEmpty()) {
			return;
		}
		
		Log.d("LOG", "on created on usersId");

		for (int i = 0; i < usersId.size(); i++) {
			users_to_add.append(usersId.get(i));

			if (i != (usersId.size() - 1)) {
				users_to_add.append(",");
			}
		}
		
		((CreateRoomActivity)getActivity()).createRoomFinaly(users_to_add.toString());
		
	}

	@Override
	public void onChange(User obj) {
		
	}

}
