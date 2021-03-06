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

package com.medicbleep.app.chat.webrtc;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import android.app.Activity;
import android.util.Log;

import com.medicbleep.app.chat.extendables.BaseActivity;
import com.medicbleep.app.chat.webrtc.WebSocketChannelClient.WebSocketChannelEvents;
import com.medicbleep.app.chat.webrtc.WebSocketChannelClient.WebSocketConnectionState;
import com.medicbleep.app.chat.webrtc.socket.models.WebRtcSDPCandidate;
import com.medicbleep.app.chat.webrtc.socket.models.WebRtcSDPMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Negotiates signaling for chatting with apprtc.appspot.com "rooms". Uses the
 * client<->server specifics of the apprtc AppEngine webapp.
 *
 * <p>
 * To use: create an instance of this object (registering a message handler) and
 * call connectToRoom(). Once room connection is established onConnectedToRoom()
 * callback with room parameters is invoked. Messages to other party (with local
 * Ice candidates and answer SDP) can be sent after WebSocket connection is
 * established.
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelEvents {
	private static final String TAG = "WSRTCClient";

	private enum ConnectionState {
		NEW, CONNECTED, CLOSED, ERROR
	};

	private final LooperExecutor executor;
	private boolean initiator;
	private SignalingEvents events;
	private WebSocketChannelClient wsClient;
	private ConnectionState roomState;
	
	private Activity activity;
	
	public WebSocketRTCClient(SignalingEvents events, LooperExecutor executor, Activity activity, WebRtcSDPMessage item, WebSocketChannelClient ws) {
		this.events = events;
		this.executor = executor;
		roomState = ConnectionState.NEW;
		this.activity = activity;
		wsClient = ws;
	}

	public SignalingParameters setRoomParameters(WebRtcSDPMessage webRtcMessage){
		roomState = ConnectionState.CONNECTED;
		return new RoomParametersFetcher(webRtcMessage).roomHttpResponseParse();
	}
	
	@Override
	public void disconnect() {
		executor.requestStop();
	}

	// Send local offer SDP to the other participant.
	@Override
	public void sendOfferSdp(final SessionDescription sdp) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (roomState != ConnectionState.CONNECTED) {
					reportError("Sending offer SDP in non connected state.");
					return;
				}
				JSONObject json = new JSONObject();
				jsonPut(json, "sdp", sdp.description);
				jsonPut(json, "type", "offer");
				sendOfferMessage(sdp.description);
			}
		});
	}

	boolean isSetAnswer = false;
	// Send local answer SDP to the other participant.
	@Override
	public void sendAnswerSdp(final SessionDescription sdp) {
		if(isSetAnswer) return;
		isSetAnswer = true; 
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (wsClient.getState() != WebSocketConnectionState.REGISTERED) {
					reportError("Sending answer SDP in non registered state.");
					return;
				}
				JSONObject json = new JSONObject();
				jsonPut(json, "sdp", sdp.description);
				jsonPut(json, "type", "answer");
				sendAnswerMessage(sdp.description);
			}
		});
	}

	// Send Ice candidate to the other participant.
	@Override
	public void sendLocalIceCandidate(final IceCandidate candidate) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				JSONObject json = new JSONObject();
				jsonPut(json, "type", "candidate");
				jsonPut(json, "label", candidate.sdpMLineIndex);
				jsonPut(json, "id", candidate.sdpMid);
				jsonPut(json, "candidate", candidate.sdp);
				if (initiator) {
					// Call initiator sends ice candidates to GAE server.
					if (roomState != ConnectionState.CONNECTED) {
						reportError("Sending ICE candidate in non connected state.");
						return;
					}
					sendPostMessage(json.toString());
				} else {
					// Call receiver sends ice candidates to websocket server.
					if (wsClient.getState() != WebSocketConnectionState.REGISTERED) {
						reportError("Sending ICE candidate in non registered state.");
						return;
					}
					sendPostMessage(json.toString());
//					wsClient.send(json.toString());
				}
			}
		});
	}

	// --------------------------------------------------------------------
	// WebSocketChannelEvents interface implementation.
	// All events are called by WebSocketChannelClient on a local looper thread
	// (passed to WebSocket client constructor).
	@Override
	public void onWebSocketOpen() {
		Log.d(TAG, "Websocket connection completed. Registering...");
		wsClient.register();
	}

	@Override
	public void onWebSocketMessage(final String msg) {
		if (wsClient.getState() != WebSocketConnectionState.REGISTERED) {
			Log.e(TAG, "Got WebSocket message in non registered state.");
			return;
		}
		try {
			WebRtcSDPMessage item = new ObjectMapper().readValue(msg, WebRtcSDPMessage.class);
//			JSONObject json = new JSONObject(msg);
//			String msgText = json.getString("msg");
//			String errorText = json.optString("error");
			if (item.getArgs().get(0).getType().equals("answer")){
				if (initiator) {
					SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("ANSWER"), item.getArgs().get(0).getPayload().getSdp());
					events.onRemoteDescription(sdp);
				} else {
					reportError("Received answer for call initiator: " + msg);
				}
			}else if (item.getArgs().get(0).getType().equals("offer")){
				if (!initiator) {
					SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), item.getArgs().get(0).getPayload().getSdp());
					events.onRemoteDescription(sdp);
				} else {
					reportError("Received offer for call receiver: " + msg);
				}
			}else if (item.getArgs().get(0).getType().equals("candidate")){
				WebRtcSDPCandidate candidateModel = item.getArgs().get(0).getPayload().getCandidate();
				IceCandidate candidate = new IceCandidate(candidateModel.getSdmMid(), Integer.valueOf(candidateModel.getSdpMLineIndex()), candidateModel.getCandidate());
				events.onRemoteIceCandidate(candidate);
			}else if (item.getArgs().get(0).getType().equals("bye")) {
				events.onChannelClose();
			}
		} catch (Exception e) {
			reportError("WebSocket message JSON parsing error: " + e.toString());
		}
	}

	@Override
	public void onWebSocketClose() {
		events.onChannelClose();
	}

	@Override
	public void onWebSocketError(String description) {
		reportError("WebSocket error: " + description);
	}

	// --------------------------------------------------------------------
	// Helper functions.
	private void reportError(final String errorMessage) {
		Log.e(TAG, errorMessage);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (roomState != ConnectionState.ERROR) {
					roomState = ConnectionState.ERROR;
					events.onChannelError(errorMessage);
				}
			}
		});
	}

	// Put a |key|->|value| mapping in |json|.
	private static void jsonPut(JSONObject json, String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	// Send SDP or ICE candidate to a room server.
	private void sendOfferMessage(String sdp){
		((BaseActivity)activity).getService().sendWebRtcMessageOffer(sdp);
	}
	
	private void sendPostMessage(String message) {
		((BaseActivity)activity).getService().sendWebRtcMessage(message);
	}
	
	private void sendAnswerMessage(String sdp){
		((BaseActivity)activity).getService().sendWebRtcMessageOfferForAnswer(sdp);
	}
}
