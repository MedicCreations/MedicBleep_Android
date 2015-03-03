package com.zzz.socket.models;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WebRtcSDPMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("name")
	@Expose
	private String name;
	
	@SerializedName("args")
	@Expose
	private List<WebRtcSDPArgs> args;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WebRtcSDPArgs> getArgs() {
		return args;
	}

	public void setArgs(List<WebRtcSDPArgs> args) {
		this.args = args;
	}

	@Override
	public String toString() {
		return "WebRtcSDPMessage [name=" + name + ", args=" + args + "]";
	}

}
