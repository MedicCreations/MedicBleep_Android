package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.clover.spika.enterprise.chat.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallMessagePayload implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("user")
	@Expose
	private User user;
	
	@SerializedName("name")
	@Expose
	private String name;
	
	@SerializedName("mute")
	@Expose
	private String mute;

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
	
	public boolean isMute(){
		return mute.equals("1") ? true : false;
	}

}
