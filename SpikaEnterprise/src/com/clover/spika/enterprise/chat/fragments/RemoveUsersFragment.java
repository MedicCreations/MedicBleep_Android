package com.clover.spika.enterprise.chat.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.clover.spika.enterprise.chat.ManageUsersActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.adapters.InviteRemoveAdapter;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.listeners.OnRemoveClickListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import java.util.ArrayList;

public class RemoveUsersFragment extends MembersFragment implements AdapterView.OnItemClickListener, OnRemoveClickListener {

	public static RemoveUsersFragment newInstance() {
		RemoveUsersFragment fragment = new RemoveUsersFragment();
		Bundle arguments = new Bundle();
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		if (getListView() != null) {
			mUserAdapter = new InviteRemoveAdapter(spiceManager, getActivity(), new ArrayList<GlobalModel>(), null, null);
			mUserAdapter.disableNameClick(true);
			mUserAdapter.setWitoutMe(true);
			getListView().setAdapter(mUserAdapter);
		}

		if (getActivity() instanceof ManageUsersActivity) {
			((ManageUsersActivity) getActivity()).setOnRemoveClickListener(this);
		}
	}

	@Override
	public void onRemove(String chatId) {

		if (mUserAdapter.getUsersSelected().size() == 0 && mUserAdapter.getGroupsSelected().size() == 0 && mUserAdapter.getRoomsSelected().size() == 0) {
			AppDialog dialog = new AppDialog(getActivity(), false);
			dialog.setInfo(getActivity().getString(R.string.you_didn_t_select_any_users));
			return;
		}

		String ids = "";
		if (mUserAdapter.getUsersSelected().size() != 0) {
			// create users ids
			StringBuilder idsBuilder = new StringBuilder();
			for (String item : mUserAdapter.getUsersSelected()) {
				idsBuilder.append(item + ",");
			}

			// remove last comma
			ids = idsBuilder.substring(0, idsBuilder.length() - 1);
		}

		String groupIds = "";
		if (mUserAdapter.getGroupsSelected().size() != 0) {
			// create group ids
			StringBuilder idsGroupBuilder = new StringBuilder();
			for (String item : mUserAdapter.getGroupsSelected()) {
				idsGroupBuilder.append(item + ",");
			}

			// remove last comma
			groupIds = idsGroupBuilder.substring(0, idsGroupBuilder.length() - 1);
		}

		String roomIds = "";
		if (mUserAdapter.getRoomsSelected().size() != 0) {
			// create group ids
			StringBuilder idsRoomBuilder = new StringBuilder();
			for (String item : mUserAdapter.getRoomsSelected()) {
				idsRoomBuilder.append(item + ",");
			}

			// remove last comma
			roomIds = idsRoomBuilder.substring(0, idsRoomBuilder.length() - 1);
		}

		handleProgress(true);
		ChatSpice.LeaveChatAdmin leaveChatAdmin = new ChatSpice.LeaveChatAdmin(chatId, ids, groupIds, roomIds);
		spiceManager.execute(leaveChatAdmin, new CustomSpiceListener<Chat>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, getActivity());
			}

			@Override
			public void onRequestSuccess(Chat result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					if (getActivity() instanceof ManageUsersActivity) {
						((ManageUsersActivity) getActivity()).setNewChat(result.chat);
					}
					mCurrentIndex = 0;
					mUserAdapter.clearData();
					mUserAdapter.resetSelected();
					mCallbacks.getMembers(mCurrentIndex, true);
				} else {
					AppDialog dialog = new AppDialog(getActivity(), false);
					dialog.setFailed(result.getCode());
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

}
