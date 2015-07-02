package com.medicbleep.app.chat.webrtc.socket.models;

import java.io.Serializable;

import com.medicbleep.app.chat.models.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebRtcSDPPayload implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public String type;
	public String sdp;
	public User user;
	public WebRtcSDPCandidate candidate;
	
	public WebRtcSDPPayload(){
	}

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
