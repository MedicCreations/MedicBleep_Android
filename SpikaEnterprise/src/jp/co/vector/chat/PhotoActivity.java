package jp.co.vector.chat;

import jp.co.vector.chat.extendables.BaseActivity;
import jp.co.vector.chat.lazy.ImageLoader;
import jp.co.vector.chat.utils.Const;
import jp.co.vector.chat.R;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PhotoActivity extends BaseActivity {

    RelativeLayout imageLayout;
    ImageView mImageView;

    String imageUrl;

    @Override
    protected void onCreate(Bundle arg0) {
	super.onCreate(arg0);
	setContentView(R.layout.activity_photo);

	imageLayout = (RelativeLayout) findViewById(R.id.imageLayout);
	mImageView = (ImageView) findViewById(R.id.mImageView);

	if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Const.IMAGE_NAME)) {
	    imageUrl = getIntent().getExtras().getString(Const.IMAGE_NAME, "");

	    ImageLoader imageLoader = new ImageLoader(this);
	    imageLoader.displayImage(imageUrl, mImageView, false);
	}
    }
}