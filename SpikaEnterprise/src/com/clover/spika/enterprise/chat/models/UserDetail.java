package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetail implements Serializable{

		@SerializedName("id")
		@Expose
		private int id;

		@SerializedName("key")
		@Expose
		private String key;

		@SerializedName("label")
		@Expose
		private String label;

		@SerializedName("keyboard_type")
		@Expose
		private int keyboard_type;

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

}
