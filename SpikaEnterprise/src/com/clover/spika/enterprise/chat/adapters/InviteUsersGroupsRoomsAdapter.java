package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnChangeListener;
import com.clover.spika.enterprise.chat.listeners.OnGroupClickedListener;
import com.clover.spika.enterprise.chat.listeners.OnRoomClickedListener;
import com.clover.spika.enterprise.chat.models.UserGroupRoom;
import com.clover.spika.enterprise.chat.views.RobotoCheckBox;

public class InviteUsersGroupsRoomsAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserGroupRoom> data = new ArrayList<UserGroupRoom>();
	private List<String> userIds = new ArrayList<String>();
	private List<String> groupIds = new ArrayList<String>();
	private List<String> roomIds = new ArrayList<String>();

	private ImageLoader imageLoader;

	private OnChangeListener<UserGroupRoom> changedListener;
	private OnGroupClickedListener groupClickedListener;
	private OnRoomClickedListener roomClickedListener;
	private boolean showCheckBox = true;

	public InviteUsersGroupsRoomsAdapter(Context context, Collection<UserGroupRoom> users, OnChangeListener<UserGroupRoom> listener, OnGroupClickedListener listenerGroup,
			OnRoomClickedListener listenerRoom) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		this.changedListener = listener;
		groupClickedListener = listenerGroup;
		roomClickedListener = listenerRoom;
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<UserGroupRoom> list) {
		data = list;
		handleHelperArrays();
		notifyDataSetChanged();
	}

	public void addData(List<UserGroupRoom> list) {
		data.addAll(list);
		handleHelperArrays();
		notifyDataSetChanged();
	}

	private void handleHelperArrays() {

		for (String selectedId : userIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getIsUser()) {
					data.get(i).setSelected(true);
				}
			}
		}

		for (String selectedId : groupIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getIs_group()) {
					data.get(i).setSelected(true);
				}
			}
		}

		for (String selectedId : roomIds) {
			for (int i = 0; i < data.size(); i++) {
				if (selectedId.equals(data.get(i).getId()) && data.get(i).getIsRoom()) {
					data.get(i).setSelected(true);
				}
			}
		}
	}

	public void clearData() {
		data.clear();
		notifyDataSetChanged();
	}

	public List<UserGroupRoom> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public UserGroupRoom getItem(int position) {
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

		UserGroupRoom item = getItem(position);

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		imageLoader.displayImage(getContext(), item.getImageThumb(), holder.profileImg);

		if (item.getIsRoom()) {
			holder.personName.setText(item.getRoomName());
			holder.personName.setTextColor(getContext().getResources().getColor(R.color.default_green));
		} else if (item.getIs_group()) {
			holder.personName.setText(item.getGroupName());
			holder.personName.setTextColor(getContext().getResources().getColor(R.color.default_blue));
		} else {
			holder.personName.setText(item.getFirstName() + " " + item.getLastName());
			holder.personName.setTextColor(Color.BLACK);
		}

		holder.personName.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (getItem(position).getIs_group()) {

					if (groupClickedListener != null) {
						groupClickedListener.onGroupClicked(getItem(position).getId(), getItem(position).getGroupName(), getItem(position).isSelected());
					}
				} else if (getItem(position).getIsRoom()) {

					if (roomClickedListener != null) {
						roomClickedListener.onRoomClicked(getItem(position).getId(), getItem(position).getGroupName(), getItem(position).isSelected());
					}
				}
			}
		});

		if (showCheckBox) {
			if (item.isSelected()) {
				holder.isSelected.setChecked(true);
			} else {
				holder.isSelected.setChecked(false);
			}

			if (!item.getIsMember()) {
				holder.isSelected.setClickable(true);
				holder.isSelected.setEnabled(true);
				holder.isSelected.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (data.get(position).isSelected()) {
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

	private void addToHelperArrays(UserGroupRoom item) {

		if (item.getIs_group()) {
			addGroup(item.getId());
		} else if (item.getIsRoom()) {
			addRoom(item.getId());
		} else if (item.getIsUser()) {
			addUser(item.getId());
		}
	}

	private void removeFromHelperArrays(UserGroupRoom item) {

		if (item.getIs_group()) {
			removeGroup(item.getId());
		} else if (item.getIsRoom()) {
			removeRoom(item.getId());
		} else if (item.getIsUser()) {
			removeUser(item.getId());
		}
	}

	private void addUser(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIsUser()) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!userIds.contains(id)) {
			userIds.add(id);
		}
	}

	private void removeUser(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIsUser()) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (userIds.contains(id)) {
			userIds.remove(id);
		}
	}

	public void addGroup(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIs_group()) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!groupIds.contains(id)) {
			groupIds.add(id);
		}
	}

	public void removeGroup(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIs_group()) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (groupIds.contains(id)) {
			groupIds.remove(id);
		}
	}

	public void addRoom(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIsRoom()) {
					data.get(i).setSelected(true);
				}
			}
		}

		if (!roomIds.contains(id)) {
			roomIds.add(id);
		}
	}

	public void removeRoom(String id) {

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId().equals(id)) {

				if (data.get(i).getIsRoom()) {
					data.get(i).setSelected(false);
				}
			}
		}

		if (roomIds.contains(id)) {
			roomIds.remove(id);
		}
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

	public void resetSelected() {
		userIds.clear();
		groupIds.clear();
		roomIds.clear();
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
