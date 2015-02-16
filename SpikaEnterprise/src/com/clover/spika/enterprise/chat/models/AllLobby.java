package com.clover.spika.enterprise.chat.models;

import java.util.ArrayList;
import java.util.List;

public class AllLobby {

	public int total_count;
	public List<Chat> chats;

	public AllLobby() {
		chats = new ArrayList<Chat>();
	}

}
