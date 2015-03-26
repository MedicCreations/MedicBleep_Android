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
	public int is_member;
	public Message last_message;
	public long modified;

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
	
	public boolean isMember(){
		return this.is_member == 0 ? false : true;
	}

	@Override
	public String toString() {
		return "Chat [chat=" + chat + ", id=" + id + ", chat_id=" + chat_id + ", chat_name=" + chat_name + ", seen_by=" + seen_by + ", total_count=" + total_count + ", messages=" + messages
				+ ", user=" + user + ", image_thumb=" + image_thumb + ", image=" + image + ", admin_id=" + admin_id + ", is_active=" + is_active + ", type=" + type + ", is_private=" + is_private
				+ ", password=" + password + ", unread=" + unread + ", category=" + category + ", is_member=" + is_member + ", last_message=" + last_message + ", modified=" + modified
				+ ", isSelected=" + isSelected + ", isNewMsg=" + isNewMsg + ", isRefresh=" + isRefresh + ", isClear=" + isClear + ", isSend=" + isSend + ", isPagging=" + isPagging + ", adapterCount="
				+ adapterCount + "]";
	}
	
	public Chat copyChat(Chat toCopy){
		Chat chat = new Chat();
		
		chat.id = toCopy.id;
		chat.chat_id = toCopy.chat_id;
		chat.chat_name = toCopy.chat_name;
		chat.seen_by = toCopy.seen_by;
		chat.total_count = toCopy.total_count;
		chat.image_thumb = toCopy.image_thumb;
		chat.image = toCopy.image;
		chat.admin_id = toCopy.admin_id;
		chat.is_active = toCopy.is_active;
		chat.type = toCopy.type;
		chat.is_private = toCopy.is_private;
		chat.password = toCopy.password;
		chat.unread = toCopy.unread;
		chat.is_member = toCopy.is_member;
		chat.modified = toCopy.modified;
		
		return chat;
	}
	
}
