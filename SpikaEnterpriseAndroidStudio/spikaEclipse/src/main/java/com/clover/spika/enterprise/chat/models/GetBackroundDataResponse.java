package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

public class GetBackroundDataResponse extends BaseModel {

	public List<Message> messages;
	public Chat chat;
	public User user;
    public int total_count;

	public GetBackroundDataResponse() {
	}

	@Override
	public String toString() {
		return "GetBackroundDataResponse [messages=" + messages + ", chat=" + chat + ", user=" + user + "]";
	}
	
}
