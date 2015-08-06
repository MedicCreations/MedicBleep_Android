package com.medicbleep.app.chat.services.custom;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.medicbleep.app.chat.api.ApiCallback;
import com.medicbleep.app.chat.api.LocalPushApi;
import com.medicbleep.app.chat.models.LocalPush;
import com.medicbleep.app.chat.models.Result;
import com.medicbleep.app.chat.utils.PasscodeUtility;
import com.medicbleep.app.chat.utils.PushHandle;

public class PoolingService extends Service {

	private int serviceTime = 5000;
	private boolean isServiceStarted = false;
	private boolean isApiStarted = false;

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		public void run() {
			work();
			handler.postDelayed(this, serviceTime);
		}
	};;

	LocalPushApi api;
	ApiCallback<LocalPush> listener;

	@Override
	public void onCreate() {
		super.onCreate();

		api = new LocalPushApi();
		listener = new ApiCallback<LocalPush>() {

			@Override
			public void onApiResponse(Result<LocalPush> result) {

				if (result.isSuccess()) {

					List<LocalPush> data = result.getResultData().chats;

					for (LocalPush push : data) {
						PushHandle.handlePushNotification(push.chat_id, null, push.organization_id, push.firstname, push.password, String.valueOf(push.type), getBaseContext());
					}
				}

				isApiStarted = false;
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!isServiceStarted) {
			isServiceStarted = true;
			handler.postDelayed(runnable, serviceTime);
		}

		return Service.START_STICKY;
	}

	private void work() {

		if (isApiStarted) {
			return;
		} else {
			isApiStarted = true;
		}

		if (PasscodeUtility.getInstance().isInApp()) {
			serviceTime = 5000;
		} else {
			serviceTime = 30000;
		}

		api.getPush(listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
		isServiceStarted = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
