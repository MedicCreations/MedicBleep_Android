package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ConfirmUsersList extends BaseModel {

	@SerializedName("users_array")
	@Expose
	private List<User> users_array;

	public ConfirmUsersList() {
	}

	public List<User> getUserList() {
		return users_array;
	}

	public void setUserList(List<User> userList) {
		this.users_array = userList;
	}

}
