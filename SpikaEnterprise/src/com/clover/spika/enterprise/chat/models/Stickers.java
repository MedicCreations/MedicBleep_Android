package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stickers implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8648077636719231721L;

	@SerializedName("id")
	@Expose
	private int id;

	@SerializedName("filename")
	@Expose
	private String fileName;
	
	@SerializedName("is_deleted")
	@Expose
	private int isDeleted;
	
	@SerializedName("created")
	@Expose
	private long created;
	
	@SerializedName("url")
	@Expose
	private String url;
	
	@SerializedName("organization_id")
	@Expose
	private int organizationId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getIsDeleted() {
		return isDeleted;
	}
	
	public boolean isDeleted() {
		return isDeleted == 1 ? true : false;
	}

	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	@Override
	public String toString() {
		return "Stickers [id=" + id + ", fileName=" + fileName + ", isDeleted=" + isDeleted + ", created=" + created + ", url=" + url + ", organizationId=" + organizationId + "]";
	}
	
	
	
}
