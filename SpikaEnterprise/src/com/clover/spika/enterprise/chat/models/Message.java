package com.clover.spika.enterprise.chat.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable {

	private boolean isMe = false;
	private boolean isFailed = false;

	@SerializedName("id")
	private String id;

	@SerializedName("chat_id")
	private String chat_id;

	@SerializedName("user_id")
	private String user_id;

	@SerializedName("firstname")
	private String firstname;

	@SerializedName("lastname")
	private String lastname;

	@SerializedName("image")
	private String image;

	@SerializedName("text")
	private String text;

	@SerializedName("file_id")
	private String file_id;

	@SerializedName("thumb_id")
	private String thumb_id;

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

	@SerializedName("root_id")
	private int rootId;

	@SerializedName("parent_id")
	private int parentId;

	@SerializedName("child_list")
	private String childListText;

	public Message() {
	}

	public String getId() {
		return id;
	}

	public int getIntegerId() {
		return Integer.parseInt(id);
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

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getName() {
		// TODO: genereate "preferences" where user can check to either use
		// first-last or last-first
		return firstname + " " + lastname;
	}

	public boolean isMe() {
		return isMe;
	}

	public void setMe(boolean isMe) {
		this.isMe = isMe;
	}

	public String getThumb_id() {
		return thumb_id;
	}

	public void setThumb_id(String thumb_id) {
		this.thumb_id = thumb_id;
	}

	public boolean isFailed() {
		return isFailed;
	}

	public void setFailed(boolean isFailed) {
		this.isFailed = isFailed;
	}

	public int getRootId() {
		return rootId;
	}

	public int getParentId() {
		return parentId;
	}

	public String getChildListText() {
		return childListText;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Message message = (Message) o;

		if (isFailed != message.isFailed)
			return false;
		if (isMe != message.isMe)
			return false;
		if (parentId != message.parentId)
			return false;
		if (rootId != message.rootId)
			return false;
		if (type != message.type)
			return false;
		if (chat_id != null ? !chat_id.equals(message.chat_id) : message.chat_id != null)
			return false;
		if (childListText != null ? !childListText.equals(message.childListText) : message.childListText != null)
			return false;
		if (created != null ? !created.equals(message.created) : message.created != null)
			return false;
		if (file_id != null ? !file_id.equals(message.file_id) : message.file_id != null)
			return false;
		if (firstname != null ? !firstname.equals(message.firstname) : message.firstname != null)
			return false;
		if (id != null ? !id.equals(message.id) : message.id != null)
			return false;
		if (image != null ? !image.equals(message.image) : message.image != null)
			return false;
		if (lastname != null ? !lastname.equals(message.lastname) : message.lastname != null)
			return false;
		if (latitude != null ? !latitude.equals(message.latitude) : message.latitude != null)
			return false;
		if (longitude != null ? !longitude.equals(message.longitude) : message.longitude != null)
			return false;
		if (modified != null ? !modified.equals(message.modified) : message.modified != null)
			return false;
		if (text != null ? !text.equals(message.text) : message.text != null)
			return false;
		if (thumb_id != null ? !thumb_id.equals(message.thumb_id) : message.thumb_id != null)
			return false;
		if (user_id != null ? !user_id.equals(message.user_id) : message.user_id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (isMe ? 1 : 0);
		result = 31 * result + (isFailed ? 1 : 0);
		result = 31 * result + (id != null ? id.hashCode() : 0);
		result = 31 * result + (chat_id != null ? chat_id.hashCode() : 0);
		result = 31 * result + (user_id != null ? user_id.hashCode() : 0);
		result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
		result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
		result = 31 * result + (image != null ? image.hashCode() : 0);
		result = 31 * result + (text != null ? text.hashCode() : 0);
		result = 31 * result + (file_id != null ? file_id.hashCode() : 0);
		result = 31 * result + (thumb_id != null ? thumb_id.hashCode() : 0);
		result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
		result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
		result = 31 * result + type;
		result = 31 * result + (created != null ? created.hashCode() : 0);
		result = 31 * result + (modified != null ? modified.hashCode() : 0);
		result = 31 * result + rootId;
		result = 31 * result + parentId;
		result = 31 * result + (childListText != null ? childListText.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Message{" + "isMe=" + isMe + ", isFailed=" + isFailed + ", id='" + id + '\'' + ", chat_id='" + chat_id + '\'' + ", user_id='" + user_id + '\'' + ", firstname='"
				+ firstname + '\'' + ", lastname='" + lastname + '\'' + ", image='" + image + '\'' + ", text='" + text + '\'' + ", file_id='" + file_id + '\'' + ", thumb_id='"
				+ thumb_id + '\'' + ", longitude='" + longitude + '\'' + ", latitude='" + latitude + '\'' + ", type=" + type + ", created='" + created + '\'' + ", modified='"
				+ modified + '\'' + ", rootId=" + rootId + ", parentId=" + parentId + ", childListText='" + childListText + '\'' + '}';
	}

	public static Message decryptContent(Context ctx, Message msg) {

		switch (msg.getType()) {

		case Const.MSG_TYPE_DEFAULT:
		case Const.MSG_TYPE_FILE:

			try {
				msg.setText(JNAesCrypto.decryptJN(msg.getText()));
			} catch (Exception e) {
				if (Const.DEBUG_CRYPTO)
					e.printStackTrace();
				msg.setText(ctx.getResources().getString(R.string.e_error_not_decrypted));
				msg.setFailed(true);
			}

			break;

		case Const.MSG_TYPE_LOCATION:

			try {
				msg.setLongitude(JNAesCrypto.decryptJN(msg.getLongitude()));
				msg.setLatitude(JNAesCrypto.decryptJN(msg.getLatitude()));
			} catch (Exception e) {
				if (Const.DEBUG_CRYPTO)
					e.printStackTrace();
				msg.setText(ctx.getResources().getString(R.string.e_error_not_decrypted));
				msg.setFailed(true);
			}

			break;

		default:
			break;
		}

		return msg;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(isMe ? (byte) 1 : (byte) 0);
		dest.writeByte(isFailed ? (byte) 1 : (byte) 0);
		dest.writeString(this.id);
		dest.writeString(this.chat_id);
		dest.writeString(this.user_id);
		dest.writeString(this.firstname);
		dest.writeString(this.lastname);
		dest.writeString(this.image);
		dest.writeString(this.text);
		dest.writeString(this.file_id);
		dest.writeString(this.thumb_id);
		dest.writeString(this.longitude);
		dest.writeString(this.latitude);
		dest.writeInt(this.type);
		dest.writeString(this.created);
		dest.writeString(this.modified);
		dest.writeInt(this.rootId);
		dest.writeInt(this.parentId);
		dest.writeString(this.childListText);
	}

	private Message(Parcel in) {
		this.isMe = in.readByte() != 0;
		this.isFailed = in.readByte() != 0;
		this.id = in.readString();
		this.chat_id = in.readString();
		this.user_id = in.readString();
		this.firstname = in.readString();
		this.lastname = in.readString();
		this.image = in.readString();
		this.text = in.readString();
		this.file_id = in.readString();
		this.thumb_id = in.readString();
		this.longitude = in.readString();
		this.latitude = in.readString();
		this.type = in.readInt();
		this.created = in.readString();
		this.modified = in.readString();
		this.rootId = in.readInt();
		this.parentId = in.readInt();
		this.childListText = in.readString();
	}

	public static final Creator<Message> CREATOR = new Creator<Message>() {
		public Message createFromParcel(Parcel source) {
			return new Message(source);
		}

		public Message[] newArray(int size) {
			return new Message[size];
		}
	};
}