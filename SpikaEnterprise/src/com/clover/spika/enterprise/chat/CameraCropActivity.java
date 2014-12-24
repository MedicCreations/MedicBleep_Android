package com.clover.spika.enterprise.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.cropper.CropImageView;
import com.clover.spika.enterprise.chat.views.cropper.util.ScalingUtilities;
import com.clover.spika.enterprise.chat.views.cropper.util.ScalingUtilities.ScalingLogic;

public class CameraCropActivity extends BaseActivity implements OnClickListener {

	private CropImageView cropImageView;

	Bitmap croppedImage;

	// thumbnail width and height
	private static final int THUMB_WIDTH = 100;
	private static final int THUMB_HEIGHT = 100;
	// compressed max size
	private static final double MAX_SIZE = 640;

	// Gallery type marker
	private static final int GALLERY = 1;
	// Camera type marker
	private static final int CAMERA = 2;
	// Uri for captured image so we can get image path
	private String _path;

	private static boolean return_flag;

	private String mFilePath;
	private String mFileThumbPath;
	private String mFileName;
	private String chatId = "";

	private LinearLayout btnSend;
	private LinearLayout btnCancel;

	private boolean mIsOverJellyBean;
	private boolean mCompressImages;
	private boolean mIsSamsung = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_crop);

		cropImageView = (CropImageView) findViewById(R.id.CropImageView);
		cropImageView.setAspectRatio(20, 20);
		// TODO set fixed rect uncomment
		//cropImageView.setFixedAspectRatio(true);

		// ROTATE
		// final Button rotateButton = (Button)
		// findViewById(R.id.Button_rotate);
		// rotateButton.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
		// }
		// });

		mIsOverJellyBean = Build.VERSION.SDK_INT > 18;
		mCompressImages = getResources().getBoolean(R.bool.send_compressed_images);

		return_flag = false;

		btnSend = (LinearLayout) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);

		if (android.os.Build.MANUFACTURER.contains("samsung")) {
			mIsSamsung = true;
		} else {
			mIsSamsung = false;
		}

		getImageIntents();
	}

	@SuppressLint("InlinedApi")
	private void getImageIntents() {
		if (getIntent().getStringExtra(Const.INTENT_TYPE).equals(Const.GALLERY_INTENT)) {
			if (mIsOverJellyBean) {
				Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				startActivityForResult(intent, GALLERY);
			} else {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				this.startActivityForResult(intent, GALLERY);
			}
		} else {
			try {
				startCamera();
			} catch (Exception ex) {
				ex.printStackTrace();

				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_failed_openning_camera));
			}
		}

		if (getIntent().getExtras().containsKey(Const.CHAT_ID)) {
			chatId = getIntent().getStringExtra(Const.CHAT_ID);
		}
	}

	public void startCamera() {

		// Check if camera exists
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

			AppDialog dialog = new AppDialog(this, true);
			dialog.setFailed(getResources().getString(R.string.e_no_camera_on_device));
		} else {

			try {
				long date = System.currentTimeMillis();
				String filename = DateFormat.format("yyyy-MM-dd_kk.mm.ss", date).toString() + ".jpg";

				_path = this.getExternalCacheDir() + "/" + filename;

				File file = new File(_path);
				Uri outputFileUri = Uri.fromFile(file);

				if (mIsSamsung) {
					CameraActivityForSamsung.start(_path, this);
				} else {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
					startActivityForResult(intent, CAMERA);
				}

			} catch (Exception ex) {
				ex.printStackTrace();

				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_no_camera_on_device));
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Image rotation for samsung mobile phones

		String pManufacturer = android.os.Build.MANUFACTURER;
		String pModel = android.os.Build.MODEL;

		if ("GT-I9300".equals(pModel) && "samsung".equals(pManufacturer)) {

			RelativeLayout main = (RelativeLayout) findViewById(R.id.relativeLayout_main);
			main.invalidate();

			setContentView(R.layout.activity_camera_crop);

			cropImageView = (CropImageView) findViewById(R.id.CropImageView);

			File file = new File(_path);
			boolean exists = file.exists();

			if (exists) {
				onPhotoTaken(_path);
			} else {
				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_something_went_wrong));
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {

			case GALLERY:
				if (mIsOverJellyBean) {
					Uri uri = null;
					if (data != null) {
						uri = data.getData();
						String selected_image_path = Helper.getImagePath(this, uri, mIsOverJellyBean);
						onPhotoTaken(selected_image_path);
					} else {
						AppDialog dialog = new AppDialog(this, true);
						dialog.setFailed(getResources().getString(R.string.e_while_loading_image_from_gallery));
					}
				} else {
					try {
						Uri selected_image = data.getData();
						String selected_image_path = Helper.getImagePath(this, selected_image, mIsOverJellyBean);
						onPhotoTaken(selected_image_path);
					} catch (Exception e) {
						e.printStackTrace();

						AppDialog dialog = new AppDialog(this, true);
						dialog.setFailed(getResources().getString(R.string.e_while_loading_image_from_gallery));
					}
				}
				break;

			case CAMERA:
				File file = new File(_path);
				boolean exists = file.exists();
				if (exists) {
					onPhotoTaken(_path);
				} else {
					AppDialog dialog = new AppDialog(this, true);
					dialog.setFailed(getResources().getString(R.string.e_something_went_wrong_while_taking_a_picture));
				}
				break;

			default:
				finish();
				break;
			}
		} else {
			// if there is no image, just finish the activity
			finish();
		}
	}

	protected void onPhotoTaken(String path) {

		if (mIsSamsung) {
			String fileName = Uri.parse(path).getLastPathSegment();
			mFilePath = path;
			mFileThumbPath = CameraCropActivity.this.getExternalCacheDir() + "/" + fileName + "_thumb";
		} else {
			String fileName = Uri.parse(path).getLastPathSegment();
			mFilePath = CameraCropActivity.this.getExternalCacheDir() + "/" + fileName;
			mFileThumbPath = CameraCropActivity.this.getExternalCacheDir() + "/" + fileName + "_thumb";
		}

		String[] items = mFilePath.split("/");
		mFileName = items[items.length - 1];

		if (!path.equals(mFilePath)) {
			try {
				Helper.copyStream(new FileInputStream(new File(path)), new FileOutputStream(new File(mFilePath)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		BaseAsyncTask<String, Void, byte[]> task = new BaseAsyncTask<String, Void, byte[]>(this, true) {

			Bitmap mBitmap;

			@SuppressWarnings("deprecation")
			@Override
			protected byte[] doInBackground(String... params) {
				try {

					if (params == null) {
						return null;
					}

					File f = new File(params[0]);
					ExifInterface exif = new ExifInterface(f.getPath());
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

					int angle = 0;

					if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
						angle = 90;
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
						angle = 180;
					} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
						angle = 270;
					}

					BitmapFactory.Options optionsMeta = new BitmapFactory.Options();
					optionsMeta.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(f.getAbsolutePath(), optionsMeta);

					int actualHeight = optionsMeta.outHeight;
					int actualWidth = optionsMeta.outWidth;

					optionsMeta.inJustDecodeBounds = false;
					optionsMeta.inPurgeable = true;
					optionsMeta.inInputShareable = true;
					optionsMeta.inTempStorage = new byte[16 * 1024];

					float maxHeight = 1024.0f;
					float maxWidth = 1024.0f;

					optionsMeta.inSampleSize = Helper.calculateInSampleSize(optionsMeta, (int) maxWidth, (int) maxHeight);

					float imgRatio = (float) actualWidth / (float) actualHeight;
					float maxRatio = maxWidth / maxHeight;

					if (actualHeight > maxHeight || actualWidth > maxWidth) {
						if (imgRatio < maxRatio) {
							imgRatio = maxHeight / actualHeight;
							actualWidth = (int) (imgRatio * actualWidth);
							actualHeight = (int) maxHeight;
						} else if (imgRatio > maxRatio) {
							imgRatio = maxWidth / actualWidth;
							actualHeight = (int) (imgRatio * actualHeight);
							actualWidth = (int) maxWidth;
						} else {
							actualHeight = (int) maxHeight;
							actualWidth = (int) maxWidth;
						}
					}

					Bitmap tempBitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, optionsMeta);
					mBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);

					float ratioX = actualWidth / (float) optionsMeta.outWidth;
					float ratioY = actualHeight / (float) optionsMeta.outHeight;
					float middleX = actualWidth / 2.0f;
					float middleY = actualHeight / 2.0f;

					Matrix mat = new Matrix();
					mat.postRotate(angle);

					Matrix scaleMatrix = new Matrix();
					scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

					Canvas canvas = new Canvas(mBitmap);
					canvas.setMatrix(scaleMatrix);
					canvas.drawBitmap(tempBitmap, middleX - tempBitmap.getWidth() / 2, middleY - tempBitmap.getHeight() / 2, null);

					mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mat, true);

					return null;
				} catch (Exception ex) {
					ex.printStackTrace();
					mBitmap = null;
				}

				return null;
			}

			@Override
			protected void onPostExecute(byte[] result) {
				super.onPostExecute(result);

				if (null != mBitmap) {
					cropImageView.setImageBitmap(mBitmap);
					cropImageView.setFixedAspectRatio(true);
					findViewById(R.id.relativeLayout_main).setBackgroundColor(getResources().getColor(R.color.black));
				} else {
					try {
						AppDialog dialog = new AppDialog(context, true);
						dialog.setFailed(getResources().getString(R.string.e_while_loading_image_from_gallery));
					} catch (Exception ignore) {
						// if activity chrashes dont show failed
					}
				}

			}
		};
		task.execute(mFilePath);

	}

	private boolean saveBitmapToFile(Bitmap bitmap, String path) {

		File file = new File(path);
		FileOutputStream fOut;

		try {

			fOut = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();

			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private void createThumb(String path, Bitmap b) {
		int width = THUMB_WIDTH, height = THUMB_HEIGHT;

		Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(b, width, height, ScalingLogic.CROP);
		b.recycle();

		saveBitmapToFile(scaledBitmap, path);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mIsSamsung && SpikaEnterpriseApp.getInstance().samsungImagePath() != null) {
			if (SpikaEnterpriseApp.getInstance().samsungImagePath().equals("-1")) {
				finish();
			} else {
				onPhotoTaken(SpikaEnterpriseApp.getInstance().samsungImagePath());
			}
		}

		if (return_flag) {
			finish();
		}
	}

	@Override
	public void onClick(View view) {

		int id = view.getId();
		if (id == R.id.btnSend) {
			btnSend.setClickable(false);

			if (mCompressImages && getIntent().getBooleanExtra(Const.FROM_WAll, false)) {
				AppDialog compressionConfirmationDialog = new AppDialog(this, false);
				compressionConfirmationDialog.setYesNo(getString(R.string.compression_confirmation_question), getString(R.string.yes), getString(R.string.no));
				compressionConfirmationDialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {
					@Override
					public void onPositiveButtonClick(View v) {
						prepareFileForUpload(compressFileBeforePrepare(cropImageView.getCroppedImage()));
					}
				});
				compressionConfirmationDialog.setOnNegativeButtonClick(new AppDialog.OnNegativeButtonCLickListener() {
					@Override
					public void onNegativeButtonClick(View v) {
						prepareFileForUpload(cropImageView.getCroppedImage());
					}
				});
			} else {
				prepareFileForUpload(cropImageView.getCroppedImage());
			}
		} else if (id == R.id.btnCancel) {
			finish();
		}
	}

	private Bitmap compressFileBeforePrepare(Bitmap bmp) {

		int curWidth = bmp.getWidth();
		int curHeight = bmp.getHeight();

		int sizeToManipulate = curWidth > curHeight ? curWidth : curHeight;
		double resizeCoefficient = MAX_SIZE / sizeToManipulate;

		int dstWidth = (int) (curWidth * resizeCoefficient);
		int dstHeight = (int) (curHeight * resizeCoefficient);

		return Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
	}

	private void prepareFileForUpload(Bitmap bmp) {

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bs);

		if (saveBitmapToFile(bmp, mFilePath)) {
			createThumb(mFileThumbPath, bmp);
			fileUploadAsync(mFilePath, mFileThumbPath);
		} else {
			AppDialog dialog = new AppDialog(this, true);
			dialog.setFailed(getResources().getString(R.string.e_failed_while_sending));
		}
	}

	private void fileUploadAsync(String filePath, final String thumbPath) {

		new FileManageApi().uploadFile(filePath, this, true, new ApiCallback<UploadFileModel>() {

			@Override
			public void onApiResponse(Result<UploadFileModel> result) {

				if (result.isSuccess()) {
					thumbUploadAsync(thumbPath, result.getResultData().getFileId());
				} else {
					if (result.hasResultData()) {
						AppDialog dialog = new AppDialog(CameraCropActivity.this, true);
						dialog.setFailed(result.getResultData().getMessage());
						btnSend.setClickable(true);
					}
				}
			}
		});
	}

	private void thumbUploadAsync(final String thumbPath, final String fileId) {
		new FileManageApi().uploadFile(thumbPath, this, true, new ApiCallback<UploadFileModel>() {

			@Override
			public void onApiResponse(Result<UploadFileModel> result) {
				if (result.isSuccess()) {

					if (getIntent().getBooleanExtra(Const.ROOM_INTENT, false)) {
						// get fileid and thumbid for create room
						if (getIntent().getBooleanExtra(Const.UPDATE_PICTURE, false)) {
							updateChatPicture(fileId, result.getResultData().getFileId());
						} else {
							Helper.setRoomFileId(getApplicationContext(), fileId);
							Helper.setRoomThumbId(getApplicationContext(), result.getResultData().getFileId());
							finish();
						}
					} else if (getIntent().getBooleanExtra(Const.FROM_WAll, false)) {
						// send message
						sendMessage(fileId, result.getResultData().getFileId());
					} else if (getIntent().getBooleanExtra(Const.PROFILE_INTENT, false)) {
						// update user
						updateUser(fileId, result.getResultData().getFileId());
					}
				} else {
					if (result.hasResultData()) {
						AppDialog dialog = new AppDialog(CameraCropActivity.this, true);
						dialog.setFailed(result.getResultData().getMessage());
					}
				}
			}
		});
	}

	private void sendMessage(final String fileId, final String thumbId) {

		String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
		String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);

		new ChatApi().sendMessage(Const.MSG_TYPE_PHOTO, chatId, mFileName, fileId, thumbId, null, null, rootId, messageId, this, new ApiCallback<Integer>() {

			@Override
			public void onApiResponse(Result<Integer> result) {

				AppDialog dialog = new AppDialog(CameraCropActivity.this, true);

				if (result.isSuccess()) {
					dialog.setSucceed();
				} else {
					dialog.setFailed(result.getResultData());
				}
			}
		});
	}

	private void updateUser(final String fileId, final String thumbId) {
		new UserApi().updateUserImage(fileId, thumbId, this, true, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					Helper.setUserImage(getApplicationContext(), fileId);
					finish();
				} else {
					if (result.hasResultData()) {
						AppDialog dialog = new AppDialog(CameraCropActivity.this, true);
						dialog.setFailed(result.getResultData().getMessage());
					}
				}
			}
		});
	}

	private void updateChatPicture(final String fileId, final String thumbId) {

		String chatId = getIntent().getStringExtra(Const.CHAT_ID);
		String chatName = getIntent().getStringExtra(Const.CHAT_NAME);

		new ChatApi().updateChat(chatId, Const.UPDATE_CHAT_EDIT, fileId, thumbId, chatName, true, this, new ApiCallback<BaseModel>() {

			@Override
			public void onApiResponse(Result<BaseModel> result) {
				if (result.isSuccess()) {
					Helper.setRoomThumbId(getApplicationContext(), fileId);
					finish();
				} else {
					AppDialog dialog = new AppDialog(CameraCropActivity.this, false);
					dialog.setFailed(null);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mFilePath != null && !mIsSamsung) {
			new File(mFilePath).delete();
		}
	}

}