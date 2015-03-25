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
	
	private int usedTimes = 0;

	public Stickers() {

	}

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
	
	public int getUsedTimes() {
		return usedTimes;
	}

	public void setUsedTimes(int usedTimes) {
		this.usedTimes = usedTimes;
	}

	@Override
	public String toString() {
		return "Stickers [id=" + id + ", fileName=" + filename + ", isDeleted=" + is_deleted + ", created=" + created + ", url=" + url + ", organizationId=" + organization_id
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + id;
		result = prime * result + is_deleted;
		result = prime * result + organization_id;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stickers other = (Stickers) obj;
		if(other.id != id) return false;
		if(other. url == null || !other.url.equals(url)) return false;
		if(other. filename == null || !other.filename.equals(filename)) return false;
		
		return true;
	}
	
	

}
