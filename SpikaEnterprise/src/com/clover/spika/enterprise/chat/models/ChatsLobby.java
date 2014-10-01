package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatsLobby {

	@SerializedName("chat_id")
	@Expose
	private int chatId;

	@SerializedName("type")
	@Expose
	private String type;

	@SerializedName("chat_name")
	@Expose
	private String chatName;

	@SerializedName("unread")
	@Expose
	private String unread;

	@SerializedName("image")
	@Expose
	private String image;
	
	@SerializedName("admin_id")
	@Expose
	private String adminId;
	
	@SerializedName("is_active")
	@Expose
	private int isActive;

	public int getChatId() {
		return chatId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	public String getChatName() {
		return chatName;
	}

	public void setChatName(String chatName) {
		this.chatName = chatName;
	}

	public String getUnread() {
		return unread;
	}

	public void setUnread(String unread) {
		this.unread = unread;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getAdminId() {
		return adminId;
	}

	public void setAdmin(String adminId) {
		this.adminId = adminId;
	}
	
	public int isActive() {
		return isActive;
	}

	public void setActive(int isActive) {
		this.isActive = isActive;
	}

}
