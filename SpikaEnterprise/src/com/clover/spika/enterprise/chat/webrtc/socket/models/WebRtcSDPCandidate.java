package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebRtcSDPCandidate implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("sdpMLineIndex")
	@Expose
	private String sdpMLineIndex;
	
	@SerializedName("sdpMid")
	@Expose
	private String sdmMid;
	
	@SerializedName("candidate")
	@Expose
	private String candidate;

	public String getSdpMLineIndex() {
		return sdpMLineIndex;
	}

	public void setSdpMLineIndex(String sdpMLineIndex) {
		this.sdpMLineIndex = sdpMLineIndex;
	}

	public String getSdmMid() {
		return sdmMid;
	}

	public void setSdmMid(String sdmMid) {
		this.sdmMid = sdmMid;
	}

	public String getCandidate() {
		return candidate;
	}

	public void setCandidate(String candidate) {
		this.candidate = candidate;
	}

	@Override
	public String toString() {
		return "WebRtcSDPCandidate [sdpMLineIndex=" + sdpMLineIndex + ", sdmMid=" + sdmMid + ", candidate=" + candidate + "]";
	}

}
