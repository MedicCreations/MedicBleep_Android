package com.medicbleep.app.chat.webrtc.socket.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CallMessageArgs implements Serializable {

	private static final long serialVersionUID = 1L;

	public String to;
	public String from;
	public String type;
	public CallMessagePayload payload;

	public CallMessageArgs() {
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CallMessagePayload getPayload() {
		return payload;
	}

	public void setPayload(CallMessagePayload payload) {
		this.payload = payload;
	}

}
