package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.UsersApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnCreateRoomListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UsersList;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class CreateRoomFragment extends CustomFragment implements OnItemClickListener, OnSearchListener, OnClickListener, OnCreateRoomListener, OnChangeListener<User> {

	private TextView noItems;

	PullToRefreshListView mainListView;
	public InviteUserAdapter adapter;
	
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
	private FrameLayout layoutForChooseCategory;
	
	List<User> usersToAdd = new ArrayList<User>();
	
	LobbyFragment lobbyFragment;
	
	private int mCategoryId = 0;
	private TextView mTvCategoryName;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new InviteUserAdapter(getActivity(), new ArrayList<User>(), this);

		mCurrentIndex = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
		((MainActivity) getActivity()).enableCreateRoom();
	}

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_create_room, container, false);

		noItems = (TextView) rootView.findViewById(R.id.noItems);

		txtUsers = (TextView) rootView.findViewById(R.id.txtUserNames);
		layoutForChooseCategory = (FrameLayout) rootView.findViewById(R.id.flForChooseCategoryFragment);
		mTvCategoryName = (TextView) rootView.findViewById(R.id.tvCategory);
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
		
		setInitialTextToTxtUsers();
		
		((MainActivity) getActivity()).setCreateRoom(this);
		
		rootView.findViewById(R.id.layoutCategory).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				openChooseCategory();
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		((MainActivity) getActivity()).disableCreateRoom();
	}
	
	@Override
	public void onClosed() {
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
		if(toClearPrevious) currentCount = data.size();

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
		
		String name = roomName.getText().toString();
		
		if (name.equals("")){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo("Room name is empty");
			return;
		}
		
		if (usersToAdd.isEmpty()){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}
		
		String my_user_id = Helper.getUserId(getActivity());
		
		StringBuilder users_to_add = new StringBuilder();
		
		users_to_add.append(my_user_id + ",");

		List<String> usersId = adapter.getSelected();

		if (usersId.isEmpty()) {
			return;
		}

		for (int i = 0; i < usersId.size(); i++) {
			users_to_add.append(usersId.get(i));

			if (i != (usersId.size() - 1)) {
				users_to_add.append(",");
			}
		}
		
		new ChatApi().createRoom(name, room_file_id, room_thumb_id, users_to_add.toString(), String.valueOf(mCategoryId),
				getActivity(), new ApiCallback<Chat>() {

			@Override
			public void onApiResponse(Result<Chat> result) {
				if (result.isSuccess()) {
					
					String chat_name = result.getResultData().getChat().getChat_name();
					String chat_id = result.getResultData().getChat().getChat_id();
					String chat_image = room_file_id;
					
					Intent intent = new Intent(getActivity(), ChatActivity.class);
					intent.putExtra(Const.TYPE, String.valueOf(Const.C_ROOM_ADMIN_ACTIVE));
					intent.putExtra(Const.CHAT_ID, chat_id);
					intent.putExtra(Const.CHAT_NAME, chat_name);
					intent.putExtra(Const.IMAGE, chat_image);
					intent.putExtra(Const.IS_ACTIVE, 1);
					
					startActivity(intent);
					
					if (lobbyFragment == null) {
						lobbyFragment = new LobbyFragment();
					}

					((MainActivity) getActivity()).setScreenTitle(getActivity().getResources().getString(R.string.lobby));
					
					MainActivity base = (MainActivity) getActivity();
					base.switchContent(lobbyFragment);
					
					Helper.setRoomFileId(getActivity(), "");
					Helper.setRoomThumbId(getActivity(), "");
					usersToAdd.clear();
					etSearch.setText("");
					roomName.setText("");
				}
			}
		});
		
	}

	@Override
	public void onChange(User obj) {
		boolean isFound = false;
		int j = 0;

		for (User user : usersToAdd) {
			if (user.getId().equals(obj.getId())) {
				isFound = true;
				break;
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
			builder.append(usersToAdd.get(i).getFirstName() + " " + usersToAdd.get(i).getLastName());
			if (i != (usersToAdd.size() - 1)) {
				builder.append(", ");
			}
		}

		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers + builder.toString());
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		txtUsers.setText(span);
		
	}
	
	private void setInitialTextToTxtUsers(){
		String selectedUsers = getActivity().getString(R.string.selected_users);
		Spannable span = new SpannableString(selectedUsers);
		span.setSpan(new ForegroundColorSpan(R.color.devil_gray), 0, selectedUsers.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		txtUsers.setText(span);
	}
	
	private void openChooseCategory(){
		startActivityForResult(new Intent(getActivity(), ChooseCategoryActivity.class), 11);
	}
	
	private void setCategory(String catName){
		mTvCategoryName.setText(catName);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null){
			mCategoryId = data.getIntExtra(Const.CATEGORY_ID, 0);
			setCategory(data.getStringExtra(Const.CATEGORY_NAME));
		}
	}
	
}
