package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.util.AttributeSet;

public class RoundImageView extends RoundedImageView {

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOval(true);
	}
}
