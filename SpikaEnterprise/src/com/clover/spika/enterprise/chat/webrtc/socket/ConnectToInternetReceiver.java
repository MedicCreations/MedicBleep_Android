package com.clover.spika.enterprise.chat.webrtc.socket;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectToInternetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("LOG", "CONNECT CHANGED");
		if (intent.getExtras() != null) {
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connManager.getActiveNetworkInfo();
			
			if (info != null && info.isConnected()) {
				SpikaEnterpriseApp.restartSocket();
			}else{
				SpikaEnterpriseApp.stopSocketWithCon(context);
			}
			
		}
	}

}
