package com.clover.spika.enterprise.chat.adapters;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
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
import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.VideoActivity;
import com.clover.spika.enterprise.chat.VoiceActivity;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.MessageSorting;

public class MessagesAdapter extends BaseAdapter {

	private Context ctx;
	private List<Message> data;
	private String seenBy = "";

	private SparseIntArray dateSeparator = new SparseIntArray();

	private ImageLoader imageLoader;

	private boolean endOfSearch = false;
	private int totalCount = 0;
	private boolean isJellyBean = true;

	public MessagesAdapter(Context context, List<Message> arrayList) {
		this.ctx = context;
		this.data = arrayList;

		imageLoader = new ImageLoader(context);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			isJellyBean = true;
		} else {
			isJellyBean = false;
		}
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

	@SuppressWarnings("deprecation")
	@SuppressLint({ "InflateParams", "NewApi" })
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
		if (isJellyBean) {
			holder.loading_bar_img.setBackgroundDrawable(null);
		} else {
			holder.loading_bar_img.setBackground(null);
		}

		holder.meMsgLayout.setVisibility(View.GONE);
		holder.youMsgLayout.setVisibility(View.GONE);

		holder.meMsgContent.setVisibility(View.GONE);
		holder.youMsgContent.setVisibility(View.GONE);

		holder.meViewImage.setVisibility(View.GONE);
		holder.youViewImage.setVisibility(View.GONE);

		holder.meListenSound.setVisibility(View.GONE);
		holder.youListenSound.setVisibility(View.GONE);

		holder.meWatchVideo.setVisibility(View.GONE);
		holder.youWatchVideo.setVisibility(View.GONE);

		holder.meViewLocation.setVisibility(View.GONE);
		holder.youViewLocation.setVisibility(View.GONE);

		holder.meFileLayout.setVisibility(View.GONE);
		holder.youFileLayout.setVisibility(View.GONE);

		holder.loading_bar.setVisibility(View.GONE);

		// Assign values
		final Message msg = (Message) getItem(position);

		if (msg.isMe()) {
			// My chat messages

			holder.meMsgLayout.setVisibility(View.VISIBLE);

			setItemBackground(position, holder.meMsgLayout);

			holder.meMsgTime.setText(getCreatedTime(msg.getCreated()));
			holder.mePersonName.setText(msg.getFirstname() + " " + msg.getLastname());

			if (msg.getType() == Const.MSG_TYPE_DEFAULT) {
				holder.meMsgContent.setVisibility(View.VISIBLE);
				holder.meMsgContent.setText(msg.getText());
			} else if (msg.getType() == Const.MSG_TYPE_PHOTO) {

				if (!msg.getThumb_id().equals((String) holder.meViewImage.getTag())) {
					holder.meViewImage.setImageDrawable(null);
					imageLoader.displayImage(ctx, msg.getThumb_id(), holder.meViewImage);
					holder.meViewImage.setTag(msg.getThumb_id());
				}

				holder.meViewImage.setVisibility(View.VISIBLE);
				holder.meViewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_VIDEO) {
				holder.meWatchVideo.setVisibility(View.VISIBLE);
				holder.meWatchVideo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VideoActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_LOCATION) {
				holder.meViewLocation.setVisibility(View.VISIBLE);
				holder.meViewLocation.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							Intent intent = new Intent(ctx, LocationActivity.class);
							intent.putExtra(Const.LATITUDE, Double.valueOf(msg.getLatitude()));
							intent.putExtra(Const.LONGITUDE, Double.valueOf(msg.getLongitude()));
							ctx.startActivity(intent);
						}
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_VOICE) {
				holder.meListenSound.setVisibility(View.VISIBLE);
				holder.meListenSound.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VoiceActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.meFileLayout.setVisibility(View.VISIBLE);
				holder.meFileName.setText(msg.getText());
				holder.meDownloadFile.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							new FileManageApi().startFileDownload(msg.getText(), msg.getFile_id(), Integer.valueOf(msg.getId()), ctx);
						}
					}
				});

			}
		} else {
			// Chat member messages, not mine

			holder.youMsgLayout.setVisibility(View.VISIBLE);

			setItemBackground(position, holder.youMsgLayout);

			holder.youMsgTime.setText(getCreatedTime(msg.getCreated()));
			holder.youPersonName.setText(msg.getFirstname() + " " + msg.getLastname());

			if (msg.getType() == Const.MSG_TYPE_DEFAULT) {
				holder.youMsgContent.setVisibility(View.VISIBLE);
				holder.youMsgContent.setText(msg.getText());
			} else if (msg.getType() == Const.MSG_TYPE_PHOTO) {

				if (!msg.getThumb_id().equals((String) holder.youViewImage.getTag())) {
					holder.youViewImage.setImageDrawable(null);
					imageLoader.displayImage(ctx, msg.getThumb_id(), holder.youViewImage);
					holder.youViewImage.setTag(msg.getThumb_id());
				}

				holder.youViewImage.setVisibility(View.VISIBLE);
				holder.youViewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_VIDEO) {

				holder.youWatchVideo.setVisibility(View.VISIBLE);
				holder.youWatchVideo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VideoActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});

			} else if (msg.getType() == Const.MSG_TYPE_LOCATION) {

				holder.youViewLocation.setVisibility(View.VISIBLE);
				holder.youViewLocation.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							Intent intent = new Intent(ctx, LocationActivity.class);
							intent.putExtra(Const.LATITUDE, Double.valueOf(msg.getLatitude()));
							intent.putExtra(Const.LONGITUDE, Double.valueOf(msg.getLongitude()));
							ctx.startActivity(intent);
						}
					}
				});

			} else if (msg.getType() == Const.MSG_TYPE_VOICE) {

				holder.youListenSound.setVisibility(View.VISIBLE);
				holder.youListenSound.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VoiceActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						ctx.startActivity(intent);
					}
				});

			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.youFileLayout.setVisibility(View.VISIBLE);
				holder.youFileName.setText(msg.getText());
				holder.youDownloadFile.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							new FileManageApi().startFileDownload(msg.getText(), msg.getFile_id(), Integer.valueOf(msg.getId()), ctx);
						}
					}
				});

			}
		}

		// Date separator
		if (dateSeparator.get(getDayTimeStamp(msg.getCreated())) != position) {
			holder.dateSeparator.setVisibility(View.GONE);
		} else {
			holder.dateSeparator.setVisibility(View.VISIBLE);
			holder.sectionDate.setText(getSectionDate(msg.getCreated()));
		}

		// Paging animation
		if (position == (0) && !endOfSearch) {
			holder.loading_bar.setVisibility(View.VISIBLE);

			Helper.startPaggingAnimation(ctx, holder.loading_bar_img, isJellyBean);

			if (ctx instanceof ChatActivity) {
				((ChatActivity) ctx).getMessages(false, false, true, false, false, false);
			}
		}
		
		//Check if last message
		if(position == (getCount() - 1) && !TextUtils.isEmpty(seenBy)){
			holder.seenByTv.setText("Seen by " + seenBy);
			holder.seenByTv.setVisibility(View.VISIBLE);
		}else{
			holder.seenByTv.setVisibility(View.GONE);
		}

		return convertView;
	}

	private boolean isMe(String userId) {

		if (Helper.getUserId(ctx).equals(userId)) {
			return true;
		}

		return false;
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

	private String getSectionDate(String createdString) {
		long created = Long.parseLong(createdString) * 1000;

		Date date = new Date(created);
		SimpleDateFormat dateFormat = new SimpleDateFormat(Const.DEFAULT_DATE_FORMAT, Locale.getDefault());

		String rez = dateFormat.format(date);

		return rez;
	}

	private int getDayTimeStamp(String created) {
		try {
			String sDate = getSectionDate(created);
			SimpleDateFormat format = new SimpleDateFormat(Const.DEFAULT_DATE_FORMAT, Locale.getDefault());
			Date oDate = format.parse(sDate);
			int iDate = (int) oDate.getTime();

			return iDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	public void setSeenBy(String seenBy){
		this.seenBy = seenBy;
	}

	public void addItems(List<Message> newItems, boolean isNew) {

		if (isNew) {
			Message msg;
			for (int i = 0; i < newItems.size(); i++) {
				boolean isFound = false;
				for (int j = 0; j < data.size(); j++) {
					if (newItems.get(i).getId().equals(data.get(j).getId())) {
						isFound = true;
						if (Long.parseLong(newItems.get(i).getModified()) > Long.parseLong(data.get(j).getModified())) {
							msg = newItems.get(i);
							msg.setMe(isMe(newItems.get(i).getUser_id()));
							msg = decryptContent(newItems.get(i));
							data.set(j, newItems.get(i));
						}
					}
				}

				if (!isFound) {
					msg = newItems.get(i);
					msg.setMe(isMe(newItems.get(i).getUser_id()));
					msg = decryptContent(newItems.get(i));
					data.add(msg);
				}
			}
		} else {
			Message msg;
			for (int i = 0; i < newItems.size(); i++) {
				msg = newItems.get(i);
				msg.setMe(isMe(newItems.get(i).getUser_id()));
				msg = decryptContent(newItems.get(i));
				newItems.set(i, msg);
			}
			data.addAll(newItems);
		}

		Collections.sort(data, new MessageSorting());
		addSeparatorDate();
		this.notifyDataSetChanged();
	}

	private Message decryptContent(Message msg) {

		switch (msg.getType()) {

		case Const.MSG_TYPE_DEFAULT:
		case Const.MSG_TYPE_FILE:

			try {
				msg.setText(JNAesCrypto.decryptJN(msg.getText()));
			} catch (Exception e) {
				e.printStackTrace();
				msg.setText(ctx.getResources().getString(R.string.e_error_not_decrypted));
				msg.setFailed(true);
			}

			break;

		case Const.MSG_TYPE_LOCATION:

			try {
				msg.setLongitude(JNAesCrypto.decryptJN(msg.getLongitude()));
				msg.setLatitude(JNAesCrypto.decryptJN(msg.getLatitude()));
			} catch (Exception e) {
				e.printStackTrace();
				msg.setText(ctx.getResources().getString(R.string.e_error_not_decrypted));
				msg.setFailed(true);
			}

			break;

		default:
			break;
		}

		return msg;
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

	private void setItemBackground(int position, View view) {
		if (position % 2 == 0) {
			view.setBackgroundColor(ctx.getResources().getColor(R.color.gray_in_adapter));
		} else {
			view.setBackgroundColor(Color.WHITE);
		}
	}

	public class ViewHolderChatMsg {

		// start: message item for my message
		public LinearLayout meMsgLayout;
		public TextView mePersonName;
		public TextView meMsgContent;
		public TextView meMsgTime;
		// end: me msg

		public ImageView meListenSound;
		public ImageView youListenSound;

		public ImageView meWatchVideo;
		public ImageView youWatchVideo;

		public ImageView meViewLocation;
		public ImageView youViewLocation;

		public ImageView meViewImage;
		public ImageView youViewImage;

		public RelativeLayout meFileLayout;
		public TextView meFileName;
		public ImageView meDownloadFile;

		public RelativeLayout youFileLayout;
		public TextView youFileName;
		public ImageView youDownloadFile;
		
		public TextView seenByTv;

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

		// start: date separator
		public RelativeLayout dateSeparator;
		public TextView sectionDate;

		// end: date separator

		public ViewHolderChatMsg(View view) {

			meMsgLayout = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
			// start: message item for my message
			meMsgTime = (TextView) view.findViewById(R.id.timeMe);
			mePersonName = (TextView) view.findViewById(R.id.mePersonName);
			meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
			// end: me msg

			meListenSound = (ImageView) view.findViewById(R.id.meListenSound);
			youListenSound = (ImageView) view.findViewById(R.id.youListenSound);

			meWatchVideo = (ImageView) view.findViewById(R.id.meWatchVideo);
			youWatchVideo = (ImageView) view.findViewById(R.id.youWatchVideo);

			meViewLocation = (ImageView) view.findViewById(R.id.meViewLocation);
			youViewLocation = (ImageView) view.findViewById(R.id.youViewLocation);

			meViewImage = (ImageView) view.findViewById(R.id.meViewImage);
			youViewImage = (ImageView) view.findViewById(R.id.youViewImage);

			meFileLayout = (RelativeLayout) view.findViewById(R.id.meFileLayout);
			meFileName = (TextView) view.findViewById(R.id.meFileName);
			meDownloadFile = (ImageView) view.findViewById(R.id.meDownloadFile);

			youFileLayout = (RelativeLayout) view.findViewById(R.id.youFileLayout);
			youFileName = (TextView) view.findViewById(R.id.youFileName);
			youDownloadFile = (ImageView) view.findViewById(R.id.youDownloadFile);
			
			seenByTv = (TextView) view.findViewById(R.id.tvSeenBy);

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

			// start: date separator
			dateSeparator = (RelativeLayout) view.findViewById(R.id.dateSeparator);
			sectionDate = (TextView) view.findViewById(R.id.sectionDate);
			// end: date separator
		}
	}
}