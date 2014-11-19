package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group {

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("type")
	@Expose
	private String type;

	@SerializedName("groupname")
	@Expose
	private String groupName;

	@SerializedName("image")
	@Expose
	private String image;

	@SerializedName("image_thumb")
	@Expose
	private String image_thumb;

	@SerializedName("category")
	@Expose
	private Category category;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName == null ? "" : groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

		Group group = (Group) o;

		if (id != group.id)
			return false;
		if (groupName != null ? !groupName.equals(group.groupName) : group.groupName != null)
			return false;
		if (image != null ? !image.equals(group.image) : group.image != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (groupName != null ? groupName.hashCode() : 0);
		result = 31 * result + (image != null ? image.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Group{" + "id=" + id + ", groupName='" + groupName + '\'' + ", image='" + image + '\'' + '}';
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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
}
