package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupMember {

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("firstname")
	@Expose
	private String firstName;

	@SerializedName("lastname")
	@Expose
	private String lastName;

	@SerializedName("image")
	@Expose
	private String image;

	@SerializedName("image_thumb")
	@Expose
	private String image_thumb;
	
	@SerializedName("group_id")
	@Expose
	private String groupId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImage_thumb() {
		return image_thumb;
	}

	public void setImage_thumb(String image_thumb) {
		this.image_thumb = image_thumb;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
}
