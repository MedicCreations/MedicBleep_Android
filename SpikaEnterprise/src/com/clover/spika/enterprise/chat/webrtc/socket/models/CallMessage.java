package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.List;

public class CallMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	public String name;
	public List<CallMessageArgs> args;

	private CallMessage() {
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
