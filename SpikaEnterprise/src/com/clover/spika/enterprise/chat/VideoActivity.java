package com.clover.spika.enterprise.chat;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;

import java.io.File;

public class VideoActivity extends BaseActivity {

	private String sFileName = null;

	private VideoView mVideoView;

	private final int VIDEO_IS_PLAYING = 2;
	private final int VIDEO_IS_PAUSED = 1;
	private final int VIDEO_IS_STOPPED = 0;

	private int mIsPlaying = VIDEO_IS_STOPPED; // 0 - play is on stop, 1 - play
												// is on pause, 2
	// - playing

	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;

	private Handler mHandlerForProgressBar = new Handler();
	private Runnable mRunnForProgressBar;

	private long mDurationOfVideo = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_video);
		
		String fileId = getIntent().getExtras().getString(Const.FILE_ID);
		File videFile = new File(Utils.getFilesFolder() + "/" + fileId);

		if(videFile.exists()){
			sFileName = videFile.getAbsolutePath();
		}else{
			new FileManageApi().downloadFileToFile(videFile, fileId, true, this, new ApiCallback<String>() {

				@Override
				public void onApiResponse(Result<String> result) {
					if (result.isSuccess()) {
						sFileName = result.getResultData();
						// Play video
						mPlayPause.setImageResource(R.drawable.pause_btn_selector);
						onPlay(0);
					} else {
						AppDialog dialog = new AppDialog(VideoActivity.this, true);
						dialog.setFailed(getResources().getString(R.string.e_error_downloading_file));
					}
				}
			}, null);
		}

		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		findViewById(R.id.sendVideo).setVisibility(View.INVISIBLE);

		mVideoView = (VideoView) findViewById(R.id.videoView);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
		mPbForPlaying = (ProgressBar) findViewById(R.id.progressBar);

		mIsPlaying = VIDEO_IS_STOPPED;

		mPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == VIDEO_IS_PLAYING) {
					// pause
					mPlayPause.setImageResource(R.drawable.play_btn_selector);
					onPlay(2);
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn_selector);
					onPlay(0);
				}
			}
		});

	}

	private void onPlay(int playPauseStop) {
		if (playPauseStop == 0) {
			startPlaying();
		} else if (playPauseStop == 1) {
			pausePlaying();
		} else {
			stopPlaying();

		}
	}

	private void startPlaying() {
		try {
			if (mIsPlaying == VIDEO_IS_STOPPED) {
				mVideoView.requestFocus();

				mVideoView.setVideoURI(Uri.parse(sFileName));
				mVideoView.setVideoPath(sFileName);

				mVideoView.start();

				mVideoView.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						mDurationOfVideo = mVideoView.getDuration();
						mPbForPlaying.setMax((int) mDurationOfVideo);

						mRunnForProgressBar = new Runnable() {

							@Override
							public void run() {
								mPbForPlaying.setProgress((int) mVideoView.getCurrentPosition());
								if (mDurationOfVideo - 99 > mVideoView.getCurrentPosition()) {
									mHandlerForProgressBar.postDelayed(mRunnForProgressBar, 100);
								} else {
									mPbForPlaying.setProgress((int) mVideoView.getDuration());
									new Handler().postDelayed(new Runnable() {
										// *******wait for video to finish
										@Override
										public void run() {
											mPlayPause.setImageResource(R.drawable.play_btn);
											onPlay(2);
										}
									}, 120);
								}
							}
						};
						mHandlerForProgressBar.post(mRunnForProgressBar);
						mIsPlaying = VIDEO_IS_PLAYING;
					}
				});
				
				mVideoView.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mPlayPause.setImageResource(R.drawable.play_btn_selector);
						stopPlaying();
					}
				});

			} else if (mIsPlaying == VIDEO_IS_PAUSED) {
				mVideoView.start();
				mHandlerForProgressBar.post(mRunnForProgressBar);
				mIsPlaying = VIDEO_IS_PLAYING;
			}
		} catch (Exception e) {
			AppDialog dialog = new AppDialog(this, true);
			dialog.setFailed(getResources().getString(R.string.e_something_went_wrong));
		}
	}

	private void stopPlaying() {
		mVideoView.stopPlayback();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mIsPlaying = VIDEO_IS_STOPPED;
	}

	private void pausePlaying() {
		mVideoView.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = VIDEO_IS_PAUSED;
	}

}
