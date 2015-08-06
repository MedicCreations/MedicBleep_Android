package com.medicbleep.app.chat.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	public int id;
	public int user_id = 0;
	public String firstname;
	public String lastname;
	public int type;
	public String image;
	public String image_thumb;
	public boolean is_member;
	public int is_admin;
	public String name;
	public String groupname;
	public String chat_id;
	public int is_user;
	public int is_group;
	public int is_room;
	public List<Map<String, String>> details;
	public Organization organization;
	public boolean isSelected = false;

	public User() {
	}

	public User(int id, String firstName, String lastName, String type, String image, String imageThumb, boolean isMember,
			List<Map<String, String>> details, boolean isSelected, Organization org) {
		super();
		this.id = id;
		this.firstname = firstName;
		this.lastname = lastName;
		this.type = Integer.valueOf(type);
		this.image = image;
		this.image_thumb = imageThumb;
		this.is_member = isMember;

		this.details = details;
		this.isSelected = isSelected;
		organization = org;
	}

	public int getId() {

		if (user_id == 0) {
			return id;
		} else {
			return user_id;
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstname == null ? "" : firstname;
	}

	public void setFirstName(String firstName) {
		this.firstname = firstName;
	}

	public String getLastName() {
		return lastname == null ? "" : lastname;
	}

	public void setLastName(String lastName) {
		this.lastname = lastName;
	}

	public String getImage() {
		return image == null ? "" : image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageThumb() {
		return image_thumb;
	}

	public void setImageThumb(String imageThumb) {
		this.image_thumb = imageThumb;
	}

	public int getType() {
		return type;
	}

	public void setType(String type) {
		this.type = Integer.valueOf(type);
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isMember() {
		return this.is_member;
	}

	public void setIsMember(boolean isMember) {
		this.is_member = isMember;
	}

	public boolean isAdmin() {

		if (this.is_admin == 0) {
			return false;
		}

		return true;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.is_admin = isAdmin ? 1 : 0;
	}

	public List<Map<String, String>> getDetails() {
		return details;
	}

	public void setDetails(List<Map<String, String>> details) {
		this.details = details;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Map<String, String> getPublicDetails() {
		Map<String, String> detailsMap = new HashMap<String, String>();

		if (getDetails() != null) {
			for (Map<String, String> map : getDetails()) {
				if (map.containsKey("public") && (map.get("public").equals("true") || "1".equals(map.get("public")))) {
					for (String key : map.keySet()) {
						if (!"public".equals(key) && !map.get(key).equals("")) {
							detailsMap.put(key, map.get(key));
						}
					}
				}
			}
		}

		return detailsMap;
	}

	public String getGroupName() {
		return groupname;
	}

	public void setGroupName(String groupName) {
		this.groupname = groupName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChatId() {
		return chat_id;
	}

	public void setChatId(String chatId) {
		this.chat_id = chatId;
	}

	public int getIsUser() {
		return is_user;
	}

	public void setIsUser(int isUser) {
		this.is_user = isUser;
	}

	public boolean getIsGroup() {

		if (is_group == 0) {
			return false;
		}

		return true;
	}

	public void setIsGroup(boolean isGroup) {

		if (isGroup) {
			this.is_group = 1;
		} else {
			this.is_group = 0;
		}
	}

	public boolean getIsRoom() {

		if (is_room == 0) {
			return false;
		}

		return true;
	}

	public void setIsRoom(boolean isRoom) {

		if (isRoom) {
			this.is_room = 1;
		} else {
			this.is_room = 0;
		}
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", user_id=" + user_id + ", firstname=" + firstname + ", lastname=" + lastname + ", type=" + type + ", image="
				+ image + ", image_thumb=" + image_thumb + ", is_member=" + is_member + ", is_admin=" + is_admin + ", name=" + name + ", groupname="
				+ groupname + ", chat_id=" + chat_id + ", is_user=" + is_user + ", is_group=" + is_group + ", is_room=" + is_room + ", details="
				+ details + ", organization=" + organization + ", isSelected=" + isSelected + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chat_id == null) ? 0 : chat_id.hashCode());
		result = prime * result + ((details == null) ? 0 : details.hashCode());
		result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result + ((groupname == null) ? 0 : groupname.hashCode());
		result = prime * result + id;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((image_thumb == null) ? 0 : image_thumb.hashCode());
		result = prime * result + (isSelected ? 1231 : 1237);
		result = prime * result + is_admin;
		result = prime * result + is_group;
		result = prime * result + (is_member ? 1231 : 1237);
		result = prime * result + is_room;
		result = prime * result + is_user;
		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + type;
		result = prime * result + user_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (chat_id == null) {
			if (other.chat_id != null)
				return false;
		} else if (!chat_id.equals(other.chat_id))
			return false;
		if (details == null) {
			if (other.details != null)
				return false;
		} else if (!details.equals(other.details))
			return false;
		if (firstname == null) {
			if (other.firstname != null)
				return false;
		} else if (!firstname.equals(other.firstname))
			return false;
		if (groupname == null) {
			if (other.groupname != null)
				return false;
		} else if (!groupname.equals(other.groupname))
			return false;
		if (id != other.id)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (image_thumb == null) {
			if (other.image_thumb != null)
				return false;
		} else if (!image_thumb.equals(other.image_thumb))
			return false;
		if (isSelected != other.isSelected)
			return false;
		if (is_admin != other.is_admin)
			return false;
		if (is_group != other.is_group)
			return false;
		if (is_member != other.is_member)
			return false;
		if (is_room != other.is_room)
			return false;
		if (is_user != other.is_user)
			return false;
		if (lastname == null) {
			if (other.lastname != null)
				return false;
		} else if (!lastname.equals(other.lastname))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (type != other.type)
			return false;
		if (user_id != other.user_id)
			return false;
		return true;
	}

}
