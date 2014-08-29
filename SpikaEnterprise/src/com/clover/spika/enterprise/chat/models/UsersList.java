package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UsersList extends BaseModel {

	@SerializedName("page")
	@Expose
	private int page;

	@SerializedName("total_count")
	@Expose
	private int totalCount;

	@SerializedName("users")
	@Expose
	private List<User> userList;

	@SerializedName("members")
	@Expose
	private List<User> membersList;

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

	public List<User> getUserList() {
		return userList == null ? new ArrayList<User>() : userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		UsersList userModel = (UsersList) o;

		if (page != userModel.page)
			return false;
		if (totalCount != userModel.totalCount)
			return false;
		if (userList != null ? !userList.equals(userModel.userList) : userModel.userList != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + page;
		result = 31 * result + totalCount;
		result = 31 * result + (userList != null ? userList.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "UserModel{" + "page=" + page + ", totalCount=" + totalCount + ", userList=" + userList + "} " + super.toString();
	}

	public List<User> getMembersList() {
		return membersList;
	}

	public void setMembersList(List<User> membersList) {
		this.membersList = membersList;
	}
}
