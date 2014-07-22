package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
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
	protected void onDraw(Canvas canvas) {
		if (isDrawValid()) {
			Bitmap b = ((BitmapDrawable) getDrawable()).getBitmap();
			Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

			int width = getWidth();
			// int height = getHeight();

			int paddingLeft = getPaddingLeft();

			canvas.drawBitmap(Helper.getRoundedBitmap(bitmap, width - paddingLeft), paddingLeft / 2, paddingLeft / 2, null);
		}
	}

	private boolean isDrawValid() {
		return getDrawable() != null && getDrawable() instanceof BitmapDrawable && getWidth() > 0 && getHeight() > 0;
	}
}
