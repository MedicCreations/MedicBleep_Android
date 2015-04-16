package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebRtcSDPCandidate implements Serializable {

	private static final long serialVersionUID = 1L;

	public String sdpMLineIndex;
	public String sdpMid;
	public String candidate;

	public WebRtcSDPCandidate() {
	}

	public String getSdpMLineIndex() {
		return sdpMLineIndex;
	}

	public void setSdpMLineIndex(String sdpMLineIndex) {
		this.sdpMLineIndex = sdpMLineIndex;
	}

	public String getSdmMid() {
		return sdpMid;
	}

	public void setSdmMid(String sdmMid) {
		this.sdpMid = sdmMid;
	}

	public String getCandidate() {
		return candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	@Override
	public String toString() {
		return "WebRtcSDPCandidate [sdpMLineIndex=" + sdpMLineIndex + ", sdmMid=" + sdpMid + ", candidate=" + candidate + "]";
	}

}
