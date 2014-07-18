package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupsLobby {

    @SerializedName("total_count")
    @Expose
    private int totalCount;

    @SerializedName("group_chats")
    @Expose
    private List<ChatsLobby> chatsList;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<ChatsLobby> getChatsList() {
		return chatsList;
	}

	public void setChatsList(List<ChatsLobby> chatsList) {
		this.chatsList = chatsList;
	}

}
