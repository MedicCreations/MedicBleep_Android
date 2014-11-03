package com.clover.spika.enterprise.chat.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AllLobby {

    @SerializedName("total_count")
    @Expose
    private int totalCount;

    @SerializedName("chats")
    @Expose
    private List<ChatsLobby> chatsList;

    public AllLobby () {
    	chatsList = new ArrayList<ChatsLobby>();
    }
    
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
