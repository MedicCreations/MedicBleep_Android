package com.clover.spika.enterprise.chat.webrtc.socket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.clover.spika.enterprise.chat.extendables.SpikaEnterpriseApp;
import com.clover.spika.enterprise.chat.utils.Const;

public class ConnectToInternetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getExtras() != null) {
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connManager.getActiveNetworkInfo();
			
			Intent inBroadcast = new Intent();
			inBroadcast.setAction(Const.INTERNET_CONNECTION_CHANGE_ACTION);
			
			if (info != null && info.isConnected()) {
				SpikaEnterpriseApp.restartSocket();
				inBroadcast.putExtra(Const.INTERNET_STATE, Const.HAS_INTERNET);
			}else{
				SpikaEnterpriseApp.stopSocketWithCon(context);
				inBroadcast.putExtra(Const.INTERNET_STATE, Const.HAS_NOT_INTERNET);
			}
			
			LocalBroadcastManager.getInstance(context).sendBroadcast(inBroadcast);
			
		}
	}

}
