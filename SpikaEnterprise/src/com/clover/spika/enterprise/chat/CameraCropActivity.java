package com.clover.spika.enterprise.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.CroppedImageView;

public class CameraCropActivity extends Activity implements OnTouchListener, OnClickListener {

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private Matrix translateMatrix = new Matrix();

	// We can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;

	// Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;

	private CroppedImageView mImageView;
	private Bitmap mBitmap;

	private int crop_container_size;

	// Gallery type marker
	private static final int GALLERY = 1;
	// Camera type marker
	private static final int CAMERA = 2;
	// Uri for captured image so we can get image path
	private String _path;

	private static boolean return_flag;

	private String mFilePath;
	private String mFileThumbPath;
	private String chatId = "";

	private LinearLayout btnSend;
	private LinearLayout btnCancel;

	private boolean mIsOverJellyBean = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_crop);

		if (Build.VERSION.SDK_INT > 18) {
			mIsOverJellyBean = true;
		}

		return_flag = false;

		mImageView = (CroppedImageView) findViewById(R.id.ivCameraCropPhoto);
		mImageView.setDrawingCacheEnabled(true);

		btnSend = (LinearLayout) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);

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

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
				startActivityForResult(intent, CAMERA);

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

			mImageView = (CroppedImageView) findViewById(R.id.ivCameraCropPhoto);
			mImageView.setDrawingCacheEnabled(true);

			scaleView();

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
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			scaleView();
		}
	}

	public void scaleView() {

		View top_view = findViewById(R.id.topView);
		View bottom_view = findViewById(R.id.bottomView);
		LinearLayout footer = (LinearLayout) findViewById(R.id.footerLayout);
		LinearLayout crop_frame = (LinearLayout) findViewById(R.id.llCropFrame);
		Display display = getWindowManager().getDefaultDisplay();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		display.getMetrics(displaymetrics);

		int width = displaymetrics.widthPixels;
		int height = displaymetrics.heightPixels;

		// 90% of width
		crop_container_size = (int) ((float) width * (1f - (10f / 100f)));

		// 10% margins
		float margin = ((float) width * (1f - (90f / 100f)));

		// Parameters for white crop border
		LinearLayout.LayoutParams par = new LinearLayout.LayoutParams(crop_container_size, crop_container_size);
		par.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		par.setMargins((int) (margin / 2f), 0, (int) (margin / 2f), 0);
		crop_frame.setLayoutParams(par);

		// Margins for other transparent views
		float top_view_height = ((float) (height - crop_container_size - footer.getHeight())) / (float) 2;
		top_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) top_view_height));
		bottom_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) top_view_height));

		// Image container
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(crop_container_size, crop_container_size);
		params.setMargins((int) (margin / 2f), (int) top_view_height, (int) (margin / 2f), 0);

		mImageView.setLayoutParams(params);
		mImageView.setImageBitmap(mBitmap);
		mImageView.setMaxZoom(4f);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView view = (ImageView) v;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;

		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {

				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM) {

				float newDist = spacing(event);

				if (newDist > 10f) {

					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}

		view.setImageMatrix(matrix);
		view.invalidate();

		return true;
	}

	/**
	 * Get the image from container - it is already cropped and zoomed If the
	 * image is smaller than container it will be black color set aside
	 * */
	private Bitmap getBitmapFromView(View view) {

		Bitmap returnedBitmap = Bitmap.createBitmap(crop_container_size, crop_container_size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		Drawable bgDrawable = view.getBackground();

		if (bgDrawable != null) {
			bgDrawable.draw(canvas);
		} else {
			canvas.drawColor(Color.BLACK);
		}

		view.draw(canvas);

		return returnedBitmap;
	}

	/** Determine the space between the first two fingers */
	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event) {

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {

		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
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

		String fileName = Uri.parse(path).getLastPathSegment();
		mFilePath = CameraCropActivity.this.getExternalCacheDir() + "/" + fileName;
		mFileThumbPath = CameraCropActivity.this.getExternalCacheDir() + "/" + fileName + "_thumb";

		if (!path.equals(mFilePath)) {
			try {
				Helper.copyStream(new FileInputStream(new File(path)), new FileOutputStream(new File(mFilePath)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		new BaseAsyncTask<String, Void, byte[]>(this, true) {

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

					// this options allow android to claim the bitmap memory
					// if
					// it runs low
					// on memory
					optionsMeta.inJustDecodeBounds = false;
					optionsMeta.inPurgeable = true;
					optionsMeta.inInputShareable = true;
					optionsMeta.inTempStorage = new byte[16 * 1024];

					// if (!isFromWall) {

					float maxHeight = 1024.0f;
					float maxWidth = 1024.0f;

					optionsMeta.inSampleSize = Helper.calculateInSampleSize(optionsMeta, (int) maxWidth, (int) maxHeight);

					// max Height and width values of the compressed image
					// is
					// taken as
					// 816x612

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
					mImageView.setImageBitmap(mBitmap);
					mImageView.setScaleType(ScaleType.MATRIX);

					matrix.setTranslate(-(mBitmap.getWidth() - crop_container_size) / 2f, -(mBitmap.getHeight() - crop_container_size) / 2f);
					mImageView.setImageMatrix(matrix);

					translateMatrix.setTranslate(-(mBitmap.getWidth() - crop_container_size) / 2f, -(mBitmap.getHeight() - crop_container_size) / 2f);
					mImageView.setImageMatrix(translateMatrix);

					matrix = translateMatrix;
				} else {
					AppDialog dialog = new AppDialog(context, true);
					dialog.setFailed(getResources().getString(R.string.e_while_loading_image_from_gallery));
				}

			}
		}.execute(mFilePath);
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
		int width = 200, height = 200;
		Bitmap sb = Bitmap.createScaledBitmap(b, width, height, true);

		saveBitmapToFile(sb, path);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (return_flag) {
			finish();
		}
	}

	@Override
	public void onClick(View view) {

		int id = view.getId();
		if (id == R.id.btnSend) {
			Bitmap resizedBitmap = getBitmapFromView(mImageView);
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
			if (saveBitmapToFile(resizedBitmap, mFilePath) == true) {
				createThumb(mFileThumbPath, resizedBitmap);
				fileUploadAsync(mFilePath, mFileThumbPath);
			} else {
				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_failed_while_sending));
			}
		} else if (id == R.id.btnCancel) {
			finish();
		} else {
		}
	}

	private void fileUploadAsync(final String filePath, final String thumbPath) {
		new FileManageApi().uploadFile(filePath, this, true, new ApiCallback<UploadFileModel>() {

			@Override
			public void onApiResponse(Result<UploadFileModel> result) {
				if (result.isSuccess()) {
					thumbUploadAsync(thumbPath, result.getResultData().getFileId());
				} else {
					if (result.hasResultData()) {
						AppDialog dialog = new AppDialog(CameraCropActivity.this, true);
						dialog.setFailed(result.getResultData().getMessage());
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
					if (!getIntent().getBooleanExtra(Const.PROFILE_INTENT, false)) {
						// send message
						sendMessage(fileId, result.getResultData().getFileId());
					} else {
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
		new ChatApi().sendMessage(Const.MSG_TYPE_PHOTO, chatId, null, fileId, thumbId, null, null, this, new ApiCallback<Integer>() {

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
					ProfileActivity.openProfile(CameraCropActivity.this, fileId);
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

}