package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocalPush extends BaseModel {

	@SerializedName("chats")
	@Expose
	private List<LocalPush> chats;

	@SerializedName("chat_id")
	@Expose
	private String chatId;

	@SerializedName("firstname")
	@Expose
	private String firstName;

	@SerializedName("unread")
	@Expose
	private int unread;

	@SerializedName("password")
	@Expose
	private String password;

	@SerializedName("type")
	@Expose
	private int type;

	public LocalPush() {
	}

	public List<LocalPush> getChats() {
		return chats;
	}

	public void setChats(List<LocalPush> chats) {
		this.chats = chats;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
