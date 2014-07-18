package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatsLobby {

    @SerializedName("chat_id")
    @Expose
    private int chatId;

    @SerializedName("chat_name")
    @Expose
    private String chatName;

    @SerializedName("unread")
    @Expose
    private String unread;
    
    @SerializedName("image")
    @Expose
    private String image;

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
    
}
