package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login extends BaseModel {

    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("token")
    @Expose
    private String token;
    
    @SerializedName("image")
    @Expose
    private String image;
    
    @SerializedName("firstname")
    @Expose
    private String firstname;
    
    @SerializedName("lastname")
    @Expose
    private String lastname;
    
    @SerializedName("organizations")
	@Expose
	private List<Organization> organizations;

    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token == null ? "" : token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Login login = (Login) o;

        if (token != null ? !token.equals(login.token) : login.token != null) return false;
        if (userId != null ? !userId.equals(login.userId) : login.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Login{" +
                "userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                "} " + super.toString();
    }
}
