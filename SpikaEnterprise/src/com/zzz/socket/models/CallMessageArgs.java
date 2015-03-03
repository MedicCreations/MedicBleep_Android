package com.zzz.socket.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallMessageArgs implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("to")
	@Expose
	private String to;
	
	@SerializedName("from")
	@Expose
	private String from;
	
	@SerializedName("type")
	@Expose
	private String type;

	@SerializedName("payload")
	@Expose
	private CallMessagePayload payload;

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
