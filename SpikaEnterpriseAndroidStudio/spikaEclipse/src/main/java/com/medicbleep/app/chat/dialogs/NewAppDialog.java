package com.medicbleep.app.chat.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.medicbleep.app.chat.CameraCropActivity;
import com.medicbleep.app.chat.R;
import com.medicbleep.app.chat.RecordVideoActivity;
import com.medicbleep.app.chat.utils.Const;

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

    /**
     * Go to recording screen from gallery or camera on update room
     */
    public void choseCamGalleryRoomUpdate(final String chatId, final String chatName) {
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
                intent.putExtra(Const.CHAT_ID, chatId);
                intent.putExtra(Const.CHAT_NAME, chatName);
                intent.putExtra(Const.UPDATE_PICTURE, true);
                intent.putExtra(Const.IS_SQUARE, true);

                getOwnerActivity().startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

                Intent intent = new Intent(getContext(), CameraCropActivity.class);
                intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
                intent.putExtra(Const.ROOM_INTENT, true);
                intent.putExtra(Const.CHAT_ID, chatId);
                intent.putExtra(Const.CHAT_NAME, chatName);
                intent.putExtra(Const.UPDATE_PICTURE, true);
                intent.putExtra(Const.IS_SQUARE, true);

                getOwnerActivity().startActivity(intent);
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

    /**
     * Go to recording screen from gallery or camera
     */
    public void choseCamGalleryProfile() {
        choseCamGalleryProfile(null);
    }

    public void choseCamGalleryProfile(final RemoveImageListener listener) {
        this.setContentView(R.layout.dialog_chose_cam_rec_new);

        TextView camera = (TextView) findViewById(R.id.takePhoto);
        TextView gallery = (TextView) findViewById(R.id.selectPhoto);

        camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

                Intent intent = new Intent(getContext(), CameraCropActivity.class);
                intent.putExtra(Const.INTENT_TYPE, Const.PHOTO_INTENT);
                intent.putExtra(Const.PROFILE_INTENT, true);
                intent.putExtra(Const.IS_SQUARE, true);
                getContext().startActivity(intent);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

                Intent intent = new Intent(getContext(), CameraCropActivity.class);
                intent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT);
                intent.putExtra(Const.PROFILE_INTENT, true);
                intent.putExtra(Const.IS_SQUARE, true);
                getContext().startActivity(intent);
            }
        });

        Button cancel = (Button) findViewById(R.id.cancelBtn);
        cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if(listener != null){
            TextView remove = (TextView) findViewById(R.id.removePhoto);
            remove.setVisibility(View.VISIBLE);
            findViewById(R.id.removePhotoView).setVisibility(View.VISIBLE);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    listener.onRemove();
                }
            });
        }

        show();
    }

    public interface RemoveImageListener{
        public void onRemove();
    }

    /**
     * Go to recording screen from gallery or camera
     */
    public void choseCamGallery(final String chatId, final String rootId, final String messageId) {
        this.setContentView(R.layout.dialog_chose_video_rec_new);

        TextView record = (TextView) findViewById(R.id.recordVideo);
        TextView select = (TextView) findViewById(R.id.selectVideo);

        record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

                Intent recordVideoIntent = new Intent(getContext(), RecordVideoActivity.class);
                recordVideoIntent.putExtra(Const.INTENT_TYPE, Const.VIDEO_INTENT_INT);
                recordVideoIntent.putExtra(Const.CHAT_ID, chatId);
                recordVideoIntent.putExtra(Const.EXTRA_ROOT_ID, rootId);
                recordVideoIntent.putExtra(Const.EXTRA_MESSAGE_ID, messageId);
                getContext().startActivity(recordVideoIntent);
            }
        });

        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

                Intent recordVideoIntent = new Intent(getContext(), RecordVideoActivity.class);
                recordVideoIntent.putExtra(Const.INTENT_TYPE, Const.GALLERY_INTENT_INT);
                recordVideoIntent.putExtra(Const.CHAT_ID, chatId);
                recordVideoIntent.putExtra(Const.EXTRA_ROOT_ID, rootId);
                recordVideoIntent.putExtra(Const.EXTRA_MESSAGE_ID, messageId);
                getContext().startActivity(recordVideoIntent);
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