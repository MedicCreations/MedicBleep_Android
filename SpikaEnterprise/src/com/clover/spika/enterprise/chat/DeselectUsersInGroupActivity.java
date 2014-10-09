package com.clover.spika.enterprise.chat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.adapters.InviteUserAdapter;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.GroupsApi;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.fragments.CreateRoomFragment;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.GroupMember;
import com.clover.spika.enterprise.chat.models.GroupMembersList;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.pulltorefresh.PullToRefreshListView;

public class DeselectUsersInGroupActivity extends BaseActivity implements OnChangeListener<User>{
	
	private String groupName;
	private String groupId;
	private boolean isChecked = false;
	
	private List<User> mUsers; 
	private List<String> mUsersToPass = new ArrayList<String>();

    public static void startActivity(String groupName, String groupId, boolean isChecked, ArrayList<String> ids,
    		@NonNull Context context, int requestCode, CreateRoomFragment frag) {
        Intent intent = new Intent(context, DeselectUsersInGroupActivity.class);
        intent.putExtra(Const.GROUP_ID, groupId);
        intent.putExtra(Const.GROUP_NAME, groupName);
        intent.putExtra(Const.IS_ACTIVE, isChecked);
        intent.putStringArrayListExtra(Const.USER_IDS, ids);
        frag.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deselect_users_in_group);
        
        groupName = getIntent().getStringExtra(Const.GROUP_NAME);
        groupId = getIntent().getStringExtra(Const.GROUP_ID);
        isChecked = getIntent().getBooleanExtra(Const.IS_ACTIVE, false);
        
        ((TextView)findViewById(R.id.screenTitle)).setText(groupName);
        
        findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//finish and pass data to activity
				String[] array = new String[mUsersToPass.size()];
				int i = 0;
				for(String itemString : mUsersToPass){
					array[i] = itemString;
					i++;
				}
				setResult(RESULT_OK, new Intent().putExtra(Const.USER_IDS, array)
												.putExtra(Const.GROUP_ID, groupId));
				finish();
			}
		});

        getUsersFromGroup();
    }
    
    private void getUsersFromGroup(){
    	new GroupsApi().getGroupMembers("-1", groupId, this, true, new ApiCallback<GroupMembersList>() {
			
			@Override
			public void onApiResponse(Result<GroupMembersList> result) {
				if(result.isSuccess()){
					List<GroupMember> members = result.getResultData().getMemberList();
					mUsers = generateUserList(members);
					
					setListView();
				}
			}
		});
    	
    }
    
    private void setListView(){
    	InviteUserAdapter adapter = new InviteUserAdapter(this, mUsers, this);
    	PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.main_list_view);
    	listView.getRefreshableView().setAdapter(adapter);
    }
    
    private List<User> generateUserList(List<GroupMember> members){
    	List<String> usersIds = getIntent().getStringArrayListExtra(Const.USER_IDS);
    	List<User> list = new ArrayList<User>();
    	for(GroupMember item : members){
    		boolean toCheck = false;
    		if(isChecked){
    			if(usersIds != null){
    				if(usersIds.contains(String.valueOf(item.getId()))){
    					mUsersToPass.add(String.valueOf(item.getId()));
    					toCheck = true;
    				}
    			}else{
    				mUsersToPass.add(String.valueOf(item.getId()));
    				toCheck = true;
    			}
    		}
    		list.add(new User(String.valueOf(item.getId()), String.valueOf(item.getId()), item.getFirstName(), 
    				item.getLastName(), null, item.getImage(), item.getImage_thumb(), false, null, toCheck));
    	}
    	
    	return list;
    }

	@Override
	public void onChange(User obj) {
		boolean isFound = false;
		int j = 0;

		for (String item : mUsersToPass) {
			if (item.equals(String.valueOf(obj.getId()))) {
				isFound = true;
				break;
			}
			j++;
		}

		if (isFound) {
			mUsersToPass.remove(j);
		} else {
			mUsersToPass.add(String.valueOf(obj.getId()));
		}
	}

}
