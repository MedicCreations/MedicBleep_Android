package com.medicbleep.app.chat.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AllLobby {

	public int total_count;
	public List<Chat> chats;
	
	public AllLobby() {
		chats = new ArrayList<Chat>();
	}

	@Override
	public String toString() {
		return "AllLobby{" +
				"total_count=" + total_count +
				", chats=" + chats +
				'}';
	}
}
