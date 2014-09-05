package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseChatActivity;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

public class RecordVideoActivity extends BaseActivity {

	private static final int RESULT_FROM_GALLERY = 55;
	private static final int RESULT_FROM_CAMERA = 56;

	private ImageButton goBack;
	private ImageButton sendVideo;

	private String mFilePath = null;
	private String chatId;

	private VideoView mVideoView;

	private int mIsPlaying = 0; // 0 - play is on stop, 1 - play is on pause, 2
								// - playing

	private ProgressBar mPbForPlaying;
	private ImageView mPlayPause;
	private ImageView mStopSound;

	private Handler mHandlerForProgressBar = new Handler();
	private Runnable mRunnForProgressBar;

	private long mDurationOfVideo = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_video);

		goBack = (ImageButton) findViewById(R.id.goBack);
		goBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		sendVideo = (ImageButton) findViewById(R.id.sendVideo);
		sendVideo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
                try {
                    uploadVideo(mFilePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
		});

		mIsPlaying = 0;

		mVideoView = (VideoView) findViewById(R.id.videoView);
		mPbForPlaying = (ProgressBar) findViewById(R.id.progressBar);
		mPlayPause = (ImageView) findViewById(R.id.ivPlayPause);
		mStopSound = (ImageView) findViewById(R.id.ivStopSound);

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

		Bundle extras = getIntent().getExtras();
		chatId = extras.getString(Const.CHAT_ID);
		gotoGalleryOrCamera(extras.getInt(Const.INTENT_TYPE));

	}

	private void sendMsg(String fileId) {
        String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
        String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);
		new ChatApi().sendMessage(Const.MSG_TYPE_VIDEO, chatId, null, fileId, null, null, null, rootId, messageId, this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {
				if (result.isSuccess()) {
					AppDialog dialog = new AppDialog(RecordVideoActivity.this, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(RecordVideoActivity.this, false);
					dialog.setFailed(result.getResultData());
				}
			}
		});
	}

    private void uploadVideo(String filePath) throws FileNotFoundException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            uploadVideoAsync(filePath);
        } else {
            new BaseChatActivity.BuildTempFileAsync(this, getVideoName(Uri.parse(filePath)), new BaseChatActivity.OnTempFileCreatedListener() {
                @Override
                public void onTempFileCreated(String path, String name) {
                    uploadVideoAsync(path);
                }
            }).execute(getContentResolver().openInputStream(Uri.parse(filePath)));
        }
    }

    private void uploadVideoAsync(String path) {
        new FileManageApi().uploadFile(path, RecordVideoActivity.this, true, new ApiCallback<UploadFileModel>() {

            @Override
            public void onApiResponse(Result<UploadFileModel> result) {

                if (result.isSuccess()) {
                    sendMsg(result.getResultData().getFileId());
                } else {
                    AppDialog dialog = new AppDialog(RecordVideoActivity.this, true);
                    dialog.setFailed("");
                }
            }
        });
    }

	private void gotoGalleryOrCamera(int chooseWhereToGo) {
		switch (chooseWhereToGo) {
		case Const.VIDEO_INTENT_INT:

			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_no_camera_on_device));
			} else {

				try {
					Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, Const.MAX_RECORDING_TIME_VIDEO);
					File videoFolder = Utils.getFileDir(this);

					videoFolder.mkdirs(); // <----
					File video = new File(videoFolder, "video.mp4");
					Uri uriSavedVideo = Uri.fromFile(video);

					mFilePath = video.getPath();

					cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);
					startActivityForResult(cameraIntent, RESULT_FROM_CAMERA);
				} catch (Exception ex) {
					ex.printStackTrace();
					AppDialog dialog = new AppDialog(this, true);
					dialog.setFailed(getResources().getString(R.string.e_no_camera_on_device));
				}
			}

			break;

		case Const.GALLERY_INTENT_INT:

			Intent intent = new Intent();
			intent.setType("video/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, RESULT_FROM_GALLERY);

			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			Uri selectedVideoUri = data.getData();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                mFilePath = getVideoPath(selectedVideoUri);
            } else {
                mFilePath = selectedVideoUri.toString();
            }

            super.onActivityResult(requestCode, resultCode, data);
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}

    private boolean isRecordTooLong(long videoDuration) {
        if (videoDuration != 0 && videoDuration > Const.MAX_RECORDING_TIME_VIDEO * 1000) {

            AppDialog dialog = new AppDialog(RecordVideoActivity.this, true);
            dialog.setFailed(getResources().getString(R.string.e_record_time_to_long));

            return true;
        } else {
            return false;
        }
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
            mVideoView.setVideoURI(Uri.parse(mFilePath));
            mVideoView.start();
			mVideoView.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mDurationOfVideo = mVideoView.getDuration();

                    mPbForPlaying.setMax((int) mDurationOfVideo);

                    mRunnForProgressBar = new Runnable() {

                        @Override
                        public void run() {
                            mPbForPlaying.setProgress(mVideoView.getCurrentPosition());
                            if (mDurationOfVideo - 99 > mVideoView.getCurrentPosition()) {
                                mHandlerForProgressBar.postDelayed(mRunnForProgressBar, 100);
                            } else {
                                mPbForPlaying.setProgress(mVideoView.getDuration());
                            }
                        }
                    };
                    mHandlerForProgressBar.post(mRunnForProgressBar);
                    mIsPlaying = 2;
                }
            });

		} else if (mIsPlaying == 1) {
			mVideoView.start();
			mHandlerForProgressBar.post(mRunnForProgressBar);
			mIsPlaying = 2;
		}

	}

	private void stopPlaying() {
		mVideoView.stopPlayback();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mPbForPlaying.setProgress(0);
		mIsPlaying = 0;
	}

	private void pausePlaying() {
		mVideoView.pause();
		mHandlerForProgressBar.removeCallbacks(mRunnForProgressBar);
		mIsPlaying = 1;
	}

	private String getVideoPath(Uri uri) {

		if (uri.getScheme().equals("content")) {

			String[] proj = { MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DISPLAY_NAME };
			Cursor cursor = getContentResolver().query(uri, proj, null, null, null);

			int column_index_path = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
			cursor.moveToFirst();

			return cursor.getString(column_index_path);

		} else if (uri.getScheme().equals("file")) {
			return new File(URI.create(uri.toString())).getAbsolutePath();
		}

		return null;
	}

	private String getVideoName(Uri uri) {
        String name = "";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
            cursor.close();
        }
        return name;
    }

}