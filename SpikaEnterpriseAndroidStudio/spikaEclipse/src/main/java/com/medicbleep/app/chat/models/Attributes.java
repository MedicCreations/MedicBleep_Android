package com.medicbleep.app.chat.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
class Attributes implements Parcelable {
	private String textType;
	private String encrypted;

	public Attributes() {
	}

	public String getTextType() {
		return textType;
	}

	public void setTextType(String textType) {
		this.textType = textType;
	}
	
	public String getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}

	protected Attributes(Parcel in) {
		textType = in.readString();
		encrypted = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(textType);
		dest.writeString(encrypted);
	}

	public static final Parcelable.Creator<Attributes> CREATOR = new Parcelable.Creator<Attributes>() {
		@Override
		public Attributes createFromParcel(Parcel in) {
			return new Attributes(in);
		}

		@Override
		public Attributes[] newArray(int size) {
			return new Attributes[size];
		}
	};
}