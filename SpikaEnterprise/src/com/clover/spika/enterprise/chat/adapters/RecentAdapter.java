package com.clover.spika.enterprise.chat.adapters;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RoundImageView;

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
		
		((RoundImageView)holder.recentImage).setBorderColor(convertView.getContext().getResources().getColor(R.color.light_light_gray));
		
		switch (getItem(position).getLastMessage().getType()) {
		case Const.MSG_TYPE_DEFAULT:
			holder.lastMessage.setText(getItem(position).getLastMessage().getText());
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
		
		holder.lastMessageTime.setText(getCreatedTime(getItem(position).getLastMessage().getCreated()));

		if (Integer.parseInt(getItem(position).getUnread()) > 0) {
			holder.unreadText.setVisibility(View.VISIBLE);
			holder.unreadText.setText(getItem(position).getUnread());
		} else {
			holder.unreadText.setVisibility(View.INVISIBLE);
			holder.unreadText.setText("");
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
		public RobotoRegularTextView recentName;
		public TextView unreadText;
		public RobotoRegularTextView lastMessage;
		public RobotoRegularTextView lastMessageTime;

		public ViewHolderCharacter(View view) {

			recentImage = (ImageView) view.findViewById(R.id.recentImage);
			recentName = (RobotoRegularTextView) view.findViewById(R.id.recentName);
			unreadText = (TextView) view.findViewById(R.id.unreadText);
			lastMessage = (RobotoRegularTextView) view.findViewById(R.id.lastMessage);
			lastMessageTime = (RobotoRegularTextView) view.findViewById(R.id.lastMessageTime);
			
		}

	}

}