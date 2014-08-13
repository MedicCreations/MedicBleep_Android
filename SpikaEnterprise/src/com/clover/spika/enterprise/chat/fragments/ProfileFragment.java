package com.clover.spika.enterprise.chat.fragments;

import com.clover.spika.enterprise.chat.NewPasscodeActivity;
import com.clover.spika.enterprise.chat.PasscodeActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class ProfileFragment extends Fragment implements OnClickListener {

	public ImageView profileImage;
	public TextView profileName;
	public TextView screenTitle;
	public Switch mSwitchPasscodeEnabled;

	public ImageLoader imageLoader;

	// TODO fix opening
	public static void openProfile(Context context, String fileId) {
		// if (TextUtils.isEmpty(fileId)) {
		// fileId = Helper.getUserImage(context);
		// }
		// Intent intent = new Intent(context,
		// ProfileActivity.class).putExtra(Const.FIRSTNAME,
		// Helper.getUserFirstName(context)).putExtra(Const.LASTNAME,
		// Helper.getUserLastName(context)).putExtra(Const.USER_IMAGE_NAME,
		// fileId);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// context.startActivity(intent);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_profile, null);

		imageLoader = new ImageLoader(getActivity());

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);

		int width = getResources().getDisplayMetrics().widthPixels;
		int padding = (int) (width / 9);

		profileImage.getLayoutParams().width = width - Helper.dpToPx(getActivity(), padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(getActivity(), padding);

		profileName = (TextView) rootView.findViewById(R.id.profileName);
		screenTitle = (TextView) rootView.findViewById(R.id.screenTitle);

		ImageView addPhoto = (ImageView) rootView.findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(this);

		mSwitchPasscodeEnabled = (Switch) rootView.findViewById(R.id.switchPasscode);
		mSwitchPasscodeEnabled.setOnClickListener(this);
		mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(getActivity()));

		return rootView;
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
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryProfile();
	}

	private void onCheckedChanged(boolean isChecked) {
		if (isChecked) {
			startActivityForResult(new Intent(getActivity(), NewPasscodeActivity.class), Const.REQUEST_NEW_PASSCODE);
		} else {
			/* Current passcode has to be checked before it can be removed */
			Intent intent = new Intent(getActivity(), PasscodeActivity.class);
			intent.putExtra(Const.CHANGE_PASSCODE_INTENT, true);
			startActivityForResult(intent, Const.REQUEST_REMOVE_PASSCODE);
		}
	}

}
