package com.clover.spika.enterprise.chat.view;


import com.clover.spika.enterprise.chat.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewHolderGroup {

    public RelativeLayout itemLayout;
    public RelativeLayout clickLayout;
    public ImageView talkImg;
    public ImageView circleImgNew;
    public TextView talkName;
    public TextView talkDesc;

    public RelativeLayout missedLayout;
    public TextView missedtext;

    public ImageView goImage;

    public ViewHolderGroup(View view) {

	itemLayout = (RelativeLayout) view.findViewById(R.id.itemLayout);
	clickLayout = (RelativeLayout) view.findViewById(R.id.clickLayout);
	talkImg = (ImageView) view.findViewById(R.id.gameImg);
	circleImgNew = (ImageView) view.findViewById(R.id.circleImgNew);
	talkName = (TextView) view.findViewById(R.id.gameName);
	talkDesc = (TextView) view.findViewById(R.id.gameStatus);

	missedLayout = (RelativeLayout) view.findViewById(R.id.missedLayout);
	missedtext = (TextView) view.findViewById(R.id.missedtext);

	goImage = (ImageView) view.findViewById(R.id.goImage);
    }
}