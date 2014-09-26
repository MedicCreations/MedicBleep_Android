package com.clover.spika.enterprise.chat.fragments;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.UserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Logger;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityStopListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

public class CreateRoomFragment extends CustomFragment implements OnItemClickListener, OnSearchListener, OnClickListener, OnCreateRoomListener {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public UserAdapter adapter;
	
	private TextView txtUsers;

	private int mCurrentIndex = 0;
	private int mTotalCount = 0;
	private String mSearchData = null;
	private String users = "";
	
	private String room_file_id = "";
	private String room_thumb_id = "";
	
	private ImageView imgRoom;
	private RobotoThinEditText roomName;
	private ImageButton btnSearch;
	private EditText etSearch;
	
	private Map<String, User> mapUsers = new HashMap<String, User>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new UserAdapter(getActivity(), new ArrayList<User>());

		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_create_room, container, false);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		txtUsers = (TextView) rootView.findViewById(R.id.txtUserNames);
		imgRoom = (ImageView) rootView.findViewById(R.id.img_room);
		imgRoom.setOnClickListener(this);
		
		roomName = (RobotoThinEditText) rootView.findViewById(R.id.et_room_name);
		
		mainListView = (PullToRefreshListView) rootView.findViewById(R.id.mainListView);
		mainListView.getRefreshableView().setMotionEventSplittingEnabled(false);
		mainListView.setOnItemClickListener(this);

		mainListView.setAdapter(adapter);
		mainListView.setOnRefreshListener(refreshListener2);
		
		btnSearch = (ImageButton) rootView.findViewById(R.id.searchBtn);
		btnSearch.setOnClickListener(this);
		
		etSearch = (EditText) rootView.findViewById(R.id.searchEt);
		etSearch.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					((MainActivity) getActivity()).hideKeyboard(etSearch);
						onSearch(etSearch.getText().toString());
				}
				return false;
			}
		});

		getUsers(mCurrentIndex, null, false);
		
		((MainActivity) getActivity()).setCreateRoom(this);
		
		return rootView;
	}

	@Override
	public void onPause() {
		super.onPause();
		((MainActivity) getActivity()).disableCreateRoom();
	}
	
	
	@Override
	public void onClosed() {
		// TODO Auto-generated method stub
		super.onClosed();
		if (getActivity() instanceof MainActivity) {
			
			room_file_id = Helper.getRoomFileId(getActivity());
			room_thumb_id = Helper.getRoomThumbId(getActivity());
			
			if (room_file_id != ""){
				((MainActivity) getActivity()).getImageLoader().displayImage(getActivity(), room_thumb_id, imgRoom);
			}	
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

	private void setData(List<User> data, boolean toClearPrevious) {
		// -2 is because of header and footer view
		int currentCount = mainListView.getRefreshableView().getAdapter().getCount() - 2 + data.size();

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
		UsersApi api = new UsersApi();
		if (search == null) {
			api.getUsersWithPage(getActivity(), mCurrentIndex, null, true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mTotalCount = result.getResultData().getTotalCount();
						setData(result.getResultData().getUserList(), toClear);
					}
				}
			});
		} else {
			api.getUsersByName(mCurrentIndex, null, search, getActivity(), true, new ApiCallback<UsersList>() {

				@Override
				public void onApiResponse(Result<UsersList> result) {
					if (result.isSuccess()) {
						mTotalCount = result.getResultData().getTotalCount();
						setData(result.getResultData().getUserList(), toClear);
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
		getUsers(mCurrentIndex, mSearchData, true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		position = position - 1;

		if (position != -1 && position != adapter.getCount()) {
			
			User user = adapter.getItem(position);
			
			if (user.isSelected()){
				user.setSelected(!user.isSelected());
				mapUsers.remove(user.getId());
				users = "";
				for (String key: mapUsers.keySet()){
					if (users == ""){
						users += mapUsers.get(key).getFirstName() + " " + mapUsers.get(key).getLastName();
					} else {
						users += ", " + mapUsers.get(key).getFirstName() + " " + mapUsers.get(key).getLastName();
					}
				}
				txtUsers.setText(users);
				
			} else {
				
				mapUsers.put(user.getId(), user);
				
				user.setSelected(!user.isSelected());
				users = txtUsers.getText().toString();
				if (users == ""){
					users += user.getFirstName() + " " + user.getLastName();
				} else {
					users += ", " + user.getFirstName() + " " + user.getLastName();
				}
				txtUsers.setText(users);
			}
			
			adapter.notifyDataSetChanged();

		}
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
	public void onCreateRoom() {
		// TODO Auto-generated method stub
		
		String name = roomName.getText().toString();
		
		if (name.equals("")){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo("Room name is empty");
			return;
		}
		
		if (mapUsers.isEmpty()){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo("You didn't select any users");
			return;
		}
		
		String my_user_id = Helper.getUserId(getActivity());
		
		String users_to_add = my_user_id;
		
		for (String key: mapUsers.keySet()){
			users_to_add += "," + mapUsers.get(key).getId();
		}
		
		new ChatApi().createRoom(name, room_file_id, room_thumb_id, users_to_add, getActivity(), new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {
				// TODO Auto-generated method stub
				if (result.isSuccess()) {
					
					String chat_name = result.getResultData().getChat().getChat_name();
					String chat_id = result.getResultData().getChat().getChat_id();
					String chat_image = room_thumb_id;
					
					Intent intent = new Intent(getActivity(), ChatActivity.class);
					intent.putExtra(Const.CHAT_ID, chat_id);
					intent.putExtra(Const.CHAT_NAME, chat_name);
					intent.putExtra(Const.IMAGE, chat_image);
					
					startActivity(intent);
					
					Helper.setRoomFileId(getActivity(), "");
					Helper.setRoomThumbId(getActivity(), "");
					mapUsers.clear();
					etSearch.setText("");
				}
			}
		});
		
	}
	
}
