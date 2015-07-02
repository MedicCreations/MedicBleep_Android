package com.medicbleep.app.chat.models;

import com.medicbleep.app.chat.extendables.BaseModel;

public class UploadFileModel extends BaseModel {

	public String file_id;

	public UploadFileModel() {
	}

	public String getFileId() {
		return file_id;
	}

	public void setFileId(String fileId) {
		this.file_id = fileId;
	}

}
