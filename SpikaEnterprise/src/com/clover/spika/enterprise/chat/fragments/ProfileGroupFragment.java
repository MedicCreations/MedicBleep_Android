package com.clover.spika.enterprise.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class ProfileGroupFragment extends CustomFragment implements OnClickListener{

	public ImageView profileImage;

	int width = 0;
	int padding = 0;

	String imageId;
	String chatName;
	boolean isAdmin;

	public ProfileGroupFragment(Intent intent) {
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

		View rootView = inflater.inflate(R.layout.fragment_profile_group, container, false);

		View addPhotoButton = rootView.findViewById(R.id.addPhoto);
		if (isAdmin) {
			addPhotoButton.setOnClickListener(this);
		}
		else {
			addPhotoButton.setVisibility(View.GONE);
		}
		
		((TextView) rootView.findViewById(R.id.profileName)).setText(chatName);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);
		profileImage.getLayoutParams().width = width - Helper.dpToPx(getActivity(), padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(getActivity(), padding);

		return rootView;
	}

	public void setData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageId = intent.getExtras().getString(Const.USER_IMAGE_NAME);
			chatName = intent.getExtras().getString(Const.CHAT_NAME);
			isAdmin = intent.getExtras().getBoolean(Const.IS_ADMIN);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.addPhoto:
			showDialog();
			break;

		default:
			break;
		}
	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryProfile();
	}
}