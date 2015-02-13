package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login extends BaseModel {

	@SerializedName("user_id")
	@Expose
	private String _userId;

	@SerializedName("token")
	@Expose
	private String _token;

	@SerializedName("image")
	@Expose
	private String _image;

	@SerializedName("firstname")
	@Expose
	private String _firstname;

	@SerializedName("lastname")
	@Expose
	private String _lastname;

	@SerializedName("organizations")
	@Expose
	private List<Organization> organizations;

	public String getUserId() {
		return _userId == null ? "" : _userId;
	}

	public void setUserId(String userId) {
		this._userId = userId;
	}

	public String getToken() {
		return _token == null ? "" : _token;
	}

	public void setToken(String token) {
		this._token = token;
	}

	public String getImage() {
		return _image;
	}

	public void setImage(String image) {
		this._image = image;
	}

	public String getFirstname() {
		return _firstname;
	}

	public void setFirstname(String firstname) {
		this._firstname = firstname;
	}

	public String getLastname() {
		return _lastname;
	}

	public void setLastname(String lastname) {
		this._lastname = lastname;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		Login login = (Login) o;

		if (_token != null ? !_token.equals(login._token) : login._token != null)
			return false;
		if (_userId != null ? !_userId.equals(login._userId) : login._userId != null)
			return false;

		return true;
	}

	public List<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (_userId != null ? _userId.hashCode() : 0);
		result = 31 * result + (_token != null ? _token.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Login{" + "userId='" + _userId + '\'' + ", token='" + _token + '\'' + "} " + super.toString();
	}
}
