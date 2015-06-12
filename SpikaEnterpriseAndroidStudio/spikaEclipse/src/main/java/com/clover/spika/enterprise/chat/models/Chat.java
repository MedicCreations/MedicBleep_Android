package com.clover.spika.enterprise.chat.models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.clover.spika.enterprise.chat.R;
import com.clover.spika.enterprise.chat.extendables.BaseModel;

public class Chat extends BaseModel implements Parcelable{

	public Chat chat;
	public int id;
	public int chat_id;
	public String chat_name;
	public String seen_by;
	public int total_count;
	public List<Message> messages;
	public User user;
	public String image_thumb;
	public String image;
	public String admin_id;
	public int is_active;
	public int type;
	public int is_private;
	public String password;
	public String unread;
	public Category category;
	public int is_member;
	public Message last_message;
	public long modified;

	public boolean isSelected = false;
	public boolean isNewMsg = false;
	public boolean isRefresh = false;
	public boolean isClear = false;
	public boolean isSend = false;
	public boolean isPagging = false;
	public int adapterCount = -1;

    private String timestampFormated = null;

	public Chat() {
	}

	public int getId() {

		if (chat_id == 0) {
			return id;
		} else {
			return chat_id;
		}
	}

    public String getTimeLastMessage(Resources res){
        if(!TextUtils.isEmpty(timestampFormated)){
            return timestampFormated;
        }else{
            if(last_message == null && last_message.created != null){
                return "";
            }else{
                timestampFormated = formatTime(Long.valueOf(last_message.created), res);
                return timestampFormated;
            }
        }
    }

    private String formatTime(long time, Resources res){
        long currentTime = System.currentTimeMillis();

        long currentTimeDay = currentTime / 86400000;
        long timeDay = time / 86400;
        if(currentTimeDay == timeDay){
            return justTime(time);
        }else{
            long offset = currentTimeDay - timeDay;
            if(offset == 1){
                return res.getString(R.string.yesterday);
            }else{
                return offset + " " + res.getString(R.string._days_ago);
            }
        }

    }

    private String justTime(long time){
        try {

            Timestamp stamp = new Timestamp(time * 1000);
            Date date = new Date(stamp.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
	
	public boolean isMember(){
		return this.is_member == 0 ? false : true;
	}

	@Override
	public String toString() {
		return "Chat [chat=" + chat + ", id=" + id + ", chat_id=" + chat_id + ", chat_name=" + chat_name + ", seen_by=" + seen_by + ", total_count=" + total_count + ", messages=" + messages
				+ ", user=" + user + ", image_thumb=" + image_thumb + ", image=" + image + ", admin_id=" + admin_id + ", is_active=" + is_active + ", type=" + type + ", is_private=" + is_private
				+ ", password=" + password + ", unread=" + unread + ", category=" + category + ", is_member=" + is_member + ", last_message=" + last_message + ", modified=" + modified
				+ ", isSelected=" + isSelected + ", isNewMsg=" + isNewMsg + ", isRefresh=" + isRefresh + ", isClear=" + isClear + ", isSend=" + isSend + ", isPagging=" + isPagging + ", adapterCount="
				+ adapterCount + "]";
	}
	
	public Chat copyChat(Chat toCopy){
		Chat chat = new Chat();
		
		chat.id = toCopy.id;
		chat.chat_id = toCopy.chat_id;
		chat.chat_name = toCopy.chat_name;
		chat.seen_by = toCopy.seen_by;
		chat.total_count = toCopy.total_count;
		chat.image_thumb = toCopy.image_thumb;
		chat.image = toCopy.image;
		chat.admin_id = toCopy.admin_id;
		chat.is_active = toCopy.is_active;
		chat.type = toCopy.type;
		chat.is_private = toCopy.is_private;
		chat.password = toCopy.password;
		chat.unread = toCopy.unread;
		chat.is_member = toCopy.is_member;
		chat.modified = toCopy.modified;
		
		return chat;
	}
	
	protected Chat(Parcel in) {
        chat = (Chat) in.readValue(Chat.class.getClassLoader());
        id = in.readInt();
        chat_id = in.readInt();
        chat_name = in.readString();
        seen_by = in.readString();
        total_count = in.readInt();
        if (in.readByte() == 0x01) {
            messages = new ArrayList<Message>();
            in.readList(messages, Message.class.getClassLoader());
        } else {
            messages = null;
        }
        user = (User) in.readValue(User.class.getClassLoader());
        image_thumb = in.readString();
        image = in.readString();
        admin_id = in.readString();
        is_active = in.readInt();
        type = in.readInt();
        is_private = in.readInt();
        password = in.readString();
        unread = in.readString();
        category = (Category) in.readValue(Category.class.getClassLoader());
        is_member = in.readInt();
        last_message = (Message) in.readValue(Message.class.getClassLoader());
        modified = in.readLong();
        isSelected = in.readByte() != 0x00;
        isNewMsg = in.readByte() != 0x00;
        isRefresh = in.readByte() != 0x00;
        isClear = in.readByte() != 0x00;
        isSend = in.readByte() != 0x00;
        isPagging = in.readByte() != 0x00;
        adapterCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(chat);
        dest.writeInt(id);
        dest.writeInt(chat_id);
        dest.writeString(chat_name);
        dest.writeString(seen_by);
        dest.writeInt(total_count);
        if (messages == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(messages);
        }
        dest.writeValue(user);
        dest.writeString(image_thumb);
        dest.writeString(image);
        dest.writeString(admin_id);
        dest.writeInt(is_active);
        dest.writeInt(type);
        dest.writeInt(is_private);
        dest.writeString(password);
        dest.writeString(unread);
        dest.writeValue(category);
        dest.writeInt(is_member);
        dest.writeValue(last_message);
        dest.writeLong(modified);
        dest.writeByte((byte) (isSelected ? 0x01 : 0x00));
        dest.writeByte((byte) (isNewMsg ? 0x01 : 0x00));
        dest.writeByte((byte) (isRefresh ? 0x01 : 0x00));
        dest.writeByte((byte) (isClear ? 0x01 : 0x00));
        dest.writeByte((byte) (isSend ? 0x01 : 0x00));
        dest.writeByte((byte) (isPagging ? 0x01 : 0x00));
        dest.writeInt(adapterCount);
    }

    public static final Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
}
	
