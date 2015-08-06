package com.medicbleep.app.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CallMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;
	public List<CallMessageArgs> args;

	public CallMessage() {
	}

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
