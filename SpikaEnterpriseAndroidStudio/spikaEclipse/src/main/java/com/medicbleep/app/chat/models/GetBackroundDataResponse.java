package com.medicbleep.app.chat.models;

import java.util.List;

import com.medicbleep.app.chat.extendables.BaseModel;

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
