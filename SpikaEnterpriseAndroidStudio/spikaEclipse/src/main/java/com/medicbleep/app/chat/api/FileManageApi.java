package com.medicbleep.app.chat.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.json.JSONException;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.dialogs.AppDialog;
import com.medicbleep.app.chat.dialogs.AppProgressDialogWithBar;
import com.medicbleep.app.chat.extendables.BaseAsyncTask;
import com.medicbleep.app.chat.extendables.SpikaEnterpriseApp;
import com.medicbleep.app.chat.listeners.ProgressBarListeners;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.models.UploadFileModel;
import com.medicbleep.app.chat.networking.NetworkManagement;
import com.medicbleep.app.chat.security.JNAesCrypto;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.ResponseBody;

public class FileManageApi {

	private AppProgressDialogWithBar progressBar;
	
	public void uploadFile(final String path, final Context ctx, boolean showProgressBar, final ApiCallback<UploadFileModel> listener){
		uploadFile(true, path, ctx, showProgressBar, listener);
	}

	public void uploadFile(final boolean toEncrypt, final String path, final Context ctx, boolean showProgressBar, final ApiCallback<UploadFileModel> listener) {
		new BaseAsyncTask<Void, Void, UploadFileModel>(ctx, showProgressBar) {

			protected void onPreExecute() {
				File checkFile = new File(path);
				if (checkFile.length() > Const.MAX_FILE_SIZE) {
					AppDialog largeFileDialog = new AppDialog(ctx, false);
					largeFileDialog.setInfo(ctx.getString(R.string.file_size_error));
					cancel(true);
				} else {
					progressBar = new AppProgressDialogWithBar(ctx);
					progressBar.showProgress();
				}
			}

			protected UploadFileModel doInBackground(Void... params) {

				// start: encrypt
				String finalPath = "";
				if(toEncrypt){
					finalPath = Utils.handleFileEncryption(path, context);
				}else{
					finalPath = path;
				}

				if (finalPath == null) {
					return null;
				}
				// end: encrypt

				HashMap<String, String> postParams = new HashMap<String, String>();
				postParams.put(Const.FILE, finalPath);

				try {
					String responseBody = NetworkManagement.httpPostFileRequest(SpikaEnterpriseApp.getSharedPreferences(), postParams, new ProgressBarListeners() {

						@Override
						public void onSetMax(long total) {
							if (progressBar.getMaxBar() == 1)
								progressBar.setMaxBar((int) total);
						}

						@Override
						public void onProgress(long current) {
							progressBar.updateBar((int) current);
						}

						@Override
						public void onFinish() {
							progressBar.dismiss();
						}
					});
					ObjectMapper mapper = new ObjectMapper();

					if (responseBody == null) {
						return null;
					}

					new File(finalPath).delete();
					
					return mapper.readValue(responseBody, UploadFileModel.class);

				} catch (IOException | JSONException e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(UploadFileModel upload) {
				super.onPostExecute(upload);

				if (listener != null) {
					Result<UploadFileModel> result;

					if (upload != null) {
						if (upload.getCode() == Const.API_SUCCESS) {
							result = new Result<UploadFileModel>(Result.ApiResponseState.SUCCESS);
							result.setResultData(upload);
						} else {
							result = new Result<UploadFileModel>(Result.ApiResponseState.FAILURE);
							result.setResultData(upload);
						}
					} else {
						result = new Result<UploadFileModel>(Result.ApiResponseState.FAILURE);
						UploadFileModel data = new UploadFileModel();
						data.setMessage(context.getResources().getString(R.string.e_while_encrypting));
						result.setResultData(data);
					}

					listener.onApiResponse(result);
				}

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
				}
			}

		}.execute();
	}
	
	public void downloadFileToFile(final File destFile, final String fileId, final boolean showProgress, final Context ctx, final ApiCallback<String> listener,
			final ProgressBarListeners pbListener){
		downloadFileToFile(true, destFile, fileId, showProgress, ctx, listener, pbListener);
	}

	public void downloadFileToFile(final boolean isCrypted, final File destFile, final String fileId, final boolean showProgress, final Context ctx, final ApiCallback<String> listener,
			final ProgressBarListeners pbListener) {
		new BaseAsyncTask<Void, Void, String>(ctx, showProgress) {

			protected void onPreExecute() {
				if (showProgress) {
					progressBar = new AppProgressDialogWithBar(ctx);
					progressBar.showProgress();
				}
			};

			protected String doInBackground(Void... params) {
				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.FILE_ID, fileId);

				try {
					ResponseBody response = NetworkManagement.httpGetGetFile(SpikaEnterpriseApp.getSharedPreferences().getToken(), Const.F_USER_GET_FILE, getParams);
					InputStream is = response.byteStream();

					File file;

					if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
						file = new File(android.os.Environment.getExternalStorageDirectory(), Const.APP_FILES_DIRECTORY + Const.APP_SPEN_FILE);
					} else {
						return null;
					}

					OutputStream os = new FileOutputStream(file);
					if (pbListener == null) {
						Helper.copyStream(is, os, response.contentLength(), new ProgressBarListeners() {

							@Override
							public void onSetMax(long total) {
								if (progressBar.getMaxBar() == 1)
									progressBar.setMaxBar((int) total);
							}

							@Override
							public void onProgress(long current) {
								progressBar.updateBar((int) current);
							}

							@Override
							public void onFinish() {
								progressBar.dismiss();

								if(isCrypted){
									((Activity) context).runOnUiThread(new Runnable() {
										public void run() {
											progressBar = new AppProgressDialogWithBar(ctx);
											progressBar.showDecrypting();
										}
									});
								}
							}
						});

					} else {
						Helper.copyStream(is, os, response.contentLength(), pbListener);
					}

					is.close();
					os.close();

					String finalFilePath = "";
					
					if(isCrypted){
						finalFilePath = Utils.handleFileDecryptionToPath(file.getAbsolutePath(), destFile.getAbsolutePath(), context);
					}else{
						Helper.copyStream(new FileInputStream(file), new FileOutputStream(destFile));
						finalFilePath = destFile.getAbsolutePath();
					}
					
					return finalFilePath;
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			protected void onPostExecute(String path) {
				super.onPostExecute(path);

				if (listener != null) {
					Result<String> result;

					if (path != null) {
						result = new Result<String>(Result.ApiResponseState.SUCCESS);
						result.setResultData(path);
					} else {
						result = new Result<String>(Result.ApiResponseState.FAILURE);
					}

					listener.onApiResponse(result);
				}

				if (progressBar != null && progressBar.isShowing()) {
					progressBar.dismiss();
				}
			}

		}.execute();
	}

	public void startFileDownload(final boolean isEncrypted, final String fileName, final String fileId, final int id, Context ctx) {
		new BaseAsyncTask<Void, Void, Void>(ctx, false) {

			private NotificationManager mNotifyManager;
			private Builder mBuilder;
			private File downloadedFile;
			private AppDialog dialog;

			protected void onPreExecute() {

				dialog = new AppDialog(context, false);
				dialog.setInfo(context.getResources().getString(R.string.download_in_progress));

				mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				mBuilder = new NotificationCompat.Builder(context);
				mBuilder.setAutoCancel(true);
				mBuilder.setOngoing(true);
				mBuilder.setContentTitle(context.getResources().getString(R.string.file_download) + ":" + fileName)
						.setContentText(context.getResources().getString(R.string.download_in_progress)).setSmallIcon(R.drawable.ic_launcher);

				mBuilder.setProgress(0, 0, true);
				mNotifyManager.notify(id, mBuilder.build());

				Log.d("LOG", "FILENAME: " + fileName);
				downloadedFile = new File(android.os.Environment.getExternalStorageDirectory(), Const.APP_FILES_DIRECTORY + Const.APP_FILED_DOWNLOADS);
				Log.w("LOG", "FILE-1: " + downloadedFile.getAbsolutePath());

				if (!downloadedFile.exists()) {
					downloadedFile.mkdir();
				}

				downloadedFile = new File(android.os.Environment.getExternalStorageDirectory(), Const.APP_FILES_DIRECTORY + Const.APP_FILED_DOWNLOADS + "/" + fileName);
				Log.e("LOG", "FILE: " + downloadedFile.getAbsolutePath());
			};

			protected Void doInBackground(Void... paramss) {

				HashMap<String, String> getParams = new HashMap<String, String>();
				getParams.put(Const.FILE_ID, fileId);

				try {

					ResponseBody response = NetworkManagement.httpGetGetFile(SpikaEnterpriseApp.getSharedPreferences().getToken(), Const.F_USER_GET_FILE, getParams);
					InputStream is = response.byteStream();

					if (JNAesCrypto.isEncryptionEnabled && isEncrypted) {
						Log.i("LOG", "FILE2: " + downloadedFile.getAbsolutePath());
						JNAesCrypto.decryptIs(is, downloadedFile, context);
					} else {
						Log.i("LOG", "FILE2: " + downloadedFile.getAbsolutePath());
						OutputStream os = new FileOutputStream(downloadedFile.getAbsolutePath());
						Helper.copyStream(is, os);
						os.close();
					}

					is.close();

				} catch (Exception e) {
					if (Const.DEBUG_CRYPTO)
						e.printStackTrace();
				}

				return null;
			};

			protected void onPostExecute(Void result) {

				if (dialog.isShowing()) {
					dialog.dismiss();
				}

				if (downloadedFile.exists()) {

					Uri uri = Uri.fromFile(downloadedFile);
					Intent intent = new Intent(Intent.ACTION_VIEW);

					if (uri.toString().contains(".doc") || uri.toString().contains(".docx")) {
						// Word document
						intent.setDataAndType(uri, "application/msword");
					} else if (uri.toString().contains(".pdf")) {
						// PDF file
						intent.setDataAndType(uri, "application/pdf");
					} else if (uri.toString().contains(".ppt") || uri.toString().contains(".pptx")) {
						// Powerpoint file
						intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
					} else if (uri.toString().contains(".xls") || uri.toString().contains(".xlsx")) {
						// Excel file
						intent.setDataAndType(uri, "application/vnd.ms-excel");
					} else if (uri.toString().contains(".zip")) {
						// ZIP audio file
						intent.setDataAndType(uri, "application/zip");
					} else if (uri.toString().contains(".rar")) {
						// ZIP audio file
						intent.setDataAndType(uri, "application/x-rar-compressed");
					} else if (uri.toString().contains(".gz")) {
						// ZIP audio file
						intent.setDataAndType(uri, "application/gzip");
					} else if (uri.toString().contains(".rtf")) {
						// RTF file
						intent.setDataAndType(uri, "application/rtf");
					} else if (uri.toString().contains(".wav") || uri.toString().contains(".mp3")) {
						// WAV audio file
						intent.setDataAndType(uri, "audio/x-wav");
					} else if (uri.toString().contains(".gif")) {
						// GIF file
						intent.setDataAndType(uri, "image/gif");
					} else if (uri.toString().contains(".jpg") || uri.toString().contains(".jpeg") || uri.toString().contains(".png")) {
						// JPG file
						intent.setDataAndType(uri, "image/jpeg");
					} else if (uri.toString().contains(".txt")) {
						// Text file
						intent.setDataAndType(uri, "text/plain");
					} else if (uri.toString().contains(".3gp") || uri.toString().contains(".mpg") || uri.toString().contains(".mpeg") || uri.toString().contains(".mpe")
							|| uri.toString().contains(".mp4") || uri.toString().contains(".avi")) {
						// Video files
						intent.setDataAndType(uri, "video/*");
					} else {
						// if you want you can also define the intent type for
						// any
						// other file

						// additionally use else clause below, to manage other
						// unknown extensions
						// in this case, Android will show all applications
						// installed on the device
						// so you can choose which application to use
						intent.setDataAndType(uri, "*/*");
					}

					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

					mBuilder.setContentIntent(contentIntent);
					mBuilder.setContentText(context.getResources().getString(R.string.download_complete));
					mBuilder.setProgress(0, 0, false);
					mBuilder.setOngoing(false);
					mNotifyManager.notify(id, mBuilder.build());

					try {
						dialog.fileDownloaded(context.getResources().getString(R.string.download_complete) + "\n" + fileName, intent);
					} catch (Exception ignore) {
					}
				} else {
					mBuilder.setContentText(context.getResources().getString(R.string.download_failed));
					mBuilder.setProgress(0, 0, false);
					mBuilder.setOngoing(false);
					mNotifyManager.notify(id, mBuilder.build());

					try {
						dialog.setFailed(context.getResources().getString(R.string.download_failed));
					} catch (Exception ignore) {
					}
				}
			};

		}.execute();
	}
}
