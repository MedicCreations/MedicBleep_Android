package com.clover.spika.enterprise.chat.api;

import android.content.Context;

import com.clover.spika.enterprise.chat.extendables.BaseAsyncTask;

public class LoginApi {

	public static void loginWithCredentials(String username, String password, final LoginListener listener, Context ctx, boolean showProgressBar) {

		new BaseAsyncTask<Void, Void, Void>(ctx, showProgressBar) {

			protected Void doInBackground(Void... params) {
				return null;
			};

			protected void onPostExecute(Void result) {

				if (listener != null) {
					listener.onLogin();
				}
			};

		}.execute();
	}

	public static void loginWithExistingCredentials(final LoginListener listener, Context ctx) {
	}

	interface LoginListener {
		void onLogin();

		void onLoginFailed();
	}
}
