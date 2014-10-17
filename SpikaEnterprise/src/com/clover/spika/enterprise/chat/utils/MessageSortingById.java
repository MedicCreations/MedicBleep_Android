package com.clover.spika.enterprise.chat.utils;

import java.util.Comparator;

import com.clover.spika.enterprise.chat.models.Message;

public class MessageSortingById implements Comparator<Message> {

	@Override
	public int compare(Message lhs, Message rhs) {

		long id1 = Long.parseLong(lhs.getId());
		long id2 = Long.parseLong(rhs.getId());

		if (id1 > id2) {
			return 1;
		} else if (id1 < id2) {
			return -1;
		}

		return 0;
	}
}