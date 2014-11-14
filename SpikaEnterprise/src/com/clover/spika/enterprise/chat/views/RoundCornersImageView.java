package com.clover.spika.enterprise.chat.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCornersImageView extends ImageView {

	private float corner = 15;

	public RoundCornersImageView(Context context) {
		super(context);
	}

	public RoundCornersImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundCornersImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setCorner(float cor) {
		corner = cor;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Drawable drawable = getDrawable();

		if (drawable == null) {
			return;
		}

		if (getWidth() == 0 || getHeight() == 0) {
			return;
		}
		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

		int w = getWidth();
		int padding = getPaddingLeft();

		Bitmap roundBitmap = getCroppedBitmap(bitmap, w - padding);
		canvas.drawBitmap(roundBitmap, padding / 2, padding / 2, null);

	}

	public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
		Bitmap sbmp;
		// int left = 0;
		// int top = 0;
		if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
			if (bmp.getWidth() > bmp.getHeight()) {
				double height = (double) radius / ((double) bmp.getWidth() / (double) bmp.getHeight());
				sbmp = Bitmap.createScaledBitmap(bmp, radius, (int) height, false);
				// top = radius - sbmp.getHeight();
			} else if (bmp.getWidth() < bmp.getHeight()) {
				double width = (double) radius / ((double) bmp.getHeight() / (double) bmp.getWidth());
				sbmp = Bitmap.createScaledBitmap(bmp, (int) width, radius, false);
				// left = radius - sbmp.getWidth();
			} else {
				sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
			}
		} else
			sbmp = bmp;
		Bitmap output = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.parseColor("#BAB399"));
		canvas.drawRoundRect(new RectF(rect), corner, corner, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(sbmp, rect, rect, paint);

		return output;
	}

}
