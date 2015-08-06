package com.medicbleep.app.chat.models;

import com.medicbleep.app.chat.extendables.BaseModel;

import java.util.List;

public class LocalPush extends BaseModel {

	public List<LocalPush> chats;
	public String chat_id;
	public String organization_id;
	public String firstname;
	public int unread;
	public String password;
	public int type;

	public LocalPush() {

	}

}
