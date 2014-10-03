package com.clover.spika.enterprise.chat.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.ProfileOtherActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnInviteClickListener;
import com.clover.spika.enterprise.chat.listeners.OnSearchManageUsersListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshBase;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class InviteUsersFragment extends Fragment implements AdapterView.OnItemClickListener, OnChangeListener<User>, 
													OnSearchManageUsersListener, OnInviteClickListener {

    public interface Callbacks {
        void getUsers(int currentIndex, String search, final boolean toClear, final boolean toUpdateMember);
    }
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override public void getUsers(int currentIndex, String search, final boolean toClear, final boolean toUpdateMember) { }
    };
    private Callbacks mCallbacks = sDummyCallbacks;

    private PullToRefreshListView mainListView;
    private InviteUserAdapter adapter;

    private int mCurrentIndex = 0;
    private int mTotalCount = 0;
    private String mSearchData = null;
    
    private TextView noItems;
    private List<User> usersToAdd = new ArrayList<User>();
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
            throw new IllegalArgumentException(activity.toString() +
                    " has to implement Callbacks interface in order to inflate this Fragment.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mCallbacks = sDummyCallbacks;
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            adapter = new InviteUserAdapter(getActivity(), new ArrayList<User>(), this);

            noItems = (TextView) view.findViewById(R.id.noItems);
            txtUsers = (TextView) view.findViewById(R.id.invitedPeople);
            
            mainListView = (PullToRefreshListView) view.findViewById(R.id.main_list_view);
            mainListView.setAdapter(adapter);
            mainListView.setOnRefreshListener(refreshListener2);
            mainListView.setOnItemClickListener(this);
            
            if(getActivity() instanceof ManageUsersActivity){
            	((ManageUsersActivity)getActivity()).setOnInviteClickListener(this);
            }
            
            setInitialTextToTxtUsers();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        position = position - 1;

        if (position != -1 && position != adapter.getCount()) {
            User user = adapter.getItem(position);
            ProfileOtherActivity.openOtherProfile(getActivity(), user.getImage(), user.getFirstName() + " " + user.getLastName());
        }
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
	public void onInvite(String chatId) {
    	
    	if(adapter.getSelected().size() == 0){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo("You didn't select any users");
			return;
		}
    	
		StringBuilder idsBuilder = new StringBuilder();
		for(String item : adapter.getSelected()){
			idsBuilder.append(item+",");
		}
		
		//remove last comma
		String ids = idsBuilder.substring(0, idsBuilder.length()-1);
		new ChatApi().addUsersToRoom(ids, chatId, getActivity(), new ApiCallback<Chat>() {
			
			@Override
			public void onApiResponse(Result<Chat> result) {
				if (result.isSuccess()){
					mCurrentIndex = 0;
					mCallbacks.getUsers(mCurrentIndex, null, true, true);
					setInitialTextToTxtUsers();
					adapter.resetSelected();
					usersToAdd.clear();
				}
			}
		});
	}

    public void setData(List<User> data, boolean toClearPrevious) {
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
    
    private void setInitialTextToTxtUsers(){
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

}
