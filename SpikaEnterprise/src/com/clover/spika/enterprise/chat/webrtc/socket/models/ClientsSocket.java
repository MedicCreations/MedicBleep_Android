package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;

import com.clover.spika.enterprise.chat.models.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClientsSocket implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("screen")
	@Expose
	private boolean screen;
	
	@SerializedName("video")
	@Expose
	private boolean video;
	
	@SerializedName("audio")
	@Expose
	private boolean audio;

	@SerializedName("user")
	@Expose
	private User user;

	public boolean isScreen() {
		return screen;
	}

	public void setScreen(boolean screen) {
		this.screen = screen;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isVideo() {
		return video;
	}

	public void setVideo(boolean video) {
		this.video = video;
	}

	public boolean isAudio() {
		return audio;
	}

	public void setAudio(boolean audio) {
		this.audio = audio;
	}

}
