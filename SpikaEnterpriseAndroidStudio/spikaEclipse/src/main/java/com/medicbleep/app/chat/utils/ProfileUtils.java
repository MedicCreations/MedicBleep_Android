package com.medicbleep.app.chat.utils;

import android.content.Context;

import com.medicbleep.app.chat.R;

public class ProfileUtils {

    /**
     * Gives a correct localized String value for given key, if it exists.
     * @param key a key representing a real world value
     * @param context context required to fetch resources
     * @return either translated String or the same key if it doesn't exist
     */
	public static String mapToKey(String key, Context context) {
        String[] keys = context.getResources().getStringArray(R.array.profile_detail_keys);
        String[] values = context.getResources().getStringArray(R.array.profile_detail_values);

        // check if given key is contained in keys
        boolean contained = false;
        int index = -1;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(key)) {
                contained = true;
                index = i;
            }
        }
        if (!contained) return key;
        return values[index];
    }

}
