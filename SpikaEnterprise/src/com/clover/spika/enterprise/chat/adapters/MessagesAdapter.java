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
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.LocationActivity;
import com.clover.spika.enterprise.chat.PhotoActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.VideoActivity;
import com.clover.spika.enterprise.chat.VoiceActivity;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.ProgressBarListeners;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.MessageSortingById;
import com.clover.spika.enterprise.chat.utils.Utils;

public class MessagesAdapter extends BaseAdapter {

	private Context ctx;
	private List<Message> data;
	private String seenBy = "";

	private SparseIntArray dateSeparator = new SparseIntArray();

	private ImageLoader imageLoader;

	private boolean endOfSearch = false;
	private int totalCount = 0;
	
	private int displayWidth = 0;
	Typeface typeface;
	
	private boolean isDownloadingSound = false;
	private MediaPlayer currentMediaPlayer = null;
	private String currentPlayingPath = null;
	private Button activePlayIcon = null;
	
	public MessagesAdapter(Context context, List<Message> arrayList) {
		this.ctx = context;
		this.data = arrayList;

		imageLoader = ImageLoader.getInstance(context);
		imageLoader.setDefaultImage(R.drawable.default_user_image);
		
		displayWidth = context.getResources().getDisplayMetrics().widthPixels;
		typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
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
		holder.loading_bar_img.setBackgroundColor(Color.TRANSPARENT);

		holder.meMsgLayout.setVisibility(View.GONE);
		holder.youMsgLayout.setVisibility(View.GONE);

		holder.meMsgContent.setVisibility(View.GONE);
		holder.meMsgContent.setTypeface(null, Typeface.NORMAL);
		holder.youMsgContent.setVisibility(View.GONE);
		holder.youMsgContent.setTypeface(null, Typeface.NORMAL);

		holder.meViewImage.setVisibility(View.GONE);
		holder.youViewImage.setVisibility(View.GONE);

		holder.meListenSound.setVisibility(View.GONE);
		holder.youListenSound.setVisibility(View.GONE);

		holder.meWatchVideo.setVisibility(View.GONE);
		holder.youWatchVideo.setVisibility(View.GONE);

		holder.meViewLocation.setVisibility(View.GONE);
		holder.youViewLocation.setVisibility(View.GONE);

		holder.meDownloadFile.setVisibility(View.GONE);
		holder.youDownloadFile.setVisibility(View.GONE);

		holder.loading_bar.setVisibility(View.GONE);
		
		holder.meWebView.setVisibility(View.GONE);
		holder.meWebView.loadUrl("about:blank");
		holder.meFlForWebView.setVisibility(View.GONE);
		
		holder.youWebView.setVisibility(View.GONE);
		holder.youWebView.loadUrl("about:blank");
		holder.youFlForWebView.setVisibility(View.GONE);

		// Assign values
		final Message msg = getItem(position);

		if (msg.isMe()) {
			// My chat messages

			holder.meMsgLayout.setVisibility(View.VISIBLE);

			holder.meMsgTime.setText(getCreatedTime(msg.getCreated()));
			
			if(msg.getType() == Const.MSG_TYPE_PHOTO || msg.getType() == Const.MSG_TYPE_GIF){
				holder.meMsgLayoutBack.setPadding(0, 0, 0, 0);
			}else{
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
				holder.meMsgLayoutBack.setPadding(padding, padding, padding, padding);
			}

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
			} else if (msg.getType() == Const.MSG_TYPE_GIF) {
				
				holder.meWebView.setVisibility(View.VISIBLE);
				holder.meFlForWebView.setVisibility(View.VISIBLE);
				
				String x = "<!DOCTYPE html><html><body><img src=\""+msg.getText()+"\" alt=\"Smileyface\" width=\"100%\" height=\"100%\"></body></html>";
				holder.meWebView.loadData(x, "text/html", "utf-8");
				
				holder.meWebView.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_UP){
							Intent intent = new Intent(ctx, PhotoActivity.class);
							intent.putExtra(Const.IMAGE, msg.getText());
							intent.putExtra(Const.TYPE, msg.getType());
							ctx.startActivity(intent);
						}
						return false;
					}
				});
				
			}else if (msg.getType() == Const.MSG_TYPE_VIDEO) {
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
				setVoiceControls(msg, holder.meListenSound);
				
//				holder.meListenSound.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(ctx, VoiceActivity.class);
//						intent.putExtra(Const.FILE_ID, msg.getFile_id());
//						ctx.startActivity(intent);
//					}
//				});
			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.meDownloadFile.setVisibility(View.VISIBLE);
				holder.meDownloadFile.setText(msg.getText());
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

			} else if (msg.getType() == Const.MSG_TYPE_DELETED) {
				holder.meMsgContent.setVisibility(View.VISIBLE);
				holder.meMsgContent.setText(ctx.getString(R.string.message_deleted));
				holder.meMsgContent.setTypeface(null, Typeface.ITALIC);
			}

			if (!TextUtils.isEmpty(msg.getChildListText())) {
				holder.meThreadIndicator.setVisibility(View.VISIBLE);
				holder.meThreadIndicator.setImageResource(R.drawable.ic_thread_root_white);
			} else if (msg.getRootId() > 0) {
				holder.meThreadIndicator.setVisibility(View.VISIBLE);
				holder.meThreadIndicator.setImageResource(R.drawable.ic_thread_reply);
			} else {
				holder.meThreadIndicator.setImageDrawable(null);
				holder.meThreadIndicator.setVisibility(View.GONE);
			}
		} else {
			// Chat member messages, not mine

			holder.youMsgLayout.setVisibility(View.VISIBLE);
			
			if (!msg.getImageThumb().equals((String) holder.profileImage.getTag())) {
				holder.profileImage.setImageDrawable(null);
				imageLoader.displayImage(convertView.getContext(), msg.getImageThumb(), holder.profileImage);
				holder.profileImage.setTag(msg.getImageThumb());
			}

			holder.youMsgTime.setText(getCreatedTime(msg.getCreated()));
			holder.youPersonName.setText(msg.getFirstname() + " " + msg.getLastname());
			
			if(msg.getType() == Const.MSG_TYPE_PHOTO || msg.getType() == Const.MSG_TYPE_GIF){
				holder.youMsgLayoutBack.setPadding(0, 0, 0, 0);
				((LayoutParams)holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
			}else if(msg.getType() == Const.MSG_TYPE_LOCATION || msg.getType() == Const.MSG_TYPE_VIDEO){
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);
				((LayoutParams)holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
			}else if(msg.getType() == Const.MSG_TYPE_VOICE){
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);
				((LayoutParams)holder.youMsgLayoutBack.getLayoutParams()).weight = 1;
			}else{
				int padding = Utils.getPxFromDp(10, convertView.getContext().getResources());
				holder.youMsgLayoutBack.setPadding(padding, padding, padding, padding);
				
				int textWidth = msg.getTextWidth();
				
				if(textWidth == -1){
					textWidth = calculateNeedTextWidth(msg.getText(), ctx);
					msg.setTextWidth(textWidth);
				}
				
				int timeWidth = msg.getTimeWidth();
				
				if(timeWidth == -1){
					timeWidth = calculateNeedTextWidth(getCreatedTime(msg.getCreated()), ctx);
					msg.setTimeWidth(timeWidth);
				}
				
				if(textWidth > displayWidth - Utils.getPxFromDp(75, ctx.getResources()) - timeWidth){
					((LayoutParams)holder.youMsgLayoutBack.getLayoutParams()).weight = 1;
				}else{
					((LayoutParams)holder.youMsgLayoutBack.getLayoutParams()).weight = 0;
				}
			}

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
			} else if (msg.getType() == Const.MSG_TYPE_GIF) {
				
				holder.youWebView.setVisibility(View.VISIBLE);
				holder.youFlForWebView.setVisibility(View.VISIBLE);
				
				String x = "<!DOCTYPE html><html><body><img src=\""+msg.getText()+"\" alt=\"Smileyface\" width=\"100%\" height=\"100%\"></body></html>";
				holder.youWebView.loadData(x, "text/html", "utf-8");
				
				holder.youWebView.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if(event.getAction() == MotionEvent.ACTION_UP){
							Intent intent = new Intent(ctx, PhotoActivity.class);
							intent.putExtra(Const.IMAGE, msg.getText());
							intent.putExtra(Const.TYPE, msg.getType());
							ctx.startActivity(intent);
						}
						return false;
					}
				});
				
			}else if (msg.getType() == Const.MSG_TYPE_VIDEO) {

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
				setVoiceControls(msg, holder.youListenSound);
				
//				holder.youListenSound.setVisibility(View.VISIBLE);
//				holder.youListenSound.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Intent intent = new Intent(ctx, VoiceActivity.class);
//						intent.putExtra(Const.FILE_ID, msg.getFile_id());
//						ctx.startActivity(intent);
//					}
//				});

			} else if (msg.getType() == Const.MSG_TYPE_FILE) {

				holder.youDownloadFile.setVisibility(View.VISIBLE);
				holder.youDownloadFile.setText(msg.getText());
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

			} else if (msg.getType() == Const.MSG_TYPE_DELETED) {
				holder.youPersonName.setText("");
				holder.youMsgContent.setVisibility(View.VISIBLE);
				holder.youMsgContent.setText(ctx.getString(R.string.message_deleted));
				holder.youMsgContent.setTypeface(null, Typeface.ITALIC);
			}

			if (!TextUtils.isEmpty(msg.getChildListText())) {
				holder.youThreadIndicator.setVisibility(View.VISIBLE);
				holder.youThreadIndicator.setImageResource(R.drawable.ic_thread_root);
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
			holder.sectionDate.setText(getDateFormat(msg.getCreated()));
		}

		// Paging animation
		if (position == (0) && !endOfSearch) {
			holder.loading_bar.setVisibility(View.VISIBLE);

			Helper.startPaggingAnimation(ctx, holder.loading_bar_img);

			if (ctx instanceof ChatActivity) {
				((ChatActivity) ctx).getMessages(false, false, true, false, false, false);
			}
		}

		// Check if last message
		if (position == (getCount() - 1) && !TextUtils.isEmpty(seenBy)) {
			holder.seenByTv.setText("Seen by " + seenBy);
			holder.seenByTv.setVisibility(View.VISIBLE);
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

		return convertView;
	}
	
	private double totalOfDownload = -1;
	private void setVoiceControls(final Message msg, final RelativeLayout holder) {
		
		final Button playPause = (Button) holder.getChildAt(Const.SoundControl.PLAY_BUTTON);
		final SeekBar seekControl = (SeekBar) holder.getChildAt(Const.SoundControl.SEEKBAR);
		final Chronometer chronoControl = (Chronometer) holder.getChildAt(Const.SoundControl.CHRONOMETER);
		
		playPause.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File sound = new File(Utils.getFilesFolder() + "/" + msg.getFile_id());
				if(sound.exists()){
					Log.d("LOG", sound.getPath()+" is EXISTS");
					if(currentMediaPlayer == null){
						currentMediaPlayer = new MediaPlayer();
						try {
							currentMediaPlayer.setDataSource(sound.getAbsolutePath());
							currentMediaPlayer.prepare();
							currentMediaPlayer.start();
							currentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

								@Override
								public void onCompletion(MediaPlayer mp) {
									currentPlayingPath = null;
									currentMediaPlayer.stop();
									currentMediaPlayer.release();
									currentMediaPlayer = null;
									playPause.setText("P");
								}
							});
							currentPlayingPath = sound.getAbsolutePath();
							playPause.setText("AKK");
							activePlayIcon = playPause;
						} catch (IOException e) {
							e.printStackTrace();
							currentMediaPlayer = null;
						}
					}else{
						if(currentPlayingPath != null && currentPlayingPath.equals(sound.getAbsolutePath())){
							currentMediaPlayer.stop();
							currentMediaPlayer.release();
							currentMediaPlayer = null;
							playPause.setText("P");
						}else{
							currentMediaPlayer.stop();
							currentMediaPlayer.release();
							currentMediaPlayer = null;
							activePlayIcon.setText("P");
							try {
								currentMediaPlayer = new MediaPlayer();
								currentMediaPlayer.setDataSource(sound.getAbsolutePath());
								currentMediaPlayer.prepare();
								currentMediaPlayer.start();
								currentMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {
										currentPlayingPath = null;
										currentMediaPlayer.stop();
										currentMediaPlayer.release();
										currentMediaPlayer = null;
										playPause.setText("P");
									}
								});
								currentPlayingPath = sound.getAbsolutePath();
								playPause.setText("AKK");
								activePlayIcon = playPause;
							} catch (IOException e) {
								e.printStackTrace();
								currentMediaPlayer = null;
							}
						}
					}
					
				}else{
					if(isDownloadingSound){
						return;
					}
					isDownloadingSound = true;
					totalOfDownload = -1;
					
					final ProgressBar pbLoading = (ProgressBar) holder.getChildAt(Const.SoundControl.DOWNLOAD_PROGRESS);
					final ProgressBar pbLoadingBar = (ProgressBar) holder.getChildAt(Const.SoundControl.PROGREEBAR);
					final TextView percentTv = (TextView) holder.getChildAt(Const.SoundControl.PERCENT_TV);
					pbLoading.setVisibility(View.VISIBLE);
					pbLoadingBar.setVisibility(View.VISIBLE);
					percentTv.setVisibility(View.VISIBLE);
					playPause.setVisibility(View.INVISIBLE);
					seekControl.setVisibility(View.INVISIBLE);
					chronoControl.setVisibility(View.INVISIBLE);
					new FileManageApi().downloadFileToFile(sound, msg.getFile_id(), false, ctx, new ApiCallback<String>() {
						
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
							if(totalOfDownload == -1) {
								totalOfDownload = total;
								pbLoadingBar.setMax((int) totalOfDownload);
							}
						}
						
						@Override
						public void onProgress(long current) {
							if(totalOfDownload != -1){
								pbLoadingBar.setProgress((int) current);
								final String percent = String.valueOf(((int)(100 * current / (double)totalOfDownload)));
								((Activity)ctx).runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										percentTv.setText(String.valueOf(percent + "%"));	
									}
								});
							}
						}
						
						@Override
						public void onFinish() {}
					});
				}
			}
		});
	}
	
	private int calculateNeedTextWidth(String text, Context c){
		Paint paint = new Paint();
		Rect bounds = new Rect();

		int text_width = 0;

		paint.setTypeface(typeface);// your preference here
		paint.setTextSize(Utils.getPxFromSp(20, c.getResources()));// have this the same as your text size

		paint.getTextBounds(text, 0, text.length(), bounds);

		text_width =  bounds.width();
		
		return text_width;
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
						if (Long.parseLong(newItems.get(i).getModified()) > Long.parseLong(data.get(j).getModified())) {
							msg = newItems.get(i);
							msg.setMe(isMe(newItems.get(i).getUser_id()));
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
			for(Message item : data){
				messIds.add(item.getIntegerId());
			}
			for (int i = 0; i < newItems.size(); i++) {
				msg = newItems.get(i);
				if(messIds.contains(msg.getIntegerId())){
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

	public class ViewHolderChatMsg {

		// start: message item for my message
		public LinearLayout meMsgLayout;
		public LinearLayout meMsgLayoutBack;
		public TextView meMsgContent;
		public ImageView meThreadIndicator;
		public TextView meMsgTime;
		public FrameLayout meFlForWebView;
		public WebView meWebView;
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
		public ImageView profileImage;
		public FrameLayout youFlForWebView;
		public WebView youWebView;
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

			meMsgLayout = (LinearLayout) view.findViewById(R.id.meWholeLayout);
			meMsgLayoutBack = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
			// start: message item for my message
			meMsgTime = (TextView) view.findViewById(R.id.timeMe);
			meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
			meThreadIndicator = (ImageView) view.findViewById(R.id.me_image_view_threads_indicator);
			
			meFlForWebView = (FrameLayout) view.findViewById(R.id.meFlForWebView);
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
			profileImage = (ImageView) view.findViewById(R.id.youProfileImage);
			
			youFlForWebView = (FrameLayout) view.findViewById(R.id.youFlForWebView);
			youWebView = (WebView) view.findViewById(R.id.youWebView);
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