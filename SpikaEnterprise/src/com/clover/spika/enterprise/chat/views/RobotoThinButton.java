package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class RobotoThinButton extends Button {

	public RobotoThinButton(Context context) {
		super(context);
		setFont();
	}

	public RobotoThinButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}

	public RobotoThinButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont();
	}

	private void setFont() {
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Thin.ttf");
		setTypeface(typeface);
	}

}
