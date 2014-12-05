package com.clover.spika.enterprise.chat.models;

import java.util.List;

public class GlobalResponse {

	private int code;
	private int page;
	private int totalCount;
	private List<GlobalModel> modelsList;

	public GlobalResponse() {
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<GlobalModel> getModelsList() {
		return modelsList;
	}

	public void setModelsList(List<GlobalModel> modelsList) {
		this.modelsList = modelsList;
	}

}
