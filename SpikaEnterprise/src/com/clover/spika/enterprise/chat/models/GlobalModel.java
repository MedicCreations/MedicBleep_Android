package com.clover.spika.enterprise.chat.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.clover.spika.enterprise.chat.utils.Const;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalModel {

	public class Type {
		public static final int USER = 1;
		public static final int GROUP = 2;
		public static final int CHAT = 3;
		public static final int ALL = 4;
	}

	public int type;
	public User user;
	public Group group;
	public Chat chat;

	public GlobalModel() {
	}

	public GlobalModel(JSONObject object) {

		try {
			type = object.getInt(Const.TYPE);
			setModel(object);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Object getModel() {

		if (type == Type.USER) {
			return user;
		} else if (type == Type.GROUP) {
			return group;
		} else if (type == Type.CHAT) {
			return chat;
		}

		return null;
	}

	public void setModel(JSONObject object) {

		JSONObject model = new JSONObject();

		try {

			if (type == Type.USER) {
				
				model = object.getJSONObject(Const.USER);
				user = new ObjectMapper().readValue(model.toString(), User.class);

			} else if (type == Type.GROUP) {

				model = object.getJSONObject(Const.GROUP);
				group = new ObjectMapper().readValue(model.toString(), Group.class);

			} else if (type == Type.CHAT) {

				model = object.getJSONObject(Const.CHAT);
				chat = new ObjectMapper().readValue(model.toString(), Chat.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getId() {

		if (type == Type.USER) {
			return user.getId();
		} else if (type == Type.GROUP) {
			return group.getId();
		} else if (type == Type.CHAT) {
			return chat.getId();
		}

		return -1;
	}

	public boolean getSelected() {

		if (type == Type.USER) {
			return user.isSelected();
		} else if (type == Type.GROUP) {
			return group.isSelected();
		} else if (type == Type.CHAT) {
			return chat.isSelected;
		}

		return false;
	}

	public void setSelected(boolean isSelected) {

		if (type == Type.USER) {
			user.setSelected(isSelected);
		} else if (type == Type.GROUP) {
			group.setSelected(isSelected);
		} else if (type == Type.CHAT) {
			chat.isSelected = isSelected;
		}
	}

	public boolean isMember() {

		if (type == Type.USER) {
			return user.isMember();
		} else if (type == Type.GROUP) {
			return group.isMember();
		} else if (type == Type.CHAT) {
			return chat.isMember();
		}

		return false;
	}

	public String getImageThumb() {

		if (type == Type.USER) {
			return user.image_thumb;
		} else if (type == Type.GROUP) {
			return group.image_thumb;
		} else if (type == Type.CHAT) {
			return chat.image_thumb;
		}

		return "";
	}

}
