package com.clover.spika.enterprise.chat.listeners;

import com.clover.spika.enterprise.chat.models.LobbyModel;

public interface LobbyChangedListener {
	public void onChangeAll(LobbyModel model);
	public void onChangeGroup(LobbyModel model);
	public void onChangeUser(LobbyModel model);
}
