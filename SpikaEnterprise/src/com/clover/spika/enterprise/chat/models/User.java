package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

	@SerializedName("id")
	@Expose
	private String id;

	@SerializedName("firstname")
	@Expose
	private String firstName;

	@SerializedName("lastname")
	@Expose
	private String lastName;

	@SerializedName("type")
	@Expose
	private String type;

	@SerializedName("image")
	@Expose
	private String image;

	@SerializedName("image_thumb")
	@Expose
	private String image_thumb;

	public String getId() {
		return id == null ? "" : id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName == null ? "" : firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName == null ? "" : lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getImage() {
		return image == null ? "" : image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		User user = (User) o;

		if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
			return false;
		if (id != null ? !id.equals(user.id) : user.id != null)
			return false;
		if (image != null ? !image.equals(user.image) : user.image != null)
			return false;
		if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
		result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
		result = 31 * result + (image != null ? image.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "User{" + "id='" + id + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", image='" + image + '\'' + '}';
	}

	public String getImage_thumb() {
		return image_thumb;
	}

	public void setImage_thumb(String image_thumb) {
		this.image_thumb = image_thumb;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
