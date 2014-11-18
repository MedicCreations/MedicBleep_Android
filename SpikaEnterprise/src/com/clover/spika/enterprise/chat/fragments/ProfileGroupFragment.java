package com.clover.spika.enterprise.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ProfileGroupActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoRegularTextView;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

public class ProfileGroupFragment extends CustomFragment implements OnClickListener {

	public ImageView profileImage;

	String imageId;
	String chatName;
	String chatId;
	boolean isAdmin;
	int isPrivate;
	String chatPassword;

	RobotoRegularTextView tvPassword;

	private ImageLoader imageLoader;

	private FrameLayout loadingLayout;

	public ProfileGroupFragment(Intent intent) {
		setData(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_profile_group, container, false);

		View addPhotoButton = rootView.findViewById(R.id.addPhoto);
		Switch switchIsPrivate = (Switch) rootView.findViewById(R.id.switch_private_room);
		switchIsPrivate.setChecked(isPrivate == 1 ? true : false);

		RelativeLayout passwordLayout = (RelativeLayout) rootView.findViewById(R.id.layoutPassword);

		tvPassword = (RobotoRegularTextView) rootView.findViewById(R.id.tvPassword);

		if (isAdmin) {

			if (null != chatPassword) {
				if (chatPassword.equals("")) {
					tvPassword.setText("");
					tvPassword.setHint("Set password");
				}
			} else {
				tvPassword.setText("");
				tvPassword.setHint("Set password");
			}

			addPhotoButton.setOnClickListener(this);
			tvPassword.setOnClickListener(this);
		} else {

			if (null != chatPassword) {
				if (chatPassword.equals("")) {
					passwordLayout.setVisibility(View.GONE);
				}
			} else {
				passwordLayout.setVisibility(View.GONE);
			}

			addPhotoButton.setVisibility(View.GONE);
			switchIsPrivate.setEnabled(false);
		}

		((TextView) rootView.findViewById(R.id.profileName)).setText(chatName);

		loadingLayout = (FrameLayout) rootView.findViewById(R.id.loadingLayout);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);
		Helper.setRoomThumbId(getActivity(), imageId);

		imageLoader = new ImageLoader(getActivity());
		imageLoader.setDefaultImage(R.drawable.default_group_image);

		imageLoader.displayImage(getActivity(), imageId, profileImage, new OnImageDisplayFinishListener() {

			@Override
			public void onFinish() {
				loadingLayout.setVisibility(View.GONE);
			}
		});

		return rootView;
	}

	public void setData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageId = intent.getExtras().getString(Const.IMAGE);
			chatName = intent.getExtras().getString(Const.CHAT_NAME);
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			isAdmin = intent.getExtras().getBoolean(Const.IS_ADMIN);
			isPrivate = intent.getExtras().getInt(Const.IS_PRIVATE);
			chatPassword = intent.getExtras().getString(Const.PASSWORD);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.addPhoto:
			showDialog();
			break;
		case R.id.tvPassword:
			final AppDialog dialog = new AppDialog(getActivity(), false);

			if (TextUtils.isEmpty(chatPassword)) {
				dialog.setPasswordInput(getString(R.string.new_password), getString(R.string.ok), getString(R.string.cancel_big), null);
				dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

					@Override
					public void onPositiveButtonClick(View v) {
						RelativeLayout parent = (RelativeLayout) v.getParent().getParent();
						String newPassword = ((RobotoThinEditText) parent.findViewById(R.id.etDialogPassword)).getText().toString();
						tvPassword.setText(newPassword);

					}
				});
			} else {
				dialog.setPasswordInput(getString(R.string.old_password), getString(R.string.ok), getString(R.string.cancel_big), chatPassword);
				dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

					@Override
					public void onPositiveButtonClick(View v) {
						dialog.setPasswordInput(getString(R.string.new_password), getString(R.string.ok), getString(R.string.cancel_big), null);
						dialog.setOnPositiveButtonClick(new OnPositiveButtonClickListener() {

							@Override
							public void onPositiveButtonClick(View v) {
								RelativeLayout parent = (RelativeLayout) v.getParent().getParent();
								String newPassword = ((RobotoThinEditText) parent.findViewById(R.id.etDialogPassword)).getText().toString();
								tvPassword.setText(newPassword);

							}
						});
					}
				});
			}

		default:
			break;
		}
	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryRoomUpdate(chatId, chatName);
	}

	@Override
	public void onResume() {
		super.onResume();
		if ((Helper.getRoomThumbId(getActivity()) != imageId) && (!Helper.getRoomThumbId(getActivity()).isEmpty())) {
			loadingLayout.setVisibility(View.VISIBLE);
			imageId = Helper.getRoomThumbId(getActivity());
			imageLoader.displayImage(getActivity(), imageId, profileImage, new OnImageDisplayFinishListener() {

				@Override
				public void onFinish() {
					loadingLayout.setVisibility(View.GONE);
				}
			});
			((ProfileGroupActivity) getActivity()).setChangeImage(Helper.getRoomThumbId(getActivity()), Helper.getRoomThumbId(getActivity()));
		}
		SpikaEnterpriseApp.getInstance().deleteSamsungPathImage();
	}
}
