package com.clover.spika.enterprise.chat.extendables;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseModel {

	public String message;
	public int code;

	public BaseModel() {
	}

	public String getMessage() {
		return message == null ? "" : message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		BaseModel baseModel = (BaseModel) o;

		if (code != baseModel.code)
			return false;
		if (message != null ? !message.equals(baseModel.message) : baseModel.message != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = message != null ? message.hashCode() : 0;
		result = 31 * result + code;
		return result;
	}

	@Override
	public String toString() {
		return "BaseModel{" +
				"message='" + message + '\'' +
				", code=" + code +
				'}';
	}
}
