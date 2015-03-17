package com.clover.spika.enterprise.chat.webrtc.socket.models;

import java.io.Serializable;
import java.util.HashMap;

public class CheckAvailableRoom implements Serializable {

	private static final long serialVersionUID = 1L;

	public HashMap<String, ClientsSocket> clients;

	public CheckAvailableRoom() {
	}

	public HashMap<String, ClientsSocket> getClients() {
		return clients;
	}

	public void setClients(HashMap<String, ClientsSocket> clients) {
		this.clients = clients;
	}

	@Override
	public String toString() {
		return "CheckAvailableRoom [clients=" + clients + "]";
	}

}
