package com.medicbleep.app.chat.listeners;

public interface OnChangeListener<T> {
	void onChange(T obj, boolean isFromDetails);
}
