package com.clover.spika.enterprise.chat.services.custom;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class PoolingService extends Service {

	private static boolean isStarted = false;

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		public void run() {
			work();
			handler.postDelayed(this, 5000);
		}
	};;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (!isStarted) {
			isStarted = true;
			handler.postDelayed(runnable, 1000);
		}

		return Service.START_STICKY;
	}

	private void work() {
		// TODO
		Log.d("Vida", "Pool");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
		isStarted = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
