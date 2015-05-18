package com.clover.spika.enterprise.chat.caching.utils;

import java.util.ArrayList;
import java.util.List;

import com.clover.spika.enterprise.chat.models.Category;
import com.clover.spika.enterprise.chat.models.Chat;
import com.clover.spika.enterprise.chat.models.Group;
import com.clover.spika.enterprise.chat.models.Message;
import com.clover.spika.enterprise.chat.models.Organization;
import com.clover.spika.enterprise.chat.models.User;
import com.clover.spika.enterprise.chat.models.UserDetail;
import com.clover.spika.enterprise.chat.models.greendao.UserDetails;

public class DaoUtils {

	public static Chat convertDaoChatToChatModel(com.clover.spika.enterprise.chat.models.greendao.Chat chat) {

		Chat finalChat = new Chat();
		
		finalChat.id = (int) chat.getId();
		finalChat.chat_id = (int) chat.getId();
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

		if (chat.getMessageList() != null) {
			finalChat.messages = convertDaoListMessage(chat.getMessageList());
		}

		return finalChat;
	}

	private static List<Message> convertDaoListMessage(List<com.clover.spika.enterprise.chat.models.greendao.Message> messageList) {
		List<Message> finalList = new ArrayList<Message>();
		for (com.clover.spika.enterprise.chat.models.greendao.Message item : messageList) {
			finalList.add(convertDaoMessageToMessageModel(item));
		}
		return finalList;
	}

	public static Category convertDaoCategoryToCategoryModel(com.clover.spika.enterprise.chat.models.greendao.Category category) {

		Category finalCategory = new Category();

		finalCategory.id = String.valueOf(category.getId());
		finalCategory.name = category.getName();

		return finalCategory;
	}

	public static User convertDaoUserToUserModel(com.clover.spika.enterprise.chat.models.greendao.User user) {

		User finalUser = new User();

		finalUser.id = (int) user.getId();
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
		finalMessage.attributes = message.getAttributes();
        finalMessage.country_code = message.getCountry_code();
		finalMessage.seen_timestamp = message.getSeen_timestamp();

		return finalMessage;
	}

	public static List<Message> converDaoMessagesToMessagesModel(List<com.clover.spika.enterprise.chat.models.greendao.Message> messages) {

		List<Message> newMessList = new ArrayList<Message>();
		for (com.clover.spika.enterprise.chat.models.greendao.Message item : messages) {
			newMessList.add(convertDaoMessageToMessageModel(item));
		}

		return newMessList;
	}

	public static Group convertDaoGroupToGroupModel(com.clover.spika.enterprise.chat.models.greendao.Groups group) {

		Group finalGroup = new Group();

		finalGroup.id = (int) group.getId();
		finalGroup.type = String.valueOf(group.getType());
		finalGroup.groupname = group.getGroupname();
		finalGroup.image = group.getImage();
		finalGroup.image_thumb = group.getImage_thumb();
		finalGroup.is_member = group.getIs_member();

		return finalGroup;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.Chat convertChatModelToChatDao(
			com.clover.spika.enterprise.chat.models.greendao.Chat chatDao, Chat chat, long finalCategoryModelId, long finalUserModelId,
			long finalMessageModelId, boolean isRecent) {

		if (chatDao == null) {

			chatDao = new com.clover.spika.enterprise.chat.models.greendao.Chat(Long.valueOf(chat.getId()), chat.chat_name, chat.seen_by,
					chat.total_count, chat.image_thumb, chat.image, chat.admin_id, chat.is_active, chat.type, chat.is_private, chat.password,
					chat.unread, chat.is_member, chat.modified, isRecent, finalCategoryModelId, finalUserModelId, finalMessageModelId);

		} else {

			chatDao.setId(Long.valueOf(chat.getId()));

			if (chat.chat_name != null) {
				chatDao.setChat_name(chat.chat_name);
			}

			if (chat.seen_by != null) {
				chatDao.setSeen_by(chat.seen_by);
			}

			if ((Integer) chat.total_count != null || chat.total_count != 0) {
				chatDao.setTotal_count(chat.total_count);
			}

			if (chat.image_thumb != null) {
				chatDao.setImage_thumb(chat.image_thumb);
			}

			if (chat.image != null) {
				chatDao.setImage(chat.image);
			}

			if (chat.admin_id != null) {
				chatDao.setAdmin_id(chat.admin_id);
			}

			chatDao.setIs_active(chat.is_active);
			chatDao.setType(chat.type);
			chatDao.setIs_private(chat.is_private);

			if (chat.password != null) {
				chatDao.setPassword(chat.password);
			}

			if (chat.unread != null) {
				chatDao.setUnread(chat.unread);
			}

			chatDao.setIs_member(chat.is_member);

			if (chat.modified != 0L) {
				chatDao.setModified(chat.modified);
			}

			if (finalCategoryModelId != 0L) {
				chatDao.setCategoryId(finalCategoryModelId);
			}

			if (finalUserModelId != 0L) {
				chatDao.setUserIdProperty(finalUserModelId);
			}

			if (finalMessageModelId != 0L) {
				chatDao.setMessageIdProperty(finalMessageModelId);
			}

			chatDao.setIsRecent(isRecent);
		}

		return chatDao;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.Message convertMessageModelToMessageDao(
			com.clover.spika.enterprise.chat.models.greendao.Message messageDao, Message message, int chatId) {

		if (messageDao != null) {

			messageDao.setId(Long.valueOf(message.id));

			if (message.chat_id != null && Long.valueOf(message.chat_id) != 0L) {
				messageDao.setChat_id(Long.valueOf(message.chat_id));
			}

			if (message.user_id != null && Long.valueOf(message.user_id) != 0L) {
				messageDao.setUser_id(Long.valueOf(message.user_id));
			}

			if (message.firstname != null) {
				messageDao.setFirstname(message.firstname);
			}

			if (message.lastname != null) {
				messageDao.setLastname(message.lastname);
			}

			if (message.text != null) {
				messageDao.setText(message.text);
			}

			if (message.file_id != null) {
				messageDao.setFile_id(message.file_id);
			}

			if (message.thumb_id != null) {
				messageDao.setThumb_id(message.thumb_id);
			}

			if (message.longitude != null) {
				messageDao.setLongitude(message.longitude);
			}

			if (message.latitude != null) {
				messageDao.setLongitude(message.latitude);
			}

			if (message.created != null) {
				messageDao.setCreated(message.created);
			}

			if (message.modified != null) {
				messageDao.setModified(message.modified);
			}

			if (message.child_list != null) {
				messageDao.setChild_list(message.child_list);
			}

			if (message.image_thumb != null) {
				messageDao.setImage_thumb(message.image_thumb);
			}

			if (message.image_thumb != null) {
				messageDao.setImage_thumb(message.image_thumb);
			}

			if (message.type != 0) {
				messageDao.setType(message.type);
			}

			if (message.root_id != 0) {
				messageDao.setRoot_id(message.root_id);
			}

			if (message.parent_id != 0) {
				messageDao.setParent_id(message.parent_id);
			}

            if (message.country_code != null) {
                messageDao.setCountry_code(message.country_code);
            }

			if (message.seen_timestamp != 0) {
				messageDao.setSeen_timestamp(message.seen_timestamp);
			}

			messageDao.setIsMe(message.isMe);
			messageDao.setIsFailed(message.isFailed);

			if (chatId != 0) {
				messageDao.setParent_id(chatId);
			}

		} else {
			messageDao = new com.clover.spika.enterprise.chat.models.greendao.Message(Long.valueOf(message.id), Long.valueOf(message.chat_id),
					Long.valueOf(message.user_id), message.firstname, message.lastname, message.image, message.text, message.file_id,
					message.thumb_id, message.longitude, message.latitude, message.created, message.modified, message.child_list,
					message.image_thumb, message.type, message.root_id, message.parent_id, message.isMe, message.isFailed, message.attributes, message.country_code, message.seen_timestamp,
					(long) chatId);
		}

		return messageDao;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.User convertUserModelToUserDao(
			com.clover.spika.enterprise.chat.models.greendao.User userDao, User user) {

		if (userDao != null) {

			userDao.setId((long) user.getId());

			if (user.user_id != 0) {
				userDao.setId((long) user.user_id);
			}

			if (user.firstname != null) {
				userDao.setFirstname(user.firstname);
			}

			if (user.lastname != null) {
				userDao.setLastname(user.lastname);
			}

			if (user.type != 0) {
				userDao.setType(user.type);
			}

			if (user.image != null) {
				userDao.setImage(user.image);
			}

			if (user.image_thumb != null) {
				userDao.setImage_thumb(user.image_thumb);
			}

			userDao.setIs_member(user.is_member);
			userDao.setIs_admin(user.is_admin);

			if (user.name != null) {
				userDao.setName(user.name);
			}

			if (user.groupname != null) {
				userDao.setGroupname(user.groupname);
			}

			if (user.chat_id != null) {
				userDao.setChat_id(user.chat_id);
			}

			if (user.organization != null) {
				userDao.setOrganization_id(Long.valueOf(user.organization.id));
			}

			userDao.setIs_user(user.is_user);
			userDao.setIs_group(user.is_group);
			userDao.setIs_room(user.is_room);

		} else {

			userDao = new com.clover.spika.enterprise.chat.models.greendao.User((long) user.getId(), user.firstname, user.lastname, user.type,
					user.image, user.image_thumb, user.is_member, user.is_admin, user.name, user.groupname, user.chat_id, user.is_user,
					user.is_group, user.is_room, user.organization != null ? Long.valueOf(user.organization.id) : 0L);
		}

		return userDao;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.Organization convertOrganizationModelToOrganizationDao(
			com.clover.spika.enterprise.chat.models.greendao.Organization organizationDao, Organization organization) {

		if (organizationDao != null) {

			if (organization.name != null) {
				organizationDao.setName(organization.name);
			}

		} else {
			organizationDao = new com.clover.spika.enterprise.chat.models.greendao.Organization((Long.valueOf(organization.id)), organization.name);
		}

		return organizationDao;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.Category convertCategoryModelToCategoryDao(
			com.clover.spika.enterprise.chat.models.greendao.Category categoryDao, Category category) {

		if (categoryDao == null) {
			categoryDao = new com.clover.spika.enterprise.chat.models.greendao.Category(Long.valueOf(category.id), category.name);
		}

		return categoryDao;
	}

	public static com.clover.spika.enterprise.chat.models.greendao.Groups convertGroupModelToGroupDao(
			com.clover.spika.enterprise.chat.models.greendao.Groups groupDao, Group group) {

		if (groupDao != null) {

			groupDao.setType(group.type);
			groupDao.setGroupname(group.groupname);
			groupDao.setImage(group.image);
			groupDao.setImage_thumb(group.image_thumb);
			groupDao.setIs_member(group.is_member);

		} else {

			groupDao = new com.clover.spika.enterprise.chat.models.greendao.Groups(group.id, group.type, group.groupname, group.image,
					group.image_thumb, group.is_member);

		}

		return groupDao;
	}

	public static UserDetail convertDaoUserDetailToUserDetailModel(UserDetails detail) {

		UserDetail result = new UserDetail();

		result.key = detail.getKey();
		result.label = detail.getLabel();
		result.keyboard_type = detail.getKeyboard_type() != null ? detail.getKeyboard_type() : 0;
		result.value = detail.getValue();
		result.public_value = detail.getPublic_value();

		return result;
	}

	public static UserDetails convertUserDetailModelToUserDetailDao(UserDetail detail, String userId) {

		UserDetails result = new UserDetails(Long.valueOf(detail.id), Long.valueOf(userId), detail.key, detail.label, detail.keyboard_type,
				detail.value, detail.public_value);

		return result;
	}

	public static Organization convertDaoOrganizationToOrganizationModel(com.clover.spika.enterprise.chat.models.greendao.Organization daoOrganization) {

		Organization result = new Organization();

		if (daoOrganization != null) {
			result.id = String.valueOf(daoOrganization.getId());
			result.name = daoOrganization.getName();
		}

		return result;
	}

}
