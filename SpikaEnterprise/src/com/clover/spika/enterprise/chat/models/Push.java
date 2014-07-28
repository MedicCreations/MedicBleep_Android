package com.clover.spika.enterprise.chat.models;

public class Push {

	private String id;
	private String message;
	private String chatName;
	private String chatImage;

	public Push() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getChatName() {
		return chatName;
	}

	public void setChatName(String chatName) {
		this.chatName = chatName;
	}

	public String getChatImage() {
		return chatImage;
	}

	public void setChatImage(String chatImage) {
		this.chatImage = chatImage;
	}

}
