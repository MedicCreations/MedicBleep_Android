package com.clover.spika.enterprise.chat.caching.utils;

import java.util.ArrayList;
import java.util.List;

import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.User;

public class DaoUtils {

	public static Chat convertDaoChatToChatModel(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {

		Chat finalChat = new Chat();

		finalChat.id = (int) chat.getId();
		finalChat.chat_id = chat.getChat_id().intValue();
		finalChat.chat_name = chat.getChat_name();
		finalChat.seen_by = chat.getSeen_by();
		finalChat.total_count = chat.getTotal_count();
		finalChat.image_thumb = chat.getImage_thumb();
		finalChat.image = chat.getImage();
		finalChat.admin_id = chat.getAdmin_id();
		finalChat.is_active = chat.getIs_active();
		finalChat.type = chat.getType();
		finalChat.is_private = chat.getIs_private();
		finalChat.password = chat.getPassword();
		finalChat.unread = chat.getUnread();
		finalChat.is_member = chat.getIs_member();
		
		finalChat.chat = finalChat.copyChat(finalChat);

		if (chat.getCategory() != null) {
			finalChat.category = convertDaoCategoryToCategoryModel(chat.getCategory());
		}

		if (chat.getUser() != null) {
			finalChat.user = convertDaoUserToUserModel(chat.getUser());
		}

		if (chat.getMessage() != null) {
			finalChat.last_message = convertDaoMessageToMessageModel(chat.getMessage());
		}
		
		if (chat.getMessageList() != null){
			finalChat.messages = convertDaoListMessage(chat.getMessageList());
		}

		return finalChat;
	}

	private static List<Message> convertDaoListMessage(List<com.clover.spika.enterprise.chat.models.greendao.Message> messageList) {
		List<Message> finalList = new ArrayList<Message>();
		for(com.clover.spika.enterprise.chat.models.greendao.Message item : messageList){
//			Log.i("LOG", "GET FROM DATABASE");
			finalList.add(convertDaoMessageToMessageModel(item));
		}
		return finalList;
	}

	public static Category convertDaoCategoryToCategoryModel(com.clover.spika.enterprise.chat.models.greendao.Category category) {

		Category finalCategory = new Category();

		finalCategory.id = (int) category.getId();
		finalCategory.name = category.getName();

		return finalCategory;
	}

	public static User convertDaoUserToUserModel(com.clover.spika.enterprise.chat.models.greendao.User user) {

		User finalUser = new User();

		finalUser.id = (int) user.getId();
		finalUser.user_id = user.getUser_id().intValue();
		finalUser.firstname = user.getFirstname();
		finalUser.lastname = user.getLastname();
		finalUser.type = user.getType();
		finalUser.image = user.getImage();
		finalUser.image_thumb = user.getImage_thumb();
		finalUser.is_member = user.getIs_member();
		finalUser.is_admin = user.getIs_admin();
		finalUser.name = user.getName();
		finalUser.groupname = user.getGroupname();
		finalUser.chat_id = user.getChat_id();
		finalUser.is_user = user.getIs_user();
		finalUser.is_group = user.getIs_group();
		finalUser.is_room = user.getIs_room();

		return finalUser;
	}

	public static Message convertDaoMessageToMessageModel(com.clover.spika.enterprise.chat.models.greendao.Message message) {

		Message finalMessage = new Message();

		finalMessage.id = String.valueOf(message.getId());
		finalMessage.chat_id = String.valueOf(message.getChat_id());
		finalMessage.user_id = String.valueOf(message.getUser_id());
		finalMessage.firstname = message.getFirstname();
		finalMessage.lastname = message.getLastname();
		finalMessage.image = message.getImage();
		finalMessage.text = message.getText();
		finalMessage.file_id = message.getFile_id();
		finalMessage.thumb_id = message.getThumb_id();
		finalMessage.longitude = message.getLongitude();
		finalMessage.latitude = message.getLatitude();
		finalMessage.created = message.getCreated();
		finalMessage.modified = message.getModified();
		finalMessage.child_list = message.getChild_list();
		finalMessage.image_thumb = message.getImage_thumb();
		finalMessage.type = message.getType();
		finalMessage.root_id = message.getRoot_id();
		finalMessage.parent_id = message.getParent_id();
		finalMessage.isMe = message.getIsMe();
		finalMessage.isFailed = message.getIsFailed();

		return finalMessage;
	}
	
	public static List<Message> converDaoMessagesToMessagesModel(List<com.clover.spika.enterprise.chat.models.greendao.Message> messages) {

		List<Message> newMessList = new ArrayList<Message>();
		for(com.clover.spika.enterprise.chat.models.greendao.Message item : messages){
			newMessList.add(convertDaoMessageToMessageModel(item));
		}

		return newMessList;
	}

	public static Group convertDaoGroupToGroupModel(com.clover.spika.enterprise.chat.models.greendao.Group group) {

		Group finalGroup = new Group();

		finalGroup.id = (int) group.getId();
		finalGroup.type = String.valueOf(group.getType());
		finalGroup.groupname = group.getGroupname();
		finalGroup.image = group.getImage();
		finalGroup.image_thumb = group.getImage_thumb();

		if (group.getCategory() != null) {
			finalGroup.category = convertDaoCategoryToCategoryModel(group.getCategory());
		}

		finalGroup.is_member = group.getIs_member();

		return finalGroup;
	}

}
