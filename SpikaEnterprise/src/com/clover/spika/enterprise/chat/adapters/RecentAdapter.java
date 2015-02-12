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
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;

public class RecentAdapter extends BaseAdapter {

	private Context mContext;
	private List<Chat> data = new ArrayList<Chat>();

	private ImageLoader imageLoader;

	public RecentAdapter(Context context, Collection<Chat> users, boolean isUsers) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoader = ImageLoader.getInstance(context);
		if (isUsers) {
			imageLoader.setDefaultImage(R.drawable.default_user_image);
		} else {
			imageLoader.setDefaultImage(R.drawable.default_group_image);
		}
	}

	public Context getContext() {
		return mContext;
	}

	public void setData(List<Chat> list) {
		data = list;
		notifyDataSetChanged();
	}

	public void addData(List<Chat> list) {
		data.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Chat getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	public boolean incrementUnread(String chatId) {
		
		boolean isFound = false;

		int finalChatId = 0;

		try {
			finalChatId = Integer.valueOf(chatId);
		} catch (Exception e) {
			return true;
		}

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getId() == finalChatId) {

				isFound = true;
				
				int ureadInt = 0;

				try {
					ureadInt = Integer.valueOf(data.get(i).getUnread());
				} catch (Exception ignore) {
				}

				ureadInt = ureadInt + 1;

				data.get(i).setUnread(String.valueOf(ureadInt));
				
				Chat chat = data.get(i);
				data.remove(i);
				data.add(0, chat);
				
				notifyDataSetChanged();
				break;
			}
		}
		
		return isFound;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderCharacter holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_lobby, parent, false);

			holder = new ViewHolderCharacter(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderCharacter) convertView.getTag();
		}

		// set image to null
		holder.recentImage.setImageDrawable(null);

		imageLoader.displayImage(getContext(), getItem(position).getImageThumb(), holder.recentImage);
		holder.recentName.setText(getItem(position).getChat_name());
		holder.lastMessage.setText("last message");

		if (Integer.parseInt(getItem(position).getUnread()) > 0) {
			holder.unreadText.setVisibility(View.VISIBLE);
			holder.unreadText.setText(getItem(position).getUnread());
		} else {
			holder.unreadText.setVisibility(View.INVISIBLE);
			holder.unreadText.setText("");
		}

		return convertView;
	}

	public class ViewHolderCharacter {

		public ImageView recentImage;
		public RobotoRegularTextView recentName;
		public TextView unreadText;
		public RobotoRegularTextView lastMessage;

		public ViewHolderCharacter(View view) {

			recentImage = (ImageView) view.findViewById(R.id.recentImage);
			recentName = (RobotoRegularTextView) view.findViewById(R.id.recentName);
			unreadText = (TextView) view.findViewById(R.id.unreadText);
			lastMessage = (RobotoRegularTextView) view.findViewById(R.id.lastMessage);
			
		}

	}

}