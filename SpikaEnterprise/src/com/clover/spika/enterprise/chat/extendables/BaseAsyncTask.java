package com.clover.spika.enterprise.chat.extendables;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppProgressAlertDialog;
import com.clover.spika.enterprise.chat.utils.Helper;
import com.clover.spika.enterprise.chat.utils.Utils;

public class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	private static int INVALID_TOKEN_CODE = 1000;
	private static int EXPIRED_TOKEN_CODE = 1001;

	protected Context context;
	protected AppProgressAlertDialog progressDialog;
	protected boolean showProgressBar = false;

	public BaseAsyncTask(Context context, boolean showProgressBar) {
		super();

		this.context = context;
		this.showProgressBar = showProgressBar;
	}

	@Override
	protected void onPreExecute() {

		if (Utils.hasNetworkConnection(context)) {

			super.onPreExecute();

			if (showProgressBar) {

				progressDialog = new AppProgressAlertDialog(context);

				if (!((Activity) context).isFinishing()) {

					if (!progressDialog.isShowing()) {
						progressDialog.show();
					}
				}
			}
		} else {
			final AppDialog dialog = new AppDialog(context, true);
			dialog.setInfo(context.getString(R.string.no_network_connection));

			this.cancel(true);
		}
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);

		if (result instanceof BaseModel && (((BaseModel) result).getCode() == INVALID_TOKEN_CODE || ((BaseModel) result).getCode() == EXPIRED_TOKEN_CODE)) {
			AppDialog dialog = new AppDialog(context, false);
			dialog.setFailed(context.getString(R.string.invalid_token_message));
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Helper.logout(context);
				}
			});
		}

		progressHandle();
	}

	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);
		progressHandle();
	}

	private void progressHandle() {
		if (showProgressBar) {
			if (progressDialog != null && progressDialog.isShowing()) {
				// because AsyncTask
				try {
					progressDialog.dismiss();
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
	}

	@Override
	protected Result doInBackground(Params... params) {
		return null;
	}

	public Context getContext() {
		return context;
	}
}