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
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.RobotoCheckBox;

public class InviteUserAdapter extends BaseAdapter {

	private Context mContext;
	private List<User> data = new ArrayList<User>();

	private ImageLoader imageLoader;

	public InviteUserAdapter(Context context, Collection<User> users) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = new ImageLoader(context);
		imageLoader.setDefaultImage(R.drawable.default_user_image);
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<User> list) {
		data = list;
		notifyDataSetChanged();
	}

	public void addData(List<User> list) {
		data.addAll(list);
		notifyDataSetChanged();
	}

	public List<User> getData() {
		return data;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public User getItem(int position) {
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

		User user = getItem(position);

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		imageLoader.displayImage(getContext(), user.getImage_thumb(), holder.profileImg);
		holder.personName.setText(user.getFirstName() + " " + user.getLastName());

		if (user.isSelected() || user.isMember()) {
			holder.isSelected.setChecked(true);
		} else {
			holder.isSelected.setChecked(false);
		}

		if (user.isMember()) {
			holder.isSelected.setClickable(false);
		} else {
			holder.isSelected.setClickable(true);
			holder.isSelected.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (data.get(position).isSelected()) {
						data.get(position).setSelected(false);
					} else {
						data.get(position).setSelected(true);
					}
				}
			});
		}

		return convertView;
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
