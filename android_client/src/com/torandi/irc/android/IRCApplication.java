package com.torandi.irc.android;

import com.torandi.irc.client.Client;

import android.app.Application;
import android.content.SharedPreferences;

public class IRCApplication extends Application {
	private Client client = null;
	private String username = null;
	private String server = null;
	private int port = Client.DEFAULT_PORT;
	
	private final static String PREFS_NAME = "USER_PREFS";
	
	public void loadSettings() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		username = prefs.getString("username", null);
		server = prefs.getString("server", null);
		port = prefs.getInt("port", Client.DEFAULT_PORT);
	}
	
	public void saveSettings() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("username", username);
		editor.putString("server", server);
		editor.putInt("port", port);
		editor.commit();
	}
	
	public 
	
	public final Client getClient() {
		return client;
	}
	public final void setClient(Client client) {
		this.client = client;
	}
	public final String getUsername() {
		return username;
	}
	public final void setUsername(String username) {
		this.username = username;
	}
	public final String getServer() {
		return server;
	}
	public final void setServer(String server) {
		this.server = server;
	}
	public final int getPort() {
		return port;
	}
	public final void setPort(int port) {
		this.port = port;
	}
}
