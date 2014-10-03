package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnRemoveClickListener;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.User;

public class RemoveUsersFragment extends MembersFragment implements AdapterView.OnItemClickListener, OnChangeListener<User>, 
													OnRemoveClickListener {

    public static RemoveUsersFragment newInstance() {
        RemoveUsersFragment fragment = new RemoveUsersFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	super.onViewCreated(view, savedInstanceState);
    	 if(getActivity() instanceof ManageUsersActivity){
         	((ManageUsersActivity)getActivity()).setOnRemoveClickListener(this);
         }
    }
    
	@Override
	public void onRemove(String chatId) {
		if(mUserAdapter.getSelected().size() == 0){
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo("You didn't select any users");
			return;
		}
		
		StringBuilder idsBuilder = new StringBuilder();
		for(String item : mUserAdapter.getSelected()){
			idsBuilder.append(item+",");
		}
		
		//remove last comma
		String ids = idsBuilder.substring(0, idsBuilder.length()-1);
		
		new ChatApi().leaveChat(chatId, ids, getActivity(), new ApiCallback<BaseModel>() {
			
			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if(result.isSuccess()){
					mCurrentIndex = 0;
					mUserAdapter.clearData();
					mUserAdapter.resetSelected();
					mCallbacks.getMembers(mCurrentIndex);
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
	}
	
}
