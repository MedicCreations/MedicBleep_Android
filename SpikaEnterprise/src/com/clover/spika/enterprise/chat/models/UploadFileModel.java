package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

public class UploadFileModel extends BaseModel {

	private String file_id;

	public UploadFileModel() {
	}

	public String getFileId() {
		return file_id;
	}

	public void setFileId(String fileId) {
		this.file_id = fileId;
	}

}
