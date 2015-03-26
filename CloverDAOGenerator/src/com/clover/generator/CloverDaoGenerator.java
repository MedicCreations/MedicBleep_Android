package com.clover.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class CloverDaoGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Schema schema = new Schema(32, "com.clover.spika.enterprise.chat.models.greendao");
		schema.enableKeepSectionsByDefault();

		generateTables(schema);

		new DaoGenerator().generateAll(schema, "./clover/models/");
	}

	/**
	 * Creation of entities and relationships between them.
	 * 
	 * @param schema
	 */
	private static void generateTables(Schema schema) {
		
		Entity category = schema.addEntity("Category");
		Entity group = schema.addEntity("Group");
		Entity organization = schema.addEntity("Organization");
		Entity listUserDetails = schema.addEntity("ListUserDetails");
		Entity mapKeyValueUserDetails = schema.addEntity("MapKeyValueUserDetails");
		Entity user = schema.addEntity("User");
		Entity message = schema.addEntity("Message");
		Entity chat = schema.addEntity("Chat");
		Entity stickers = schema.addEntity("Stickers");

		// Category
		category.addLongProperty("id").notNull().unique().primaryKey();
		category.addStringProperty("name");

		// Group
		group.addLongProperty("id").notNull().unique().primaryKey();
		group.addStringProperty("type");
		group.addStringProperty("groupname");
		group.addStringProperty("image");
		group.addStringProperty("image_thumb");
		group.addIntProperty("is_member");

		Property categoryIdProperty = group.addLongProperty("categoryId").getProperty();
		group.addToOne(category, categoryIdProperty);
		
		// Organization
		organization.addLongProperty("id").notNull().unique().primaryKey();
		organization.addStringProperty("name");

		// UserDetails in User
		// List
		listUserDetails.addIdProperty();
		// Map
		mapKeyValueUserDetails.addIdProperty();
		mapKeyValueUserDetails.addStringProperty("key");
		mapKeyValueUserDetails.addStringProperty("value");
		
		Property mapKeyValueUserDetailsProperty = mapKeyValueUserDetails.addLongProperty("listId").notNull().getProperty();

		mapKeyValueUserDetails.addToOne(listUserDetails, mapKeyValueUserDetailsProperty);
		listUserDetails.addToMany(mapKeyValueUserDetails, mapKeyValueUserDetailsProperty);
		
		listUserDetails.implementsSerializable();

		// User
		user.addLongProperty("id").notNull().unique().primaryKey();
		user.addLongProperty("user_id");
		user.addStringProperty("firstname");
		user.addStringProperty("lastname");
		user.addIntProperty("type");
		user.addStringProperty("image");
		user.addStringProperty("image_thumb");
		user.addBooleanProperty("is_member");
		user.addIntProperty("is_admin");
		user.addStringProperty("name");
		user.addStringProperty("groupname");
		user.addStringProperty("chat_id");
		user.addIntProperty("is_user");
		user.addIntProperty("is_group");
		user.addIntProperty("is_room");

//		Property organizationIdProperty = group.addLongProperty("organizationId").getProperty();
//		user.addToOne(organization, organizationIdProperty);
//
//		Property userDetailsIdProperty = group.addLongProperty("userDetailsId").getProperty();
//		user.addToOne(listUserDetails, userDetailsIdProperty);

		// Message
		message.addLongProperty("id").notNull().unique().primaryKey();
		message.addLongProperty("chat_id");
		message.addLongProperty("user_id");
		message.addStringProperty("firstname");
		message.addStringProperty("lastname");
		message.addStringProperty("image");
		message.addStringProperty("text");
		message.addStringProperty("file_id");
		message.addStringProperty("thumb_id");
		message.addStringProperty("longitude");
		message.addStringProperty("latitude");
		message.addStringProperty("created");
		message.addStringProperty("modified");
		message.addStringProperty("child_list");
		message.addStringProperty("image_thumb");
		message.addIntProperty("type");
		message.addIntProperty("root_id");
		message.addIntProperty("parent_id");
		message.addBooleanProperty("isMe");
		message.addBooleanProperty("isFailed");
		
		Property chatIdProperty = message.addLongProperty("chatIdProperty").getProperty();

		message.addToOne(chat, chatIdProperty);

		// Chat
		chat.addLongProperty("id").notNull().unique().primaryKey();
		chat.addLongProperty("chat_id");
		chat.addStringProperty("chat_name");
		chat.addStringProperty("seen_by");
		chat.addIntProperty("total_count");
		chat.addStringProperty("image_thumb");
		chat.addStringProperty("image");
		chat.addStringProperty("admin_id");
		chat.addIntProperty("is_active");
		chat.addIntProperty("type");
		chat.addIntProperty("is_private");
		chat.addStringProperty("password");
		chat.addStringProperty("unread");
		chat.addIntProperty("is_member");
		chat.addLongProperty("modified");
		chat.addBooleanProperty("isRecent");

		Property categoryIdPropertyChat = chat.addLongProperty("categoryId").getProperty();
		chat.addToOne(category, categoryIdPropertyChat);
		
		Property userIdPropertyChat = chat.addLongProperty("userIdProperty").getProperty();
		chat.addToOne(user, userIdPropertyChat);
		
		Property messageIdPropertyChat = chat.addLongProperty("messageIdProperty").getProperty();
		chat.addToOne(message, messageIdPropertyChat);
		
		chat.addToMany(message, chatIdProperty);
		
		//Stickers
		stickers.addLongProperty("id").notNull().unique().primaryKey();
		stickers.addStringProperty("filename");
		stickers.addIntProperty("is_deleted");
		stickers.addLongProperty("created");
		stickers.addStringProperty("url");
		stickers.addIntProperty("organization_id");
		stickers.addIntProperty("usedTimes");
	}

}
