package com.clover.spika.enterprise.chat.utils;

import java.util.Comparator;

import com.clover.spika.enterprise.chat.model.Message;


public class MessageSorting implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {

	long postedTime1 = Long.parseLong(lhs.getSort_key());
	long postedTime2 = Long.parseLong(rhs.getSort_key());

	if (postedTime1 > postedTime2) {
	    return 1;
	} else if (postedTime1 < postedTime2) {
	    return -1;
	}

	return 0;
    }
}