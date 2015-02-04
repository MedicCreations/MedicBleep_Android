package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class PeopleAdapter extends BaseAdapter {

	private Context mContext;
	private List<GlobalModel> data = new ArrayList<GlobalModel>();

	private ImageLoader imageLoader;

	public PeopleAdapter(Context context, Collection<GlobalModel> users, int defaultImage) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(defaultImage);
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<GlobalModel> list) {
		data = list;
		notifyDataSetChanged();
	}

	public void addData(List<GlobalModel> list) {
		data.addAll(list);
		notifyDataSetChanged();
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

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_people, parent, false);

			holder = new ViewHolderCharacter(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderCharacter) convertView.getTag();
		}

		// set image to null
		holder.itemImage.setImageDrawable(null);

		GlobalModel item = getItem(position);

		imageLoader.displayImage(getContext(), item.getImageThumb(), holder.itemImage);

		holder.itemName.setText(((User) getItem(position).getModel()).getFirstName() + " " + ((User) getItem(position).getModel()).getLastName());

		return convertView;
	}

	public class ViewHolderCharacter {

		public ImageView itemImage;
		public RobotoRegularTextView itemName;

		public ViewHolderCharacter(View view) {
			itemImage = (ImageView) view.findViewById(R.id.item_image);
			itemName = (RobotoRegularTextView) view.findViewById(R.id.item_name);
		}
	}

}
