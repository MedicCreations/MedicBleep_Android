package com.clover.spika.enterprise.chat.listeners;

public interface ProgressBarListeners {
	public void onSetMax(long total);
	public void onProgress(long current);
}
