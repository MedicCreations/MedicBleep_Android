package com.clover.spika.enterprise.chat.extendables;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.dialogs.AppDialog;
import com.clover.spika.enterprise.chat.dialogs.AppProgressDialog;
import com.clover.spika.enterprise.chat.networking.NetworkManagement;

public class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	protected Context context;
	protected AppProgressDialog progressDialog;
	protected boolean showProgressBar = false;

	public BaseAsyncTask(Context context, boolean showProgressBar) {
		super();

		this.context = context;
		this.showProgressBar = showProgressBar;
	}

	@Override
	protected void onPreExecute() {

		if (NetworkManagement.hasNetworkConnection(context)) {

			super.onPreExecute();

			if (showProgressBar) {

				progressDialog = new AppProgressDialog(context);

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

		if (showProgressBar) {
			if (progressDialog != null && progressDialog.isShowing()) {
                // because AsyncTask
                try {
                    progressDialog.dismiss();
                } catch (IllegalArgumentException ignored) { }
            }
		}
	}

	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);

		if (showProgressBar) {
			if (progressDialog != null && progressDialog.isShowing()) {
                // because AsyncTask
                try {
                    progressDialog.dismiss();
                } catch (IllegalArgumentException ignored) { }
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