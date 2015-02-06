package com.clover.spika.enterprise.chat.models;

import java.util.List;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PreLogin extends BaseModel {

	@SerializedName("organizations")
	@Expose
	private List<Organization> _organizations;

	public List<Organization> getOrganizations() {
		return _organizations;
	}

	public void setOrganizations(List<Organization> organizations) {
		this._organizations = organizations;
	}

}
