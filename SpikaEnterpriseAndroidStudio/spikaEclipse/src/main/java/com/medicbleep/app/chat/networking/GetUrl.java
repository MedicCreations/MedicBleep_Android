package com.medicbleep.app.chat.networking;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.text.TextUtils;

public class GetUrl {

	private static final String FORMAT = "UTF-8";

	private String params = "";

	public GetUrl(HashMap<String, String> map) {

		if (map != null) {

			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {

				Map.Entry<String, String> pairs = (Entry<String, String>) it.next();

				try {
					params += URLEncoder.encode(pairs.getKey(), FORMAT) + "=" + URLEncoder.encode(pairs.getValue(), FORMAT);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				if (it.hasNext()) {
					params += "&";
				}
			}
		}
	}

	public synchronized void add(Object name, Object value) {

		if (!params.trim().equals("")) {
			params += "&";
		}

		try {
			params += URLEncoder.encode(name.toString(), FORMAT) + "=" + URLEncoder.encode(value.toString(), FORMAT);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public String toString() {

		if (!TextUtils.isEmpty(params)) {
			return "?" + params;
		} else {
			return "";
		}
	}

}
