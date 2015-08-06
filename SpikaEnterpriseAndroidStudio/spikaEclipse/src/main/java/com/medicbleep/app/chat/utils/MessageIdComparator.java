package com.medicbleep.app.chat.utils;

import com.medicbleep.app.chat.models.Message;

import java.util.Comparator;

public class MessageIdComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        if (Integer.parseInt(lhs.getId()) > Integer.parseInt(rhs.getId())) {
            return 1;
        } else if (Integer.parseInt(lhs.getId()) < Integer.parseInt(rhs.getId())) {
            return -1;
        } else {
            return 0;
        }
    }
}
