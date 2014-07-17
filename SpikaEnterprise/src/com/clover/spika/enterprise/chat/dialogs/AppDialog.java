package com.clover.spika.enterprise.chat.dialogs;

import java.util.HashMap;

import org.json.JSONObject;

import com.clover.spika.enterprise.chat.CharacterListActivity;
import com.clover.spika.enterprise.chat.ChatActivity;
import com.clover.spika.enterprise.chat.GroupListActivity;
import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;
import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;
import com.clover.spika.enterprise.chat.utils.Const;
import com.clover.spika.enterprise.chat.utils.Helper;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppDialog extends Dialog {

	Context cntx;
	boolean isFinish = false;
	boolean checked = false;

	public AppDialog(final Context context, boolean isFinish) {
		super(context, R.style.Theme_Dialog);

		this.cntx = context;
		this.isFinish = isFinish;
	}

	/**
	 * Show info dialog
	 * 
	 * @param message
	 */
	public void setInfo(String message) {
		this.setContentView(R.layout.dialog_alert);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.controlLayout);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					((BaseActivity) cntx).finish();
				}
			}
		});

		TextView infoText = (TextView) findViewById(R.id.infoText);
		infoText.setText(message);

		show();
	}

	/**
	 * Show succeeded dialog
	 */
	public void setSucceed() {
		this.setContentView(R.layout.dialog_succeed);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					((BaseActivity) cntx).finish();
				}
			}
		});

		show();
	}

	/**
	 * show failed dialog with description from int result
	 * 
	 * @param failedText
	 */
	public void setFailed(final int errorCode) {
		this.setContentView(R.layout.dialog_failed);

		String failedText = Helper.errorDescriptions(cntx, errorCode);

		TextView failedDesc = (TextView) findViewById(R.id.failedDescription);
		failedDesc.setText(failedText);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (errorCode == Const.E_INVALID_TOKEN || errorCode == Const.E_EXPIRED_TOKEN) {
					Intent intent = new Intent(cntx, CharacterListActivity.class);
					((BaseActivity) cntx).startActivity(intent);
					((BaseActivity) cntx).finish();

					if (GroupListActivity.instance != null) {
						GroupListActivity.instance.finish();
					}
				} else if (isFinish) {
					((BaseActivity) cntx).finish();
				}
			}
		});

		show();
	}

	/**
	 * show failed dialog with string description
	 * 
	 * @param failedText
	 */
	public void setFailed(final String failedText) {
		this.setContentView(R.layout.dialog_failed);

		TextView failedDesc = (TextView) findViewById(R.id.failedDescription);
		failedDesc.setText(failedText);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				if (isFinish) {
					((BaseActivity) cntx).finish();
				}
			}
		});

		show();
	}

	/**
	 * Ok/Cancel/Forget dialog for editing MainActivity
	 * 
	 * @param type
	 * @param alert
	 */
	public void okCancelDialog(final int type, final String alert, final Object var) {
		this.setContentView(R.layout.dialog_ok_cancel_checkbox);

		TextView alertText = (TextView) findViewById(R.id.alertText);
		alertText.setText(alert);

		LinearLayout btnOk = (LinearLayout) findViewById(R.id.btnOk);
		LinearLayout btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
		final ImageView checkNoMore = (ImageView) findViewById(R.id.checkNoMore);

		btnOk.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				SpikaEnterpriseApp.getSharedPreferences(cntx).setCustomBoolean(String.valueOf(type), checked);

				if (Const.T_DELETE_MSG == type) {
					deleteMessage((String) var);
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		checkNoMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checked) {
					// TODO
					// checkNoMore.setImageDrawable(cntx.getResources().getDrawable(R.drawable.gb_checkbox_unchecked));
					checked = false;
				} else {
					// TODO
					// checkNoMore.setImageDrawable(cntx.getResources().getDrawable(R.drawable.gb_checkbox_checked));
					checked = true;
				}
			}
		});

		show();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!hasFocus) {
			this.dismiss();
		}

		super.onWindowFocusChanged(hasFocus);
	}

	public void deleteMessage(final String msgId) {
		new BaseAsyncTask<Void, Void, Integer>(cntx, true) {

			protected Integer doInBackground(Void... params) {

				try {

					HashMap<String, String> getParams = new HashMap<String, String>();
					getParams.put(Const.MODULE, String.valueOf(Const.M_CHAT));
					getParams.put(Const.FUNCTION, Const.F_DELETE_MESSAGE);
					getParams.put(Const.TOKEN, SpikaEnterpriseApp.getSharedPreferences(context).getToken());

					JSONObject reqData = new JSONObject();
					reqData.put(Const.MESSAGE_ID, msgId);

					JSONObject result = NetworkManagement.httpPostRequest(getParams, reqData);

					if (result != null) {
						return result.getInt(Const.CODE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return Const.E_FAILED;
			};

			protected void onPostExecute(Integer result) {
				super.onPostExecute(result);

				if (result == Const.E_SUCCESS || result == Const.E_SOMETHING_WENT_WRONG) {
					AppDialog dialog = new AppDialog(context, false);
					dialog.setSucceed();
					if (cntx instanceof ChatActivity) {
						((ChatActivity) cntx).adapter.removeMessage(msgId);
						((ChatActivity) cntx).getMessages(false, false, false, true, true, true);
					}
				} else {
					AppDialog dialog = new AppDialog(cntx, false);
					dialog.setFailed(result);
				}
			};

		}.execute();
	}

}