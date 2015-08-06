package com.medicbleep.app.chat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoThinTextView extends TextView {

	public RobotoThinTextView(Context context) {
		super(context);
		setFont();
	}

	public RobotoThinTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}

	public RobotoThinTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont();
	}

	private void setFont() {
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		setTypeface(typeface);
	}

}
