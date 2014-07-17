package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

public class Chat extends BaseModel {

	private List<Message> msgList = null;
	private int totalItems = -1;
	private boolean isNewMsg = false;
	private boolean isRefresh = false;
	private boolean isClear = false;
	private boolean isSend = false;
	private boolean isPagging = false;
	private int adapterCount = -1;

	public List<Message> getMsgList() {
		return msgList;
	}

	public void setMsgList(List<Message> msgList) {
		this.msgList = msgList;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

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

}
