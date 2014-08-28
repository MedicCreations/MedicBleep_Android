package com.clover.spika.enterprise.chat.adapters;

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
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Group;

import java.util.ArrayList;
import java.util.List;

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

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(R.drawable.default_group_image);
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

		imageLoader.displayImage(cntx, group.getImage_thumb(), holder.talkImg);
		// holder.talkImg.setImageResource(R.drawable.skiper);

		holder.talkName.setText(group.getGroupName());

		if (SpikaEnterpriseApp.getSharedPreferences(cntx).getCustomBoolean(String.valueOf(group.getId()))) {
			holder.missedLayout.setVisibility(View.VISIBLE);
		} else {
			holder.missedLayout.setVisibility(View.GONE);
		}

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(cntx.getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

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

		public ViewHolderGroup(View view) {

			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
			clickLayout = (RelativeLayout) view.findViewById(R.id.clickLayout);
			talkImg = (ImageView) view.findViewById(R.id.groupImage);
			talkName = (TextView) view.findViewById(R.id.groupName);

			missedLayout = (RelativeLayout) view.findViewById(R.id.missedLayout);
			missedtext = (TextView) view.findViewById(R.id.missedtext);

		}
	}

}