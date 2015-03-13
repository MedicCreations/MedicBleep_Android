package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;

import java.io.Serializable;
import java.util.List;

public class StickersHolder extends BaseModel implements Serializable {

	private static final long serialVersionUID = 4080026425457931068L;

	public List<Stickers> stickers;

	public StickersHolder() {

	}
	
	public void setStickersList(List<Stickers> inStickers){
		this.stickers = inStickers;
	}

}
