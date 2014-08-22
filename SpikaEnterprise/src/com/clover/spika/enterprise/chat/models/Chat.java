package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.SerializedName;

public class Chat extends BaseModel {

	@SerializedName("chat")
	private Chat chat;

	@SerializedName("chat_id")
	private String chat_id;

	@SerializedName("chat_name")
	private String chat_name;

	@SerializedName("seen_by")
	private String seen_by;

	@SerializedName("total_count")
	private String total_count;

	@SerializedName("messages")
	private List<Message> messages;

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

	public String getChat_id() {
		return chat_id;
	}

	public void setChat_id(String chat_id) {
		this.chat_id = chat_id;
	}

	public String getChat_name() {
		return chat_name;
	}

	public void setChat_name(String chat_name) {
		this.chat_name = chat_name;
	}

	public String getTotal_count() {
		return total_count;
	}

	public void setTotal_count(String total_count) {
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

}
