package com.zzz.socket.models;

import java.io.Serializable;

import com.clover.spika.enterprise.chat.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CallMessagePayload implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("user")
	@Expose
	private User user;

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

}
