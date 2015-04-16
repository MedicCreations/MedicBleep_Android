package com.clover.spika.enterprise.chat.models;

import java.util.ArrayList;
import java.util.List;

public class GroupsLobby {

	public int total_count;
	public List<Chat> group_chats;

	public GroupsLobby() {
		group_chats = new ArrayList<Chat>();
	}

	public int getTotalCount() {
		return total_count;
	}

	public void setTotalCount(int totalCount) {
		this.total_count = totalCount;
	}

	public List<Chat> getChatsList() {
		return group_chats;
	}

	public void setChatsList(List<Chat> chatsList) {
		this.group_chats = chatsList;
	}

}
