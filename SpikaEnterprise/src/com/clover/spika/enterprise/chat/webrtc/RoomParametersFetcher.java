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

import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import android.util.Log;

import com.clover.spika.enterprise.chat.webrtc.AppRTCClient.SignalingParameters;
import com.clover.spika.enterprise.chat.webrtc.socket.models.WebRtcSDPCandidate;
import com.clover.spika.enterprise.chat.webrtc.socket.models.WebRtcSDPMessage;

/**
 * AsyncTask that converts an AppRTC room URL into the set of signaling
 * parameters to use with that room.
 */
public class RoomParametersFetcher {
	
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

	public RoomParametersFetcher(WebRtcSDPMessage webRtcMessage) {
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
				if (webRtcMessage.getArgs().get(0).getPayload().getSdp() != null){
					String newSdp = webRtcMessage.getArgs().get(0).getPayload().getSdp();
//					if(newSdp.contains("a=group:BUNDLE audio video data")){
//						newSdp = newSdp.replace("a=group:BUNDLE audio video data", "a=group:BUNDLE audio video");
//					}
//					if(newSdp.contains("a=rtpmap:106 CN/32000")){
//						Log.e("NEW", "REPLACED...1");
//						newSdp = newSdp.replace("a=rtpmap:106 CN/32000\\r\\n", "");
//					}
//					if(newSdp.contains("a=rtpmap:105 CN/16000")){
//						Log.e("NEW", "REPLACED...2");
//						newSdp = newSdp.replace("a=rtpmap:105 CN/16000\\r\\n", "");
//					}
//					if(newSdp.contains("a=rtpmap:13 CN/8000")){
//						Log.e("NEW", "REPLACED...3");
//						newSdp = newSdp.replace("a=rtpmap:13 CN/8000\\r\\n", "");
//					}
//					if(newSdp.contains("m=video 9 RTP/SAVPF 100 116 117 96")){
//						Log.e("NEW", "REPLACED...4");
//						newSdp = newSdp.replace("m=video 9 RTP/SAVPF 100 116 117 96", "m=video 9 RTP/SAVPF 111 100 116 117 96");
//					}
//					if(newSdp.contains("m=application 9 DTLS/SCTP 5000")){
//						Log.e("NEW", "REPLACED...5");
//						newSdp = newSdp.replace("m=application 9 DTLS/SCTP 5000\\r\\n", "");
//					}
//					if(newSdp.contains("m=application 9 DTLS/SCTP 5000")){
//						Log.e("NEW", "REPLACED...5.1");
//						newSdp = newSdp.substring(0, newSdp.indexOf("m=application 9 DTLS/SCTP 5000"));
//					}
//					if(newSdp.contains("m=application 9 DTLS/SCTP 5000")){
//						Log.e("NEW", "REPLACED...5.2");
//					}
					sdp = new SessionDescription(SessionDescription.Type.fromCanonicalForm("OFFER"), newSdp);
				}

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
				audioConstraints, sdp, iceCandidatesy);
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
//		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
//		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//		
//		constraints.optional.add(new MediaConstraints.KeyValuePair("internalSctpDataChannels", "true"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("maxWidth", "640"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("minWidth", "320"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("maxHeight", "480"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("minHeight", "240"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("maxFrameRate", "30"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("minFrameRate", "24"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("maxAspectRatio", "4:3"));
//		constraints.optional.add(new MediaConstraints.KeyValuePair("minAspectRatio", "4:3"));
		
		return constraints;
	}

}
