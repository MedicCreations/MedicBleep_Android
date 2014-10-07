package com.clover.spika.enterprise.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.ChatApi;
import com.clover.spika.enterprise.chat.api.FileManageApi;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UploadFileModel;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class CameraFullPhotoActivity extends BaseActivity implements OnClickListener {

    // thumbnail width and height
    private static final int THUMB_WIDTH = 100;
    private static final int THUMB_HEIGHT = 100;
    // compressed max size
    private static final double MAX_SIZE = 640;

    private ImageView mImageView;
    private Bitmap mBitmap;

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

    private boolean mIsOverJellyBean;
    private boolean mCompressImages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_full_photo);

        mIsOverJellyBean = Build.VERSION.SDK_INT > 18;
        mCompressImages = getResources().getBoolean(R.bool.send_compressed_images);

        return_flag = false;

        mImageView = (ImageView) findViewById(R.id.ivCameraFullPhoto);
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
        mFilePath = CameraFullPhotoActivity.this.getExternalCacheDir() + "/" + fileName;
        mFileThumbPath = CameraFullPhotoActivity.this.getExternalCacheDir() + "/" + fileName + "_thumb";

        if (!path.equals(mFilePath)) {
            try {
                Helper.copyStream(new FileInputStream(new File(path)), new FileOutputStream(new File(mFilePath)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

//			File f = new File(mFilePath);
//			try {
//				Bitmap tempBitmap = BitmapFactory.decodeStream(new FileInputStream(f));
//				mBitmap = tempBitmap;
//				mImageView.setImageBitmap(mBitmap);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

        new BaseAsyncTask<String, Void, byte[]>(this, false) {

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
        int width = THUMB_WIDTH, height = THUMB_HEIGHT;
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
            btnSend.setClickable(false);

            if (mCompressImages) {
                AppDialog compressionConfirmationDialog = new AppDialog(this, false);
                compressionConfirmationDialog.setYesNo(getString(R.string.compression_confirmation_question));
                compressionConfirmationDialog.setOnPositiveButtonClick(new AppDialog.OnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(View v) {
                        compressFileBeforePrepare();
                        prepareFileForUpload();
                    }
                });
                compressionConfirmationDialog.setOnNegativeButtonClick(new AppDialog.OnNegativeButtonCLickListener() {
                    @Override
                    public void onNegativeButtonClick(View v) {
                        prepareFileForUpload();
                    }
                });
            } else {
                prepareFileForUpload();
            }
        } else if (id == R.id.btnCancel) {
            finish();
        }
    }

    void compressFileBeforePrepare() {
        int curWidth = mBitmap.getWidth();
        int curHeight = mBitmap.getHeight();

        int sizeToManipulate = curWidth > curHeight ? curWidth : curHeight;
        double resizeCoefficient = MAX_SIZE / sizeToManipulate;

        int dstWidth = (int) (curWidth * resizeCoefficient);
        int dstHeight = (int) (curHeight * resizeCoefficient);

        Log.d("COMPRESSION", "curWidth: " + curWidth + "; curHeight: " + curHeight + " | resizeCoefficient: " + resizeCoefficient
                + ", dstWidth: " + dstWidth + ", dstHeight: " + dstHeight);

        mBitmap = Bitmap.createScaledBitmap(mBitmap, dstWidth, dstHeight, false);
    }

    void prepareFileForUpload() {
        Bitmap resizedBitmap = mBitmap;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);
        if (saveBitmapToFile(resizedBitmap, mFilePath)) {
            createThumb(mFileThumbPath, resizedBitmap);
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
                        AppDialog dialog = new AppDialog(CameraFullPhotoActivity.this, true);
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
                        //get fileid and thumbid for create room
                        Helper.setRoomFileId(getApplicationContext(), fileId);
                        Helper.setRoomThumbId(getApplicationContext(), result.getResultData().getFileId());

                        finish();
                    } else if (!getIntent().getBooleanExtra(Const.PROFILE_INTENT, false)) {
                        // send message
                        sendMessage(fileId, result.getResultData().getFileId());
                    } else {
                        // update user
                        updateUser(fileId, result.getResultData().getFileId());
                    }
                } else {
                    if (result.hasResultData()) {
                        AppDialog dialog = new AppDialog(CameraFullPhotoActivity.this, true);
                        dialog.setFailed(result.getResultData().getMessage());
                    }
                }
            }
        });
    }

    private void sendMessage(final String fileId, final String thumbId) {
        String rootId = getIntent().getStringExtra(Const.EXTRA_ROOT_ID);
        String messageId = getIntent().getStringExtra(Const.EXTRA_MESSAGE_ID);
        new ChatApi().sendMessage(Const.MSG_TYPE_PHOTO, chatId, null, fileId, thumbId, null, null,
                rootId, messageId, this, new ApiCallback<Integer>() {

                    @Override
                    public void onApiResponse(Result<Integer> result) {

                        AppDialog dialog = new AppDialog(CameraFullPhotoActivity.this, true);

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
                        AppDialog dialog = new AppDialog(CameraFullPhotoActivity.this, true);
                        dialog.setFailed(result.getResultData().getMessage());
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFilePath != null) {
            new File(mFilePath).delete();
        }
    }


}
