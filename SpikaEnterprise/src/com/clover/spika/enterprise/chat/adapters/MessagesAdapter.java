package com.clover.spika.enterprise.chat.adapters;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.MessageSorting;

public class MessagesAdapter extends BaseAdapter {

	private Context ctx;
	private List<Message> data;

	private ImageLoader imageLoader;

	private boolean endOfSearch = false;
	private boolean isScrolling = false;
	private int totalCount = 0;

	public MessagesAdapter(Context context, List<Message> arrayList) {
		this.ctx = context;
		this.data = arrayList;

		imageLoader = new ImageLoader(context);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Message getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolderChatMsg holder;
		if (convertView == null) {

			convertView = LayoutInflater.from(ctx).inflate(R.layout.item_chat_main, null);

			holder = new ViewHolderChatMsg(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderChatMsg) convertView.getTag();
		}

		// set items to null
		holder.loading_bar_img.setBackgroundDrawable(null);
		holder.meMsgLayout.setVisibility(View.GONE);
		holder.youMsgLayout.setVisibility(View.GONE);
		holder.loading_bar.setVisibility(View.GONE);

		// Assign values
		final Message msg = (Message) getItem(position);

		final boolean me = isMe(msg.getUser_id());

		if (!isScrolling) {
			// if (me) {
			// imageLoader.displayImage(cntx,
			// msg.getCharacter().getImage_name(), holder.meIcon, true);
			// } else {
			// imageLoader.displayImage(cntx,
			// msg.getCharacter().getImage_name(), holder.youIcon, true);
			// }
		}

		if (me) {
			// My chat messages

			holder.meMsgLayout.setVisibility(View.VISIBLE);

			if (position % 2 == 0) {
				holder.meMsgLayout.setBackgroundColor(ctx.getResources().getColor(R.color.gray_in_adapter));
			} else {
				holder.meMsgLayout.setBackgroundColor(Color.WHITE);
			}

			holder.meMsgTime.setText(msg.getCreated());
			holder.mePersonName.setText(msg.getFirstname() + " " + msg.getLastname());

			if (msg.getType() == 0) {
				holder.meMsgContent.setText(msg.getText());
			} else if (msg.getType() == 1) {

				// if (!isScrolling) {
				// imageLoader.displayImage(cntx, msg.getFile_id(),
				// holder.imagePreviewMe, false);
				// }
			}
		} else {
			// Chat member messages, not mine

			holder.youMsgLayout.setVisibility(View.VISIBLE);

			if (position % 2 == 0) {
				holder.youMsgLayout.setBackgroundColor(ctx.getResources().getColor(R.color.gray_in_adapter));
			} else {
				holder.youMsgLayout.setBackgroundColor(Color.WHITE);
			}

			holder.youMsgTime.setText(msg.getCreated());
			holder.youPersonName.setText(msg.getFirstname() + " " + msg.getLastname());

			if (msg.getType() == 0) {
				holder.youMsgContent.setText(msg.getText());
			} else if (msg.getType() == 1) {

				// if (!isScrolling) {
				// imageLoader.displayImage(cntx, msg.getFile_id(),
				// holder.imagePreviewYou, false);
				// }
			}
		}

		if (position == (0) && !endOfSearch) {
			holder.loading_bar.setVisibility(View.VISIBLE);

			Helper.startPaggingAnimation(ctx, holder.loading_bar_img);

			if (ctx instanceof ChatActivity) {
				((ChatActivity) ctx).getMessages(false, false, true, false, false, false);
			}
		}

		return convertView;
	}

	private boolean isMe(String userId) {

		if (Helper.getUserId(ctx).equals(userId)) {
			return true;
		}

		return false;
	}

	public void addItems(List<Message> newItems, boolean isNew) {
		
		if (isNew) {
			for (int i = 0; i < newItems.size(); i++) {
				boolean isFound = false;
				for (int j = 0; j < data.size(); j++) {
					if (newItems.get(i).getId().equals(data.get(j).getId())) {
						isFound = true;
						if (Long.parseLong(newItems.get(i).getModified()) > Long.parseLong(data.get(j).getModified())) {
							data.set(j, newItems.get(i));
						}
					}
				}

				if (!isFound) {
					data.add(newItems.get(i));
				}
			}
		} else {
			data.addAll(newItems);
		}

		Collections.sort(data, new MessageSorting());

		this.notifyDataSetChanged();
	}

	public void removeMessage(String msgId) {
		boolean isFound = false;
		int position = 0;

		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getId().equals(msgId)) {
				isFound = true;
				position = i;

				break;
			}
		}

		if (isFound) {
			data.remove(position);
			setTotalCount(--totalCount);
			notifyDataSetChanged();
		}
	}

	public void clearItems() {
		data.clear();
		notifyDataSetChanged();
	}

	public List<Message> getData() {
		return this.data;
	}

	public void setEndOfSearch(boolean value) {
		this.endOfSearch = value;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalItem) {
		this.totalCount = totalItem;
		
		if (getCount() >= totalItem) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
	}

	public boolean isScrolling() {
		return isScrolling;
	}

	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	public class ViewHolderChatMsg {

		// start: message item for my message
		public LinearLayout meMsgLayout;
		public TextView mePersonName;
		public TextView meMsgContent;
		public TextView meMsgTime;
		// end: me msg

		// start: message item for you message
		public LinearLayout youMsgLayout;
		public TextView youPersonName;
		public TextView youMsgContent;
		public TextView youMsgTime;
		// end: you msg

		// start: loading bar
		public RelativeLayout loading_bar;
		public ImageView loading_bar_img;

		// end: loading bar

		public ViewHolderChatMsg(View view) {

			meMsgLayout = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
			// start: message item for my message
			meMsgTime = (TextView) view.findViewById(R.id.timeMe);
			mePersonName = (TextView) view.findViewById(R.id.mePersonName);
			meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
			// end: me msg

			youMsgLayout = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutYou);
			// start: message item for you message
			youMsgTime = (TextView) view.findViewById(R.id.timeYou);
			youPersonName = (TextView) view.findViewById(R.id.youPersonName);
			youMsgContent = (TextView) view.findViewById(R.id.youMsgContent);
			// end: you msg

			// start: loading bar
			loading_bar = (RelativeLayout) view.findViewById(R.id.loading_bar);
			loading_bar_img = (ImageView) view.findViewById(R.id.loading_bar_img);
			// end: loading bar
		}
	}
}