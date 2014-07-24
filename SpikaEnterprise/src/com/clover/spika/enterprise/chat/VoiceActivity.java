package com.clover.spika.enterprise.chat;

import java.io.IOException;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.utils.Const;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class VoiceActivity extends BaseActivity {

	private static String sFileName = null;

	private Handler mHandlerForProgressBar = new Handler();
	private Runnable mRunnForProgressBar;

	private int mIsPlaying = 0; // 0 - play is on stop, 1 - play is on pause, 2
								// - playing
	private MediaPlayer mPlayer = null;
	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;
	private ImageView mStopSound;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);
		disableSidebar();

		findViewById(R.id.sendAudio).setVisibility(View.INVISIBLE);
		findViewById(R.id.goBack).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		new FileManageApi().downloadFile(getIntent().getExtras().getString(Const.FILE_ID), this, new ApiCallback<String>() {

			@Override
			public void onApiResponse(Result<String> result) {
				if (result.isSuccess()) {
					sFileName = result.getResultData();
				} else {
					AppDialog dialog = new AppDialog(VoiceActivity.this, true);
					dialog.setFailed(getResources().getString(R.string.e_error_downloading_file));
				}
			}
		});

		mPbForPlaying = (ProgressBar) findViewById(R.id.progressBar);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
		mStopSound = (ImageView) findViewById(R.id.ivStopSound);

		mIsPlaying = 0;

		mPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == 2) {
					// pause
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(1);
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn);
					onPlay(0);
				}
			}
		});

		mStopSound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == 2 || mIsPlaying == 1) {
					// stop
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(2);
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
		if (mIsPlaying == 0) {
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(sFileName);
				mPlayer.prepare();
				mPlayer.start();
				mPbForPlaying.setMax((int) mPlayer.getDuration());

				mRunnForProgressBar = new Runnable() {

					@Override
					public void run() {
						mPbForPlaying.setProgress((int) mPlayer.getCurrentPosition());
						if (mPlayer.getDuration() - 99 > mPlayer.getCurrentPosition()) {
							mHandlerForProgressBar.postDelayed(mRunnForProgressBar, 100);
						} else {
							mPbForPlaying.setProgress((int) mPlayer.getDuration());
						}
					}
				};
				mHandlerForProgressBar.post(mRunnForProgressBar);
				mIsPlaying = 2;

			} catch (IOException e) {
				Log.e("LOG", "prepare() failed");
			}
		} else if (mIsPlaying == 1) {
			mPlayer.start();
			mHandlerForProgressBar.post(mRunnForProgressBar);
			mIsPlaying = 2;
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mPlayer = null;
		mIsPlaying = 0;
	}

	private void pausePlaying() {
		mPlayer.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = 1;
	}

	public void onPause() {
		super.onPause();

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mIsPlaying = 0;
			mPbForPlaying.setProgress(0);
			mPlayPause.setImageResource(R.drawable.play_btn);
		}
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
	}

}
