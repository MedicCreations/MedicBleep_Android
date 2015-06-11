package com.clover.spika.enterprise.chat.adapters;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.VideoActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.GifLoaderSpice;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.MessageSortingById;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.clover.spika.enterprise.chat.views.RoundImageView;
import com.octo.android.robospice.SpiceManager;

public class MessagesAdapter extends BaseAdapter {
	
	private static final int MAX_CHARACTER = 220;  // set to 220
	private static final int SUBSTRING_MESSAGE_AT = 200;  // set to 200

	private Context ctx;
	private List<Message> data;
	private String seenBy = "";

	private SparseIntArray dateSeparator = new SparseIntArray();

	private ImageLoaderSpice imageLoaderSpice;
	private GifLoaderSpice gifLoaderSpice;

	private boolean endOfSearch = false;
	private int totalCount = 0;

	private int displayWidth = 0;
	Typeface typeface;

	private boolean isDownloadingSound = false;
	private MediaPlayer currentMediaPlayer = null;
	private String currentPlayingPath = null;
	private Button activePlayIcon = null;
	private Chronometer activeChronometer = null;
	private SeekBar activeSeekbar = null;

	private OnMessageLongAndSimpleClickCustomListener listenerLongAndSimpleClick;

	public MessagesAdapter(SpiceManager manager, Context context, List<Message> arrayList) {
		this.ctx = context;
		this.data = arrayList;

		imageLoaderSpice = ImageLoaderSpice.getInstance(context);
		imageLoaderSpice.setSpiceManager(manager);

		gifLoaderSpice = GifLoaderSpice.getInstance(ctx);
		gifLoaderSpice.setSpiceManager(manager);
		
		displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
	}
	
	public void setSpiceManager(SpiceManager manager){
		imageLoaderSpice.setSpiceManager(manager);
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
		holder.meMsgLayout.setVisibility(View.GONE);
		holder.meMsgLayout.setAlpha(1f);
		holder.youMsgLayout.setVisibility(View.GONE);

		holder.meMsgContent.setVisibility(View.GONE);
		holder.meMsgContent.setTypeface(null, Typeface.NORMAL);
		holder.meMsgContent.setTextColor(Color.WHITE);
		holder.meMsgContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		holder.meViewForReadMore.setVisibility(View.GONE);
		holder.meViewForReadMore.setOnClickListener(null);
		holder.youMsgContent.setVisibility(View.GONE);
		holder.youMsgContent.setTypeface(null, Typeface.NORMAL);
		holder.youMsgContent.setTextColor(ctx.getResources().getColor(R.color.medic_bleep_you_text_gray));
		holder.youMsgContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		holder.youViewForReadMore.setVisibility(View.GONE);
		holder.youViewForReadMore.setOnClickListener(null);

        holder.meMessageStatus.setBackgroundResource(0);

		holder.meViewImage.setVisibility(View.GONE);
		holder.meViewImage.setTag(null);
		holder.youViewImage.setVisibility(View.GONE);
		holder.youViewImage.setTag(null);

		holder.meListenSound.setVisibility(View.GONE);
		holder.youListenSound.setVisibility(View.GONE);

		holder.meWatchVideo.setVisibility(View.GONE);
		holder.youWatchVideo.setVisibility(View.GONE);

		holder.meViewLocation.setVisibility(View.GONE);
		holder.youViewLocation.setVisibility(View.GONE);

		holder.meDownloadFile.setVisibility(View.GONE);
		holder.youDownloadFile.setVisibility(View.GONE);

		holder.loading_bar.setVisibility(View.GONE);

		// holder.meGifView.setVisibility(View.GONE);
		holder.meWebView.setVisibility(View.GONE);
		holder.meFlForGif.setVisibility(View.GONE);

		// holder.youGifView.setVisibility(View.GONE);
		holder.youWebView.setVisibility(View.GONE);
		holder.youFlForGif.setVisibility(View.GONE);

		holder.meMsgLayoutBack.setBackgroundResource(R.drawable.me_message_bubble);
		((LayoutParams) holder.meMsgLayoutBack.getLayoutParams()).weight = 0; 
		holder.meFlForGif.getChildAt(0).setVisibility(View.VISIBLE); // progress for gif

		holder.youMsgLayoutBack.setBackgroundResource(R.drawable.you_message_bubble);
		holder.youFlForGif.getChildAt(0).setVisibility(View.VISIBLE); // progress fpr gif

		// Assign values
		final Message msg = getItem(position);

		if (msg.isMe()) {
			// My chat messages

			holder.meMsgLayout.setVisibility(View.VISIBLE);

			holder.meMsgTime.setText(getCreatedTime(msg.getCreated()).toLowerCase(Locale.getDefault()));

//			if (msg.getType() == Const.MSG_TYPE_PHOTO || msg.getType() == Const.MSG_TYPE_GIF) {
//				holder.meMsgLayoutBack.setPadding(0, 0, 0, 0);
//			} else {
//				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
//				holder.meMsgLayoutBack.setPadding(padding, padding, padding, 0);
//			}
			
			if (msg.getType() == Const.MSG_TYPE_TEMP_MESS_ERROR) {
				holder.meMsgLayout.setAlpha(.4f);
				holder.meMsgContent.setVisibility(View.VISIBLE);
				holder.meMsgContent.setTextColor(ctx.getResources().getColor(R.color.red));
				holder.meMsgContent.setText(msg.getText());
			}else if (msg.getType() == Const.MSG_TYPE_TEMP_MESS) {
				holder.meMsgLayout.setAlpha(.4f);
				holder.meMsgContent.setVisibility(View.VISIBLE);
				holder.meMsgContent.setTextColor(Color.LTGRAY);
				holder.meMsgContent.setText(msg.getText());
			}else if (msg.getType() == Const.MSG_TYPE_DEFAULT) {
				
				if(msg.getIsCodeTextStyle()){
					holder.meMsgContent.setVisibility(View.VISIBLE);
					holder.meMsgLayoutBack.setBackgroundColor(ctx.getResources().getColor(R.color.code_preview_black));
					((LayoutParams) holder.meMsgLayoutBack.getLayoutParams()).weight = 1; 
					holder.meMsgContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					holder.meMsgContent.setTypeface(Typeface.MONOSPACE);
				}else{
					holder.meMsgContent.setVisibility(View.VISIBLE);
				}
				
				String myMsg = msg.getText();
				if(myMsg.length() > MAX_CHARACTER){
					String endString = "more";
					if(msg.isUserExpandContent()){
						endString = "less";
					}else{
						int index = myMsg.indexOf(" ", SUBSTRING_MESSAGE_AT);
						if(index == -1) index = SUBSTRING_MESSAGE_AT;
						myMsg = myMsg.substring(0, index);
					}
					Spannable span = new SpannableString(myMsg + " " + endString);
					span.setSpan(new UnderlineSpan(), myMsg.length() + 1, myMsg.length() + 1 + endString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					holder.meMsgContent.setText(span);
					holder.meViewForReadMore.setVisibility(View.VISIBLE);
					holder.meViewForReadMore.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							msg.setUserExpandContent(!msg.isUserExpandContent());
							notifyDataSetChanged();
						}
					});
				}else{
					holder.meMsgContent.setText(myMsg);
				}
				
			} else if (msg.getType() == Const.MSG_TYPE_PHOTO) {
				
				holder.meViewImage.setImageDrawable(null);
//				holder.meViewImage.setTag(msg.isEncrypted());
				imageLoaderSpice.displayImage(holder.meViewImage, msg.thumb_id, ImageLoaderSpice.NO_IMAGE);

				holder.meViewImage.setVisibility(View.VISIBLE);
				holder.meViewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getFile_id());
						intent.putExtra(Const.IS_ENCRYPTED, msg.isEncrypted());
						ctx.startActivity(intent);
						if (ctx instanceof ChatActivity)
							((ChatActivity) ctx).setIsResume(false);
					}
				});
				holder.meViewImage.setOnLongClickListener(setLongClickListener(msg));
			} else if (msg.getType() == Const.MSG_TYPE_GIF) {

				holder.meFlForGif.setVisibility(View.VISIBLE);
				holder.meWebView.setVisibility(View.VISIBLE);
				holder.meWebView.getSettings().setAllowFileAccess(true);
				holder.meWebView.getSettings().setJavaScriptEnabled(true);
				holder.meWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
				holder.meWebView.getSettings().setBuiltInZoomControls(true);
				holder.meWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				holder.meMsgLayoutBack.setBackgroundColor(Color.RED);

				String style = "style=\"border: solid #fff 1px;border-radius: 10px;\"";
				gifLoaderSpice.displayImage(ctx, msg.getText(), holder.meWebView, style);

				holder.meWebView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getText());
						intent.putExtra(Const.FILE, (String) holder.meWebView.getTag());
						intent.putExtra(Const.TYPE, msg.getType());
						ctx.startActivity(intent);
						if (ctx instanceof ChatActivity)
							((ChatActivity) ctx).setIsResume(false);
					}
				});

				holder.meWebView.setOnLongClickListener(setLongClickListener(msg));
				
			}else if (msg.getType() == Const.MSG_TYPE_VIDEO) {
				holder.meWatchVideo.setVisibility(View.VISIBLE);
				holder.meWatchVideo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VideoActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						intent.putExtra(Const.IS_ENCRYPTED, msg.isEncrypted());
						ctx.startActivity(intent);
					}
				});

				holder.meWatchVideo.setOnLongClickListener(setLongClickListener(msg));
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
				holder.meViewLocation.setOnLongClickListener(setLongClickListener(msg));
			} else if (msg.getType() == Const.MSG_TYPE_VOICE) {

				resetVoiceControls(holder.meListenSound);
				holder.meListenSound.setVisibility(View.VISIBLE);
				setVoiceControls(msg, holder.meListenSound);

				holder.meListenSound.setOnLongClickListener(setLongClickListener(msg));

			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.meDownloadFile.setVisibility(View.VISIBLE);
				holder.meDownloadFile.setText(msg.getText());
				holder.meDownloadFile.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							new FileManageApi().startFileDownload(msg.isEncrypted(), msg.getText(), msg.getFile_id(), Integer.valueOf(msg.getId()), ctx);
						}
					}
				});

				holder.meDownloadFile.setOnLongClickListener(setLongClickListener(msg));

			} else if (msg.getType() == Const.MSG_TYPE_DELETED) {
				holder.meMsgContent.setVisibility(View.VISIBLE);
				holder.meMsgContent.setText(ctx.getString(R.string.message_deleted));
				holder.meMsgContent.setTypeface(null, Typeface.ITALIC);
			}

            if(msg.getType() != Const.MSG_TYPE_TEMP_MESS_ERROR && msg.getType() != Const.MSG_TYPE_TEMP_MESS && msg.getType() != Const.MSG_TYPE_DELETED){
                if(msg.seen_timestamp > 0){
                    holder.meMessageStatus.setBackgroundResource(R.drawable.message_seen_white);
                }else{
                    holder.meMessageStatus.setBackgroundResource(R.drawable.message_sent_white);
                }
            }

			if (!TextUtils.isEmpty(msg.getChildListText())) {
				holder.meThreadIndicator.setVisibility(View.VISIBLE);
				holder.meThreadIndicator.setImageResource(R.drawable.right_thread_arrow);
			} else if (msg.getRootId() > 0) {
				holder.meThreadIndicator.setVisibility(View.VISIBLE);
				holder.meThreadIndicator.setImageResource(R.drawable.ic_thread_reply);
			} else if (msg.type == Const.MSG_TYPE_TEMP_MESS_ERROR) {
				holder.meThreadIndicator.setVisibility(View.VISIBLE);
				holder.meThreadIndicator.setImageResource(R.drawable.send_mess_failed);
			}else {
				holder.meThreadIndicator.setImageDrawable(null);
				holder.meThreadIndicator.setVisibility(View.GONE);
			}

		} else {
			// Chat member messages, not mine

			holder.youMsgLayout.setVisibility(View.VISIBLE);

			holder.youMsgTime.setText(getCreatedTime(msg.getCreated()).toLowerCase(Locale.getDefault()));
			holder.youPersonName.setText("~ " + msg.getFirstname() + " " + msg.getLastname());

			if (msg.getType() == Const.MSG_TYPE_PHOTO || msg.getType() == Const.MSG_TYPE_GIF) {
//				holder.youMsgLayoutBack.setPadding(0, 0, 0, 0);
				((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
			} else if (msg.getType() == Const.MSG_TYPE_LOCATION || msg.getType() == Const.MSG_TYPE_VIDEO) {
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
//				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);
				((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
			} else if (msg.getType() == Const.MSG_TYPE_VOICE) {
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
//				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);
				((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 1;
			} else {
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
//				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);

				int textWidth = msg.getTextWidth();

				if (textWidth == -1) {
					textWidth = calculateNeedTextWidth(msg.getText(), ctx);
					msg.setTextWidth(textWidth);
				}

				int timeWidth = msg.getTimeWidth();

				if (timeWidth == -1) {
					timeWidth = calculateNeedTextWidth(getCreatedTime(msg.getCreated()), ctx);
					msg.setTimeWidth(timeWidth);
				}

				if (msg.getIsCodeTextStyle() || textWidth > displayWidth - Utils.getPxFromDp(75, ctx.getResources()) - timeWidth) {
					((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 1;
				} else {
					((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
				}
			}

			if (msg.getType() == Const.MSG_TYPE_DEFAULT) {
				
				if(msg.getIsCodeTextStyle()){
					holder.youMsgContent.setVisibility(View.VISIBLE);
					holder.youMsgLayoutBack.setBackgroundColor(ctx.getResources().getColor(R.color.code_preview_black));
					holder.youMsgContent.setTextColor(Color.WHITE);
					((LayoutParams) holder.youMsgLayoutBack.getLayoutParams()).weight = 1;
					holder.youMsgContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
					holder.youMsgContent.setTypeface(Typeface.MONOSPACE);
				}else{
					holder.youMsgContent.setVisibility(View.VISIBLE);
				}
				
				String youMsg = msg.getText();
				if(youMsg.length() > MAX_CHARACTER){
					String endString = "more";
					if(msg.isUserExpandContent()){
						endString = "less";
					}else{
						int index = youMsg.indexOf(" ", SUBSTRING_MESSAGE_AT);
						if(index == -1) index = SUBSTRING_MESSAGE_AT;
						youMsg = youMsg.substring(0, index);
					}
					Spannable span = new SpannableString(youMsg + " " + endString);
					span.setSpan(new UnderlineSpan(), youMsg.length() + 1, youMsg.length() + 1 + endString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					holder.youMsgContent.setText(span);
					holder.youViewForReadMore.setVisibility(View.VISIBLE);
					holder.youViewForReadMore.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							msg.setUserExpandContent(!msg.isUserExpandContent());
							notifyDataSetChanged();
						}
					});
				}else{
					holder.youMsgContent.setText(youMsg);
				}
				
			} else if (msg.getType() == Const.MSG_TYPE_PHOTO) {

				holder.youViewImage.setImageDrawable(null);
//				holder.youViewImage.setTag(msg.isEncrypted());
				imageLoaderSpice.displayImage(holder.youViewImage, msg.getThumb_id(), ImageLoaderSpice.NO_IMAGE);

				holder.youViewImage.setVisibility(View.VISIBLE);
				holder.youViewImage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getFile_id());
						intent.putExtra(Const.IS_ENCRYPTED, msg.isEncrypted());
						ctx.startActivity(intent);
						if (ctx instanceof ChatActivity)
							((ChatActivity) ctx).setIsResume(false);
					}
				});
			} else if (msg.getType() == Const.MSG_TYPE_GIF) {

				holder.youFlForGif.setVisibility(View.VISIBLE);
				
				holder.youWebView.setVisibility(View.VISIBLE);
				holder.youWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
				holder.youWebView.getSettings().setAllowFileAccess(true);
				holder.youWebView.getSettings().setJavaScriptEnabled(true);
				holder.youWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
				holder.youWebView.getSettings().setBuiltInZoomControls(true);

				String style = "style=\"border: solid #fff 1px;border-radius: 10px; margin-top:5%; margin-left:5%;\"";
				gifLoaderSpice.displayImage(ctx, msg.getText(), holder.youWebView, style);

				holder.youWebView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, PhotoActivity.class);
						intent.putExtra(Const.IMAGE, msg.getText());
						intent.putExtra(Const.FILE, (String) holder.youWebView.getTag());
						intent.putExtra(Const.TYPE, msg.getType());
						ctx.startActivity(intent);
						if (ctx instanceof ChatActivity)
							((ChatActivity) ctx).setIsResume(false);
					}
				});
				
			}else if (msg.getType() == Const.MSG_TYPE_VIDEO) {

				holder.youWatchVideo.setVisibility(View.VISIBLE);
				holder.youWatchVideo.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(ctx, VideoActivity.class);
						intent.putExtra(Const.FILE_ID, msg.getFile_id());
						intent.putExtra(Const.IS_ENCRYPTED, msg.isEncrypted());
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

				resetVoiceControls(holder.youListenSound);
				holder.youListenSound.setVisibility(View.VISIBLE);
				setVoiceControls(msg, holder.youListenSound);

			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.youDownloadFile.setVisibility(View.VISIBLE);
				holder.youDownloadFile.setText(msg.getText());
				holder.youDownloadFile.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (msg.isFailed()) {
							new AppDialog(ctx, false).setFailed(ctx.getResources().getString(R.string.e_error_not_decrypted));
						} else {
							new FileManageApi().startFileDownload(msg.isEncrypted(), msg.getText(), msg.getFile_id(), Integer.valueOf(msg.getId()), ctx);
						}
					}
				});

			} else if (msg.getType() == Const.MSG_TYPE_DELETED) {
				holder.youPersonName.setText("");
				holder.youMsgContent.setVisibility(View.VISIBLE);
				holder.youMsgContent.setText(ctx.getString(R.string.message_deleted));
				holder.youMsgContent.setTypeface(null, Typeface.ITALIC);
			}

			if (!TextUtils.isEmpty(msg.getChildListText())) {
				holder.youThreadIndicator.setVisibility(View.VISIBLE);
				holder.youThreadIndicator.setImageResource(R.drawable.left_thread_arrow);
			} else if (msg.getRootId() > 0) {
				holder.youThreadIndicator.setVisibility(View.VISIBLE);
				holder.youThreadIndicator.setImageResource(R.drawable.ic_thread_reply);
			} else {
				holder.youThreadIndicator.setImageDrawable(null);
				holder.youThreadIndicator.setVisibility(View.GONE);
			}
		}

		// Date separator
		if (dateSeparator.get(getDayTimeStamp(msg.getCreated())) != position) {
			holder.dateSeparator.setVisibility(View.GONE);
		} else {
			holder.dateSeparator.setVisibility(View.VISIBLE);
			holder.sectionDate.setText(getDateFormat(msg.getCreated()).toUpperCase(Locale.getDefault()));
		}

		// Paging animation
//		if (position == (0) && !endOfSearch) {
//			holder.loading_bar.setVisibility(View.VISIBLE);
//
//			if (ctx instanceof ChatActivity) {
//				((ChatActivity) ctx).getMessages(false, false, true, false, false, false);
//			}
//		}

		// Check if last message
		if (position == (getCount() - 1) && !TextUtils.isEmpty(seenBy)) {
			holder.seenByTv.setText("Seen by " + seenBy);
			holder.seenByTv.setVisibility(View.GONE); //wait to confirm without seen by
			convertView.setPadding(0, 0, 0, Utils.getPxFromDp(10, convertView.getContext().getResources()));
		} else {
			holder.seenByTv.setVisibility(View.GONE);
			convertView.setPadding(0, 0, 0, Utils.getPxFromDp(10, convertView.getContext().getResources()));
		}

		if (position == (getCount() - 1)) {
			convertView.setPadding(0, 0, 0, Utils.getPxFromDp(10, convertView.getContext().getResources()));
		} else {
			convertView.setPadding(0, 0, 0, 0);
		}

		convertView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (listenerLongAndSimpleClick != null)
					listenerLongAndSimpleClick.onLongClick(msg);
				return false;
			}
		});

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listenerLongAndSimpleClick != null)
					listenerLongAndSimpleClick.onSimpleClick(msg);
			}
		});
		
		return convertView;
	}

	private OnLongClickListener setLongClickListener(final Message msg) {
		return new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (listenerLongAndSimpleClick != null)
					listenerLongAndSimpleClick.onLongClick(msg);
				return false;
			}
		};
	}

	private void resetVoiceControls(RelativeLayout holder) {
		Button playPause = (Button) holder.getChildAt(Const.SoundControl.PLAY_BUTTON);
		playPause.setBackgroundResource(R.drawable.play_button);
		playPause.setVisibility(View.VISIBLE);
		SeekBar seekControl = (SeekBar) holder.getChildAt(Const.SoundControl.SEEKBAR);
		seekControl.setMax(100);
		seekControl.setProgress(0);
		seekControl.setVisibility(View.VISIBLE);
		Chronometer chronoControl = (Chronometer) holder.getChildAt(Const.SoundControl.CHRONOMETER);
		chronoControl.setText("00:00");
		chronoControl.setVisibility(View.VISIBLE);
		ProgressBar pbLoading = (ProgressBar) holder.getChildAt(Const.SoundControl.DOWNLOAD_PROGRESS);
		pbLoading.setVisibility(View.INVISIBLE);
		ProgressBar pbLoadingBar = (ProgressBar) holder.getChildAt(Const.SoundControl.PROGREEBAR);
		pbLoadingBar.setVisibility(View.INVISIBLE);
		pbLoadingBar.setProgress(0);
		TextView percentTv = (TextView) holder.getChildAt(Const.SoundControl.PERCENT_TV);
		percentTv.setVisibility(View.INVISIBLE);
		percentTv.setText("0%");
	}

	private double totalOfDownload = -1;

	private void setVoiceControls(final Message msg, final RelativeLayout holder) {

		final Button playPause = (Button) holder.getChildAt(Const.SoundControl.PLAY_BUTTON);
		final SeekBar seekControl = (SeekBar) holder.getChildAt(Const.SoundControl.SEEKBAR);
		final Chronometer chronoControl = (Chronometer) holder.getChildAt(Const.SoundControl.CHRONOMETER);

		playPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				preformOnSoundClick(msg.isEncrypted(), 0, chronoControl, playPause, seekControl, msg.getFile_id(), holder);
			}
		});

		seekControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				preformOnSoundClick(msg.isEncrypted(), seekBar.getProgress(), chronoControl, playPause, seekControl, msg.getFile_id(), holder);
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

	private void preformOnSoundClick(final boolean toCrypt, final int startOffset, final Chronometer chronoControl, final Button playPause, final SeekBar seekControl, String fileId, RelativeLayout holder) {
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

			preformDownload(toCrypt, holder, playPause, seekControl, chronoControl, sound, fileId);
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

	private void preformDownload(boolean toCrypt, RelativeLayout holder, final Button playPause, final SeekBar seekControl, final Chronometer chronoControl, final File sound, final String fileId) {
		final ProgressBar pbLoading = (ProgressBar) holder.getChildAt(Const.SoundControl.DOWNLOAD_PROGRESS);
		final ProgressBar pbLoadingBar = (ProgressBar) holder.getChildAt(Const.SoundControl.PROGREEBAR);
		final TextView percentTv = (TextView) holder.getChildAt(Const.SoundControl.PERCENT_TV);
		pbLoading.setVisibility(View.VISIBLE);
		pbLoadingBar.setVisibility(View.VISIBLE);
		percentTv.setVisibility(View.VISIBLE);
		playPause.setVisibility(View.INVISIBLE);
		seekControl.setVisibility(View.INVISIBLE);
		chronoControl.setVisibility(View.INVISIBLE);
		new FileManageApi().downloadFileToFile(toCrypt, sound, fileId, false, ctx, new ApiCallback<String>() {

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
					((Activity) ctx).runOnUiThread(new Runnable() {

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

	private int calculateNeedTextWidth(String text, Context c) {
		Paint paint = new Paint();
		Rect bounds = new Rect();

		int text_width = 0;

		paint.setTypeface(typeface);// your preference here
		paint.setTextSize(Utils.getPxFromSp(20, c.getResources()));// have this the same as your text size

		paint.getTextBounds(text, 0, text.length(), bounds);

		text_width = bounds.width();

		return text_width;
	}

	private boolean isMe(String userId) {

		if (Helper.getUserId().equals(userId)) {
			return true;
		}

		return false;
	}

	private String getCreatedTime(String created) {

		try {

			Timestamp stamp = new Timestamp(Long.valueOf(created) * 1000);
			Date date = new Date(stamp.getTime());
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

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

	private String getDateFormat(String createdString) {
		long created = Long.parseLong(createdString) * 1000;

		Date date = new Date(created);
		SimpleDateFormat dateFormat = new SimpleDateFormat(Const.DATE_SEPARATOR_FORMAT, Locale.getDefault());

		String rez = dateFormat.format(date);

		return rez;
	}

	public void setSeenBy(String seenBy) {
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
						if (Long.parseLong(newItems.get(i).getModified()) > Long.parseLong(data.get(j).getModified())
                                || newItems.get(i).seen_timestamp != data.get(j).seen_timestamp) {
							msg = newItems.get(i);
							msg.setMe(isMe(newItems.get(i).getUser_id()));
                            msg.seen_timestamp = newItems.get(i).seen_timestamp;
							msg = Message.decryptContent(ctx, newItems.get(i));
							data.set(j, newItems.get(i));
						}
					}
				}

				if (!isFound) {
					msg = newItems.get(i);
					msg.setMe(isMe(newItems.get(i).getUser_id()));
					msg = Message.decryptContent(ctx, newItems.get(i));
					data.add(msg);
				}
			}
		} else {
			Message msg;
			List<Integer> messIds = new ArrayList<Integer>();
			for (Message item : data) {
				messIds.add(item.getIntegerId());
			}
			for (int i = 0; i < newItems.size(); i++) {
				msg = newItems.get(i);
				if (messIds.contains(msg.getIntegerId())) {
					newItems.remove(i);
					i--;
					continue;
				}
				msg.setMe(isMe(newItems.get(i).getUser_id()));
				msg = Message.decryptContent(ctx, newItems.get(i));
				newItems.set(i, msg);
			}
			data.addAll(newItems);
		}

		Collections.sort(data, new MessageSortingById());
		addSeparatorDate();
		this.notifyDataSetChanged();
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
	
	public void setOnLongAndSimpleClickCustomListener(OnMessageLongAndSimpleClickCustomListener lis) {
		listenerLongAndSimpleClick = lis;
	}

	public interface OnMessageLongAndSimpleClickCustomListener {
		public void onLongClick(Message message);

		public void onSimpleClick(Message message);
	}
	
	public void setMessageDelted(String messageId) {
		for(Message item : data){
			if(item.getId().equals(messageId)){
				item.type = Const.MSG_TYPE_DELETED;
				break;
			}
		}
		notifyDataSetChanged();
	}
	
	private List<Message> tempMessageList = new ArrayList<Message>();
	public Message addTempMessage(String text, int type) {
		if(type != Const.MSG_TYPE_DEFAULT) return null;
		Message tempMess = new Message();
		tempMess.setText(text);
		tempMess.type = Const.MSG_TYPE_TEMP_MESS;
		tempMess.isMe = true;
		tempMess.created = String.valueOf((int)(System.currentTimeMillis() / 1000));
		try {
			tempMess.id = String.valueOf(Long.valueOf(data.get(data.size() - 1).id) + 10);
		} catch (Exception e) {
			tempMess.id = "10";
		}
		tempMessageList.add(tempMess);
		data.add(tempMess);
		setEndOfSearch(true); // disable pagging while message sending to web
		notifyDataSetChanged();
		return tempMess;
	}
	
	public void deleteAllTempChat() {
		for(Message item : tempMessageList){
			data.remove(item);
		}
		tempMessageList.clear();
		notifyDataSetChanged();
	}
	
	public void addNewMessage(Message mess) {
		mess = Message.decryptContent(ctx, mess);
		for(Message item : tempMessageList){
			if(item.text.equals(mess.text)){
				data.remove(item);
				tempMessageList.remove(item);
				break;
			}
		}
		data.add(mess);
		Collections.sort(data, new MessageSortingById());
		addSeparatorDate();
		if (data.size() >= totalCount) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
		notifyDataSetChanged();
	}
	
	public void tempMessageError(Message tempMessage) {
		tempMessage.type = Const.MSG_TYPE_TEMP_MESS_ERROR;
		if (data.size() >= totalCount) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
		notifyDataSetChanged();
	}
	
	public void prepareResend(Message message) {
		tempMessageList.remove(message);
		data.remove(message);
		if (data.size() >= totalCount) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
		notifyDataSetChanged();
	}
	
	public void addNewMessage(Message mess, Message tempToRemove) {
		mess = Message.decryptContent(ctx, mess);
		tempMessageList.remove(tempToRemove);
		data.remove(tempToRemove);
		data.add(mess);
		Collections.sort(data, new MessageSortingById());
		addSeparatorDate();
		if (data.size() >= totalCount) {
			setEndOfSearch(true);
		} else {
			setEndOfSearch(false);
		}
		notifyDataSetChanged();
	}

	public class ViewHolderChatMsg {

		// start: message item for my message
		public LinearLayout meMsgLayout;
		public LinearLayout meMsgLayoutBack;
		public TextView meMsgContent;
		public ImageView meThreadIndicator;
		public TextView meMsgTime;
		public FrameLayout meFlForGif;
        public View meMessageStatus;
		// public ImageView meGifView;
		public WebView meWebView;
		public View meViewForReadMore;
		// end: me msg

		public RelativeLayout meListenSound;
		public RelativeLayout youListenSound;

		public TextView meWatchVideo;
		public TextView youWatchVideo;

		public TextView meViewLocation;
		public TextView youViewLocation;

		public ImageView meViewImage;
		public ImageView youViewImage;

		public TextView meDownloadFile;

		public TextView youDownloadFile;

		public TextView seenByTv;

		// start: message item for you message
		public RelativeLayout youMsgLayout;
		public LinearLayout youMsgLayoutBack;
		public TextView youPersonName;
		public TextView youMsgContent;
		public ImageView youThreadIndicator;
		public TextView youMsgTime;
		public FrameLayout youFlForGif;
		public WebView youWebView;
		public View youViewForReadMore;
		// public ImageView youGifView;
		// end: you msg

		// start: loading bar
		public RelativeLayout loading_bar;

		// end: loading bar

		// start: date separator
		public RelativeLayout dateSeparator;
		public TextView sectionDate;

		// end: date separator

		public ViewHolderChatMsg(View view) {

			meMsgLayout = (LinearLayout) view.findViewById(R.id.meWholeLayout);
			meMsgLayoutBack = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
			// start: message item for my message
			meMsgTime = (TextView) view.findViewById(R.id.timeMe);
			meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
			meThreadIndicator = (ImageView) view.findViewById(R.id.me_image_view_threads_indicator);
			meViewForReadMore = view.findViewById(R.id.meViewForReadMoreClick);
            meMessageStatus = view.findViewById(R.id.meMessageStatus);

			meFlForGif = (FrameLayout) view.findViewById(R.id.meFlForWebView);
			// meGifView = (ImageView) view.findViewById(R.id.meGifView);
			meWebView = (WebView) view.findViewById(R.id.meWebView);
			// end: me msg

			meListenSound = (RelativeLayout) view.findViewById(R.id.meRlSound);
			youListenSound = (RelativeLayout) view.findViewById(R.id.youRlSound);

			meWatchVideo = (TextView) view.findViewById(R.id.meWatchVideo);
			youWatchVideo = (TextView) view.findViewById(R.id.youWatchVideo);

			meViewLocation = (TextView) view.findViewById(R.id.meViewLocation);
			youViewLocation = (TextView) view.findViewById(R.id.youViewLocation);

			meViewImage = (ImageView) view.findViewById(R.id.meViewImage);
			youViewImage = (ImageView) view.findViewById(R.id.youViewImage);

			meDownloadFile = (TextView) view.findViewById(R.id.meDownloadFile);

			youDownloadFile = (TextView) view.findViewById(R.id.youDownloadFile);

			seenByTv = (TextView) view.findViewById(R.id.tvSeenBy);

			youMsgLayout = (RelativeLayout) view.findViewById(R.id.youWholeLayout);
			youMsgLayoutBack = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutYou);
			// start: message item for you message
			youMsgTime = (TextView) view.findViewById(R.id.timeYou);
			youPersonName = (TextView) view.findViewById(R.id.youPersonName);
			youMsgContent = (TextView) view.findViewById(R.id.youMsgContent);
			youThreadIndicator = (ImageView) view.findViewById(R.id.you_image_view_threads_indicator);
			youViewForReadMore = view.findViewById(R.id.youViewForReadMoreClick);

			youFlForGif = (FrameLayout) view.findViewById(R.id.youFlForWebView);
			// youGifView = (ImageView) view.findViewById(R.id.youGifView);
			youWebView = (WebView) view.findViewById(R.id.youWebView);
			// end: you msg

			// start: loading bar
			loading_bar = (RelativeLayout) view.findViewById(R.id.loading_bar);
			// end: loading bar

			// start: date separator
			dateSeparator = (RelativeLayout) view.findViewById(R.id.dateSeparator);
			sectionDate = (TextView) view.findViewById(R.id.sectionDate);
			// end: date separator
		}
	}

}