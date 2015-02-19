package com.clover.spika.enterprise.chat.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.GlobalModel;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class GroupsAdapter extends BaseAdapter {

	private Context mContext;
	private List<GlobalModel> data = new ArrayList<GlobalModel>();

	private ImageLoader imageLoader;

	public GroupsAdapter(Context context, Collection<GlobalModel> users, int defaultImage) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(R.drawable.default_group_image);
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
	
	public List<GlobalModel> getData(){
		return data;
	}
	
	public void manageData(String manageWith, List<GlobalModel> allData){
		data.clear();
		data.addAll(allData);
		for (int i = 0; i < data.size(); i++) {
			if (((Chat) data.get(i).getModel()).getChat_name().toLowerCase(Locale.getDefault()).contains(manageWith.toLowerCase())) {
				continue;
			} else {
				data.remove(i);
				i--;
			}
		}
		this.notifyDataSetChanged();
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
		holder.groupImage.setImageDrawable(null);
		imageLoader.setDefaultImage(R.drawable.default_group_image);
		
		GlobalModel item = getItem(position);

		imageLoader.displayImage(getContext(), item.getImageThumb(), holder.groupImage);
		((RoundImageView)holder.groupImage).setBorderColor(convertView.getContext().getResources().getColor(R.color.light_light_gray));
		holder.groupName.setText(((Chat) item.getModel()).getChat_name());

		return convertView;
	}

	public class ViewHolderCharacter {

		public ImageView groupImage;
		public RobotoRegularTextView groupName;
		public RobotoRegularTextView lastMessage;

		public ViewHolderCharacter(View view) {

			groupImage = (ImageView) view.findViewById(R.id.item_image);
			groupName = (RobotoRegularTextView) view.findViewById(R.id.item_name);
			
		}

	}

}