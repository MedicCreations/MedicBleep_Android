package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;
import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public class UserWrapper extends BaseModel implements Serializable{

    @SerializedName("user")
    @Expose
    private User user;
    
    @SerializedName("detail_values")
    @Expose
    private List<UserDetail> userDetailList;

	public List<UserDetail> getUserDetailList() {
		return userDetailList;
	}

	public void setUserDetailList(List<UserDetail> userDetailList) {
		this.userDetailList = userDetailList;
	}

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserWrapper that = (UserWrapper) o;

        if (user != null ? !user.equals(that.user) : that.user != null) return false;

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
        return "UserWrapper{" +
                "user=" + user +
                "} " + super.toString();
    }
}
