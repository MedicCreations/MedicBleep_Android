package com.clover.spika.enterprise.chat.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message implements Parcelable {
	
	public boolean isMe = false;
	public boolean isFailed = false;
	public String id;
	public String chat_id;
	public String user_id;
	public String firstname;
	public String lastname;
	public String image;
	public String text;
	public String file_id;
	public String thumb_id;
	public String longitude;
	public String latitude;
	public int type;
	public String created;
	public String modified;
	public int root_id;
	public int parent_id;
	public String child_list;
	public String image_thumb;

	private int textWidth = -1;
	private int timeWidth = -1;

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
		return root_id;
	}

	public int getParentId() {
		return parent_id;
	}

	public String getChildListText() {
		return child_list;
	}

	public String getImageThumb() {
		return image_thumb;
	}

	public void setImageThumb(String imageThumb) {
		this.image_thumb = imageThumb;
	}

	public int getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
	}

	public int getTimeWidth() {
		return timeWidth;
	}

	public void setTimeWidth(int timeWidth) {
		this.timeWidth = timeWidth;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Message message = (Message) o;

		if (created != null ? !created.equals(message.created) : message.created != null)
			return false;
		if (id != null ? !id.equals(message.id) : message.id != null)
			return false;
		if (modified != null ? !modified.equals(message.modified) : message.modified != null)
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
		result = 31 * result + root_id;
		result = 31 * result + parent_id;
		result = 31 * result + (child_list != null ? child_list.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Message{" + "isMe=" + isMe + ", isFailed=" + isFailed + ", id='" + id + '\'' + ", chat_id='" + chat_id + '\'' + ", user_id='" + user_id + '\'' + ", firstname='"
				+ firstname + '\'' + ", lastname='" + lastname + '\'' + ", image='" + image + '\'' + ", text='" + text + '\'' + ", file_id='" + file_id + '\'' + ", thumb_id='"
				+ thumb_id + '\'' + ", longitude='" + longitude + '\'' + ", latitude='" + latitude + '\'' + ", type=" + type + ", created='" + created + '\'' + ", modified='"
				+ modified + '\'' + ", rootId=" + root_id + ", parentId=" + parent_id + ", childListText='" + child_list + '\'' + '}';
	}

	public static Message decryptContent(Context ctx, Message msg) {

		switch (msg.getType()) {

		case Const.MSG_TYPE_DEFAULT:
		case Const.MSG_TYPE_GIF:
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
		dest.writeInt(this.root_id);
		dest.writeInt(this.parent_id);
		dest.writeString(this.child_list);
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
		this.root_id = in.readInt();
		this.root_id = in.readInt();
		this.child_list = in.readString();
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