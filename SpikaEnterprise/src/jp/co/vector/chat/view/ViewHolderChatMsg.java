package jp.co.vector.chat.view;

import jp.co.vector.chat.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewHolderChatMsg {

    public RelativeLayout dateSeparator;
    public TextView sectionDate;

    public LinearLayout defaultMsgLayoutMe;

    // start: message item for my message
    public RelativeLayout meIconLayout;
    public ImageView meIcon;
    public RelativeLayout meItemMsgLayout;
    public TextView mePersonName;
    public TextView meMsgContent;
    public TextView timeMe;
    public RelativeLayout settingsLayoutMe;
    // end: me msg

    // start: me image msg
    public LinearLayout imageMsgLayoutMe;
    public TextView mePersonNameImage;
    public ImageView imagePreviewMe;
    public TextView imgDescriptionMe;
    // end: me image msg

    // start: message item for you message
    public RelativeLayout youIconLayout;
    public ImageView youIcon;
    public RelativeLayout youItemMsgLayout;
    public RelativeLayout defaultMsgLayoutYou;
    public TextView youPersonName;
    public TextView youMsgContent;
    public TextView timeYou;
    // end: you msg

    // start: you image msg
    public RelativeLayout imageMsgLayoutYou;
    public TextView youPersonNameImage;
    public ImageView imagePreviewYou;
    public TextView imgDescriptionYou;
    // end: you image msg

    // start: options layout
    public TextView likeYou;
    public TextView likeNbrYou;
    public TextView reportMsgYou;
    public TextView likeMe;
    public TextView likeNbrMe;
    // end: options layout

    // start: loading bar
    public RelativeLayout loading_bar;
    public ImageView loading_bar_img;

    // end: loading bar

    public ViewHolderChatMsg(View view) {

	dateSeparator = (RelativeLayout) view.findViewById(R.id.dateSeparator);

	// start: message item for my message
	meIconLayout = (RelativeLayout) view.findViewById(R.id.meIconLayout);
	meIcon = (ImageView) view.findViewById(R.id.meIcon);
	meItemMsgLayout = (RelativeLayout) view.findViewById(R.id.meItemMsgLayout);
	defaultMsgLayoutMe = (LinearLayout) view.findViewById(R.id.defaultMsgLayoutMe);
	mePersonName = (TextView) view.findViewById(R.id.mePersonName);
	meMsgContent = (TextView) view.findViewById(R.id.meMsgContent);
	timeMe = (TextView) view.findViewById(R.id.timeMe);
	settingsLayoutMe = (RelativeLayout) view.findViewById(R.id.settingsLayoutMe);
	// end: me msg

	// start: me image msg
	imageMsgLayoutMe = (LinearLayout) view.findViewById(R.id.imageMsgLayoutMe);
	mePersonNameImage = (TextView) view.findViewById(R.id.mePersonNameImage);
	imagePreviewMe = (ImageView) view.findViewById(R.id.imagePreviewMe);
	imgDescriptionMe = (TextView) view.findViewById(R.id.imgDescriptionMe);
	// end: me image msg

	// start: message item for you message
	youIconLayout = (RelativeLayout) view.findViewById(R.id.youIconLayout);
	youIcon = (ImageView) view.findViewById(R.id.youIcon);
	youItemMsgLayout = (RelativeLayout) view.findViewById(R.id.youItemMsgLayout);
	defaultMsgLayoutYou = (RelativeLayout) view.findViewById(R.id.defaultMsgLayoutYou);
	youPersonName = (TextView) view.findViewById(R.id.youPersonName);
	youMsgContent = (TextView) view.findViewById(R.id.youMsgContent);
	timeYou = (TextView) view.findViewById(R.id.timeYou);
	// end: you msg

	// start: you image msg
	imageMsgLayoutYou = (RelativeLayout) view.findViewById(R.id.imageMsgLayoutYou);
	youPersonNameImage = (TextView) view.findViewById(R.id.youPersonNameImage);
	imagePreviewYou = (ImageView) view.findViewById(R.id.imagePreviewYou);
	imgDescriptionYou = (TextView) view.findViewById(R.id.imgDescriptionYou);
	// end: you image msg

	// start: options layout
	likeYou = (TextView) view.findViewById(R.id.likeYou);
	likeNbrYou = (TextView) view.findViewById(R.id.likeNbrYou);
	reportMsgYou = (TextView) view.findViewById(R.id.reportMsgYou);
	likeMe = (TextView) view.findViewById(R.id.likeMe);
	likeNbrMe = (TextView) view.findViewById(R.id.likeNbrMe);
	// end: options layout

	sectionDate = (TextView) view.findViewById(R.id.sectionDate);

	// start: loading bar
	loading_bar = (RelativeLayout) view.findViewById(R.id.loading_bar);
	loading_bar_img = (ImageView) view.findViewById(R.id.loading_bar_img);
	// end: loading bar
    }
}