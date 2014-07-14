package com.clover.spika.enterprise.chat.view;

import com.clover.spika.enterprise.chat.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewHolderCharacter {

	public RelativeLayout itemLayout;
	public RelativeLayout itemCliclkLayout;
	public ImageView selectableImg;
	public RelativeLayout imageLayout;
	public ImageView profileImg;

	public TextView personName;
	public TextView gameName;

	public ViewHolderCharacter(View view) {

		itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
		itemCliclkLayout = (RelativeLayout) view
				.findViewById(R.id.itemCliclkLayout);
		selectableImg = (ImageView) view.findViewById(R.id.selectableImg);
		imageLayout = (RelativeLayout) view.findViewById(R.id.imageLayout);
		profileImg = (ImageView) view.findViewById(R.id.gameImg);

		personName = (TextView) view.findViewById(R.id.personName);
		gameName = (TextView) view.findViewById(R.id.gameName);
	}

}