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

}
