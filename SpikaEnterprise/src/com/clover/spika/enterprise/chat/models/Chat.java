package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

import java.util.List;

public class Chat extends BaseModel {

	public Chat chat;
	public int id;
	public int chat_id;
	public String chat_name;
	public String seen_by;
	public int total_count;
	public List<Message> messages;
	public User user;
	public String image_thumb;
	public String image;
	public String admin_id;
	public int is_active;
	public int type;
	public int is_private;
	public String password;
	public String unread;
	public Category category;
	public boolean is_member;
	public Message last_message;

	public boolean isSelected = false;
	public boolean isNewMsg = false;
	public boolean isRefresh = false;
	public boolean isClear = false;
	public boolean isSend = false;
	public boolean isPagging = false;
	public int adapterCount = -1;

	public Chat() {
	}

	public int getId() {

		if (chat_id == 0) {
			return id;
		} else {
			return chat_id;
		}
	}
}
