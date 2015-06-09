package com.clover.spika.enterprise.chat.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.clover.spika.enterprise.chat.CameraCropActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.utils.Const;

public class NewAppDialog extends Dialog {

	public NewAppDialog(final Context context, boolean isFinish) {
		super(context, R.style.Theme_Dialog);

		try {
			setOwnerActivity((Activity) context);
		} catch (Exception e) {
			dismiss();
		}

	}

	/**
	 * Go to recording screen from gallery or camera on create room
	 */
	public void choseCamGalleryRoom() {
		this.setContentView(R.layout.dialog_chose_cam_rec_new);

		TextView camera = (TextView) findViewById(R.id.takePhoto);
        TextView gallery = (TextView) findViewById(R.id.selectPhoto);

		camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.IS_SQUARE, true);
				// getContext().startActivity(intent);
				getOwnerActivity().startActivityForResult(intent, 1);
			}
		});

		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getContext(), CameraCropActivity.class);
				intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
				intent.putExtra(Const.ROOM_INTENT, true);
				intent.putExtra(Const.IS_SQUARE, true);
				getOwnerActivity().startActivityForResult(intent, 1);
			}
		});

        Button cancel = (Button) findViewById(R.id.cancelBtn);
        cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

		show();
	}

}