package com.clover.spika.enterprise.chat;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.animation.AnimUtils;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.ExtAudioRecorder;

public class RecordAudioActivity extends BaseActivity {

	private static int START_PLAYING = 0;
	private static int PAUSE_PLAYING = 1;
	private static int STOP_PLAYING = 2;

	private static int PLAYING = 2;
	private static int PAUSE = 1;
	private static int STOP = 0;

	private boolean mIsRecording;
	private String sFileName = null;
	private ExtAudioRecorder mExtAudioRecorder;

	private Chronometer mRecordTime;
	private Handler mHandlerForProgressBar = new Handler();
	private Runnable mRunnForProgressBar;

	private int mIsPlaying = STOP; // 0 - play is on stop, 1 - play is on pause,
									// 2 - playing
	private MediaPlayer mPlayer = null;
	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;
	private ImageView mStopSound;
	private RelativeLayout mRlSoundControler;

	private ImageButton sendAudio;
	private ImageButton startRec;
	private ImageView recCircle;

	private AsyncTask<Void, Void, Void> recordingAsync;
	private CountDownTimer mRecordingTimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_audio);
		disableSidebar();

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
				new FileManageApi().uploadFile(sFileName, RecordAudioActivity.this, true, new ApiCallback<UploadFileModel>() {

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
				new AppDialog(RecordAudioActivity.this, false).setInfo(getResources().getString(R.string.e_exceed_voice_duration));
				onRecord(false);
			}

		};

		recCircle = (ImageView) findViewById(R.id.recCircle);
		mRecordTime = (Chronometer) findViewById(R.id.recordTime);
		mRlSoundControler = (RelativeLayout) findViewById(R.id.soundControler);
		mPbForPlaying = (ProgressBar) findViewById(R.id.progressBar);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
		mStopSound = (ImageView) findViewById(R.id.ivStopSound);

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
	}

	private void onRecord(boolean start) {
		if (start) {
			onPlay(STOP_PLAYING);
			mPlayPause.setImageResource(R.drawable.play_btn);

			startRecordingAsync();

			startRec.setImageResource(R.drawable.icon_audio_rec);
			recCircle.setVisibility(View.VISIBLE);
			recCircle.setDrawingCacheEnabled(true);
			AnimUtils.rotationInfinite(recCircle, false, 3000);
		} else {

			if (!mIsRecording) {
				recordingAsync.cancel(true);
			}

			stopRecording();

			startRec.setImageResource(R.drawable.icon_audio_start);
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
		mExtAudioRecorder.setOutputFile(sFileName);
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

		mPbForPlaying.setProgress(0);
		mRecordTime.setVisibility(View.INVISIBLE);

		mPlayPause.setBackgroundResource(R.drawable.play_btn);
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
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(PAUSE_PLAYING);
				} else {
					// play
					mPlayPause.setImageResource(R.drawable.pause_btn);
					onPlay(START_PLAYING);
				}
			}
		});

		mStopSound.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsPlaying == PLAYING || mIsPlaying == PAUSE) {
					// stop
					mPlayPause.setImageResource(R.drawable.play_btn);
					onPlay(STOP_PLAYING);
				}
			}
		});

		mRlSoundControler.setVisibility(View.VISIBLE);

	}

	private void onPlay(int playPauseStop) { // 0 is to start playing, 1 is to
												// pause playing and 2 is for
												// stop playing

		if (playPauseStop == START_PLAYING) {
			startPlaying();
		} else if (playPauseStop == PAUSE_PLAYING) {
			pausePlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		if (mIsPlaying == STOP) {
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
				mIsPlaying = PLAYING;

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (mIsPlaying == PAUSE) {
			mPlayer.start();
			mHandlerForProgressBar.post(mRunnForProgressBar);
			mIsPlaying = PLAYING;
		}

	}

	private void stopPlaying() {

		if (mPlayer != null) {
			mPlayer.release();
		}

		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mPlayer = null;
		mIsPlaying = STOP;
	}

	private void pausePlaying() {
		mPlayer.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = PAUSE;
	}

	private void setRecordingFile() {

		File audio = getFileDir(getApplicationContext());
		audio.mkdirs();
		sFileName = audio.getAbsolutePath() + "/voice.wav";
	}

	private File getFileDir(Context context) {
		File cacheDir = null;

		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), Const.APP_FILES_DIRECTORY);
		else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();

		return cacheDir;
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
			mPbForPlaying.setProgress(0);
			mPlayPause.setImageResource(R.drawable.play_btn);
		}
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
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
			mPbForPlaying.setProgress(0);
			mPlayPause.setImageResource(R.drawable.play_btn);
		}
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);

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
		new ChatApi().sendMessage(Const.MSG_TYPE_VOICE, getIntent().getExtras().getString(Const.CHAT_ID), null, fileId, null, null, null, this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {
				if (result.isSuccess()) {
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