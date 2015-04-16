package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalResponse {

	public int code;
	public int page;
	public int total_count;
	public List<GlobalModel> search_result;

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
		return total_count;
	}

	public void setTotalCount(int totalCount) {
		this.total_count = totalCount;
	}

	public List<GlobalModel> getModelsList() {
		return search_result;
	}

	public void setModelsList(List<GlobalModel> modelsList) {
		this.search_result = modelsList;
	}

}
