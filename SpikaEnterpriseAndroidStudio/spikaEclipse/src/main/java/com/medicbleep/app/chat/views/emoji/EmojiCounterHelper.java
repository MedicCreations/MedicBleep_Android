package com.medicbleep.app.chat.views.emoji;

import android.text.TextUtils;

import com.medicbleep.app.chat.utils.Preferences;

public class EmojiCounterHelper {
	
	public static void increaseEmojiCounter(String emojiId, Preferences prefs){
		if(TextUtils.isEmpty(emojiId)) {
			return;
		}
		int temp = prefs.getCustomInt(emojiId);
		if(temp < 1) {
			prefs.setCustomInt(emojiId, 1);
		}else{
			prefs.setCustomInt(emojiId, temp + 1);
		}
	}
	
	public static int getEmojiCounter(String emojiId, Preferences prefs){
		if(TextUtils.isEmpty(emojiId)) {
			return 0;
		}
		int temp = prefs.getCustomInt(emojiId);
		if(temp < 1){
			return 0;
		}else{
			return temp;
		}
	}

}
