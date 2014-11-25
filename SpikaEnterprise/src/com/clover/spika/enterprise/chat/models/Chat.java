package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Chat extends BaseModel {

	@SerializedName("chat")
	private Chat chat;

	@SerializedName("chat_id")
	private long chat_id;

	@SerializedName("chat_name")
	private String chat_name;

	@SerializedName("seen_by")
	private String seen_by;

	@SerializedName("total_count")
	private int total_count;

	@SerializedName("messages")
	private List<Message> messages;

	@SerializedName("user")
	private User user;

	@SerializedName("image_thumb")
	private String imageThumb;

	@SerializedName("image")
	private String image;

	@SerializedName("admin_id")
	private String adminId;

	@SerializedName("is_active")
	private int isActive;

	@SerializedName("type")
	private int type;

	@SerializedName("is_private")
	private int isPrivate;

	@SerializedName("password")
	private String password;

	@SerializedName("unread")
	@Expose
	private String unread;

	@SerializedName("category")
	@Expose
	private Category category;

	private boolean isNewMsg = false;
	private boolean isRefresh = false;
	private boolean isClear = false;
	private boolean isSend = false;
	private boolean isPagging = false;
	private int adapterCount = -1;

	public boolean isNewMsg() {
		return isNewMsg;
	}

	public void setNewMsg(boolean isNewMsg) {
		this.isNewMsg = isNewMsg;
	}

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setRefresh(boolean isRefresh) {
		this.isRefresh = isRefresh;
	}

	public boolean isClear() {
		return isClear;
	}

	public void setClear(boolean isClear) {
		this.isClear = isClear;
	}

	public boolean isSend() {
		return isSend;
	}

	public void setSend(boolean isSend) {
		this.isSend = isSend;
	}

	public boolean isPagging() {
		return isPagging;
	}

	public void setPagging(boolean isPagging) {
		this.isPagging = isPagging;
	}

	public int getAdapterCount() {
		return adapterCount;
	}

	public void setAdapterCount(int adapterCount) {
		this.adapterCount = adapterCount;
	}

	public long getChat_id() {
		return chat_id;
	}

	public void setChat_id(long chat_id) {
		this.chat_id = chat_id;
	}

	public String getChat_name() {
		return chat_name;
	}

	public void setChat_name(String chat_name) {
		this.chat_name = chat_name;
	}

	public int getTotal_count() {
		return total_count;
	}

	public void setTotal_count(int total_count) {
		this.total_count = total_count;
	}

	public List<Message> getMessagesList() {
		return messages;
	}

	public void setMessagesList(List<Message> messages) {
		this.messages = messages;
	}

	public String getSeen_by() {
		return seen_by;
	}

	public void setSeen_by(String seen_by) {
		this.seen_by = seen_by;
	}

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public User getUser() {
		return user;
	}

	public String getImageThumb() {
		return imageThumb;
	}

	public void setImageThumb(String imageThumb) {
		this.imageThumb = imageThumb;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public int isActive() {
		return isActive;
	}

	public void setActive(int isActive) {
		this.isActive = isActive;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int isPrivate() {
		return isPrivate;
	}

	public void setPrivate(int isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

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

	public String getUnread() {
		return unread;
	}

	public void setUnread(String unread) {
		this.unread = unread;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
