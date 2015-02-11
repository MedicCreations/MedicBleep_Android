package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User implements Serializable {

	private static final long serialVersionUID = -3552133443458915941L;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("user_id")
	@Expose
	private int user_id = 0;

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

	@SerializedName("is_member")
	@Expose
	private boolean is_member;

	@SerializedName("is_admin")
	@Expose
	private boolean is_admin;

	@SerializedName("name")
	@Expose
	private String name;

	@SerializedName("groupname")
	@Expose
	private String groupName;

	@SerializedName("chat_id")
	@Expose
	private String chat_id;

	@SerializedName("is_user")
	@Expose
	private int is_user;

	@SerializedName("is_group")
	@Expose
	private int is_group;

	@SerializedName("is_room")
	@Expose
	private int is_room;

	@SerializedName("details")
	@Expose
	private List<Map<String, String>> details;

	private boolean isSelected = false;

	public User(int id, String firstName, String lastName, String type, String image, String imageThumb, boolean isMember, List<Map<String, String>> details, boolean isSelected) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.type = type;
		this.image = image;
		this.image_thumb = imageThumb;
		this.is_member = isMember;
		this.details = details;
		this.isSelected = isSelected;
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

	public String getImageThumb() {
		return image_thumb;
	}

	public void setImageThumb(String imageThumb) {
		this.image_thumb = imageThumb;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isMember() {
		return is_member;
	}

	public void setIsMember(boolean isMember) {
		this.is_member = isMember;
	}

	public boolean isAdmin() {
		return is_admin;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.is_admin = isAdmin;
	}

	public List<Map<String, String>> getDetails() {
		return details;
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		User user = (User) o;

		if (is_member != user.is_member)
			return false;
		if (isSelected != user.isSelected)
			return false;
		if (details != null ? !details.equals(user.details) : user.details != null)
			return false;
		if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
			return false;
		if (image != null ? !image.equals(user.image) : user.image != null)
			return false;
		if (image_thumb != null ? !image_thumb.equals(user.image_thumb) : user.image_thumb != null)
			return false;
		if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
			return false;
		if (type != null ? !type.equals(user.type) : user.type != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = firstName != null ? firstName.hashCode() : 0;
		result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (image != null ? image.hashCode() : 0);
		result = 31 * result + (image_thumb != null ? image_thumb.hashCode() : 0);
		result = 31 * result + (is_member ? 1 : 0);
		result = 31 * result + (details != null ? details.hashCode() : 0);
		result = 31 * result + (isSelected ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "User{" + "id='" + id + '\'' + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", type='" + type + '\'' + ", image='" + image + '\''
				+ ", imageThumb='" + image_thumb + '\'' + ", isMember=" + is_member + ", details=" + details + ", isSelected=" + isSelected + '}';
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

}
