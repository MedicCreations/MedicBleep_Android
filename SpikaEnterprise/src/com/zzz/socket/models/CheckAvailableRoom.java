package com.zzz.socket.models;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckAvailableRoom implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("clients")
	@Expose
	private HashMap<String, ClientsSocket> clients;

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
