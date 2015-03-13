package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

public class Login extends BaseModel {

	public String user_id;
	public String token;
	public String image;
	public String image_thumb;
	public String firstname;
	public String lastname;
	public List<Organization> organizations;

	public Login() {
	}

	public String getUserId() {
		return user_id == null ? "" : user_id;
	}

	public String getToken() {
		return token == null ? "" : token;
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

		if (token != null ? !token.equals(login.token) : login.token != null)
			return false;
		if (user_id != null ? !user_id.equals(login.user_id) : login.user_id != null)
			return false;

		return true;
	}

	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}

	public String getImageThumb() {
		return image_thumb;
	}

	public void setImageThumb(String imageThumb) {
		this.image_thumb = imageThumb;
	}
}
