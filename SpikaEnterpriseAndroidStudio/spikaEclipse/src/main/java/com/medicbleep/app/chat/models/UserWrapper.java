package com.medicbleep.app.chat.models;

import com.medicbleep.app.chat.extendables.BaseModel;

import java.io.Serializable;
import java.util.List;

public class UserWrapper extends BaseModel implements Serializable {

	private static final long serialVersionUID = 1L;

	public User user;
	public List<UserDetail> detail_values;

	public UserWrapper() {
	}

	public List<UserDetail> getUserDetailList() {
		return detail_values;
	}

	public void setUserDetailList(List<UserDetail> userDetailList) {
		this.detail_values = userDetailList;
	}

	public User getUser() {
		return user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		UserWrapper that = (UserWrapper) o;

		if (user != null ? !user.equals(that.user) : that.user != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (user != null ? user.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "UserWrapper{" + "user=" + user + "} " + super.toString();
	}
}
