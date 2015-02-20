/******************************************************************************
 *
 *  Copyright 2011 Tavendo GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package com.zzz.test.socket;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.api.ApiCallback;
import com.clover.spika.enterprise.chat.models.Result;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketOptions;
import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification;

public class EchoClientActivity extends Activity {

	static EditText mHostname;
	static EditText mPort;
	static TextView mStatusline;
	static Button mStart;

	static EditText mMessage;
	static Button mSendMessage;
	
	String sessionId = "1";

	private void alert(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();
	}

	private void setButtonConnect() {
		mHostname.setEnabled(true);
		mPort.setEnabled(true);
		mStart.setText("Connect");
		mStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				start();
			}
		});
	}

	private void setButtonDisconnect() {
		mHostname.setEnabled(false);
		mPort.setEnabled(false);
		mStart.setText("Disconnect");
		mStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mConn.disconnect();
			}
		});
	}

//	private final WebSocket mConnection = new WebSocketConnection();

	private final WebSocket mConn = new WebSocketConnection();
	private long openedTime = 0;
	
	private void start() {

		// final String wsuri = "ws://" + mHostname.getText() + ":" +
		// mPort.getText();
		final String wsuri = "wss://www.spikaent.com:32443/socket.io/1/websocket/"+sessionId;
		
		URI uri;
		try {
			uri = new URI(wsuri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		mStatusline.setText("Status: Connecting to " + wsuri + " ..");

		setButtonDisconnect();

		try {
			mConn.connect(uri, new WebSocketConnectionObserver() {

				@Override
				public void onTextMessage(String arg0) {
					Log.d("LOG", "MESSAGE RECEIVED: "+arg0);
					Log.w("LOG", "TIME: "+System.currentTimeMillis());
					Log.v("LOG", "AFTER: "+(System.currentTimeMillis() - openedTime));
				}

				@Override
				public void onRawTextMessage(byte[] arg0) {
					Log.d("LOG", "RAW MESSAGE RECEIVED: "+arg0);
					Log.w("LOG", "TIME: "+System.currentTimeMillis());
					Log.v("LOG", "AFTER: "+(System.currentTimeMillis() - openedTime));
				}

				@Override
				public void onOpen() {
					mStatusline.setText("Status: Connected to " + wsuri);
					mSendMessage.setEnabled(true);
					mMessage.setEnabled(true);
					Log.d("LOG", "OPEN IN TIME: "+System.currentTimeMillis());
					openedTime = System.currentTimeMillis();
				}

				@Override
				public void onClose(WebSocketCloseNotification arg0, String arg1) {
					alert("Connection lost.");
					mStatusline.setText("Status: Ready.");
					setButtonConnect();
					mSendMessage.setEnabled(false);
					mMessage.setEnabled(false);
					Log.d("LOG", "CLOSE IN TIME: "+System.currentTimeMillis());
					Log.v("LOG", "CLOSED AFTER: "+(System.currentTimeMillis() - openedTime));
				}

				@Override
				public void onBinaryMessage(byte[] arg0) {
					Log.d("LOG", "BYTE MESSAGE RECEIVED: "+arg0);
					Log.w("LOG", "TIME: "+System.currentTimeMillis());
					Log.v("LOG", "AFTER: "+(System.currentTimeMillis() - openedTime));
				}
			});
		} catch (WebSocketException e) {

			Log.d("LOG", e.toString());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.www_socket_main);

		mHostname = (EditText) findViewById(R.id.hostname);
		mPort = (EditText) findViewById(R.id.port);
		mStatusline = (TextView) findViewById(R.id.statusline);
		mStart = (Button) findViewById(R.id.start);
		mMessage = (EditText) findViewById(R.id.msg);
		mSendMessage = (Button) findViewById(R.id.sendMsg);

		setButtonConnect();
		mSendMessage.setEnabled(false);
		mMessage.setEnabled(false);

		mSendMessage.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mConn.sendTextMessage(mMessage.getText().toString());
				Log.d("LOG", "SEND MESSAGE: "+mMessage.getText().toString());
				Log.w("LOG", "TIME: "+System.currentTimeMillis());
				Log.v("LOG", "AFTER: "+(System.currentTimeMillis() - openedTime));
			}
		});
		
		new SocketClient().getSessionId(this, true, new ApiCallback<String>() {
			
			@Override
			public void onApiResponse(Result<String> result) {
				Log.d("LOG", result+" RES");
				sessionId = result.getResultData();
				sessionId = sessionId.substring(0, sessionId.indexOf(":"));
				Log.d("LOG", "sessionId "+sessionId+" sessionId");
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConn.isConnected()) {
			mConn.disconnect();
		}
	}

}
