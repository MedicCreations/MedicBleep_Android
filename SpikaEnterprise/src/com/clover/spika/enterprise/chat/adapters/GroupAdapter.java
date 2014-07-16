package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class GroupAdapter extends BaseAdapter {

	public Context cntx;
	public List<Group> data;
	public List<String> selected = new ArrayList<String>();
	public List<String> deselected = new ArrayList<String>();
	private String newGroupPeriod = null;

	int radius = 0;

	ImageLoader imageLoader;

	public GroupAdapter(Context context, List<Group> arrayList) {
		this.cntx = context;
		this.data = arrayList;

		imageLoader = new ImageLoader(context);

	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderGroup holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(cntx).inflate(R.layout.item_group, parent, false);

			holder = new ViewHolderGroup(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderGroup) convertView.getTag();
		}

		// set image to null
		holder.talkImg.setImageDrawable(null);

		// Assign values
		final Group group = (Group) getItem(position);

//		imageLoader.displayImage(cntx, group.getImage_name(), holder.talkImg, true);
		holder.talkImg.setImageResource(R.drawable.skiper);
		
		holder.talkName.setText(Helper.substringText(group.getGroup_name(), 25));

		String[] groupData = { group.getGroupId(), group.getOwner_id(), group.getGroup_name() };
		holder.clickLayout.setTag(groupData);

		if (SpikaEnterpriseApp.getSharedPreferences(cntx).getCustomBoolean(group.getGroupId())) {
			holder.missedLayout.setVisibility(View.VISIBLE);
		} else {
			holder.missedLayout.setVisibility(View.GONE);
		}
		
		if(position % 2 == 0){
			holder.itemLayout.setBackgroundColor(cntx.getResources().getColor(R.color.gray_in_adapter));
		}else{
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		holder.clickLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				String[] groupData = (String[]) view.getTag();

				Intent intent = new Intent(cntx, ChatActivity.class);
				intent.putExtra(Const.GROUP_ID, groupData[0]);
				intent.putExtra(Const.OWNER_ID, groupData[1]);
				intent.putExtra(Const.GROUP_NAME, groupData[2]);
				((Activity) cntx).startActivity(intent);
			}
		});

		return convertView;
	}

	@Override
	public Group getItem(int position) {
		return data.get(position);
	}

	public void addItems(List<Group> newItems) {
		data.addAll(newItems);
		this.notifyDataSetChanged();
	}

	public void clearItems() {
		data.clear();
		notifyDataSetChanged();
	}

	public List<Group> getData() {
		return this.data;
	}

	@Override
	public int getCount() {
		return this.data.size();
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public String getNewGroupPeriod() {
		return newGroupPeriod;
	}

	public void setNewGroupPeriod(String newGroupPeriod) {
		this.newGroupPeriod = newGroupPeriod;
	}

	public class ViewHolderGroup {

		public RelativeLayout itemLayout;
		public RelativeLayout clickLayout;
		public ImageView talkImg;
		public TextView talkName;

		public RelativeLayout missedLayout;
		public TextView missedtext;

		public ImageView goImage;

		public ViewHolderGroup(View view) {

			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
			clickLayout = (RelativeLayout) view.findViewById(R.id.clickLayout);
			talkImg = (ImageView) view.findViewById(R.id.groupImage);
			talkName = (TextView) view.findViewById(R.id.groupName);

			missedLayout = (RelativeLayout) view.findViewById(R.id.missedLayout);
			missedtext = (TextView) view.findViewById(R.id.missedtext);

			goImage = (ImageView) view.findViewById(R.id.goImage);
		}
	}

}