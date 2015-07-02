package com.medicbleep.app.chat.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class RobotoThinEditText extends EditText {

	public RobotoThinEditText(Context context) {
		super(context);
		setFont();
	}

	public RobotoThinEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont();
	}

	public RobotoThinEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont();
	}

	private void setFont() {
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		setTypeface(typeface);
	}

}
