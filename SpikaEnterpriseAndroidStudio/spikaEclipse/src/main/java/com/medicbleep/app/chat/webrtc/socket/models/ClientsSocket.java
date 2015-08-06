package com.medicbleep.app.chat.webrtc.socket.models;

import java.io.Serializable;

import com.medicbleep.app.chat.models.User;

public class ClientsSocket implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public boolean screen;
	public boolean video;
	public boolean audio;
	public User user;
	
	public ClientsSocket(){}

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
