/*
 * libjingle
 * Copyright 2013 Google Inc.
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

import java.util.List;

import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import com.zzz.socket.models.WebRtcSDPMessage;

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
public interface AppRTCClient {


  /**
   * Asynchronously connect to an AppRTC room URL using supplied connection
   * parameters. Once connection is established onConnectedToRoom()
   * callback with room parameters is invoked.
   */
  public void connectToRoom();

  /**
   * Send offer SDP to the other participant.
   */
  public void sendOfferSdp(final SessionDescription sdp);

  /**
   * Send answer SDP to the other participant.
   */
  public void sendAnswerSdp(final SessionDescription sdp);

  /**
   * Send Ice candidate to the other participant.
   */
  public void sendLocalIceCandidate(final IceCandidate candidate);

  /**
   * Disconnect from room.
   */
  public void disconnectFromRoom();
  
  public SignalingParameters setRoomParameters(WebRtcSDPMessage webRtcMessage);

  /**
   * Struct holding the signaling parameters of an AppRTC room.
   */
  public static class SignalingParameters {
    public final List<PeerConnection.IceServer> iceServers;
    public final boolean initiator;
    public final MediaConstraints pcConstraints;
    public final MediaConstraints videoConstraints;
    public final MediaConstraints audioConstraints;
    public final String clientId;
    public final String wssUrl;
    public final String wssPostUrl;
    public final SessionDescription offerSdp;
    public final List<IceCandidate> iceCandidates;

    public SignalingParameters(
        List<PeerConnection.IceServer> iceServers,
        boolean initiator, MediaConstraints pcConstraints,
        MediaConstraints videoConstraints, MediaConstraints audioConstraints,
        String clientId, String wssUrl, String wssPostUrl,
        SessionDescription offerSdp, List<IceCandidate> iceCandidates) {
      this.iceServers = iceServers;
      this.initiator = initiator;
      this.pcConstraints = pcConstraints;
      this.videoConstraints = videoConstraints;
      this.audioConstraints = audioConstraints;
      this.clientId = clientId;
      this.wssUrl = wssUrl;
      this.wssPostUrl = wssPostUrl;
      this.offerSdp = offerSdp;
      this.iceCandidates = iceCandidates;
    }
  }

  /**
   * Callback interface for messages delivered on signaling channel.
   *
   * <p>Methods are guaranteed to be invoked on the UI thread of |activity|.
   */
  public static interface SignalingEvents {
    /**
     * Callback fired once the room's signaling parameters
     * SignalingParameters are extracted.
     */
    public void onConnectedToRoom(final SignalingParameters params);

    /**
     * Callback fired once remote SDP is received.
     */
    public void onRemoteDescription(final SessionDescription sdp);

    /**
     * Callback fired once remote Ice candidate is received.
     */
    public void onRemoteIceCandidate(final IceCandidate candidate);

    /**
     * Callback fired once channel is closed.
     */
    public void onChannelClose();

    /**
     * Callback fired once channel error happened.
     */
    public void onChannelError(final String description);
  }
}
