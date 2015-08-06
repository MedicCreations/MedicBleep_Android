package com.medicbleep.app.chat.adapters;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.lazy.ImageLoaderSpice;
import com.medicbleep.app.chat.models.Chat;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.views.RoundImageView;
import com.octo.android.robospice.SpiceManager;

public class RecentAdapter extends BaseAdapter {

	private Context mContext;
	private List<Chat> data = new ArrayList<Chat>();

	private ImageLoaderSpice imageLoaderSpice;
	private int defaultImage = ImageLoaderSpice.NO_IMAGE;

	public RecentAdapter(SpiceManager manager, Context context, Collection<Chat> users, boolean isUsers) {
		this.mContext = context;
		this.data.addAll(users);

		imageLoaderSpice = ImageLoaderSpice.getInstance(context);
		imageLoaderSpice.setSpiceManager(manager);
		if (isUsers) {
			defaultImage = R.drawable.default_user_image;
		} else {
			defaultImage = R.drawable.default_group_image;
		}
	}

	public Context getContext() {
		return mContext;
	}

	public void setSpiceManager(SpiceManager manager) {
		imageLoaderSpice.setSpiceManager(manager);
	}

	public void setData(List<Chat> list) {
		data = list;
		notifyDataSetChanged();
	}

	public void addData(List<Chat> list) {
		data.addAll(list);
		notifyDataSetChanged();
	}

	public List<Chat> getData() {
		return data;
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
					ureadInt = Integer.valueOf(data.get(i).unread);
				} catch (Exception ignore) {
				}

				ureadInt = ureadInt + 1;

				data.get(i).unread = String.valueOf(ureadInt);

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

		if (getItem(position).type == Const.C_PRIVATE) {
			defaultImage = R.drawable.default_user_image;
		} else {
			defaultImage = R.drawable.default_group_image;
		}

		Chat item = getItem(position);

        holder.recentImage.setTag(false);
		imageLoaderSpice.displayImage(holder.recentImage, item.image_thumb, defaultImage);

		holder.recentName.setText(item.chat_name);

		((RoundImageView) holder.recentImage).setBorderColor(convertView.getContext().getResources().getColor(R.color.light_light_gray));

		if (item.last_message != null) {
			switch (item.last_message.getType()) {
			case Const.MSG_TYPE_DEFAULT:
				holder.lastMessage.setText(item.last_message.getText());
				break;
			case Const.MSG_TYPE_DELETED:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.deleted));
				break;
			case Const.MSG_TYPE_FILE:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.file));
				break;
			case Const.MSG_TYPE_GIF:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.smiley));
				break;
			case Const.MSG_TYPE_LOCATION:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.location));
				break;
			case Const.MSG_TYPE_PHOTO:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.photo));
				break;
			case Const.MSG_TYPE_VIDEO:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.video));
				break;
			case Const.MSG_TYPE_VOICE:
				holder.lastMessage.setText(mContext.getResources().getString(R.string.audio));
				break;

			default:
				holder.lastMessage.setText("");
				break;
			}

//			holder.lastMessageTime.setText(getCreatedTime(item.last_message.getCreated()));
            holder.lastMessageTime.setText(item.getTimeLastMessage(mContext.getResources()));
		}else{
            holder.lastMessageTime.setText("");
            holder.lastMessage.setText("");
        }

		if (item.unread != null && Integer.parseInt(item.unread) > 0) {
			holder.unreadText.setVisibility(View.VISIBLE);
			holder.unreadText.setText(item.unread);
		} else {
			holder.unreadText.setVisibility(View.INVISIBLE);
			holder.unreadText.setText("");
		}

        if(position == getCount() - 1){
            ((RelativeLayout.LayoutParams)holder.clickLayout.getLayoutParams()).bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mContext.getResources().getDisplayMetrics());
        }else{
            ((RelativeLayout.LayoutParams)holder.clickLayout.getLayoutParams()).bottomMargin = 0;
        }

		return convertView;
	}

	private String getCreatedTime(String created) {

		try {

			Timestamp stamp = new Timestamp(Long.valueOf(created) * 1000);
			Date date = new Date(stamp.getTime());
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public class ViewHolderCharacter {

		public ImageView recentImage;
		public TextView recentName;
		public TextView unreadText;
		public TextView lastMessage;
		public TextView lastMessageTime;
        public RelativeLayout clickLayout;

		public ViewHolderCharacter(View view) {

			recentImage = (ImageView) view.findViewById(R.id.recentImage);
			recentName = (TextView) view.findViewById(R.id.recentName);
			unreadText = (TextView) view.findViewById(R.id.unreadText);
			lastMessage = (TextView) view.findViewById(R.id.lastMessage);
			lastMessageTime = (TextView) view.findViewById(R.id.lastMessageTime);
            clickLayout = (RelativeLayout) view.findViewById(R.id.clickLayout);
		}

	}

}