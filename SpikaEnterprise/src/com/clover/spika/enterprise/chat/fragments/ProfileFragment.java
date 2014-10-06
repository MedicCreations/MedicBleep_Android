package com.clover.spika.enterprise.chat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CreateRoomActivity;
import com.clover.spika.enterprise.chat.MainActivity;
import com.clover.spika.enterprise.chat.NewPasscodeActivity;
import com.clover.spika.enterprise.chat.PasscodeActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.api.UserApi;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.models.Result;
import com.clover.spika.enterprise.chat.models.UserWrapper;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;
import com.clover.spika.enterprise.chat.views.DetailsScrollView;

public class ProfileFragment extends CustomFragment implements OnClickListener {

	private Switch mSwitchPasscodeEnabled;
	private ImageView profileImage;
    private DetailsScrollView mDetailScrollView;

	String imageId;
	String firstname;
	String lastname;

    public static ProfileFragment newInstance(String imageId, String firstName, String lastName) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.USER_IMAGE_NAME, imageId);
        arguments.putString(Const.FIRSTNAME, firstName);
        arguments.putString(Const.LASTNAME, lastName);
        fragment.setArguments(arguments);
        return fragment;
    }

	public ProfileFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setData(getArguments());
	}

	@Override
	public void onResume() {
		super.onResume();
		onClosed();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

		rootView.findViewById(R.id.addPhoto).setOnClickListener(this);
		((TextView) rootView.findViewById(R.id.profileName)).setText(firstname + " " + lastname);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);

		mSwitchPasscodeEnabled = (Switch) rootView.findViewById(R.id.switchPasscode);
		mSwitchPasscodeEnabled.setOnClickListener(this);
		mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(getActivity()));

        mDetailScrollView = (DetailsScrollView) rootView.findViewById(R.id.scrollViewDetails);

		return rootView;
	}

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.progressBarDetails).setVisibility(View.VISIBLE);
        new UserApi().getProfile(getActivity(), Helper.getUserId(getActivity()), new ApiCallback<UserWrapper>() {
            @Override
            public void onApiResponse(Result<UserWrapper> result) {
                if (result.isSuccess()) {
                    view.findViewById(R.id.progressBarDetails).setVisibility(View.INVISIBLE);
                    mDetailScrollView.createDetailsView(result.getResultData().getUser().getPublicDetails());
                }
            }
        });
    }

    @Override
	public void onClosed() {
		if (getActivity() instanceof MainActivity) {

			if (!Helper.getUserImage(getActivity()).equals(imageId)) {
				imageId = Helper.getUserImage(getActivity());
			}

			((MainActivity) getActivity()).getImageLoader().displayImage(getActivity(), imageId, profileImage);
		}
	}

	public void setData(Bundle bundle) {
		if (bundle != null) {
			imageId = bundle.getString(Const.USER_IMAGE_NAME);
			firstname = bundle.getString(Const.FIRSTNAME);
			lastname = bundle.getString(Const.LASTNAME);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Const.PASSCODE_ENTRY_VALIDATION_REQUEST) {
			if (resultCode == Activity.RESULT_OK) {
				// if by some chance session is not set to valid, set it now
				if (!PasscodeUtility.getInstance().isSessionValid()) {
					PasscodeUtility.getInstance().setSessionValid(true);
				}
			} else {
				PasscodeUtility.getInstance().setSessionValid(false);
				getActivity().finish();
			}
		} else if (Const.REQUEST_NEW_PASSCODE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				PasscodeUtility.getInstance().setSessionValid(true);

				if (data != null && data.hasExtra(NewPasscodeActivity.EXTRA_PASSCODE)) {
					PasscodeUtility.getInstance().setPasscode(getActivity(), data.getStringExtra(NewPasscodeActivity.EXTRA_PASSCODE));
					PasscodeUtility.getInstance().setPasscodeEnabled(getActivity(), true);
				}
			} else {
				PasscodeUtility.getInstance().setSessionValid(false);
				mSwitchPasscodeEnabled.setChecked(false);
			}
		} else if (Const.REQUEST_REMOVE_PASSCODE == requestCode) {
			if (resultCode == Activity.RESULT_OK) {
				PasscodeUtility.getInstance().setPasscodeEnabled(getActivity(), false);
				PasscodeUtility.getInstance().setPasscode(getActivity(), "");
				PasscodeUtility.getInstance().setSessionValid(true);
			} else {
				PasscodeUtility.getInstance().setSessionValid(true);
				mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(getActivity()));
			}
		}
	}
}
