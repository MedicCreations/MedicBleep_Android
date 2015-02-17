package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Chat extends BaseModel {

	@SerializedName("chat")
	private Chat chat;

	@SerializedName("id")
	private int id;

	@SerializedName("chat_id")
	private int chat_id = 0;

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

	@SerializedName("is_member")
	@Expose
	private boolean isMember;
	
	@SerializedName("last_message")
	@Expose
	private Message lastMessage;

	private boolean isSelected = false;
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
	
	public Message getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(Message lastMessage) {
		this.lastMessage = lastMessage;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chat other = (Chat) obj;
		if (adapterCount != other.adapterCount)
			return false;
		if (adminId == null) {
			if (other.adminId != null)
				return false;
		} else if (!adminId.equals(other.adminId))
			return false;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (chat == null) {
			if (other.chat != null)
				return false;
		} else if (!chat.equals(other.chat))
			return false;
		if (chat_id != other.chat_id)
			return false;
		if (chat_name == null) {
			if (other.chat_name != null)
				return false;
		} else if (!chat_name.equals(other.chat_name))
			return false;
		if (id != other.id)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (imageThumb == null) {
			if (other.imageThumb != null)
				return false;
		} else if (!imageThumb.equals(other.imageThumb))
			return false;
		if (isActive != other.isActive)
			return false;
		if (isClear != other.isClear)
			return false;
		if (isMember != other.isMember)
			return false;
		if (isNewMsg != other.isNewMsg)
			return false;
		if (isPagging != other.isPagging)
			return false;
		if (isPrivate != other.isPrivate)
			return false;
		if (isRefresh != other.isRefresh)
			return false;
		if (isSelected != other.isSelected)
			return false;
		if (isSend != other.isSend)
			return false;
		if (lastMessage == null) {
			if (other.lastMessage != null)
				return false;
		} else if (!lastMessage.equals(other.lastMessage))
			return false;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (seen_by == null) {
			if (other.seen_by != null)
				return false;
		} else if (!seen_by.equals(other.seen_by))
			return false;
		if (total_count != other.total_count)
			return false;
		if (type != other.type)
			return false;
		if (unread == null) {
			if (other.unread != null)
				return false;
		} else if (!unread.equals(other.unread))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + adapterCount;
		result = prime * result + ((adminId == null) ? 0 : adminId.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((chat == null) ? 0 : chat.hashCode());
		result = prime * result + chat_id;
		result = prime * result + ((chat_name == null) ? 0 : chat_name.hashCode());
		result = prime * result + id;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((imageThumb == null) ? 0 : imageThumb.hashCode());
		result = prime * result + isActive;
		result = prime * result + (isClear ? 1231 : 1237);
		result = prime * result + (isMember ? 1231 : 1237);
		result = prime * result + (isNewMsg ? 1231 : 1237);
		result = prime * result + (isPagging ? 1231 : 1237);
		result = prime * result + isPrivate;
		result = prime * result + (isRefresh ? 1231 : 1237);
		result = prime * result + (isSelected ? 1231 : 1237);
		result = prime * result + (isSend ? 1231 : 1237);
		result = prime * result + ((lastMessage == null) ? 0 : lastMessage.hashCode());
		result = prime * result + ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((seen_by == null) ? 0 : seen_by.hashCode());
		result = prime * result + total_count;
		result = prime * result + type;
		result = prime * result + ((unread == null) ? 0 : unread.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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

	public boolean isMember() {
		return isMember;
	}

	public void setMember(boolean isMember) {
		this.isMember = isMember;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public int getId() {

		if (chat_id == 0) {
			return id;
		} else {
			return chat_id;
		}
	}

	public void setId(int id) {
		this.id = id;
	}
}
