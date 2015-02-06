package com.clover.spika.enterprise.chat.extendables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseModel {

	@SerializedName("message")
	@Expose
	private String _message;

	@SerializedName("code")
	@Expose
	private int _code;

	public String getMessage() {
		return _message == null ? "" : _message;
	}

	public void setMessage(String message) {
		this._message = message;
	}

	public int getCode() {
		return _code;
	}

	public void setCode(int code) {
		this._code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		BaseModel baseModel = (BaseModel) o;

		if (_code != baseModel._code)
			return false;
		if (_message != null ? !_message.equals(baseModel._message) : baseModel._message != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = _message != null ? _message.hashCode() : 0;
		result = 31 * result + _code;
		return result;
	}

}
