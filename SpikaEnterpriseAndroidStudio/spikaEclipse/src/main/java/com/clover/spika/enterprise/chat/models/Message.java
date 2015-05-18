package com.clover.spika.enterprise.chat.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.security.JNAesCrypto;
import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	public String attributes;
    public String country_code;
	public int seen_timestamp;

	private int textWidth = -1;
	private int timeWidth = -1;
	private boolean isUserExpandContent = false;
	private boolean isTextCodeStyle = false;

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
	
	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	
	public void setIsCodeTextStyle(){
		if(TextUtils.isEmpty(attributes)){
			isTextCodeStyle = false;
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			Attributes result = mapper.readValue(attributes, Attributes.class);
			if(result.getTextType() == null){
				isTextCodeStyle = false;
				return;
			}else if(result.getTextType().equals("code")){
				isTextCodeStyle = true;
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		isTextCodeStyle = false;
	}
	
	public boolean getIsCodeTextStyle(){
		return isTextCodeStyle;
	}
	
	public boolean isUserExpandContent() {
		return isUserExpandContent;
	}

	public void setUserExpandContent(boolean isUserExpandContent) {
		this.isUserExpandContent = isUserExpandContent;
	}
	
	public boolean isEncrypted(){
		if(TextUtils.isEmpty(attributes)){
			return true;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			Attributes result = mapper.readValue(attributes, Attributes.class);
			if(result.getEncrypted().equals("0")){
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((chat_id == null) ? 0 : chat_id.hashCode());
		result = prime * result + ((child_list == null) ? 0 : child_list.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((file_id == null) ? 0 : file_id.hashCode());
		result = prime * result + ((firstname == null) ? 0 : firstname.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + ((image_thumb == null) ? 0 : image_thumb.hashCode());
		result = prime * result + (isFailed ? 1231 : 1237);
		result = prime * result + (isMe ? 1231 : 1237);
		result = prime * result + ((lastname == null) ? 0 : lastname.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((modified == null) ? 0 : modified.hashCode());
		result = prime * result + parent_id;
		result = prime * result + root_id;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + textWidth;
		result = prime * result + ((thumb_id == null) ? 0 : thumb_id.hashCode());
		result = prime * result + timeWidth;
		result = prime * result + type;
		result = prime * result + ((user_id == null) ? 0 : user_id.hashCode());
		return result;
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
		if (type != message.type)
			return false;
		
		return true;
	}


	@Override
	public String toString() {
		return "Message{" +
				"isMe=" + isMe +
				", isFailed=" + isFailed +
				", id='" + id + '\'' +
				", chat_id='" + chat_id + '\'' +
				", user_id='" + user_id + '\'' +
				", firstname='" + firstname + '\'' +
				", lastname='" + lastname + '\'' +
				", image='" + image + '\'' +
				", text='" + text + '\'' +
				", file_id='" + file_id + '\'' +
				", thumb_id='" + thumb_id + '\'' +
				", longitude='" + longitude + '\'' +
				", latitude='" + latitude + '\'' +
				", type=" + type +
				", created='" + created + '\'' +
				", modified='" + modified + '\'' +
				", root_id=" + root_id +
				", parent_id=" + parent_id +
				", child_list='" + child_list + '\'' +
				", image_thumb='" + image_thumb + '\'' +
				", attributes='" + attributes + '\'' +
				", country_code='" + country_code + '\'' +
				", seen_timestamp=" + seen_timestamp +
				", textWidth=" + textWidth +
				", timeWidth=" + timeWidth +
				", isUserExpandContent=" + isUserExpandContent +
				", isTextCodeStyle=" + isTextCodeStyle +
				'}';
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
        return 0;
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
        dest.writeString(this.image_thumb);
        dest.writeString(this.attributes);
        dest.writeString(this.country_code);
		dest.writeInt(seen_timestamp);
        dest.writeInt(this.textWidth);
		dest.writeInt(this.timeWidth);
        dest.writeByte(isUserExpandContent ? (byte) 1 : (byte) 0);
		dest.writeByte(isTextCodeStyle ? (byte) 1 : (byte) 0);
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
        this.parent_id = in.readInt();
        this.child_list = in.readString();
        this.image_thumb = in.readString();
        this.attributes = in.readString();
        this.country_code = in.readString();
		this.seen_timestamp = in.readInt();
        this.textWidth = in.readInt();
        this.timeWidth = in.readInt();
        this.isUserExpandContent = in.readByte() != 0;
        this.isTextCodeStyle = in.readByte() != 0;
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