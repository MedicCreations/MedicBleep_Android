package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("name")
	@Expose
	private String name;
	
	@SerializedName("args")
	@Expose
	private List<CallMessageArgs> args;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CallMessageArgs> getArgs() {
		return args;
	}

	public void setArgs(List<CallMessageArgs> args) {
		this.args = args;
	}

}
