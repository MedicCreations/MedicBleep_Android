package com.clover.spika.enterprise.chat.models;

public class Group {

	public int id;
	public String type;
	public String groupname;
	public String image;
	public String image_thumb;
	public Category category;
	public boolean is_member;
	public boolean isSelected = false;

	public Group() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupname == null ? "" : groupname;
	}

	public void setGroupName(String groupName) {
		this.groupname = groupName;
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
		if (groupname != null ? !groupname.equals(group.groupname) : group.groupname != null)
			return false;
		if (image != null ? !image.equals(group.image) : group.image != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (groupname != null ? groupname.hashCode() : 0);
		result = 31 * result + (image != null ? image.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Group{" + "id=" + id + ", groupName='" + groupname + '\'' + ", image='" + image + '\'' + '}';
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

	public boolean isMember() {
		return is_member;
	}

	public void setMember(boolean isMember) {
		this.is_member = isMember;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}
