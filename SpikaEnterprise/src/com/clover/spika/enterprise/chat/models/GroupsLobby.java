package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GroupsLobby {

	@SerializedName("total_count")
	@Expose
	private int totalCount;

	@SerializedName("group_chats")
	@Expose
	private List<Chat> chatsList;

	public GroupsLobby() {
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
