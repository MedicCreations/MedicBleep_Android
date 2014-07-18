package com.clover.spika.enterprise.chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.ProfileActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.MessageSorting;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends BaseAdapter {

	private Context cntx;
	private List<Message> data;
	private List<String> myIds = new ArrayList<String>();
	private SparseIntArray dateSeparator = new SparseIntArray();

	ImageLoader imageLoader;

	int radius = 0;

	private String groupId = "";

	private boolean endOfSearch = false;
	private boolean isScrolling = false;
	private int totalItem = 0;

	public MessagesAdapter(Context context, List<Message> arrayList) {
		this.cntx = context;
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

			convertView = LayoutInflater.from(cntx).inflate(R.layout.item_chat_main, parent);

			holder = new ViewHolderChatMsg(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderChatMsg) convertView.getTag();
		}

		// set image to null
		holder.meIcon.setImageDrawable(null);
		holder.youIcon.setImageDrawable(null);
		holder.imagePreviewYou.setImageDrawable(null);
		holder.imagePreviewMe.setImageDrawable(null);
		holder.loading_bar_img.setBackgroundDrawable(null);

		holder.defaultMsgLayoutMe.setVisibility(View.GONE);
		holder.imageMsgLayoutMe.setVisibility(View.GONE);
		holder.imageMsgLayoutYou.setVisibility(View.GONE);
		holder.settingsLayoutMe.setVisibility(View.GONE);
		holder.meItemMsgLayout.setVisibility(View.GONE);
		holder.youItemMsgLayout.setVisibility(View.GONE);

		holder.loading_bar.setVisibility(View.GONE);

		// Assign values
		final Message msg = (Message) getItem(position);

		// final boolean me = isMe(msg.getCharacter().getCharacterId());
		final boolean me = true;

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

			holder.meItemMsgLayout.setVisibility(View.VISIBLE);

			if (msg.getType() == 0) {
				holder.defaultMsgLayoutMe.setVisibility(View.VISIBLE);
				// holder.mePersonName.setText(msg.getCharacter().getUsername());
				holder.meMsgContent.setText(msg.getText());
			} else if (msg.getType() == 1) {
				holder.imageMsgLayoutMe.setVisibility(View.VISIBLE);
				// holder.mePersonNameImage.setText(msg.getCharacter().getUsername());

				if (!isScrolling) {
					imageLoader.displayImage(cntx, msg.getFile_id(), holder.imagePreviewMe, false);
				}

				holder.imagePreviewMe.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(cntx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE_NAME, msg.getFile_id());
						cntx.startActivity(intent);
					}
				});
			}

			holder.settingsLayoutMe.setVisibility(View.VISIBLE);

			holder.timeMe.setText(getTime(msg.getCreated()));

			holder.meIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(cntx, ProfileActivity.class);
					// intent.putExtra(Const.USER_IMAGE_NAME,
					// msg.getCharacter().getImage_name());
					// intent.putExtra(Const.USER_NICKNAME,
					// msg.getCharacter().getUsername());

					cntx.startActivity(intent);
				}
			});
		} else {

			holder.youItemMsgLayout.setVisibility(View.VISIBLE);

			if (msg.getType() == 0) {
				holder.defaultMsgLayoutYou.setVisibility(View.VISIBLE);
				holder.imageMsgLayoutYou.setVisibility(View.GONE);
				holder.youMsgContent.setText(msg.getText());
				// holder.youPersonName.setText(msg.getCharacter().getUsername());
			} else if (msg.getType() == 1) {
				holder.defaultMsgLayoutYou.setVisibility(View.GONE);
				holder.imageMsgLayoutYou.setVisibility(View.VISIBLE);
				// holder.youPersonNameImage.setText(msg.getCharacter().getUsername());

				if (!isScrolling) {
					imageLoader.displayImage(cntx, msg.getFile_id(), holder.imagePreviewYou, false);
				}

				holder.imagePreviewYou.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(cntx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE_NAME, msg.getFile_id());
						cntx.startActivity(intent);
					}
				});
			}

			holder.timeYou.setText(getTime(msg.getCreated()));

			holder.reportMsgYou.setVisibility(View.VISIBLE);

			holder.youIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(cntx, ProfileActivity.class);
					// intent.putExtra(Const.USER_IMAGE_NAME,
					// msg.getCharacter().getImage_name());
					// intent.putExtra(Const.USER_NICKNAME,
					// msg.getCharacter().getUsername());

					cntx.startActivity(intent);
				}
			});
		}

		if (dateSeparator.get(getDayTimeStamp(msg.getCreated())) != position) {
			holder.dateSeparator.setVisibility(View.GONE);
		} else {
			holder.dateSeparator.setVisibility(View.VISIBLE);
			holder.dateSeparator.setBackgroundColor(cntx.getResources().getColor(R.color.transparent));
			holder.sectionDate.setText(getSectionDate(msg.getCreated()));
		}

		if (position == (0) && !endOfSearch) {
			holder.loading_bar.setVisibility(View.VISIBLE);

			Helper.startPaggingAnimation(cntx, holder.loading_bar_img);

			if (cntx instanceof ChatActivity) {
				((ChatActivity) cntx).getMessages(false, false, true, false, false, false);
			}
		}

		return convertView;
	}

	private String getTime(String createdString) {

		long created = Long.parseLong(createdString) * 1000;

		Date date = new Date(created);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);

		String time = dateFormat.format(date);

		return time;
	}

	private String getSectionDate(String createdString) {
		long created = Long.parseLong(createdString) * 1000;

		Date date = new Date(created);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd. - EEEE", Locale.US);

		String rez = dateFormat.format(date);

		return rez;
	}

	private void addSeparatorDate() {

		dateSeparator.clear();

		for (int i = 0; i < data.size(); i++) {

			int key = getDayTimeStamp(data.get(i).getCreated());
			int current = dateSeparator.get(key, -1);

			if (current == -1) {
				dateSeparator.put(key, i);
			}
		}
	}

	private Integer getDayTimeStamp(String created) {
		try {
			String sDate = getSectionDate(created);
			SimpleDateFormat format = new SimpleDateFormat("MM.dd. - EEEE", Locale.US);
			Date oDate = format.parse(sDate);
			Integer iDate = (int) oDate.getTime();

			return iDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
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
		addSeparatorDate();
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
			setTotalItem(--totalItem);
			notifyDataSetChanged();
		}
	}

	public void addTempMsg(Message msg) {
		data.add(msg);
		addSeparatorDate();
		this.notifyDataSetChanged();
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

	public int getTotalItem() {
		return totalItem;
	}

	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;

		if (getCount() >= totalItem) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
	}

	public List<String> getMyIds() {
		return myIds;
	}

	public void setMyIds(List<String> myIds) {
		this.myIds = myIds;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void likeMessage(final String msgId) {
		new BaseAsyncTask<Void, Void, Integer>(cntx, true) {

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.FUNCTION, Const.F_RATE_MESSAGE);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.MESSAGE_ID, msgId);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setSucceed();
					if (cntx instanceof ChatActivity) {
						((ChatActivity) cntx).getMessages(false, false, false, true, true, true);
					}
				} else {
					AppDialog dialog = new AppDialog(cntx, false);
					dialog.setFailed(Helper.errorDescriptions(cntx, result));
				}
			};

		}.execute();
	}

	public void reportMessage(final String msgId) {
		new BaseAsyncTask<Void, Void, Integer>(cntx, true) {

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.FUNCTION, Const.F_REPORT_MESSAGE);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.MESSAGE_ID, msgId);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setSucceed();
					if (cntx instanceof ChatActivity) {
						((ChatActivity) cntx).getMessages(false, false, false, true, true, true);
					}
				} else {
					AppDialog dialog = new AppDialog(cntx, false);
					dialog.setFailed(Helper.errorDescriptions(cntx, result));
				}
			};

		}.execute();
	}

	public boolean isScrolling() {
		return isScrolling;
	}

	public void setScrolling(boolean isScrolling) {
		this.isScrolling = isScrolling;
	}

	public class ViewHolderChatMsg {

		public RelativeLayout dateSeparator;
		public TextView sectionDate;

		public LinearLayout defaultMsgLayoutMe;

		// start: message item for my message
		public RelativeLayout meIconLayout;
		public ImageView meIcon;
		public RelativeLayout meItemMsgLayout;
		public TextView mePersonName;
		public TextView meMsgContent;
		public TextView timeMe;
		public RelativeLayout settingsLayoutMe;
		// end: me msg

		// start: me image msg
		public LinearLayout imageMsgLayoutMe;
		public TextView mePersonNameImage;
		public ImageView imagePreviewMe;
		public TextView imgDescriptionMe;
		// end: me image msg

		// start: message item for you message
		public RelativeLayout youIconLayout;
		public ImageView youIcon;
		public RelativeLayout youItemMsgLayout;
		public RelativeLayout defaultMsgLayoutYou;
		public TextView youPersonName;
		public TextView youMsgContent;
		public TextView timeYou;
		// end: you msg

		// start: you image msg
		public RelativeLayout imageMsgLayoutYou;
		public TextView youPersonNameImage;
		public ImageView imagePreviewYou;
		public TextView imgDescriptionYou;
		// end: you image msg

		// start: options layout
		public TextView likeYou;
		public TextView likeNbrYou;
		public TextView reportMsgYou;
		public TextView likeMe;
		public TextView likeNbrMe;
		// end: options layout

		// start: loading bar
		public RelativeLayout loading_bar;
		public ImageView loading_bar_img;

		// end: loading bar

		public ViewHolderChatMsg(View view) {

			dateSeparator = (RelativeLayout) view.findViewById(R.id.dateSeparator);

			// start: message item for my message
			meIconLayout = (RelativeLayout) view.findViewById(R.id.meIconLayout);
			meIcon = (ImageView) view.findViewById(R.id.meIcon);
			meItemMsgLayout = (RelativeLayout) view.findViewById(R.id.meItemMsgLayout);
			defaultMsgLayoutMe = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
			mePersonName = (TextView) view.findViewById(R.id.mePersonName);
			meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
			timeMe = (TextView) view.findViewById(R.id.timeMe);
			settingsLayoutMe = (RelativeLayout) view.findViewById(R.id.settingsLayoutMe);
			// end: me msg

			// start: me image msg
			imageMsgLayoutMe = (LinearLayout) view.findViewById(R.id.imageMsgLayoutMe);
			mePersonNameImage = (TextView) view.findViewById(R.id.mePersonNameImage);
			imagePreviewMe = (ImageView) view.findViewById(R.id.imagePreviewMe);
			imgDescriptionMe = (TextView) view.findViewById(R.id.imgDescriptionMe);
			// end: me image msg

			// start: message item for you message
			youIconLayout = (RelativeLayout) view.findViewById(R.id.youIconLayout);
			youIcon = (ImageView) view.findViewById(R.id.youIcon);
			youItemMsgLayout = (RelativeLayout) view.findViewById(R.id.youItemMsgLayout);
			defaultMsgLayoutYou = (RelativeLayout) view.findViewById(R.id.defaultMsgLayoutYou);
			youPersonName = (TextView) view.findViewById(R.id.youPersonName);
			youMsgContent = (TextView) view.findViewById(R.id.youMsgContent);
			timeYou = (TextView) view.findViewById(R.id.timeYou);
			// end: you msg

			// start: you image msg
			imageMsgLayoutYou = (RelativeLayout) view.findViewById(R.id.imageMsgLayoutYou);
			youPersonNameImage = (TextView) view.findViewById(R.id.youPersonNameImage);
			imagePreviewYou = (ImageView) view.findViewById(R.id.imagePreviewYou);
			imgDescriptionYou = (TextView) view.findViewById(R.id.imgDescriptionYou);
			// end: you image msg

			// start: options layout
			likeYou = (TextView) view.findViewById(R.id.likeYou);
			likeNbrYou = (TextView) view.findViewById(R.id.likeNbrYou);
			reportMsgYou = (TextView) view.findViewById(R.id.reportMsgYou);
			likeMe = (TextView) view.findViewById(R.id.likeMe);
			likeNbrMe = (TextView) view.findViewById(R.id.likeNbrMe);
			// end: options layout

			sectionDate = (TextView) view.findViewById(R.id.sectionDate);

			// start: loading bar
			loading_bar = (RelativeLayout) view.findViewById(R.id.loading_bar);
			loading_bar_img = (ImageView) view.findViewById(R.id.loading_bar_img);
			// end: loading bar
		}
	}
}