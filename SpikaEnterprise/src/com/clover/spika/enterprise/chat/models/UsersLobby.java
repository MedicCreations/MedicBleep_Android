package com.clover.spika.enterprise.chat.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsersLobby {

	@SerializedName("total_count")
	@Expose
	private int totalCount;

	@SerializedName("user_chats")
	@Expose
	private List<Chat> chatsList;

	public UsersLobby() {
		chatsList = new ArrayList<Chat>();
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<Chat> getChatsList() {
		return chatsList;
	}

	public void setChatsList(List<Chat> chatsList) {
		this.chatsList = chatsList;
	}

}
