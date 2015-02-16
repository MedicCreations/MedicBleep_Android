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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		Chat chat1 = (Chat) o;

		if (adapterCount != chat1.adapterCount)
			return false;
		if (isClear != chat1.isClear)
			return false;
		if (isNewMsg != chat1.isNewMsg)
			return false;
		if (isPagging != chat1.isPagging)
			return false;
		if (isRefresh != chat1.isRefresh)
			return false;
		if (isSend != chat1.isSend)
			return false;
		if (chat != null ? !chat.equals(chat1.chat) : chat1.chat != null)
			return false;
		if (chat_id != chat1.chat_id)
			return false;
		if (chat_name != null ? !chat_name.equals(chat1.chat_name) : chat1.chat_name != null)
			return false;
		if (messages != null ? !messages.equals(chat1.messages) : chat1.messages != null)
			return false;
		if (seen_by != null ? !seen_by.equals(chat1.seen_by) : chat1.seen_by != null)
			return false;
		if (total_count != chat1.total_count)
			return false;
		if (user != null ? !user.equals(chat1.user) : chat1.user != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (chat != null ? chat.hashCode() : 0);
		result = 31 * result + (chat_name != null ? chat_name.hashCode() : 0);
		result = 31 * result + (seen_by != null ? seen_by.hashCode() : 0);
		result = 31 * result + (messages != null ? messages.hashCode() : 0);
		result = 31 * result + (user != null ? user.hashCode() : 0);
		result = 31 * result + (isNewMsg ? 1 : 0);
		result = 31 * result + (isRefresh ? 1 : 0);
		result = 31 * result + (isClear ? 1 : 0);
		result = 31 * result + (isSend ? 1 : 0);
		result = 31 * result + (isPagging ? 1 : 0);
		result = 31 * result + adapterCount;
		return result;
	}

	public int getId() {

		if (chat_id == 0) {
			return id;
		} else {
			return chat_id;
		}
	}
}
