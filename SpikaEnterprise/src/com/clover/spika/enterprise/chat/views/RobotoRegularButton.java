package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class RobotoRegularButton extends Button {

	public RobotoRegularButton(Context context) {
		super(context);
		init();
	}

	public RobotoRegularButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RobotoRegularButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		setTypeface(typeface);
	}

}
