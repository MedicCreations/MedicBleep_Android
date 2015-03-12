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

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.GlobalModel.Type;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.octo.android.robospice.SpiceManager;

public class GlobalModelAdapter extends BaseAdapter {

	private Context mContext;
	private List<GlobalModel> data = new ArrayList<GlobalModel>();

	private ImageLoaderSpice imageLoaderSpice;

	public GlobalModelAdapter(SpiceManager manager, Context context, Collection<GlobalModel> users, int defaultImage) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoaderSpice = ImageLoaderSpice.getInstance(context);
		imageLoaderSpice.setSpiceManager(manager);
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

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_global_model, parent, false);

			holder = new ViewHolderCharacter(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderCharacter) convertView.getTag();
		}

		// set image to null
		holder.itemImage.setImageDrawable(null);

		if (position % 2 == 0) {
			holder.itemLayout.setBackgroundColor(getContext().getResources().getColor(R.color.gray_in_adapter));
		} else {
			holder.itemLayout.setBackgroundColor(Color.WHITE);
		}

		final GlobalModel item = getItem(position);

		imageLoaderSpice.displayImage(holder.itemImage, item.getImageThumb(), ImageLoaderSpice.DEFAULT_GROUP_IMAGE);
		((RoundImageView) holder.itemImage).setBorderColor(convertView.getContext().getResources().getColor(R.color.light_light_gray));

		if (item.type == Type.USER) {
			holder.itemName.setText(((User) getItem(position).getModel()).getFirstName() + " " + ((User) getItem(position).getModel()).getLastName());
		} else if (item.type == Type.CHAT) {
			holder.itemName.setText(((Chat) item.getModel()).chat_name);
		} else if (item.type == Type.GROUP) {
			holder.itemName.setText(((Group) item.getModel()).getGroupName());
		}

		if (SpikaEnterpriseApp.getSharedPreferences(mContext).getCustomBoolean(String.valueOf(item.getId()))) {
			holder.missedLayout.setVisibility(View.VISIBLE);
		} else {
			holder.missedLayout.setVisibility(View.GONE);
		}

		return convertView;
	}

	public class ViewHolderCharacter {

		public RelativeLayout itemLayout;
		public ImageView itemImage;
		public RobotoRegularTextView itemName;
		public RelativeLayout missedLayout;

		public ViewHolderCharacter(View view) {

			itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
			itemImage = (ImageView) view.findViewById(R.id.item_image);
			itemName = (RobotoRegularTextView) view.findViewById(R.id.item_name);
			missedLayout = (RelativeLayout) view.findViewById(R.id.missedLayout);
		}
	}

}
