package com.clover.spika.enterprise.chat.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.clover.spika.enterprise.chat.utils.Const;
import com.google.gson.Gson;

public class GlobalModel {

	public class Type {
		public static final int USER = 1;
		public static final int GROUP = 2;
		public static final int CHAT = 3;
		public static final int ALL = 4;
	}

	private int type;
	private User user;
	private Group group;
	private Chat chat;

	public GlobalModel() {
	}

	public GlobalModel(JSONObject object) {

		try {
			setType(object.getInt(Const.TYPE));
			setModel(object);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Object getModel() {

		if (getType() == Type.USER) {
			return getUser();
		} else if (getType() == Type.GROUP) {
			return getGroup();
		} else if (getType() == Type.CHAT) {
			return getChat();
		}

		return null;
	}

	public void setModel(JSONObject object) {

		JSONObject model = new JSONObject();

		try {

			if (getType() == Type.USER) {

				model = object.getJSONObject(Const.USER);
				setUser(new Gson().fromJson(model.toString(), User.class));

			} else if (getType() == Type.GROUP) {

				model = object.getJSONObject(Const.GROUP);
				setGroup(new Gson().fromJson(model.toString(), Group.class));

			} else if (getType() == Type.CHAT) {

				model = object.getJSONObject(Const.CHAT);
				setChat(new Gson().fromJson(model.toString(), Chat.class));

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getId() {

		if (getType() == Type.USER) {
			return getUser().getId();
		} else if (getType() == Type.GROUP) {
			return getGroup().getId();
		} else if (getType() == Type.CHAT) {
			return getChat().getChat_id();
		}

		return -1;
	}

	public boolean getSelected() {

		if (getType() == Type.USER) {
			return getUser().isSelected();
		} else if (getType() == Type.GROUP) {
			return getGroup().isSelected();
		} else if (getType() == Type.CHAT) {
			return getChat().isSelected();
		}

		return false;
	}

	public void setSelected(boolean isSelected) {

		if (getType() == Type.USER) {
			getUser().setSelected(isSelected);
		} else if (getType() == Type.GROUP) {
			getGroup().setSelected(isSelected);
		} else if (getType() == Type.CHAT) {
			getChat().setSelected(isSelected);
		}
	}

	public boolean isMember() {

		if (getType() == Type.USER) {
			return getUser().isMember();
		} else if (getType() == Type.GROUP) {
			return getGroup().isMember();
		} else if (getType() == Type.CHAT) {
			return getChat().isMember();
		}

		return false;
	}

	public String getImageThumb() {

		if (getType() == Type.USER) {
			return getUser().getImageThumb();
		} else if (getType() == Type.GROUP) {
			return getGroup().getImage_thumb();
		} else if (getType() == Type.CHAT) {
			return getChat().getImageThumb();
		}

		return null;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	private User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	private Group getGroup() {
		return group;
	}

	private void setGroup(Group group) {
		this.group = group;
	}

	private Chat getChat() {
		return chat;
	}

	private void setChat(Chat chat) {
		this.chat = chat;
	}
}
