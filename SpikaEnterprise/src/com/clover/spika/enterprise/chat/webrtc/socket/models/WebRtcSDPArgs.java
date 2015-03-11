package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebRtcSDPArgs implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("type")
	@Expose
	private String type;
	
	@SerializedName("to")
	@Expose
	private String to;
	
	@SerializedName("from")
	@Expose
	private String from;
	
	@SerializedName("payload")
	@Expose
	private WebRtcSDPPayload payload;
	
	@SerializedName("roomType")
	@Expose
	private String roomType;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public WebRtcSDPPayload getPayload() {
		return payload;
	}

	public void setPayload(WebRtcSDPPayload payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "WebRtcSDPArgs [type=" + type + ", to=" + to + ", from=" + from + ", payload=" + payload + ", roomType=" + roomType + "]";
	}

}
