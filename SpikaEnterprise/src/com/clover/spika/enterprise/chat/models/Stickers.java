package com.clover.spika.enterprise.chat.models;

import java.io.Serializable;

public class Stickers implements Serializable {

	private static final long serialVersionUID = -8648077636719231721L;

	public int id;
	public String filename;
	public int is_deleted;
	public long created;
	public String url;
	public int organization_id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFileName() {
		return filename;
	}

	public void setFileName(String fileName) {
		this.filename = fileName;
	}

	public int getIsDeleted() {
		return is_deleted;
	}

	public boolean isDeleted() {
		return is_deleted == 1 ? true : false;
	}

	public void setIsDeleted(int isDeleted) {
		this.is_deleted = isDeleted;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getOrganizationId() {
		return organization_id;
	}

	public void setOrganizationId(int organizationId) {
		this.organization_id = organizationId;
	}

	@Override
	public String toString() {
		return "Stickers [id=" + id + ", fileName=" + filename + ", isDeleted=" + is_deleted + ", created=" + created + ", url=" + url + ", organizationId=" + organization_id
				+ "]";
	}

}
