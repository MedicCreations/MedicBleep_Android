package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;

public class UserDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	public int id;
	public String key;
	public String label;
	public int keyboard_type;
	public String value;
	public boolean public_value;

	private int position;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getKeyboardType() {
		return keyboard_type;
	}

	public void setKeyboardType(int keyboardType) {
		this.keyboard_type = keyboardType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isPublicValue() {
		return public_value;
	}

	public void setPublicValue(boolean publicValue) {
		this.public_value = publicValue;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
