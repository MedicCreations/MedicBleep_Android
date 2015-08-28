package com.medicbleep.app.chat.models;

import com.medicbleep.app.chat.extendables.BaseModel;

public class LobbyModel extends BaseModel {

	public int page;
	public UsersLobby users;
	public GroupsLobby groups;
	public AllLobby all_chats;

	public LobbyModel() {
		users = new UsersLobby();
		groups = new GroupsLobby();
		all_chats = new AllLobby();
	}

	@Override
	public String toString() {
		return "LobbyModel{" +
				"page=" + page +
				", users=" + users +
				", groups=" + groups +
				", all_chats=" + all_chats +
				'}';
	}
}
