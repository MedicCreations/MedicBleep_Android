package com.medicbleep.app.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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
