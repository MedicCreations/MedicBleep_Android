package com.clover.spika.enterprise.chat.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable {

    @SerializedName("message_id")
    @Expose
    private String message_id;

    @SerializedName("group_id")
    @Expose
    private String group_id;

    @SerializedName("character_id")
    @Expose
    private String character_id;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("rating")
    @Expose
    private String rating;

    @SerializedName("is_deleted")
    @Expose
    private int is_deleted;

    @SerializedName("is_rated")
    @Expose
    private String is_rated;

    @SerializedName("is_reported")
    @Expose
    private String is_reported;

    @SerializedName("file_id")
    @Expose
    private String file_id;

    @SerializedName("type")
    @Expose
    private int type;

    @SerializedName("character")
    @Expose
    private Character character;

    @SerializedName("created")
    @Expose
    private String created;

    @SerializedName("modified")
    @Expose
    private String modified;

    @SerializedName("sort_key")
    @Expose
    private String sort_key;

    public Message() {
    }

    public Message(Parcel source) {
	message_id = source.readString();
	group_id = source.readString();
	character_id = source.readString();
	text = source.readString();
	rating = source.readString();
	is_deleted = source.readInt();
	is_rated = source.readString();
	is_reported = source.readString();
	type = source.readInt();
//	character = source.readParcelable(Character.class.getClassLoader());
	created = source.readString();
	modified = source.readString();
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
	public Message createFromParcel(Parcel source) {
	    return new Message(source);
	}

	public Message[] newArray(int size) {
	    return new Message[size];
	}
    };

    public String getMessageId() {
	return message_id;
    }

    public void setMessageId(String message_id) {
	this.message_id = message_id;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
	parcel.writeString(message_id);
	parcel.writeString(group_id);
	parcel.writeString(character_id);
	parcel.writeString(text);
	parcel.writeString(rating);
	parcel.writeInt(is_deleted);
	parcel.writeString(is_rated);
	parcel.writeString(is_reported);
	parcel.writeInt(type);
//	parcel.writeParcelable((Parcelable) character, flags);
	parcel.writeString(created);
	parcel.writeString(modified);
    }

    public String getGroup_id() {
	return group_id;
    }

    public void setGroup_id(String group_id) {
	this.group_id = group_id;
    }

    public String getCharacter_id() {
	return character_id;
    }

    public void setCharacter_id(String character_id) {
	this.character_id = character_id;
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    public String getRating() {
	return rating;
    }

    public void setRating(String rating) {
	this.rating = rating;
    }

    public int getIs_deleted() {
	return is_deleted;
    }

    public void setIs_deleted(int is_deleted) {
	this.is_deleted = is_deleted;
    }

    public String getIs_rated() {
	return is_rated;
    }

    public void setIs_rated(String is_rated) {
	this.is_rated = is_rated;
    }

    public String getIs_reported() {
	return is_reported;
    }

    public void setIs_reported(String is_reported) {
	this.is_reported = is_reported;
    }

    public String getFile() {
	return file_id;
    }

    public void setFile(String file_id) {
	this.file_id = file_id;
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

    public Character getCharacter() {
	return character;
    }

    public void setCharacter(Character character) {
	this.character = character;
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

    public String getSort_key() {
	return sort_key;
    }

    public void setSort_key(String sort_key) {
	this.sort_key = sort_key;
    }

}