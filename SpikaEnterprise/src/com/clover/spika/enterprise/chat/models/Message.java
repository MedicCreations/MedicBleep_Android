package com.clover.spika.enterprise.chat.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable {

	@SerializedName("id")
	private String id;

	@SerializedName("chat_id")
	private String chat_id;

	@SerializedName("user_id")
	private String user_id;

	@SerializedName("name")
	private String name;

	@SerializedName("image")
	private String image;

	@SerializedName("text")
	private String text;

	@SerializedName("file_id")
	private String file_id;

	@SerializedName("longitude")
	private String longitude;

	@SerializedName("latitude")
	private String latitude;

	@SerializedName("type")
	private int type;

	@SerializedName("created")
	private String created;

	@SerializedName("modified")
	private String modified;

	public Message() {
	}

	public Message(Parcel source) {
		text = source.readString();
		type = source.readInt();
		created = source.readString();
		modified = source.readString();
		id = source.readString();
		chat_id = source.readString();
		user_id = source.readString();
		name = source.readString();
		image = source.readString();
		file_id = source.readString();
		latitude = source.readString();
		longitude = source.readString();
	}

	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
		public Message createFromParcel(Parcel source) {
			return new Message(source);
		}

		public Message[] newArray(int size) {
			return new Message[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(text);
		parcel.writeInt(type);
		parcel.writeString(created);
		parcel.writeString(modified);
		parcel.writeString(id);
		parcel.writeString(chat_id);
		parcel.writeString(user_id);
		parcel.writeString(name);
		parcel.writeString(image);
		parcel.writeString(file_id);
		parcel.writeString(latitude);
		parcel.writeString(longitude);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChat_id() {
		return chat_id;
	}

	public void setChat_id(String chat_id) {
		this.chat_id = chat_id;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFile_id() {
		return file_id;
	}

	public void setFile_id(String file_id) {
		this.file_id = file_id;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

}