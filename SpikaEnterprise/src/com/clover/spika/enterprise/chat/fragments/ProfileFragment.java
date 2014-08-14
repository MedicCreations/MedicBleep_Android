package com.clover.spika.enterprise.chat.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.NewPasscodeActivity;
import com.clover.spika.enterprise.chat.PasscodeActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

public class ProfileFragment extends Fragment implements OnClickListener {

	public Switch mSwitchPasscodeEnabled;
	public ImageView profileImage;

	int width = 0;
	int padding = 0;

	String imageId;
	String firstname;
	String lastname;

	public ProfileFragment(Intent intent) {
		setData(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		width = getResources().getDisplayMetrics().widthPixels;
		padding = (int) (width / 9);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

		rootView.findViewById(R.id.addPhoto).setOnClickListener(this);
		((TextView) rootView.findViewById(R.id.profileName)).setText(firstname + " " + lastname);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);
		profileImage.getLayoutParams().width = width - Helper.dpToPx(getActivity(), padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(getActivity(), padding);

		mSwitchPasscodeEnabled = (Switch) rootView.findViewById(R.id.switchPasscode);
		mSwitchPasscodeEnabled.setOnClickListener(this);
		mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(getActivity()));

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getActivity() instanceof MainActivity) {
			((MainActivity) getActivity()).getIMageLoader().displayImage(getActivity(), imageId, profileImage);
		}
	}

	public void setData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageId = intent.getExtras().getString(Const.USER_IMAGE_NAME);
			firstname = intent.getExtras().getString(Const.FIRSTNAME);
			lastname = intent.getExtras().getString(Const.LASTNAME);
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
