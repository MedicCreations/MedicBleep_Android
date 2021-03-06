package com.medicbleep.app.chat.dialogs;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.medicbleep.app.chat.CameraCropActivity;
import com.medicbleep.app.chat.LoginActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.RecordVideoActivity;
import com.medicbleep.app.chat.models.HelperModel;
import com.medicbleep.app.chat.models.UserDetail;
import com.medicbleep.app.chat.utils.Const;
import com.medicbleep.app.chat.utils.Helper;
import com.medicbleep.app.chat.utils.Utils;
import com.medicbleep.app.chat.views.RobotoThinEditText;

public class AppDialog extends Dialog {

	boolean isFinish = false;
	boolean checked = false;

	private OnPositiveButtonClickListener mOnPositiveButtonClick;
	private OnNegativeButtonCLickListener mOnNegativeButtonClick;
	private OnDismissDialogListener mOnDismissDialogListener;
	private Object dismissObject = null;

	public AppDialog(final Context context, boolean isFinish) {
		super(context, R.style.Theme_Dialog);

		try {
			setOwnerActivity((Activity) context);
		} catch (Exception e) {
			dismiss();
		}

		this.isFinish = isFinish;
	}

	public void setOnPositiveButtonClick(OnPositiveButtonClickListener mOnPositiveButtonClick) {
		this.mOnPositiveButtonClick = mOnPositiveButtonClick;
	}

	public void setOnNegativeButtonClick(OnNegativeButtonCLickListener mOnNegativeButtonClick) {
		this.mOnNegativeButtonClick = mOnNegativeButtonClick;
	}

	public void setOnDialogDismissListener(OnDismissDialogListener mOnDismissDialogListener) {
		this.mOnDismissDialogListener = mOnDismissDialogListener;
	}

	public void setDismissObject(Object object) {
		this.dismissObject = object;
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

	public void setPasswordInput(String message, String yesText, String noText, final String password) {
		this.setContentView(R.layout.dialog_input_password);

		final TextView infoText = (TextView) findViewById(R.id.infoText);
		infoText.setText(message);

		final RobotoThinEditText etPassword = (RobotoThinEditText) findViewById(R.id.etDialogPassword);

		TextView yesTextView = (TextView) findViewById(R.id.text_yes);
		yesTextView.setText(TextUtils.isEmpty(yesText) ? getOwnerActivity().getString(android.R.string.yes) : yesText);

		View btnYes = findViewById(R.id.layout_yes);
		btnYes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (password != null) {
					String enteredPassword = etPassword.getText().toString();

					String hashPassword = null;
					try {
						hashPassword = Utils.getHexString(enteredPassword);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					if (!TextUtils.isEmpty(hashPassword) && hashPassword.equals(password)) {
						if (mOnPositiveButtonClick != null) {
							mOnPositiveButtonClick.onPositiveButtonClick(v, AppDialog.this);
						}
					} else {
						infoText.setText(getOwnerActivity().getString(R.string.password_error));
						etPassword.setText("");
					}
				} else {
					if (mOnPositiveButtonClick != null) {
						mOnPositiveButtonClick.onPositiveButtonClick(v, AppDialog.this);
					}
					dismiss();
				}
			}
		});

		TextView noTextView = (TextView) findViewById(R.id.text_no);
		noTextView.setText(TextUtils.isEmpty(noText) ? getOwnerActivity().getString(android.R.string.no) : noText);

		View btnNo = findViewById(R.id.layout_no);
		btnNo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnNegativeButtonClick != null) {
					mOnNegativeButtonClick.onNegativeButtonClick(v, AppDialog.this);
				}
				dismiss();
			}
		});

		show();
	}

	public void setEditDialog(final UserDetail userDetail, OnDismissDialogListener mOnDismissDialogListener) {
		this.setContentView(R.layout.dialog_edit_profile_item);
		setOnDialogDismissListener(mOnDismissDialogListener);

		final TextView infoText = (TextView) findViewById(R.id.infoText);
		infoText.setText(userDetail.getLabel());

		final RobotoThinEditText etValue = (RobotoThinEditText) findViewById(R.id.etDialogPassword);

		if (!TextUtils.isEmpty(userDetail.getValue())) {
			etValue.setText(userDetail.getValue());
		}

		View btnYes = findViewById(R.id.layout_yes);
		btnYes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String value = etValue.getText().toString();

				if (!TextUtils.isEmpty(value)) {
					HelperModel response = new HelperModel();
					response.setValue(value);
					response.setPosition(userDetail.getPosition());

					setDismissObject(response);
					dismiss();
				}
			}
		});

		View btnNo = findViewById(R.id.layout_no);
		btnNo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		show();
	}

	public void setYesNo(String message) {
		setYesNo(message, null, null);
	}

	public void setYesNo(String message, String yesText, String noText) {
		this.setContentView(R.layout.dialog_confirmation);

		TextView yesTextView = (TextView) findViewById(R.id.text_yes);
		yesTextView.setText(TextUtils.isEmpty(yesText) ? getOwnerActivity().getString(android.R.string.yes) : yesText);

		View btnYes = findViewById(R.id.layout_yes);
		btnYes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnPositiveButtonClick != null) {
					mOnPositiveButtonClick.onPositiveButtonClick(v, AppDialog.this);
				}
				dismiss();
			}
		});

		TextView noTextView = (TextView) findViewById(R.id.text_no);
		noTextView.setText(TextUtils.isEmpty(noText) ? getOwnerActivity().getString(android.R.string.no) : noText);

		View btnNo = findViewById(R.id.layout_no);
		btnNo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnNegativeButtonClick != null) {
					mOnNegativeButtonClick.onNegativeButtonClick(v, AppDialog.this);
				}
				dismiss();
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
	 * Show succeeded dialog
	 */
	public void setSucceed(final Intent intent) {
		this.setContentView(R.layout.dialog_succeed);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					getOwnerActivity().startActivity(intent);
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
					Intent intent = new Intent(getContext(), LoginActivity.class);
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
		failedDesc.setText(TextUtils.isEmpty(failedText) ? "" : failedText);

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
	public void choseCamGallery(final String chatId, final String rootId, final String messageId) {
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
				recordVideoIntent.putExtra(Const.EXTRA_ROOT_ID, rootId);
				recordVideoIntent.putExtra(Const.EXTRA_MESSAGE_ID, messageId);
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
				recordVideoIntent.putExtra(Const.EXTRA_ROOT_ID, rootId);
				recordVideoIntent.putExtra(Const.EXTRA_MESSAGE_ID, messageId);
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
				intent.putExtra(Const.IS_SQUARE, true);
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
				intent.putExtra(Const.IS_SQUARE, true);
				getContext().startActivity(intent);
			}
		});

		show();
	}

	/**
	 * Go to recording screen from gallery or camera on create room
	 */
	public void choseCamGalleryRoom() {
		this.setContentView(R.layout.dialog_chose_cam_rec);

		ImageButton camera = (ImageButton) findViewById(R.id.camera);
		ImageButton gallery = (ImageButton) findViewById(R.id.galleryImageButton);

		camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.IS_SQUARE, true);
				// getContext().startActivity(intent);
				getOwnerActivity().startActivityForResult(intent, 1);
			}
		});

		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.IS_SQUARE, true);
				getOwnerActivity().startActivityForResult(intent, 1);
			}
		});

		show();
	}

	/**
	 * Go to recording screen from gallery or camera on update room
	 */
	public void choseCamGalleryRoomUpdate(final String chatId, final String chatName) {
		this.setContentView(R.layout.dialog_chose_cam_rec);

		ImageButton camera = (ImageButton) findViewById(R.id.camera);
		ImageButton gallery = (ImageButton) findViewById(R.id.galleryImageButton);

		camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.CHAT_ID, chatId);
				intent.putExtra(Const.CHAT_NAME, chatName);
				intent.putExtra(Const.UPDATE_PICTURE, true);
				intent.putExtra(Const.IS_SQUARE, true);

				getContext().startActivity(intent);
			}
		});

		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.CHAT_ID, chatId);
				intent.putExtra(Const.CHAT_NAME, chatName);
				intent.putExtra(Const.UPDATE_PICTURE, true);
				intent.putExtra(Const.IS_SQUARE, true);

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

	@Override
	public void dismiss() {

		if (mOnDismissDialogListener != null) {
			mOnDismissDialogListener.onDismissDialog(dismissObject);
		}

		super.dismiss();
	}

	public interface OnPositiveButtonClickListener {
		void onPositiveButtonClick(View v, Dialog d);
	}

	public interface OnNegativeButtonCLickListener {
		void onNegativeButtonClick(View v, Dialog d);
	}

	public interface OnDismissDialogListener {
		void onDismissDialog(Object object);
	}

}