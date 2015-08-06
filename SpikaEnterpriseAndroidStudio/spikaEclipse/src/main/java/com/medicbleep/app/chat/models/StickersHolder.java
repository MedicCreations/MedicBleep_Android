package com.medicbleep.app.chat.models;

import java.io.Serializable;
import java.util.List;

import com.medicbleep.app.chat.extendables.BaseModel;

public class StickersHolder extends BaseModel implements Serializable {

	private static final long serialVersionUID = 4080026425457931068L;

	public List<Stickers> stickers;

	public StickersHolder() {

	}
	
	public void setStickersList(List<Stickers> inStickers){
		this.stickers = inStickers;
	}

	@Override
	public String toString() {
		return "StickersHolder [stickers=" + stickers + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stickers == null) ? 0 : stickers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StickersHolder other = (StickersHolder) obj;
		if (stickers == null) {
			if (other.stickers != null)
				return false;
		} else if (!stickers.equals(other.stickers))
			return false;
		return true;
	}
	
}
