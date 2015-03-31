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
		Entity groups = schema.addEntity("Groups");
		Entity organization = schema.addEntity("Organization");
		Entity user = schema.addEntity("User");
		Entity message = schema.addEntity("Message");
		Entity chat = schema.addEntity("Chat");
		Entity stickers = schema.addEntity("Stickers");
		Entity userDetails = schema.addEntity("UserDetails");
		
		// UserDetails
		userDetails.addIdProperty().autoincrement();
		userDetails.addLongProperty("user_id").notNull();
		userDetails.addStringProperty("key");
		userDetails.addStringProperty("label");
		userDetails.addIntProperty("keyboard_type");
		userDetails.addStringProperty("value");
		userDetails.addIntProperty("public_value");
		
		// Category
		category.addLongProperty("id").notNull().unique().primaryKey();
		category.addStringProperty("name");

		// Group
		groups.addLongProperty("id").notNull().unique().primaryKey();
		groups.addStringProperty("type");
		groups.addStringProperty("groupname");
		groups.addStringProperty("image");
		groups.addStringProperty("image_thumb");
		groups.addIntProperty("is_member");

		// Organization
		organization.addLongProperty("id").notNull().unique().primaryKey();
		organization.addStringProperty("name");

		// User
		user.addLongProperty("id").notNull().unique().primaryKey();
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
		user.addLongProperty("organization_id");

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
