package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.clover.spika.enterprise.chat.models.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CallMessagePayload implements Serializable {

	private static final long serialVersionUID = 1L;

	public User user;
	public String name;
	public String mute;
	
	public CallMessagePayload(){
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMute() {
		return mute.equals("1") ? true : false;
	}

}
