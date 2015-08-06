package com.medicbleep.app.chat.models;

import java.util.ArrayList;
import java.util.List;

public class UsersLobby {

	public int total_count;
	public List<Chat> user_chats;

	public UsersLobby() {
		user_chats = new ArrayList<Chat>();
	}

	public int getTotalCount() {
		return total_count;
	}

	public void setTotalCount(int totalCount) {
		this.total_count = totalCount;
	}

	public List<Chat> getChatsList() {
		return user_chats;
	}

	public void setChatsList(List<Chat> chatsList) {
		this.user_chats = chatsList;
	}

}
