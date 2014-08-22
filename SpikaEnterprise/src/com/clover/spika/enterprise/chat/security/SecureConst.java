package com.clover.spika.enterprise.chat.security;

import android.os.Build;

public class SecureConst {

	private static final String PASSWORD = "jFglBLpOJQ4RLlVTl5EulWS2NLrTgHzB";
	public static final int ITERATIONS = 3;

	public static char[] getPassword() {

		StringBuilder builder = new StringBuilder();

//		builder.append(Build.VERSION_CODES.DONUT);
		builder.append(PASSWORD);
//		builder.append(Build.VERSION_CODES.HONEYCOMB);

		return builder.toString().toCharArray();
	}
}
