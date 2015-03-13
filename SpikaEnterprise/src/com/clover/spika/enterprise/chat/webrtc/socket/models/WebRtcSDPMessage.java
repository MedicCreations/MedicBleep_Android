package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.List;

public class WebRtcSDPMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;
	public List<WebRtcSDPArgs> args;

	public WebRtcSDPMessage() {
	}

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
