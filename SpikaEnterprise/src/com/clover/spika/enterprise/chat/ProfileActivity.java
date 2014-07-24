package com.clover.spika.enterprise.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.lazy.ImageLoader;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.PasscodeUtility;

public class ProfileActivity extends BaseActivity implements OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int REQUEST_NEW_PASSCODE = 9001;
    private static final int REQUEST_REMOVE_PASSCODE = 9002;

	private ImageView profileImage;
    private TextView profileName;
    private Switch mSwitchPasscodeEnabled;

    private ImageLoader imageLoader;
    
    private boolean isWrongPassChecked = false;

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_profile);

		imageLoader = new ImageLoader(this);

		profileImage = (ImageView) findViewById(R.id.profileImage);

		int width = getResources().getDisplayMetrics().widthPixels;
		int padding = (int) (width / 10);

		profileImage.getLayoutParams().width = width - Helper.dpToPx(this, padding);
		profileImage.getLayoutParams().height = width - Helper.dpToPx(this, padding);

		profileName = (TextView) findViewById(R.id.profileName);

        ImageView addPhoto = (ImageView) findViewById(R.id.addPhoto);
		addPhoto.setOnClickListener(this);

        mSwitchPasscodeEnabled = (Switch) findViewById(R.id.switchPasscode);
        mSwitchPasscodeEnabled.setOnCheckedChangeListener(this);
        mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(this));

		getIntentData(getIntent());
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_NEW_PASSCODE == requestCode) {
            if (RESULT_OK == resultCode) {
                PasscodeUtility.getInstance().setSessionValid(true);

                if (data != null && data.hasExtra(NewPasscodeActivity.EXTRA_PASSCODE)) {
                    PasscodeUtility.getInstance().setPasscode(this, data.getStringExtra(NewPasscodeActivity.EXTRA_PASSCODE));
                    PasscodeUtility.getInstance().setPasscodeEnabled(this, true);
                }
            } else {
                PasscodeUtility.getInstance().setSessionValid(false);
                mSwitchPasscodeEnabled.setChecked(false);
            }
        } else if(REQUEST_REMOVE_PASSCODE == requestCode){
        	if(RESULT_OK == resultCode){
        		PasscodeUtility.getInstance().setPasscodeEnabled(this, false);
        		PasscodeUtility.getInstance().setPasscode(this, "");
        		PasscodeUtility.getInstance().setSessionValid(true);
        	}else{
        		PasscodeUtility.getInstance().setSessionValid(true);
        		isWrongPassChecked=true;
        		mSwitchPasscodeEnabled.setChecked(PasscodeUtility.getInstance().isPasscodeEnabled(this));
        	}
        }
    }

    private void getIntentData(Intent intent) {
		if (intent != null && intent.getExtras() != null) {
			imageLoader.displayImage(this, intent.getExtras().getString(Const.USER_IMAGE_NAME), profileImage, true);
			profileName.setText(intent.getExtras().getString(Const.FIRSTNAME) + " "
					+ intent.getExtras().getString(Const.LASTNAME));

			setScreenTitle(intent.getExtras().getString(Const.FIRSTNAME));
		}
	}

	private void choosePhoto() {
		Intent intent = new Intent(this, CameraCropActivity.class);
		intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
		intent.putExtra(Const.PROFILE_INTENT, true);
		startActivity(intent);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.addPhoto:
			choosePhoto();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		getIntentData(intent);
	}

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	if(isWrongPassChecked){
    		isWrongPassChecked = false;
    		return;
    	}
//        if (isChecked) {
//            startActivityForResult(new Intent(this, NewPasscodeActivity.class), REQUEST_NEW_PASSCODE);
//        } else {
//        	startActivityForResult(new Intent(this, PasscodeActivity.class), REQUEST_REMOVE_PASSCODE);
//        }
    }
    
}