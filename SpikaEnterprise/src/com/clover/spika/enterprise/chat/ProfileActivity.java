package com.clover.spika.enterprise.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

public class ProfileActivity extends BaseActivity implements OnClickListener {

	private static final int REQUEST_NEW_PASSCODE = 9001;
	private static final int REQUEST_REMOVE_PASSCODE = 9002;

	private ImageView profileImage;
	private TextView profileName;
	private TextView screenTitle;
	private Switch mSwitchPasscodeEnabled;

	private ImageLoader imageLoader;

	public static void openProfile(Context context, String fileId) {
		if (TextUtils.isEmpty(fileId)) {
			fileId = Helper.getUserImage(context);
		}
		Intent intent = new Intent(context, ProfileActivity.class).putExtra(Const.FIRSTNAME, Helper.getUserFirstName(context)).putExtra(Const.LASTNAME, Helper.getUserLastName(context)).putExtra(Const.USER_IMAGE_NAME, fileId);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_profile);

		imageLoader = new ImageLoader(this);

		profileImage = (ImageView) findViewById(R.id.profileImage);

		int width = getResources().getDisplayMetrics().widthPixels;
		int padding = (int) (width / 9);

		profileImage.getLayoutParams().width = width - Helper.dpToPx(this, padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(this, padding);

		profileName = (TextView) findViewById(R.id.profileName);
		screenTitle = (TextView) findViewById(R.id.screenTitle);

		ImageView addPhoto = (ImageView) findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(this);

		mSwitchPasscodeEnabled = (Switch) findViewById(R.id.switchPasscode);
		mSwitchPasscodeEnabled.setOnClickListener(this);
		mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(this));

		getIntentData(getIntent());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("Vida", "onActivityResult");

		if (REQUEST_NEW_PASSCODE == requestCode) {
			if (RESULT_OK == resultCode) {
				PasscodeUtility.getInstance().setSessionValid(true);

				if (data != null && data.hasExtra(NewPasscodeActivity.EXTRA_PASSCODE)) {
					PasscodeUtility.getInstance().setPasscode(this, data.getStringExtra(NewPasscodeActivity.EXTRA_PASSCODE));
					PasscodeUtility.getInstance().setPasscodeEnabled(this, true);
				}
			} else {
				PasscodeUtility.getInstance().setSessionValid(false);
				mSwitchPasscodeEnabled.setChecked(false);
			}
		} else if (REQUEST_REMOVE_PASSCODE == requestCode) {
			if (RESULT_OK == resultCode) {
				PasscodeUtility.getInstance().setPasscodeEnabled(this, false);
				PasscodeUtility.getInstance().setPasscode(this, "");
				PasscodeUtility.getInstance().setSessionValid(true);
			} else {
				Log.d("Vida", "HERE");
				PasscodeUtility.getInstance().setSessionValid(true);
				mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(this));
			}
		}
	}

	private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageLoader.displayImage(this, intent.getExtras().getString(Const.USER_IMAGE_NAME), profileImage, false);
			profileName.setText(intent.getExtras().getString(Const.FIRSTNAME) + " " + intent.getExtras().getString(Const.LASTNAME));

			if (intent.getExtras() != null && intent.getExtras().containsKey(Const.FIRSTNAME)) {
				screenTitle.setText(intent.getExtras().getString(Const.FIRSTNAME, getResources().getString(R.string.profile)));
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.addPhoto:
			showDialog();
			break;

		case R.id.switchPasscode:
			/*
			 * checked state is changed before click is even performed, thus
			 * forwarding current state is enough
			 */
			onCheckedChanged(mSwitchPasscodeEnabled.isChecked());
			break;

		default:
			break;
		}
	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(this, false);
		dialog.choseCamGalleryProfile();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

	private void onCheckedChanged(boolean isChecked) {
		Log.d("Vida", "isChecked: " + isChecked);
		if (isChecked) {
			startActivityForResult(new Intent(this, NewPasscodeActivity.class), REQUEST_NEW_PASSCODE);
		} else {
			/* Current passcode has to be checked before it can be removed */
			Intent intent = new Intent(this, PasscodeActivity.class);
			intent.putExtra(Const.CHANGE_PASSCODE_INTENT, true);
			startActivityForResult(intent, REQUEST_REMOVE_PASSCODE);
		}
	}
}