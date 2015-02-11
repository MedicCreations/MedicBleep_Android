package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StickersHolder extends BaseModel implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 4080026425457931068L;
	@SerializedName("stickers")
    @Expose
    private List<Stickers> stickersList;

	public List<Stickers> getStickersList() {
		return stickersList;
	}

	public void setStickersList(List<Stickers> stickersList) {
		this.stickersList = stickersList;
	}

}
