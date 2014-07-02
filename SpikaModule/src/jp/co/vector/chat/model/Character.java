package jp.co.vector.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Character implements Parcelable {

    @SerializedName("character_id")
    @Expose
    private String character_id;

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("uuid")
    @Expose
    private String uuid;

    @SerializedName("token")
    @Expose
    private String token;

    @SerializedName("token_timestamp")
    @Expose
    private String token_timestamp;

    @SerializedName("image_name")
    @Expose
    private String image_name;

    @SerializedName("android_push_token")
    @Expose
    private String android_push_token;

    @SerializedName("ios_push_token")
    @Expose
    private String ios_push_token;

    @SerializedName("created")
    @Expose
    private String created;

    @SerializedName("modified")
    @Expose
    private String modified;

    private boolean isSelected = false;

    public Character() {
    }

    public Character(Parcel source) {
	character_id = source.readString();
	username = source.readString();
	image_name = source.readString();
    }

    public static final Parcelable.Creator<Character> CREATOR = new Parcelable.Creator<Character>() {
	public Character createFromParcel(Parcel source) {
	    return new Character(source);
	}

	public Character[] newArray(int size) {
	    return new Character[size];
	}
    };

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public String getToken_timestamp() {
	return token_timestamp;
    }

    public void setToken_timestamp(String token_timestamp) {
	this.token_timestamp = token_timestamp;
    }

    public String getImage_name() {
	return image_name;
    }

    public void setImage_name(String image_name) {
	this.image_name = image_name;
    }

    public String getAndroid_push_token() {
	return android_push_token;
    }

    public void setAndroid_push_token(String android_push_token) {
	this.android_push_token = android_push_token;
    }

    public String getIos_push_token() {
	return ios_push_token;
    }

    public void setIos_push_token(String ios_push_token) {
	this.ios_push_token = ios_push_token;
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

    public boolean isSelected() {
	return isSelected;
    }

    public void setSelected(boolean isSelected) {
	this.isSelected = isSelected;
    }

    public String getCharacterId() {
	return character_id;
    }

    public void setCharacterId(String character_id) {
	this.character_id = character_id;
    }

    @Override
    public int describeContents() {
	return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
	parcel.writeString(character_id);
	parcel.writeString(username);
	parcel.writeString(image_name);
    }

}
