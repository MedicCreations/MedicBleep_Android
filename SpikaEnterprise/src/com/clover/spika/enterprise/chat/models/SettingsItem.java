package com.clover.spika.enterprise.chat.models;

public class SettingsItem {

	private String name;
	private boolean isDisabled = false;

	public SettingsItem(String name, boolean isDisabled) {
		this.name = name;
		this.isDisabled = isDisabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

}
