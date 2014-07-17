package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group {

	@SerializedName("group_id")
	@Expose
	private String group_id;

	@SerializedName("group_name")
	@Expose
	private String group_name;

	@SerializedName("image_name")
	@Expose
	private String image_name;

	public Group() {
	}

	public String getGroupId() {
		return group_id;
	}

	public void setGroupId(String group_id) {
		this.group_id = group_id;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public String getImage_name() {
		return image_name;
	}

	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}

}
