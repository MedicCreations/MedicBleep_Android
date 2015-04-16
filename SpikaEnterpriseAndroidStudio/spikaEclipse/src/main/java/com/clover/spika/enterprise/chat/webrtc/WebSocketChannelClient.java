/*
 * libjingle
 * Copyright 2014 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.clover.spika.enterprise.chat.webrtc;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.clover.spika.enterprise.chat.extendables.BaseActivity;
import com.clover.spika.enterprise.chat.webrtc.autobahn.WebSocketConnection;

/**
 * WebSocket client implementation.
 *
 * <p>
 * All public methods should be called from a looper executor thread passed in a
 * constructor, otherwise exception will be thrown. All events are dispatched on
 * the same thread.
 */

public class WebSocketChannelClient {
	private static final String TAG = "WSChannelRTCClient";
	private final WebSocketChannelEvents events;
	private final LooperExecutor executor;
	private WebSocketConnection ws;
	private WebSocketConnectionState state;
	// WebSocket send queue. Messages are added to the queue when WebSocket
	// client is not registered and are consumed in register() call.
	private final LinkedList<String> wsSendQueue;
	
	private Activity activity;

	/**
	 * Possible WebSocket connection states.
	 */
	public enum WebSocketConnectionState {
		NEW, CONNECTED, REGISTERED, CLOSED, ERROR
	};

	/**
	 * Callback interface for messages delivered on WebSocket. All events are
	 * dispatched from a looper executor thread.
	 */
	public interface WebSocketChannelEvents {
		public void onWebSocketOpen();

		public void onWebSocketMessage(final String message);

		public void onWebSocketClose();

		public void onWebSocketError(final String description);
	}

	public WebSocketChannelClient(LooperExecutor executor, WebSocketChannelEvents events, Activity activity) {
		this.executor = executor;
		this.events = events;
		wsSendQueue = new LinkedList<String>();
		state = WebSocketConnectionState.NEW;
		this.activity = activity;
	}

	public WebSocketConnectionState getState() {
		return state;
	}

	public void connect() {
		
		ws = ((BaseActivity)activity).getService().getWebSocketConnection();
		((BaseActivity)activity).getService().setState(state);
		
		executor.requestStart();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				state = WebSocketConnectionState.CONNECTED;
				((BaseActivity)activity).getService().setState(state);
				events.onWebSocketOpen();
			}
		});
		
		((BaseActivity)activity).getService().setWebRtcParameters(events, executor, state);
		
	}

	public void register() {
		checkIfCalledOnValidThread();
		if (state != WebSocketConnectionState.CONNECTED) {
			Log.w(TAG, "WebSocket register() in state " + state);
			return;
		}
		state = WebSocketConnectionState.REGISTERED;
		((BaseActivity) activity).getService().setState(state);
		// Send any previously accumulated messages.
		for (String sendMessage : wsSendQueue) {
			send(sendMessage);
		}
		wsSendQueue.clear();
	}

	public void send(String message) {
		checkIfCalledOnValidThread();
		switch (state) {
		case NEW:
		case CONNECTED:
			// Store outgoing messages and send them after websocket client
			// is registered.
			Log.d(TAG, "WS ACC: " + message);
			wsSendQueue.add(message);
			return;
		case ERROR:
		case CLOSED:
			Log.e(TAG, "WebSocket send() in error or closed state : " + message);
			return;
		case REGISTERED:
			JSONObject json = new JSONObject();
			try {
				json.put("cmd", "send");
				json.put("msg", message);
				message = json.toString();
				((BaseActivity)activity).getService().sendWebRtcMessage(message);
			} catch (JSONException e) {
				reportError("WebSocket send JSON error: " + e.getMessage());
			}
			break;
		}
		return;
	}

	private void reportError(final String errorMessage) {
		Log.e(TAG, errorMessage);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (state != WebSocketConnectionState.ERROR) {
					state = WebSocketConnectionState.ERROR;
					events.onWebSocketError(errorMessage);
				}
			}
		});
	}

	// Helper method for debugging purposes. Ensures that WebSocket method is
	// called on a looper thread.
	private void checkIfCalledOnValidThread() {
		if (!executor.checkOnLooperThread()) {
			throw new IllegalStateException("WebSocket method is not called on valid thread");
		}
	}

}
