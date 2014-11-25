package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsersAndGroupsList extends BaseModel {

    @SerializedName("page")
    @Expose
    private int page;

    @SerializedName("total_count")
    @Expose
    private int totalCount;

    @SerializedName("search_result")
    @Expose
    private List<UserGroupRoom> usersAndGroupsList;

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<UserGroupRoom> getUsersAndGroupsList() {
		return usersAndGroupsList;
	}

	public void setUsersAndGroupsList(List<UserGroupRoom> memberList) {
		this.usersAndGroupsList = memberList;
	}

}
