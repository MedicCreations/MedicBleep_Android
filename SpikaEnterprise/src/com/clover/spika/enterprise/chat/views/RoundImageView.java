package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.clover.spika.enterprise.chat.utils.Helper;

public class RoundImageView extends ImageView {

	public RoundImageView(Context context) {
		super(context);
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			int radius = bitmap.getWidth();
			bitmap = Helper.getRoundedBitmap(bitmap, radius);
		}
		super.setImageBitmap(bitmap);
	}

}
