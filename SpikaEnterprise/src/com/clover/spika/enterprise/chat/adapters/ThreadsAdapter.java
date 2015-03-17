package com.clover.spika.enterprise.chat.adapters;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.ThreadsActivity;
import com.clover.spika.enterprise.chat.VideoActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.lazy.GifLoader;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.TreeNode;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.octo.android.robospice.SpiceManager;

import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RoundImageView;

public class ThreadsAdapter extends BaseAdapter {

	private static final int VIEW_TYPE_MESSAGE = 0;
	private static final int VIEW_TYPE_PHOTO = 1;
	private static final int VIEW_TYPE_VIDEO = 2;
	private static final int VIEW_TYPE_LOCATION = 3;
	private static final int VIEW_TYPE_SOUND = 4;
	private static final int VIEW_TYPE_FILE = 5;
	private static final int VIEW_TYPE_DELETED = 6;
	private static final int VIEW_TYPE_GIF = 7;

	private static final int TOTAL_VIEW_TYPES = VIEW_TYPE_GIF + 1;

	private static final int INDENTATION_PADDING = 50;

	private int mSelectedItem = -1;
	private int mMaxIndentLevel = 0;
	private Context mContext;
	private List<TreeNode> mMessageList = new ArrayList<TreeNode>();

	Typeface typeface;
	private int displayWidth = 0;

	// *************SOUND
	private boolean isDownloadingSound = false;
	private MediaPlayer currentMediaPlayer = null;
	private String currentPlayingPath = null;
	private Button activePlayIcon = null;
	private Chronometer activeChronometer = null;
	private SeekBar activeSeekbar = null;
	// *************************

	private ImageLoaderSpice imageLoaderSpice;

	public ThreadsAdapter(SpiceManager manager, Context context) {
		if (context instanceof Activity) {
			this.mContext = context;

			imageLoaderSpice = ImageLoaderSpice.getInstance(context);
			imageLoaderSpice.setSpiceManager(manager);

			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			int maxIndent = dm.widthPixels / 2;
			mMaxIndentLevel = maxIndent / INDENTATION_PADDING;
		} else {
			throw new IllegalArgumentException("Context must be an Activity context to proceed.");
		}
	}

	public void updateContent(List<TreeNode> collection) {
		this.mMessageList.clear();
		this.mMessageList.addAll(collection);

		String thisUserId = Helper.getUserId(mContext);
		for (TreeNode node : mMessageList) {
			Message.decryptContent(mContext, node.getMessage());
			node.getMessage().setMe(node.getMessage().getUser_id().equals(thisUserId));
		}

		notifyDataSetChanged();
	}

	public Context getContext() {
		return this.mContext;
	}

	public void setSelectedItem(int position) {
		this.mSelectedItem = position;
		notifyDataSetInvalidated();
	}

	private int getIndentPadding(int level) {
		level = level > mMaxIndentLevel ? mMaxIndentLevel : level;
		return INDENTATION_PADDING * level;
	}

	@Override
	public int getItemViewType(int position) {
		switch (getItem(position).getMessage().getType()) {
		case Const.MSG_TYPE_DELETED:
			return VIEW_TYPE_DELETED;

		case Const.MSG_TYPE_PHOTO:
			return VIEW_TYPE_PHOTO;

		case Const.MSG_TYPE_GIF:
			return VIEW_TYPE_GIF;

		case Const.MSG_TYPE_LOCATION:
			return VIEW_TYPE_LOCATION;

		case Const.MSG_TYPE_VIDEO:
			return VIEW_TYPE_VIDEO;

		case Const.MSG_TYPE_VOICE:
			return VIEW_TYPE_SOUND;

		case Const.MSG_TYPE_FILE:
			return VIEW_TYPE_FILE;

		case Const.MSG_TYPE_DEFAULT:
		default:
			return VIEW_TYPE_MESSAGE;
		}
	}

	@Override
	public int getViewTypeCount() {
		return TOTAL_VIEW_TYPES;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != VIEW_TYPE_DELETED;
	}

	@Override
	public int getCount() {
		return mMessageList.size();
	}

	@Override
	public TreeNode getItem(int position) {
		return mMessageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mMessageList.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		int type = getItemViewType(position);
		if (convertView == null) {
			holder = new ViewHolder();

			switch (type) {
			case VIEW_TYPE_DELETED:
				convertView = inflateDeleted(holder, parent);
				break;

			case VIEW_TYPE_PHOTO:
				convertView = inflatePhoto(holder, parent);
				break;

			case VIEW_TYPE_GIF:
				convertView = inflateGif(holder, parent);
				break;

			case VIEW_TYPE_SOUND:
				convertView = inflateSound(holder, parent);
				break;

			case VIEW_TYPE_LOCATION:
			case VIEW_TYPE_VIDEO:
			case VIEW_TYPE_FILE:
				convertView = inflateMedia(holder, parent, type);
				break;

			case VIEW_TYPE_MESSAGE:
			default:
				convertView = inflateMessage(holder, parent);
				break;
			}

			holder.relativeLayoutHolder = (RelativeLayout) convertView.findViewById(R.id.relative_layout_thread_item_holder);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		TreeNode node = mMessageList.get(position);

		switch (type) {
		case VIEW_TYPE_DELETED:
			break;

		case VIEW_TYPE_PHOTO:
			populatePhoto(holder, node, position);
			break;

		case VIEW_TYPE_GIF:
			populateGif(holder, node, position);
			break;

		case VIEW_TYPE_LOCATION:
			populateLocation(holder, node, position);
			break;

		case VIEW_TYPE_VIDEO:
			populateVideo(holder, node, position);
			break;

		case VIEW_TYPE_SOUND:
			populateSound(holder, node, position);
			break;

		case VIEW_TYPE_FILE:
			populateFile(holder, node, position);
			break;

		case VIEW_TYPE_MESSAGE:
		default:
			populateMessage(holder, node, position);
			break;
		}

		convertView.setPadding(getIndentPadding(node.getLevel()), 0, 0, 0);

		if (type == VIEW_TYPE_GIF || type == VIEW_TYPE_SOUND) {
			final int pos = position;
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mContext instanceof ThreadsActivity)
						((ThreadsActivity) mContext).onItemClick(null, null, pos, getItemId(pos));
				}
			});

			convertView.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (mContext instanceof ThreadsActivity)
						((ThreadsActivity) mContext).onItemLongClick(null, null, pos, getItemId(pos));
					return false;
				}
			});
		}

		return convertView;
	}

	private View inflateMessage(final ViewHolder holder, final ViewGroup parent) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_message, parent, false);

		holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
		((RoundImageView) holder.imageViewUser).setBorderColor(mContext.getResources().getColor(R.color.light_light_gray));
		holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
		holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);
		holder.threadTime = (TextView) convertView.findViewById(R.id.timeThread);

		return convertView;
	}

	private View inflateDeleted(final ViewHolder holder, final ViewGroup parent) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_deleted, parent, false);

		holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);

		return convertView;
	}

	private View inflatePhoto(final ViewHolder holder, final ViewGroup parent) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_photo, parent, false);

		holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
		((RoundImageView) holder.imageViewUser).setBorderColor(mContext.getResources().getColor(R.color.light_light_gray));
		holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
		holder.imageViewPhoto = (ImageView) convertView.findViewById(R.id.image_view_photo);
		holder.imageViewPhoto.setOnClickListener(mOnClickPhoto);
		holder.threadTime = (TextView) convertView.findViewById(R.id.timeThread);

		return convertView;
	}

	private View inflateGif(final ViewHolder holder, final ViewGroup parent) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_gif, parent, false);

		holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
		((RoundImageView) holder.imageViewUser).setBorderColor(mContext.getResources().getColor(R.color.light_light_gray));
		holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
		holder.gifWebView = (WebView) convertView.findViewById(R.id.webViewGif);
		holder.gifWebView.setOnClickListener(mOnClickGif);
		holder.threadTime = (TextView) convertView.findViewById(R.id.timeThread);

		holder.gifWebView.getSettings().setAllowFileAccess(true);
		holder.gifWebView.getSettings().setJavaScriptEnabled(true);
		holder.gifWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		holder.gifWebView.getSettings().setBuiltInZoomControls(true);
		holder.gifWebView.setBackgroundColor(Color.TRANSPARENT);

		return convertView;
	}

	private View inflateMedia(final ViewHolder holder, final ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_media, parent, false);

		holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
		((RoundImageView) holder.imageViewUser).setBorderColor(mContext.getResources().getColor(R.color.light_light_gray));
		holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
		holder.textViewMessage = (TextView) convertView.findViewById(R.id.text_view_message);
		holder.threadTime = (TextView) convertView.findViewById(R.id.timeThread);

		switch (viewType) {
		case VIEW_TYPE_LOCATION:
			holder.textViewMessage.setOnClickListener(mOnClickLocation);
			break;

		case VIEW_TYPE_VIDEO:
			holder.textViewMessage.setOnClickListener(mOnClickVideo);
			break;

		case VIEW_TYPE_FILE:
			holder.textViewMessage.setOnClickListener(mOnClickFile);
			break;
		}

		return convertView;
	}

	private View inflateSound(final ViewHolder holder, final ViewGroup parent) {
		View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_thread_sound, parent, false);

		holder.imageViewUser = (ImageView) convertView.findViewById(R.id.image_view_user);
		((RoundImageView) holder.imageViewUser).setBorderColor(mContext.getResources().getColor(R.color.light_light_gray));
		holder.textViewUser = (TextView) convertView.findViewById(R.id.text_view_user);
		holder.threadTime = (TextView) convertView.findViewById(R.id.timeThread);
		holder.soundControl = (RelativeLayout) convertView.findViewById(R.id.rlSoundControl);

		return convertView;
	}

	private void populateMessage(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.textViewMessage.setText(node.getMessage().getText());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));

		int textWidth = node.getMessage().getTextWidth();

		if (textWidth == -1) {
			textWidth = calculateNeedTextWidth(node.getMessage().getText(), mContext);
			node.getMessage().setTextWidth(textWidth);
		}

		int timeWidth = node.getMessage().getTimeWidth();

		if (timeWidth == -1) {
			timeWidth = calculateNeedTextWidth(getCreatedTime(node.getMessage().getCreated()), mContext);
			node.getMessage().setTimeWidth(timeWidth);
		}

		if (textWidth > displayWidth - Utils.getPxFromDp(75, mContext.getResources()) - timeWidth - getIndentPadding(node.getLevel())) {
			((LayoutParams) holder.textViewMessage.getLayoutParams()).weight = 1;
		} else {
			((LayoutParams) holder.textViewMessage.getLayoutParams()).weight = 0;
		}

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.devil_gray));
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}
	}

	private void populatePhoto(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));

		imageLoaderSpice.displayImage(holder.imageViewPhoto, node.getMessage().getThumb_id(), 0);
		holder.imageViewPhoto.setTag(R.id.tag_file_id, node.getMessage().getFile_id());

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}
	}

	private void populateGif(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));

		String style = "style=\"border: solid #fff 1px;border-radius: 10px; margin-left:5%;margin-top:5%\"";
		GifLoader.getInstance(mContext).displayImage(mContext, node.getMessage().getText(), holder.gifWebView, style);

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}
	}

	private void populateLocation(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));
		holder.textViewMessage.setText(mContext.getResources().getString(R.string.location_tap_to_view));

		holder.textViewMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_location, 0);
		holder.textViewMessage.setTag(R.id.tag_latitude, node.getMessage().getLatitude());
		holder.textViewMessage.setTag(R.id.tag_longitude, node.getMessage().getLongitude());

		Bitmap iconLoc = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_location);
		int widthOfIcon = iconLoc.getWidth();

		int textWidth = node.getMessage().getTextWidth();

		if (textWidth == -1) {
			textWidth = calculateNeedTextWidth(mContext.getResources().getString(R.string.location_tap_to_view), mContext);
			node.getMessage().setTextWidth(textWidth);
		}

		int timeWidth = node.getMessage().getTimeWidth();

		if (timeWidth == -1) {
			timeWidth = calculateNeedTextWidth(getCreatedTime(node.getMessage().getCreated()), mContext);
			node.getMessage().setTimeWidth(timeWidth);
		}

		if (textWidth > displayWidth - Utils.getPxFromDp(80, mContext.getResources()) - timeWidth - widthOfIcon - getIndentPadding(node.getLevel())) {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 1;
		} else {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 0;
		}

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.devil_gray));
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}
	}

	private void populateVideo(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));
		holder.textViewMessage.setText(mContext.getResources().getString(R.string.video_tap_to_play));

		holder.textViewMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_video, 0);
		holder.textViewMessage.setTag(R.id.tag_file_id, node.getMessage().getFile_id());

		Bitmap iconLoc = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_video);
		int widthOfIcon = iconLoc.getWidth();

		int textWidth = node.getMessage().getTextWidth();

		if (textWidth == -1) {
			textWidth = calculateNeedTextWidth(mContext.getResources().getString(R.string.video_tap_to_play), mContext);
			node.getMessage().setTextWidth(textWidth);
		}

		int timeWidth = node.getMessage().getTimeWidth();

		if (timeWidth == -1) {
			timeWidth = calculateNeedTextWidth(getCreatedTime(node.getMessage().getCreated()), mContext);
			node.getMessage().setTimeWidth(timeWidth);
		}

		if (textWidth > displayWidth - Utils.getPxFromDp(80, mContext.getResources()) - timeWidth - widthOfIcon - getIndentPadding(node.getLevel())) {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 1;
		} else {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 0;
		}

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.devil_gray));
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}

	}

	private void populateSound(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));

		setVoiceControls(node.getMessage(), holder.soundControl);

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}
	}

	private void populateFile(ViewHolder holder, TreeNode node, int position) {
		imageLoaderSpice.displayImage(holder.imageViewUser, node.getMessage().getImage(), R.drawable.default_user_image);
		holder.textViewUser.setText(node.getMessage().getName());
		holder.threadTime.setText(getCreatedTime(node.getMessage().getCreated()));
		holder.textViewMessage.setText(node.getMessage().getText());

		holder.textViewMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_file, 0);
		holder.textViewMessage.setTag(R.id.tag_file_id, node.getMessage());

		Bitmap iconLoc = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_file);
		int widthOfIcon = iconLoc.getWidth();

		int textWidth = node.getMessage().getTextWidth();

		if (textWidth == -1) {
			textWidth = calculateNeedTextWidth(node.getMessage().getText(), mContext);
			node.getMessage().setTextWidth(textWidth);
		}

		int timeWidth = node.getMessage().getTimeWidth();

		if (timeWidth == -1) {
			timeWidth = calculateNeedTextWidth(getCreatedTime(node.getMessage().getCreated()), mContext);
			node.getMessage().setTimeWidth(timeWidth);
		}

		if (textWidth > displayWidth - Utils.getPxFromDp(80, mContext.getResources()) - timeWidth - widthOfIcon - getIndentPadding(node.getLevel())) {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 1;
		} else {
			((LayoutParams) ((View) holder.textViewMessage.getParent()).getLayoutParams()).weight = 0;
		}

		if (position == this.mSelectedItem) {
			holder.relativeLayoutHolder.setBackgroundResource(R.drawable.shape_selected_item);
			holder.textViewUser.setTextColor(Color.WHITE);
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.devil_gray));
			holder.threadTime.setTextColor(Color.WHITE);
		} else {
			holder.relativeLayoutHolder.setBackgroundColor(Color.TRANSPARENT);
			holder.textViewUser.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
			holder.textViewMessage.setTextColor(mContext.getResources().getColor(R.color.black));
			holder.threadTime.setTextColor(mContext.getResources().getColor(R.color.text_gray_image));
		}

	}

	private View.OnClickListener mOnClickPhoto = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_file_id) != null) {
				String fileId = (String) v.getTag(R.id.tag_file_id);
				Intent photoIntent = new Intent(mContext, PhotoActivity.class);
				photoIntent.putExtra(Const.IMAGE, fileId);
				mContext.startActivity(photoIntent);
			}
		}
	};

	private View.OnClickListener mOnClickGif = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				Intent photoIntent = new Intent(mContext, PhotoActivity.class);
				photoIntent.putExtra(Const.IMAGE, "Gif");
				photoIntent.putExtra(Const.FILE, (String) v.getTag());
				photoIntent.putExtra(Const.TYPE, Const.MSG_TYPE_GIF);
				mContext.startActivity(photoIntent);
			}
		}
	};

	private View.OnClickListener mOnClickLocation = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_latitude) != null && v.getTag(R.id.tag_longitude) != null) {
				Intent locationIntent = new Intent(mContext, LocationActivity.class);
				locationIntent.putExtra(Const.LATITUDE, Double.parseDouble((String) v.getTag(R.id.tag_latitude)));
				locationIntent.putExtra(Const.LONGITUDE, Double.parseDouble((String) v.getTag(R.id.tag_longitude)));
				mContext.startActivity(locationIntent);
			}
		}
	};

	private View.OnClickListener mOnClickVideo = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.tag_file_id) != null) {
				Intent videoIntent = new Intent(mContext, VideoActivity.class);
				videoIntent.putExtra(Const.FILE_ID, (String) v.getTag(R.id.tag_file_id));
				mContext.startActivity(videoIntent);
			}
		}
	};

	private View.OnClickListener mOnClickFile = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Message message = (Message) v.getTag(R.id.tag_file_id);
			if (message != null) {
				new FileManageApi().startFileDownload(message.getText(), message.getFile_id(), Integer.valueOf(message.getId()), mContext);
			}
		}
	};

	private static final class ViewHolder {
		RelativeLayout relativeLayoutHolder;

		ImageView imageViewUser;
		ImageView imageViewPhoto;
		TextView textViewMessage;
		TextView textViewUser;
		TextView threadTime;
		RelativeLayout soundControl;
		WebView gifWebView;
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

	private int calculateNeedTextWidth(String text, Context c) {
		Paint paint = new Paint();
		Rect bounds = new Rect();

		int text_width = 0;

		paint.setTypeface(typeface);// your preference here
		paint.setTextSize(Utils.getPxFromSp(20, c.getResources()));// have this
																	// the same
																	// as your
																	// text size

		paint.getTextBounds(text, 0, text.length(), bounds);

		text_width = bounds.width();

		return text_width;
	}

	// ***********SOUND CONTROLERS
	private double totalOfDownload = -1;

	private void setVoiceControls(final Message msg, final RelativeLayout holder) {

		final Button playPause = (Button) holder.getChildAt(Const.SoundControl.PLAY_BUTTON);
		final SeekBar seekControl = (SeekBar) holder.getChildAt(Const.SoundControl.SEEKBAR);
		final Chronometer chronoControl = (Chronometer) holder.getChildAt(Const.SoundControl.CHRONOMETER);

		playPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				preformOnSoundClick(0, chronoControl, playPause, seekControl, msg.getFile_id(), holder);
			}
		});

		seekControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				preformOnSoundClick(seekBar.getProgress(), chronoControl, playPause, seekControl, msg.getFile_id(), holder);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (observer != null)
					observer.stop();
				observer = null;
				if (activeSeekbar != null && activeSeekbar != seekBar)
					activeSeekbar.setProgress(0);
				if (currentMediaPlayer != null) {
					currentMediaPlayer.stop();
					currentMediaPlayer.release();
				}
				currentMediaPlayer = null;
				if (activeChronometer != null) {
					activeChronometer.stop();
					activeChronometer.setBase(SystemClock.elapsedRealtime());
				}
				if (activePlayIcon != null) {
					activePlayIcon.setBackgroundResource(R.drawable.play_button);
				}
				seekBar.setMax(100);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});
	}

	private void preformOnSoundClick(final int startOffset, final Chronometer chronoControl, final Button playPause, final SeekBar seekControl, String fileId, RelativeLayout holder) {
		File sound = new File(Utils.getFilesFolder() + "/" + fileId);
		if (sound.exists()) {
			if (currentMediaPlayer == null) {

				play(chronoControl, sound, playPause, seekControl, startOffset);

			} else {
				if (currentPlayingPath != null && currentPlayingPath.equals(sound.getAbsolutePath())) {
					if (observer != null)
						observer.stop();
					observer = null;
					activeSeekbar.setProgress(0);
					currentMediaPlayer.stop();
					currentMediaPlayer.release();
					currentMediaPlayer = null;
					activeChronometer.stop();
					activeChronometer.setBase(SystemClock.elapsedRealtime());
					playPause.setBackgroundResource(R.drawable.play_button);
				} else {
					if (observer != null)
						observer.stop();
					observer = null;
					activeSeekbar.setProgress(0);
					currentMediaPlayer.stop();
					currentMediaPlayer.release();
					currentMediaPlayer = null;
					activePlayIcon.setBackgroundResource(R.drawable.play_button);
					activeChronometer.stop();
					activeChronometer.setBase(SystemClock.elapsedRealtime());

					play(chronoControl, sound, playPause, seekControl, startOffset);

				}
			}

		} else {
			if (isDownloadingSound) {
				return;
			}
			isDownloadingSound = true;
			totalOfDownload = -1;

			preformDownload(holder, playPause, seekControl, chronoControl, sound, fileId);
		}
	}

	private class MediaObserver implements Runnable {
		private AtomicBoolean stop = new AtomicBoolean(false);

		public void stop() {
			stop.set(true);
		}

		@Override
		public void run() {
			while (!stop.get()) {
				long elapsedMillis = SystemClock.elapsedRealtime() - activeChronometer.getBase();
				Log.e("LOG", elapsedMillis + " :ELG");
				activeSeekbar.setProgress((int) elapsedMillis);
				try {
					Thread.sleep(33);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private MediaObserver observer = null;

	private void play(final Chronometer chronoControl, File sound, final Button playPause, final SeekBar seekControl, final int startOffset) {
		currentMediaPlayer = new MediaPlayer();
		if (activeChronometer != null)
			activeChronometer.stop();
		try {

			currentMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					seekControl.setMax(mp.getDuration());
					if (startOffset != 0) {
						double offset = (((double) seekControl.getMax() * (double) startOffset) / (double) 100);
						currentMediaPlayer.seekTo((int) offset);
						chronoControl.setBase((long) (SystemClock.elapsedRealtime() - offset));
					}
				}
			});

			currentMediaPlayer.setDataSource(sound.getAbsolutePath());
			currentMediaPlayer.prepare();
			currentMediaPlayer.start();
			chronoControl.setBase(SystemClock.elapsedRealtime());
			chronoControl.start();
			activeChronometer = chronoControl;
			activeSeekbar = seekControl;

			observer = new MediaObserver();
			new Thread(observer).start();

			currentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					observer.stop();
					activeSeekbar.setProgress(0);
					currentPlayingPath = null;
					currentMediaPlayer.stop();
					currentMediaPlayer.release();
					currentMediaPlayer = null;
					chronoControl.stop();
					chronoControl.setBase(SystemClock.elapsedRealtime());
					playPause.setBackgroundResource(R.drawable.play_button);
				}
			});

			currentPlayingPath = sound.getAbsolutePath();
			playPause.setBackgroundResource(R.drawable.pause_button);
			activePlayIcon = playPause;
		} catch (IOException e) {
			e.printStackTrace();
			currentMediaPlayer = null;
		}
	}

	private void preformDownload(RelativeLayout holder, final Button playPause, final SeekBar seekControl, final Chronometer chronoControl, final File sound, final String fileId) {
		final ProgressBar pbLoading = (ProgressBar) holder.getChildAt(Const.SoundControl.DOWNLOAD_PROGRESS);
		final ProgressBar pbLoadingBar = (ProgressBar) holder.getChildAt(Const.SoundControl.PROGREEBAR);
		final TextView percentTv = (TextView) holder.getChildAt(Const.SoundControl.PERCENT_TV);
		pbLoading.setVisibility(View.VISIBLE);
		pbLoadingBar.setVisibility(View.VISIBLE);
		percentTv.setVisibility(View.VISIBLE);
		playPause.setVisibility(View.INVISIBLE);
		seekControl.setVisibility(View.INVISIBLE);
		chronoControl.setVisibility(View.INVISIBLE);
		new FileManageApi().downloadFileToFile(sound, fileId, false, mContext, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				pbLoading.setVisibility(View.INVISIBLE);
				pbLoadingBar.setVisibility(View.INVISIBLE);
				pbLoadingBar.setProgress(0);
				percentTv.setVisibility(View.INVISIBLE);
				percentTv.setText("0%");
				playPause.setVisibility(View.VISIBLE);
				seekControl.setVisibility(View.VISIBLE);
				chronoControl.setVisibility(View.VISIBLE);

				isDownloadingSound = false;
			}
		}, new ProgressBarListeners() {

			@Override
			public void onSetMax(long total) {
				if (totalOfDownload == -1) {
					totalOfDownload = total;
					pbLoadingBar.setMax((int) totalOfDownload);
				}
			}

			@Override
			public void onProgress(long current) {
				if (totalOfDownload != -1) {
					pbLoadingBar.setProgress((int) current);
					final String percent = String.valueOf(((int) (100 * current / (double) totalOfDownload)));
					((Activity) mContext).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							percentTv.setText(String.valueOf(percent + "%"));
						}
					});
				}
			}

			@Override
			public void onFinish() {
			}
		});
	}
}
