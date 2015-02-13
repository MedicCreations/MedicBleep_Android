package com.clover.spika.enterprise.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.ChooseCategoryActivity;
import com.clover.spika.enterprise.chat.ProfileGroupActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.SetAdminActivity;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppDialog.OnPositiveButtonClickListener;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoaderSpice;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.views.RobotoThinEditText;

public class ProfileGroupFragment extends CustomFragment implements OnClickListener {

	public ImageView profileImage;

	String imageId;
	String chatName;
	String chatId;
	boolean isAdmin;
	int isPrivate;
	String chatPassword;
	String categoryName = null;
	String categoryId = null;

	Button tvPassword;
	Button tvSetAdmin;
	Button tvChangeCat;
	TextView tvChangeCategoryLabel;

	private FrameLayout loadingLayout;

	View addPhotoButton;
	LinearLayout passwordLayout;
	LinearLayout layoutSetAdmin;
	LinearLayout layoutChangeCategory;
	Switch switchIsPrivate;

	public ProfileGroupFragment() {
	}

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

		addPhotoButton = rootView.findViewById(R.id.addPhoto);
		switchIsPrivate = (Switch) rootView.findViewById(R.id.switch_private_room);
		switchIsPrivate.setChecked(isPrivate == 1 ? true : false);

		passwordLayout = (LinearLayout) rootView.findViewById(R.id.layoutPassword);
		layoutSetAdmin = (LinearLayout) rootView.findViewById(R.id.layoutSetAdmin);
		layoutChangeCategory = (LinearLayout) rootView.findViewById(R.id.layoutChangeCategory);
		tvChangeCategoryLabel = (TextView) rootView.findViewById(R.id.tvChangeCategory);

		tvPassword = (Button) rootView.findViewById(R.id.tvPassword);
		tvSetAdmin = (Button) rootView.findViewById(R.id.tvSetAdmin);
		tvChangeCat = (Button) rootView.findViewById(R.id.tvChangeCat);

		setVisual();

		((TextView) rootView.findViewById(R.id.profileName)).setText(chatName);

		loadingLayout = (FrameLayout) rootView.findViewById(R.id.loadingLayout);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);
		Helper.setRoomThumbId(getActivity(), imageId);

		getImageLoader().displayImage(profileImage, imageId, ImageLoaderSpice.DEFAULT_GROUP_IMAGE, new OnImageDisplayFinishListener() {

			@Override
			public void onFinish() {
				loadingLayout.setVisibility(View.GONE);
			}
		});

		return rootView;
	}

	public void setVisual() {
		if (isAdmin) {

			tvPassword.setHint(getString(R.string.set_password));

			tvChangeCat.setClickable(true);

			addPhotoButton.setOnClickListener(this);
			tvPassword.setOnClickListener(this);
			tvSetAdmin.setOnClickListener(this);
			tvChangeCat.setOnClickListener(this);
		} else {

			passwordLayout.setVisibility(View.GONE);
			tvChangeCat.setClickable(false);
			tvChangeCategoryLabel.setText(getString(R.string.category));
			layoutSetAdmin.setVisibility(View.GONE);
			addPhotoButton.setVisibility(View.GONE);
			switchIsPrivate.setEnabled(false);
		}

		if (!TextUtils.isEmpty(categoryName) && !categoryName.equals("0")) {
			tvChangeCat.setText(categoryName);
		} else {
			if (isAdmin) {
				tvChangeCat.setText(getString(R.string.set));
			} else {
				tvChangeCat.setText(getString(R.string.none));
			}
		}
	}

	public void setData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageId = intent.getExtras().getString(Const.IMAGE);
			chatName = intent.getExtras().getString(Const.CHAT_NAME);
			chatId = intent.getExtras().getString(Const.CHAT_ID);
			isAdmin = intent.getExtras().getBoolean(Const.IS_ADMIN);
			isPrivate = intent.getExtras().getInt(Const.IS_PRIVATE);
			chatPassword = intent.getExtras().getString(Const.PASSWORD);
			categoryName = intent.getExtras().getString(Const.CATEGORY_NAME);
			categoryId = intent.getExtras().getString(Const.CATEGORY_ID);
		}
	}

	@Override
	public void onClick(View view) {

		Intent intent;

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
			break;

		case R.id.tvSetAdmin:

			intent = new Intent(getActivity(), SetAdminActivity.class);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivityForResult(intent, Const.ADMIN_REQUEST);
			break;

		case R.id.tvChangeCat:

			intent = new Intent(getActivity(), ChooseCategoryActivity.class);
			intent.putExtra(Const.CHAT_ID, chatId);
			startActivityForResult(intent, Const.ADMIN_REQUEST);
			break;

		default:
			break;
		}
	}

	private void showDialog() {
		AppDialog dialog = new AppDialog(getActivity(), false);
		dialog.choseCamGalleryRoomUpdate(chatId, chatName);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Helper.setRoomThumbId(getActivity(), "");
	}

	@Override
	public void onResume() {
		super.onResume();
		if ((Helper.getRoomThumbId(getActivity()) != imageId) && (!Helper.getRoomThumbId(getActivity()).isEmpty())) {
			loadingLayout.setVisibility(View.VISIBLE);
			imageId = Helper.getRoomThumbId(getActivity());
			getImageLoader().displayImage(profileImage, imageId, ImageLoaderSpice.DEFAULT_GROUP_IMAGE, new OnImageDisplayFinishListener() {

				@Override
				public void onFinish() {
					loadingLayout.setVisibility(View.GONE);
				}
			});
			((ProfileGroupActivity) getActivity()).setChangeImage(Helper.getRoomThumbId(getActivity()), Helper.getRoomThumbId(getActivity()));
		}
		SpikaEnterpriseApp.deleteSamsungPathImage();
	}
}
