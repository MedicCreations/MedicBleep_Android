package com.clover.spika.enterprise.chat;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.robospice.ChatSpice;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.SendMessageResponse;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.services.robospice.CustomSpiceListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.ExtAudioRecorder;
import com.clover.spika.enterprise.chat.utils.Utils;
import com.octo.android.robospice.persistence.exception.SpiceException;

public class RecordAudioActivity extends BaseActivity {

	private static int START_PLAYING = 0;
	private static int PAUSE_PLAYING = 1;
	private static int STOP_PLAYING = 2;

	private static int PLAYING = 2;
	private static int PAUSE = 1;
	private static int STOP = 0;

	private boolean mIsRecording;
	private String mFilePath = null;
	private String mFileName = null;
	private ExtAudioRecorder mExtAudioRecorder;

	private Chronometer mRecordTime;

	private int mIsPlaying = STOP; // 0 - play is on stop, 1 - play is on pause,
									// 2 - playing
	private MediaPlayer mPlayer = null;
	private ImageView mPlayPause;
	private RelativeLayout mRlSoundControler;

	private ImageButton sendAudio;
	private ImageButton startRec;
	private ImageView recCircle;

	private AsyncTask<Void, Void, Void> recordingAsync;
	private CountDownTimer mRecordingTimer;

	private Chronometer firstChornometer;
	private TextView secondChronometer;
	private CountDownTimer soundLeft;
	private SeekBar seekBarSound;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);

		findViewById(R.id.goBack).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		sendAudio = (ImageButton) findViewById(R.id.sendAudio);
		sendAudio.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new FileManageApi().uploadFile(mFilePath, RecordAudioActivity.this, true, new ApiCallback<UploadFileModel>() {

					@Override
					public void onApiResponse(Result<UploadFileModel> result) {
						if (result.isSuccess()) {
							sendMsg(result.getResultData().getFileId());
						} else {
							AppDialog dialog = new AppDialog(RecordAudioActivity.this, false);
							dialog.setFailed(getResources().getString(R.string.e_error_uploading_file));
						}
					}
				});
			}
		});

		mRecordingTimer = new CountDownTimer(Const.MAX_RECORDING_TIME_VOICE, 1000) {

			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				new AppDialog(RecordAudioActivity.this, false).setInfo(getResources().getString(R.string.e_record_time_to_long));
				onRecord(false);
			}

		};

		recCircle = (ImageView) findViewById(R.id.recCircle);
		mRecordTime = (Chronometer) findViewById(R.id.recordTime);
		mRlSoundControler = (RelativeLayout) findViewById(R.id.soundControler);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPauseSound);

		startRec = (ImageButton) findViewById(R.id.startRec);
		startRec.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsRecording) {
					onRecord(!mIsRecording);
				} else {
					hideAndRestartSoundController();
					mIsRecording = true;
					onRecord(mIsRecording);
				}
			}
		});

		firstChornometer = (Chronometer) findViewById(R.id.firstChrono);
		secondChronometer = (TextView) findViewById(R.id.secondChrono);
		seekBarSound = (SeekBar) findViewById(R.id.seekBarSound);

		mRlSoundControler.setVisibility(View.INVISIBLE);

		seekBarSound.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				startPlaying(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (observer != null)
					observer.stop();
				observer = null;
				stopPlaying();
				if (firstChornometer != null) {
					firstChornometer.stop();
					firstChornometer.setBase(SystemClock.elapsedRealtime());
				}
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
		});
	}

	private void onRecord(boolean start) {
		if (start) {
			onPlay(STOP_PLAYING);
			mPlayPause.setImageResource(R.drawable.play_btn_selector);

			startRecordingAsync();

			startRec.setImageResource(R.drawable.selector_recording_audio);
			recCircle.setVisibility(View.VISIBLE);
			recCircle.setDrawingCacheEnabled(true);
			AnimUtils.rotationInfinite(recCircle, true, 3000);
		} else {

			if (!mIsRecording) {
				recordingAsync.cancel(true);
			}

			stopRecording();

			startRec.setImageResource(R.drawable.selector_record_audio);
			recCircle.setVisibility(View.INVISIBLE);
		}
	}

	private void startRecordingAsync() {
		recordingAsync = new BaseAsyncTask<Void, Void, Void>(this, false) {

			protected void onPreExecute() {
				mRecordTime.setVisibility(View.VISIBLE);
				mRecordTime.setBase(SystemClock.elapsedRealtime());
				mRecordTime.start();
				mRecordingTimer.start();
			};

			protected Void doInBackground(Void... params) {
				startRecording();
				return null;
			};
		}.execute();
	}

	private void startRecording() {
		setRecordingFile();

		mExtAudioRecorder = ExtAudioRecorder.getInstanse(false);
		mExtAudioRecorder.setOutputFile(mFilePath);
		mExtAudioRecorder.prepare();
		mExtAudioRecorder.start();
	}

	// stop recodrding for extaudio class
	private void stopRecording() {
		mExtAudioRecorder.stop();
		mExtAudioRecorder.release();
		mRecordTime.stop();
		mRecordTime.setVisibility(View.INVISIBLE);
		applyAlphaAnimationToView(mRecordTime, true);
		showSoundController();
		mIsRecording = false;
		mRecordingTimer.cancel();
	}

	private void hideAndRestartSoundController() {

		mRlSoundControler.setVisibility(View.INVISIBLE);

		sendAudio.setVisibility(View.INVISIBLE);

		// TODO RESET CHRONNO AND SEEK

		mRecordTime.setVisibility(View.INVISIBLE);

		mPlayPause.setImageResource(R.drawable.play_btn_selector);
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	private void showSoundController() {

		sendAudio.setVisibility(View.VISIBLE);
		applyAlphaAnimationToView(sendAudio, false);

		mIsPlaying = STOP;

		mPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == PLAYING) {
					// pause
					mPlayPause.setImageResource(R.drawable.play_btn_selector);
					onPlay(PAUSE_PLAYING);
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn_selector);
					onPlay(START_PLAYING);
				}
			}
		});

		mRlSoundControler.setVisibility(View.VISIBLE);

	}

	private void onPlay(int playPauseStop) { // 0 is to start playing, 1 is to
												// pause playing and 2 is for
												// stop playing

		if (playPauseStop == START_PLAYING) {
			startPlaying(0);
		} else if (playPauseStop == PAUSE_PLAYING) {
			// pausePlaying();
			stopPlaying();
		} else {
			stopPlaying();
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
				long elapsedMillis = SystemClock.elapsedRealtime() - firstChornometer.getBase();
				seekBarSound.setProgress((int) elapsedMillis);
				try {
					Thread.sleep(33);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private MediaObserver observer = null;

	private void startPlaying(final int offset) {
		if (mIsPlaying == STOP) {
			mPlayer = new MediaPlayer();
			try {
				mPlayer.setDataSource(mFilePath);
				mPlayer.prepare();
				mPlayer.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						seekBarSound.setMax(mp.getDuration());
						if (offset != 0) {
							mPlayer.seekTo((int) offset);
							firstChornometer.setBase((long) (SystemClock.elapsedRealtime() - offset));
						}
						startChronoSecond(mp.getDuration(), offset);
					}
				});
				mPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						stopChronoAndSeek();
						stopPlaying();
						mPlayPause.setImageResource(R.drawable.play_btn_selector);
					}
				});
				mPlayer.start();
				startChronoAndSeek();
				mIsPlaying = PLAYING;

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (mIsPlaying == PAUSE) {
			mPlayer.start();
			// CONTINUE SEEKBAR AND CHRONO
			mIsPlaying = PLAYING;
		}

	}

	private void startChronoSecond(int duration, int offset) {
		soundLeft = new CountDownTimer(duration - offset, 500) {

			@Override
			public void onTick(long millisUntilFinished) {
				int va = (int) ((millisUntilFinished % 60000) / 1000);
				secondChronometer.setText(String.format("-00:%02d", va));
			}

			@Override
			public void onFinish() {
				secondChronometer.setText("00:00");
			}
		};
		soundLeft.start();
	}

	protected void stopChronoAndSeek() {
		if (observer != null)
			observer.stop();
		seekBarSound.setProgress(0);
		firstChornometer.stop();
		firstChornometer.setBase(SystemClock.elapsedRealtime());
		if (soundLeft != null)
			soundLeft.cancel();
		secondChronometer.setText("00:00");
	}

	private void startChronoAndSeek() {
		firstChornometer.setBase(SystemClock.elapsedRealtime());
		firstChornometer.start();

		observer = new MediaObserver();
		new Thread(observer).start();
	}

	private void stopPlaying() {

		if (mPlayer != null) {
			mPlayer.release();
		}

		stopChronoAndSeek();
		mPlayer = null;
		mIsPlaying = STOP;
	}

	private void pausePlaying() {
		mPlayer.pause();
		mIsPlaying = PAUSE;
	}

	private void setRecordingFile() {
		mFilePath = new File(Utils.getFileDir(this), "voice.wav").getAbsolutePath();
		String[] items = mFilePath.split("/");
		mFileName = items[items.length - 1];
	}

	public void onPause() {
		super.onPause();
		if (mExtAudioRecorder != null) {
			stopRecording();
			mExtAudioRecorder.release();
			mExtAudioRecorder.stop();
			mExtAudioRecorder = null;
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mIsPlaying = STOP;
			// TODO RESET SEEKBAR AND CHRONO
			mPlayPause.setImageResource(R.drawable.play_btn_selector);
		}
	}

	public void onFinish() {
		if (mExtAudioRecorder != null) {
			mExtAudioRecorder.release();
			mExtAudioRecorder.stop();
			mExtAudioRecorder = null;
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			mIsPlaying = STOP;
			// TODO RESET SEEKBAR AND CHRONO
			mPlayPause.setImageResource(R.drawable.play_btn_selector);
		}

		super.onDestroy();
	}

	private void applyAlphaAnimationToView(View view, boolean toDisapear) {
		AlphaAnimation animation;
		if (!toDisapear) {
			animation = new AlphaAnimation(0.0f, 1.0f);
		} else {
			animation = new AlphaAnimation(1.0f, 0.0f);
		}
		animation.setDuration(200);
		view.startAnimation(animation);
	}

	@Override
	protected void onDestroy() {
		mRecordingTimer.cancel();
		mRecordingTimer = null;
		super.onDestroy();
	}

	private void sendMsg(String fileId) {
		String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
		String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);

		handleProgress(true);
		ChatSpice.SendMessage sendMessage = new ChatSpice.SendMessage(Const.MSG_TYPE_VOICE, getIntent().getExtras().getString(Const.CHAT_ID), mFileName, fileId, null, null, null,
				rootId, messageId, this);
		spiceManager.execute(sendMessage, new CustomSpiceListener<SendMessageResponse>() {

			@Override
			public void onRequestFailure(SpiceException ex) {
				handleProgress(false);
				Utils.onFailedUniversal(null, RecordAudioActivity.this);
			}

			@Override
			public void onRequestSuccess(SendMessageResponse result) {
				handleProgress(false);

				if (result.getCode() == Const.API_SUCCESS) {
					AppDialog dialog = new AppDialog(RecordAudioActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(RecordAudioActivity.this, false);
					dialog.setFailed(getResources().getString(R.string.e_error_uploading_file));
				}
			}
		});
	}
}