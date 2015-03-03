package com.zzz.socket.models;

import java.io.Serializable;

import com.clover.spika.enterprise.chat.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebRtcSDPPayload implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("type")
	@Expose
	private String type;
	
	@SerializedName("sdp")
	@Expose
	private String sdp;
	
	@SerializedName("user")
	@Expose
	private User user;
	
	@SerializedName("candidate")
	@Expose
	private WebRtcSDPCandidate candidate;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSdp() {
		return sdp;
	}

	public void setSdp(String sdp) {
		this.sdp = sdp;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public WebRtcSDPCandidate getCandidate() {
		return candidate;
	}

	public void setCandidate(WebRtcSDPCandidate candidate) {
		this.candidate = candidate;
	}

	@Override
	public String toString() {
		return "WebRtcSDPPayload [type=" + type + ", sdp=" + sdp + ", user=" + user + ", candidate=" + candidate + "]";
	}

}
