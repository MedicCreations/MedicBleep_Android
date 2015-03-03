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

package com.zzz.my.webrtc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import android.util.Log;

import com.zzz.my.webrtc.AppRTCClient.SignalingParameters;
import com.zzz.socket.models.WebRtcSDPCandidate;
import com.zzz.socket.models.WebRtcSDPMessage;

/**
 * AsyncTask that converts an AppRTC room URL into the set of signaling
 * parameters to use with that room.
 */
public class RoomParametersFetcher {
	private static final String TAG = "RoomRTCClient";
	private final RoomParametersFetcherEvents events;
	
	private WebRtcSDPMessage webRtcMessage = null;

	/**
	 * Room parameters fetcher callbacks.
	 */
	public static interface RoomParametersFetcherEvents {
		/**
		 * Callback fired once the room's signaling parameters
		 * SignalingParameters are extracted.
		 */
		public void onSignalingParametersReady(final SignalingParameters params);

		/**
		 * Callback for room parameters extraction error.
		 */
		public void onSignalingParametersError(final String description);
	}

	public RoomParametersFetcher(final RoomParametersFetcherEvents events, WebRtcSDPMessage webRtcMessage) {
		this.events = events;
		this.webRtcMessage = webRtcMessage;
	}

	public void makeRequest() {
		roomHttpResponseParse();
	}

	public SignalingParameters roomHttpResponseParse() {
		String username = "turn";
        String password = "turn";
        String uri = "turn:spikaent.com:3478";
        
        String uriStun = "stun:spikaent.com:3478";
        
		LinkedList<PeerConnection.IceServer> turnServersNEW = new LinkedList<PeerConnection.IceServer>();
		turnServersNEW.add(new PeerConnection.IceServer(uriStun, username, password));
		turnServersNEW.add(new PeerConnection.IceServer(uri, username, password));
		
		SessionDescription sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), "sdp");
		
		boolean isInitiator = true;
		LinkedList<IceCandidate> iceCandidatesy = new LinkedList<IceCandidate>();
		
		if(webRtcMessage != null){
			isInitiator = false;
			if(webRtcMessage.getArgs().get(0).getPayload() == null){
				sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), "sdp");
			}else{
				if (webRtcMessage.getArgs().get(0).getPayload().getSdp() != null)
					sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("ANSWER"), webRtcMessage.getArgs().get(0).getPayload().getSdp());

				WebRtcSDPCandidate candModel = webRtcMessage.getArgs().get(0).getPayload().getCandidate();
				Log.d("LOG", "CANDIDATE: " + webRtcMessage.getArgs().get(0).getPayload().getCandidate());
				if (webRtcMessage.getArgs().get(0).getPayload().getCandidate() != null)
					iceCandidatesy.add(new IceCandidate(candModel.getSdmMid(), Integer.valueOf(candModel.getSdpMLineIndex()), candModel.getCandidate()));

			}
		}
		
		MediaConstraints pcConstraints = createDeafultMediaConstraints();
		addDTLSConstraintIfMissing(pcConstraints);
		MediaConstraints videoConstraints = createDeafultMediaConstraints();
		MediaConstraints audioConstraints = createDeafultMediaConstraints();
		
		SignalingParameters params2 = new SignalingParameters(turnServersNEW, isInitiator, pcConstraints, videoConstraints, 
				audioConstraints, null, null, null, sdp, iceCandidatesy);
//		events.onSignalingParametersReady(params2);
		return params2;
		
	}

	// Mimic Chrome and set DtlsSrtpKeyAgreement to true if not set to false by
	// the web-app.
	private void addDTLSConstraintIfMissing(MediaConstraints pcConstraints) {
		for (MediaConstraints.KeyValuePair pair : pcConstraints.mandatory) {
			if (pair.getKey().equals("DtlsSrtpKeyAgreement")) {
				return;
			}
		}
		for (MediaConstraints.KeyValuePair pair : pcConstraints.optional) {
			if (pair.getKey().equals("DtlsSrtpKeyAgreement")) {
				return;
			}
		}
		// DTLS isn't being specified (e.g. for debug=loopback calls), so enable
		// it for normal calls and disable for loopback calls.
		pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
	}

	private MediaConstraints createDeafultMediaConstraints() {
		MediaConstraints constraints = new MediaConstraints();
		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
		
		constraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("maxWidth", "640"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("minWidth", "320"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("maxHeight", "480"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("minHeight", "240"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("maxFrameRate", "30"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("minFrameRate", "24"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("maxAspectRatio", "4:3"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("minAspectRatio", "4:3"));
		
		return constraints;
	}

	// Requests & returns a TURN ICE Server based on a request URL. Must be run
	// off the main thread!
	private LinkedList<PeerConnection.IceServer> requestTurnServers(String url) throws IOException, JSONException {
		LinkedList<PeerConnection.IceServer> turnServers = new LinkedList<PeerConnection.IceServer>();
		Log.d(TAG, "Request TURN from: " + url);
		URLConnection connection = (new URL(url)).openConnection();
		connection.addRequestProperty("user-agent", "Mozilla/5.0");
		connection.addRequestProperty("origin", "https://apprtc.appspot.com");
		String response = drainStream(connection.getInputStream());
		Log.d(TAG, "TURN response: " + response);
		JSONObject responseJSON = new JSONObject(response);
		String username = responseJSON.getString("username");
		String password = responseJSON.getString("password");
		JSONArray turnUris = responseJSON.getJSONArray("uris");
		for (int i = 0; i < turnUris.length(); i++) {
			String uri = turnUris.getString(i);
			turnServers.add(new PeerConnection.IceServer(uri, username, password));
		}
		return turnServers;
	}

	// Return the list of ICE servers described by a WebRTCPeerConnection
	// configuration string.
	private LinkedList<PeerConnection.IceServer> iceServersFromPCConfigJSON(String pcConfig) throws JSONException {
		JSONObject json = new JSONObject(pcConfig);
		JSONArray servers = json.getJSONArray("iceServers");
		LinkedList<PeerConnection.IceServer> ret = new LinkedList<PeerConnection.IceServer>();
		for (int i = 0; i < servers.length(); ++i) {
			JSONObject server = servers.getJSONObject(i);
			String url = server.getString("urls");
			String credential = server.has("credential") ? server.getString("credential") : "";
			ret.add(new PeerConnection.IceServer(url, "", credential));
		}
		return ret;
	}

	// Return the contents of an InputStream as a String.
	private String drainStream(InputStream in) {
		Scanner s = new Scanner(in).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

}
