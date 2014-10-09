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
import com.clover.spika.enterprise.chat.models.UserOrGroup;
import com.clover.spika.enterprise.chat.views.RobotoCheckBox;

public class InviteUsersOrGroupsAdapter extends BaseAdapter {

	private Context mContext;
	private List<UserOrGroup> data = new ArrayList<UserOrGroup>();
	private List<String> userIds = new ArrayList<String>();
	private List<String> groupIds = new ArrayList<String>();

	private ImageLoader imageLoader;

	private OnChangeListener<UserOrGroup> changedListener;
	private OnGroupClickedListener groupClickedListener;
	private boolean showCheckBox = true;

	public InviteUsersOrGroupsAdapter(Context context, Collection<UserOrGroup> users, OnChangeListener<UserOrGroup> listener,
			OnGroupClickedListener listenerGroup) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = ImageLoader.getInstance();
		imageLoader.setDefaultImage(R.drawable.default_user_image);

		this.changedListener = listener;
		groupClickedListener = listenerGroup;
	}
	
	public Context getContext() {
		return mContext;
	}

	public void setData(List<UserOrGroup> list) {
		data = list;

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

		notifyDataSetChanged();
	}

	public void addData(List<UserOrGroup> list) {
		data.addAll(list);
		
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
		
		notifyDataSetChanged();
	}
	
	public void clearData(){
		data.clear();
		notifyDataSetChanged();
	}

	public List<UserOrGroup> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public UserOrGroup getItem(int position) {
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

		UserOrGroup user = getItem(position);

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		imageLoader.displayImage(getContext(), user.getImageThumb(), holder.profileImg);
		
		if(user.getIs_group()){
			holder.personName.setText(user.getGroupName());
			holder.personName.setTextColor(Color.GREEN);
		}else{
			holder.personName.setText(user.getFirstName() + " " + user.getLastName());
			holder.personName.setTextColor(Color.BLACK);
		}
		
		holder.personName.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if((getItem(position).getIs_group())){
					if(groupClickedListener != null) groupClickedListener.onGroupClicked(getItem(position).getId(), 
													getItem(position).getGroupName(), getItem(position).isSelected());
				}
			}
		});
		
		if(showCheckBox){
			if (user.isSelected()) {
				holder.isSelected.setChecked(true);
			} else {
				holder.isSelected.setChecked(false);
			}

			holder.isSelected.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (data.get(position).isSelected()) {
						data.get(position).setSelected(false);
						removeId(data.get(position));
					} else {
						data.get(position).setSelected(true);
						setId(data.get(position));
					}

					if (changedListener != null) {
						changedListener.onChange(data.get(position));
					}
				}
			});
			
		}else{
			holder.isSelected.setVisibility(View.GONE);
		}

		return convertView;
	}

	private void setId(UserOrGroup item) {
		if(item.getIs_group()){
			groupIds.add(item.getId());
			return;
		}
		userIds.add(item.getId());
	}

	private void removeId(UserOrGroup item) {
		if(item.getIs_group()){
			groupIds.remove(item.getId());
			return;
		}
		userIds.remove(item.getId());
	}
	
	public void removeGroup(String id){
		for(int i = 0; i < data.size(); i++){
			if(data.get(i).getId().equals(id)){
				if(data.get(i).getIs_group()){
					data.get(i).setSelected(false);
				}
			}
		}
		if(groupIds.contains(id)) groupIds.remove(id);
	}
	
	public void addGroup(String id){
		for(int i = 0; i < data.size(); i++){
			if(data.get(i).getId().equals(id)){
				if(data.get(i).getIs_group()){
					data.get(i).setSelected(true);
				}
			}
		}
		if(!groupIds.contains(id)) groupIds.add(id);
	}

	public List<String> getSelected() {
		List<String> allList = new ArrayList<String>();
		allList.addAll(groupIds);
		allList.addAll(userIds);
		return allList;
	}
	
	public List<String> getUsersSelected() {
		return userIds;
	}
	
	public List<String> getGroupsSelected() {
		return groupIds;
	}
	
	public void resetSelected() {
		userIds.clear();
		groupIds.clear();
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
