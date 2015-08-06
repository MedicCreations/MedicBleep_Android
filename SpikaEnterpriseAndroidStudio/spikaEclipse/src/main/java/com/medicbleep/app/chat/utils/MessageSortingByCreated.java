package com.medicbleep.app.chat.utils;

import com.medicbleep.app.chat.models.Message;

import java.util.Comparator;

public class MessageSortingByCreated implements Comparator<Message> {

	@Override
	public int compare(Message lhs, Message rhs) {

		long postedTime1 = Long.parseLong(lhs.getCreated());
		long postedTime2 = Long.parseLong(rhs.getCreated());

		if (postedTime1 > postedTime2) {
			return 1;
		} else if (postedTime1 < postedTime2) {
			return -1;
		}

		return 0;
	}
}