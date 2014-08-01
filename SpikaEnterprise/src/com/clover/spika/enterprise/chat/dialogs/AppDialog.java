package com.clover.spika.enterprise.chat.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CameraCropActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.RecordVideoActivity;
import com.clover.spika.enterprise.chat.UserListActivity;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class AppDialog extends Dialog {

	boolean isFinish = false;
	boolean checked = false;

	public AppDialog(final Context context, boolean isFinish) {
		super(context, R.style.Theme_Dialog);
		setOwnerActivity((Activity) context);

		this.isFinish = isFinish;
	}

	/**
	 * Show info dialog
	 * 
	 * @param message
	 */
	public void setInfo(String message) {
		this.setContentView(R.layout.dialog_alert);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.controlLayout);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					getOwnerActivity().finish();
				}
			}
		});

		TextView infoText = (TextView) findViewById(R.id.infoText);
		infoText.setText(message);

		show();
	}

	/**
	 * Show succeeded dialog
	 */
	public void setSucceed() {
		this.setContentView(R.layout.dialog_succeed);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					getOwnerActivity().finish();
				}
			}
		});

		show();
	}

	/**
	 * show failed dialog with description from int result
	 */
	public void setFailed(final int errorCode) {
		this.setContentView(R.layout.dialog_failed);

		String failedText = Helper.errorDescriptions(getContext(), errorCode);

		TextView failedDesc = (TextView) findViewById(R.id.failedDescription);
		failedDesc.setText(failedText);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (errorCode == Const.E_INVALID_TOKEN || errorCode == Const.E_EXPIRED_TOKEN) {
					Intent intent = new Intent(getContext(), UserListActivity.class);
					getOwnerActivity().startActivity(intent);
					getOwnerActivity().finish();

				} else if (isFinish) {
					getOwnerActivity().finish();
				}
			}
		});

		show();
	}

	/**
	 * show failed dialog with string description
	 * 
	 * @param failedText
	 */
	public void setFailed(final String failedText) {
		this.setContentView(R.layout.dialog_failed);

		TextView failedDesc = (TextView) findViewById(R.id.failedDescription);
		failedDesc.setText(failedText);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					getOwnerActivity().finish();
				}
			}
		});

		show();
	}

	/**
	 * Go to recording screen from gallery or camera
	 */
	public void choseCamGallery(final String chatId) {
		this.setContentView(R.layout.dialog_chose_cam_rec);

		ImageButton camera = (ImageButton) findViewById(R.id.camera);
		ImageButton gallery = (ImageButton) findViewById(R.id.galleryImageButton);

		camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent recordVideoIntent = new Intent(getContext(), RecordVideoActivity.class);
				recordVideoIntent.putExtra(Const.INTENT_TYPE, Const.VIDEO_INTENT_INT);
				recordVideoIntent.putExtra(Const.CHAT_ID, chatId);
				getContext().startActivity(recordVideoIntent);
			}
		});

		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent recordVideoIntent = new Intent(getContext(), RecordVideoActivity.class);
				recordVideoIntent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT_INT);
				recordVideoIntent.putExtra(Const.CHAT_ID, chatId);
				getContext().startActivity(recordVideoIntent);
			}
		});

		show();
	}

	/**
	 * Go to recording screen from gallery or camera
	 */
	public void choseCamGalleryProfile() {
		this.setContentView(R.layout.dialog_chose_cam_rec);

		ImageButton camera = (ImageButton) findViewById(R.id.camera);
		ImageButton gallery = (ImageButton) findViewById(R.id.galleryImageButton);

		camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
				intent.putExtra(Const.PROFILE_INTENT, true);
				getContext().startActivity(intent);
			}
		});

		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
				intent.putExtra(Const.PROFILE_INTENT, true);
				getContext().startActivity(intent);
			}
		});

		show();
	}

	/**
	 * Open file or confirm the download
	 */
	public void fileDownloaded(String message, final Intent intent) {
		this.setContentView(R.layout.dialog_open_file);

		TextView infoText = (TextView) findViewById(R.id.infoText);
		infoText.setText(message);

		LinearLayout btnOpen = (LinearLayout) findViewById(R.id.btnOpen);
		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);

		btnOpen.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				getContext().startActivity(intent);
			}
		});

		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		show();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!hasFocus) {
			this.dismiss();
		}

		super.onWindowFocusChanged(hasFocus);
	}

}