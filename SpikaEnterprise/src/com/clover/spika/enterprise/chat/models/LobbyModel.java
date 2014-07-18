package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LobbyModel extends BaseModel {

    @SerializedName("page")
    @Expose
    private int page;

    @SerializedName("users")
    @Expose
    private UsersLobby usersLoby;

    @SerializedName("groups")
    @Expose
    private GroupsLobby groupsLobby;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public UsersLobby getUsersLoby() {
		return usersLoby;
	}

	public void setUsersLoby(UsersLobby usersLoby) {
		this.usersLoby = usersLoby;
	}

	public GroupsLobby getGroupsLobby() {
		return groupsLobby;
	}

	public void setGroupsLobby(GroupsLobby groupsLobby) {
		this.groupsLobby = groupsLobby;
	}
    
}
