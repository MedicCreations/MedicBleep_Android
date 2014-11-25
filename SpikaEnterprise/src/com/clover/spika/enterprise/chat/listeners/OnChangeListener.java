package com.clover.spika.enterprise.chat.listeners;

public interface OnChangeListener<T> {
	void onChange(T obj, boolean isFromDetails);
}
