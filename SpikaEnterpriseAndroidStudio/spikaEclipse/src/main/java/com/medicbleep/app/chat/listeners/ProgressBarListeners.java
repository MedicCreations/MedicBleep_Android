package com.medicbleep.app.chat.listeners;

public interface ProgressBarListeners {
	public void onSetMax(long total);
	public void onProgress(long current);
	public void onFinish();
}
