package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserGroupRoom {

	@SerializedName("id")
	@Expose
	private String id;

	@SerializedName("user_id")
	@Expose
	private String userId;

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
	private String imageThumb;

	@SerializedName("groupname")
	@Expose
	private String groupName;

	@SerializedName("name")
	@Expose
	private String roomName;

	@SerializedName("is_member")
	@Expose
	private boolean isMember;

	@SerializedName("is_user")
	@Expose
	private int isUser;

	@SerializedName("is_group")
	@Expose
	private int isGroup;

	@SerializedName("is_room")
	@Expose
	private int isRoom;

	private boolean isSelected = false;

	public UserGroupRoom(String id, String userId, String firstName, String lastName, String image, String imageThumb, String groupName, int isUser, int is_group,
			boolean isSelected) {
		super();
		this.id = id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.image = image;
		this.imageThumb = imageThumb;
		this.groupName = groupName;
		this.isUser = isUser;
		this.isGroup = is_group;
		this.isSelected = isSelected;
	}

	public String getId() {
		if (id == null || id.equals("")) {
			return userId == null ? "" : userId;
		}
		return id;
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

	public String getImageThumb() {
		return imageThumb;
	}

	public void setImageThumb(String imageThumb) {
		this.imageThumb = imageThumb;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean getIsUser() {
		return isUser == 1 ? true : false;
	}

	public void setIsUser(boolean isUser) {
		if (isUser) {
			this.isUser = 1;
			return;
		}
		this.isUser = 0;
	}

	public boolean getIs_group() {
		return isGroup == 1 ? true : false;
	}

	public void setIs_group(boolean is_group) {
		if (is_group) {
			this.isGroup = 1;
			return;
		}
		this.isGroup = 0;
	}

	public boolean getIsRoom() {
		return isRoom == 1 ? true : false;
	}

	public void setIsRoom(boolean isRoom) {

		if (isRoom) {
			this.isRoom = 1;
			return;
		}

		this.isRoom = 0;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public boolean getIsMember() {
		return isMember;
	}

	public void setIsMember(boolean isMember) {
		this.isMember = isMember;
	}

}
