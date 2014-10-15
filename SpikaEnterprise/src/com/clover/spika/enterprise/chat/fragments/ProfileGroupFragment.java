package com.clover.spika.enterprise.chat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.extendables.CustomFragment;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.listeners.OnImageDisplayFinishListener;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

public class ProfileGroupFragment extends CustomFragment implements OnClickListener {

	public ImageView profileImage;

	String imageId;
	String chatName;
	String chatId;
	boolean isAdmin;
	
	private ImageLoader imageLoader;
	
	private ProgressBar pbLoading;

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
		if (isAdmin) {
			addPhotoButton.setOnClickListener(this);
		}
		else {
			addPhotoButton.setVisibility(View.GONE);
		}
		
		((TextView) rootView.findViewById(R.id.profileName)).setText(chatName);
		
		pbLoading = (ProgressBar) rootView.findViewById(R.id.loadingPB);

		profileImage = (ImageView) rootView.findViewById(R.id.profileImage);		
		Helper.setRoomThumbId(getActivity(), imageId);
		
		imageLoader = new ImageLoader(getActivity());
		imageLoader.setDefaultImage(R.drawable.default_group_image);
		
		imageLoader.displayImage(getActivity(), imageId, profileImage, new OnImageDisplayFinishListener() {
			
			@Override
			public void onFinish() {
				pbLoading.setVisibility(View.GONE);
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
		dialog.choseCamGalleryRoomUpdate(chatId, chatName);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if ((Helper.getRoomThumbId(getActivity()) != imageId) && (!Helper.getRoomThumbId(getActivity()).isEmpty())) {
			imageId = Helper.getRoomThumbId(getActivity());
			imageLoader.displayImage(getActivity(), imageId, profileImage, new OnImageDisplayFinishListener() {
				
				@Override
				public void onFinish() {
					pbLoading.setVisibility(View.GONE);
				}
			});
		}
		SpikaEnterpriseApp.getInstance().deleteSamsungPathImage();
	}
}
