package com.clover.spika.enterprise.chat.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

	public int id;
	public String name;

	public Category() {
	}

	public Category(int id, String name) {
		this.id = id;
		this.name = name;
	}

}
