package com.clover.spika.enterprise.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.json.JSONObject;

import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.CroppedImageView;

import com.clover.spika.enterprise.chat.R;

import android.annotation.SuppressLint;
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

public class CameraCropActivity extends BaseActivity implements OnTouchListener, OnClickListener {

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
	private String groupId = "";

	private LinearLayout btnCreate;
	private LinearLayout btnCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_crop);

		return_flag = false;

		mImageView = (CroppedImageView) findViewById(R.id.ivCameraCropPhoto);
		mImageView.setDrawingCacheEnabled(true);

		btnCreate = (LinearLayout) findViewById(R.id.btnCreate);
		btnCreate.setOnClickListener(this);
		btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);

		getImageIntents();
	}

	private void getImageIntents() {
		if (getIntent().getStringExtra(Const.INTENT_TYPE).equals(Const.GALLERY_INTENT)) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			this.startActivityForResult(intent, GALLERY);
		} else {
			try {
				startCamera();
			} catch (Exception ex) {
				ex.printStackTrace();

				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_failed_openning_camera));
			}
		}

		groupId = getIntent().getStringExtra(Const.GROUP_ID);
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
				dialog.setFailed(getResources().getString(R.string.e_something_went_wrong_again));
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
				try {
					Uri selected_image = data.getData();
					String selected_image_path = Helper.getImagePath(this, selected_image);
					onPhotoTaken(selected_image_path);
				} catch (Exception e) {
					e.printStackTrace();

					AppDialog dialog = new AppDialog(this, true);
					dialog.setFailed(getResources().getString(R.string.e_while_loading_image_from_gallery));
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

		if (!path.equals(mFilePath)) {
			copy(new File(path), new File(mFilePath));
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
					// } else {
					// Bitmap tempBitmap = BitmapFactory.decodeStream(new
					// FileInputStream(f), null, optionsMeta);
					// mBitmap = Bitmap.createBitmap(actualWidth, actualHeight,
					// Bitmap.Config.ARGB_4444);
					//
					// float ratioX = actualWidth / (float)
					// optionsMeta.outWidth;
					// float ratioY = actualHeight / (float)
					// optionsMeta.outHeight;
					// float middleX = actualWidth / 2.0f;
					// float middleY = actualHeight / 2.0f;
					//
					// Matrix scaleMatrix = new Matrix();
					// scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
					//
					// Canvas canvas = new Canvas(mBitmap);
					// canvas.setMatrix(scaleMatrix);
					// canvas.drawBitmap(tempBitmap, middleX -
					// tempBitmap.getWidth() / 2, middleY -
					// tempBitmap.getHeight() / 2, new
					// Paint(Paint.FILTER_BITMAP_FLAG));
					//
					// mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
					// mBitmap.getWidth(), mBitmap.getHeight(), mat, true);
					// }

					return null;
					// } catch (OutOfMemoryError error) {
					// mBitmap = null;
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
		if (id == R.id.btnCreate) {
			Bitmap resizedBitmap = getBitmapFromView(mImageView);
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
			if (saveBitmapToFile(resizedBitmap, mFilePath) == true) {
				fileUploadAsync(mFilePath);
			} else {
				AppDialog dialog = new AppDialog(this, true);
				dialog.setFailed(getResources().getString(R.string.e_failed_while_sending));
			}
		} else if (id == R.id.btnCancel) {
			finish();
		} else {
		}
	}

	private void fileUploadAsync(final String filePath) {
		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			String imageName = null;
			int resultCode = 0;

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> postParams = new HashMap<String, String>();
					postParams.put(Const.FILE, filePath);

					JSONObject result = NetworkManagement.httpPostFileRequest(SpikaEnterpriseApp.getSharedPreferences(context), postParams);

					if (result != null) {
						resultCode = result.getInt(Const.CODE);
						imageName = result.getString(Const.FILE_ID);
						return resultCode;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return resultCode;
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result.equals(Const.E_SUCCESS)) {
					sendMessage(imageName);
				} else {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setFailed(result);
				}
			};

		}.execute();
	}

	public void copy(File src, File dst) {

		InputStream in;
		OutputStream out;

		try {

			in = new FileInputStream(src);
			out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;

			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			in.close();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(final String imagePath) {

		new BaseAsyncTask<Void, Void, Integer>(this, true) {

			protected void onPreExecute() {
				super.onPreExecute();
			};

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.FUNCTION, Const.F_POST_MESSAGE);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.GROUP_ID, groupId);
					reqData.put(Const.FILE_ID, imagePath);

					reqData.put(Const.MSG_TYPE, String.valueOf(ChatActivity.T_IMAGE));

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result == Const.E_SUCCESS) {
					AppDialog dialog = new AppDialog(context, true);
					dialog.setSucceed();
				} else {
					AppDialog dialog = new AppDialog(context, true);
					dialog.setFailed(result);
				}
			};

		}.execute();
	}

}