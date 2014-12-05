package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.DeselectUsersInGroupActivity;
import com.clover.spika.enterprise.chat.DeselectUsersInRoomActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.RobotoCheckBox;

public class InviteRemoveAdapter extends BaseAdapter {

	public static final int FROM_GROUP_MEMBERS = 12;
	public static final int FROM_ROOM_MEMBERS = 13;

	private Context mContext;
	private List<GlobalModel> data = new ArrayList<GlobalModel>();

	private List<String> userIds = new ArrayList<String>();
	private List<String> groupIds = new ArrayList<String>();
	private List<String> roomIds = new ArrayList<String>();
	private List<String> addAllGroup = new ArrayList<String>();
	private List<String> addAllRoom = new ArrayList<String>();

	SparseArray<List<String>> usersFromGroups = new SparseArray<List<String>>();
	SparseArray<List<String>> usersFromRooms = new SparseArray<List<String>>();

	List<GlobalModel> usersToAdd = new ArrayList<GlobalModel>();

	CustomFragment fragment;

	private ImageLoader imageLoader;

	private OnChangeListener<GlobalModel> changedListener;
	private boolean showCheckBox = true;

	public InviteRemoveAdapter(Context context, Collection<GlobalModel> users, OnChangeListener<GlobalModel> listener, CustomFragment fragment) {
		this.mContext = context;
		this.data.addAll(users);

		this.fragment = fragment;

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		this.changedListener = listener;
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<GlobalModel> list) {
		data = list;
		handleHelperArrays();
		notifyDataSetChanged();
	}

	public void addData(List<GlobalModel> list) {
		data.addAll(list);
		handleHelperArrays();
		notifyDataSetChanged();
	}

	private void handleHelperArrays() {

		for (String selectedId : userIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getType() == Type.USER) {
					data.get(i).setSelected(true);
				}
			}
		}

		for (String selectedId : groupIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getType() == Type.GROUP) {
					data.get(i).setSelected(true);
				}
			}
		}

		for (String selectedId : roomIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getType() == Type.CHAT) {
					data.get(i).setSelected(true);
				}
			}
		}
	}

	public void clearData() {
		data.clear();
		notifyDataSetChanged();
	}

	public List<GlobalModel> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public GlobalModel getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderCharacter holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_invite_person, parent, false);

			holder = new ViewHolderCharacter(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderCharacter) convertView.getTag();
		}

		// set image to null
		holder.profileImg.setImageDrawable(null);

		GlobalModel item = getItem(position);

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		imageLoader.displayImage(getContext(), item.getImageThumb(), holder.profileImg);

		if (item.getType() == Type.CHAT) {
			holder.personName.setText(((Chat) item.getModel()).getChat_name());
			holder.personName.setTextColor(getContext().getResources().getColor(R.color.default_green));
		} else if (item.getType() == Type.GROUP) {
			holder.personName.setText(((Group) item.getModel()).getGroupName());
			holder.personName.setTextColor(getContext().getResources().getColor(R.color.default_blue));
		} else {

			String name = ((User) item.getModel()).getFirstName() + " " + ((User) item.getModel()).getLastName();

			if (((User) item.getModel()).isAdmin()) {
				name = name + " " + mContext.getString(R.string.admin);
			}

			holder.personName.setText(name);
			holder.personName.setTextColor(Color.BLACK);
		}

		holder.personName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (getItem(position).getType() == Type.GROUP) {

					ArrayList<String> ids = null;
					if (usersFromGroups.get(getItem(position).getId()) != null) {

						List<String> idsList = usersFromGroups.get(getItem(position).getId());
						ids = new ArrayList<String>();

						for (String item : idsList) {
							ids.add(item);
						}
					}

					DeselectUsersInGroupActivity.startActivity(((Group) getItem(position).getModel()).getGroupName(), getItem(position).getId(), getItem(position).getSelected(),
							ids, mContext, FROM_GROUP_MEMBERS, fragment);
				} else if (getItem(position).getType() == Type.CHAT) {

					ArrayList<String> ids = null;

					if (usersFromRooms.get(getItem(position).getId()) != null) {

						List<String> idsList = usersFromRooms.get(getItem(position).getId());
						ids = new ArrayList<String>();

						for (String item : idsList) {
							ids.add(item);
						}
					}

					DeselectUsersInRoomActivity.startActivity(((Chat) getItem(position).getModel()).getChat_name(), getItem(position).getId(), getItem(position).getSelected(),
							ids, mContext, FROM_ROOM_MEMBERS, fragment);
				}
			}
		});

		if (showCheckBox) {
			if (item.getSelected()) {
				holder.isSelected.setChecked(true);
			} else {
				holder.isSelected.setChecked(false);
			}

			if (!item.isMember()) {
				holder.isSelected.setClickable(true);
				holder.isSelected.setEnabled(true);
				holder.isSelected.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (data.get(position).getSelected()) {
							removeFromHelperArrays(data.get(position));
						} else {
							addToHelperArrays(data.get(position));
						}

						if (changedListener != null) {
							changedListener.onChange(data.get(position), false);
						}
					}
				});
			} else {
				holder.isSelected.setClickable(false);
				holder.isSelected.setOnClickListener(null);
				holder.isSelected.setEnabled(false);
			}

		} else {
			holder.isSelected.setVisibility(View.GONE);
		}

		return convertView;
	}

	public void addToHelperArrays(GlobalModel item) {

		if (item.getType() == Type.CHAT) {
			addRoom(item.getId());
			addAllRoom(item.getId());
		} else if (item.getType() == Type.GROUP) {
			addGroup(item.getId());
			addAllGroup(item.getId());
		} else if (item.getType() == Type.USER) {
			addUser(item.getId());
		}
	}

	private void removeFromHelperArrays(GlobalModel item) {

		if (item.getType() == Type.CHAT) {
			removeRoom(item.getId());
			removeAllRoom(item.getId());
			usersFromRooms.remove(item.getId());
		} else if (item.getType() == Type.GROUP) {
			removeGroup(item.getId());
			removeAllGroup(item.getId());
			usersFromGroups.remove(item.getId());
		} else if (item.getType() == Type.USER) {
			removeUser(item.getId());
		}
	}

	private void addUser(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.USER) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!userIds.contains(String.valueOf(id))) {
			userIds.add(String.valueOf(id));
		}
	}

	private void removeUser(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.USER) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (userIds.contains(String.valueOf(id))) {
			userIds.remove(String.valueOf(id));
		}
	}

	public void addGroup(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.GROUP) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!groupIds.contains(String.valueOf(id))) {
			groupIds.add(String.valueOf(id));
		}
	}

	public void removeGroup(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.GROUP) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (groupIds.contains(String.valueOf(id))) {
			groupIds.remove(String.valueOf(id));
		}
	}

	public void addRoom(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.CHAT) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!roomIds.contains(String.valueOf(id))) {
			roomIds.add(String.valueOf(id));
		}
	}

	public void removeRoom(int id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == id) {

				if (data.get(i).getType() == Type.CHAT) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (roomIds.contains(String.valueOf(id))) {
			roomIds.remove(String.valueOf(id));
		}
	}

	public void addAllRoom(int id) {

		if (!addAllRoom.contains(String.valueOf(id))) {
			addAllRoom.add(String.valueOf(id));
		}
	}

	public void removeAllRoom(int id) {

		if (addAllRoom.contains(String.valueOf(id))) {
			addAllRoom.remove(String.valueOf(id));
		}
	}

	public void addAllGroup(int id) {

		if (!addAllGroup.contains(String.valueOf(id))) {
			addAllGroup.add(String.valueOf(id));
		}
	}

	public void removeAllGroup(int id) {

		if (addAllGroup.contains(String.valueOf(id))) {
			addAllGroup.remove(String.valueOf(id));
		}
	}

	public void addFromGroup(int groupId, String[] users, boolean isFromDetails) {

		List<String> list = new ArrayList<String>();

		for (String item : users) {
			list.add(item);
		}

		usersFromGroups.remove(groupId);
		usersFromGroups.put(groupId, list);

		addGroup(groupId);
		notifyDataSetChanged();

		GlobalModel item = getGroupById(groupId);
		if (item != null) {
			if (!checkIfItemInUserAdd(item)) {
				if (changedListener != null) {
					changedListener.onChange(item, isFromDetails);
				}
			}
		}
	}

	public void addFromRoom(int roomId, String[] users, boolean isFromDetails) {

		List<String> list = new ArrayList<String>();

		for (String item : users) {
			list.add(item);
		}

		usersFromRooms.remove(roomId);
		usersFromRooms.put(roomId, list);

		addRoom(roomId);
		notifyDataSetChanged();

		GlobalModel item = getGroupById(roomId);
		if (item != null) {
			if (!checkIfItemInUserAdd(item)) {
				if (changedListener != null) {
					changedListener.onChange(item, isFromDetails);
				}
			}
		}
	}

	public void removeFromGroup(int groupId, boolean isFromDetails) {

		usersFromGroups.remove(groupId);
		removeGroup(groupId);
		notifyDataSetChanged();

		GlobalModel item = getGroupById(groupId);
		if (item != null) {
			if (checkIfItemInUserAdd(item)) {
				if (changedListener != null) {
					changedListener.onChange(item, isFromDetails);
				}
			}
		}
	}

	public void removeFromRoom(int roomId, boolean isFromDetails) {

		usersFromRooms.remove(roomId);
		removeRoom(roomId);
		notifyDataSetChanged();

		GlobalModel item = getGroupById(roomId);
		if (item != null) {
			if (checkIfItemInUserAdd(item)) {
				if (changedListener != null) {
					changedListener.onChange(item, isFromDetails);
				}
			}
		}
	}

	public void removeFromUsersForString(int i) {
		usersToAdd.remove(i);
	}

	public void addUsersForString(GlobalModel obj) {
		usersToAdd.add(obj);
	}

	private GlobalModel getGroupById(int id) {

		for (GlobalModel item : getData()) {
			if (item.getId() == id) {
				return item;
			}
		}

		return null;
	}

	private boolean checkIfItemInUserAdd(GlobalModel item) {

		for (GlobalModel item2 : usersToAdd) {

			if (item2.getType() == Type.GROUP && item2.getId() == item.getId()) {
				return true;
			}
		}

		return false;
	}

	public List<String> getUsersSelected() {
		return userIds;
	}

	public List<String> getGroupsSelected() {
		return groupIds;
	}

	public List<String> getRoomsSelected() {
		return roomIds;
	}

	public List<String> getRoomsAllSelected() {
		return addAllRoom;
	}

	public List<String> getGroupsAllSelected() {
		return addAllGroup;
	}

	public SparseArray<List<String>> getUsersFromGroups() {
		return usersFromGroups;
	}

	public SparseArray<List<String>> getUsersFromRooms() {
		return usersFromRooms;
	}

	public List<GlobalModel> getUsersForString() {
		return usersToAdd;
	}

	public void resetSelected() {
		userIds.clear();
		groupIds.clear();
		roomIds.clear();

		addAllGroup.clear();
		addAllRoom.clear();

		usersFromGroups.clear();
		usersFromRooms.clear();

		usersToAdd.clear();
	}

	public class ViewHolderCharacter {

		public RelativeLayout itemLayout;
		public ImageView profileImg;

		public TextView personName;
		public RobotoCheckBox isSelected;

		public ViewHolderCharacter(View view) {

			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
			profileImg = (ImageView) view.findViewById(R.id.userImage);

			personName = (TextView) view.findViewById(R.id.personName);
			isSelected = (RobotoCheckBox) view.findViewById(R.id.isSelected);
		}
	}

}
