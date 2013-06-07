package irc.client.model;

import java.util.HashMap;

public class Network {
	private int id, port;
	private String address;
	private HashMap<String, Channel> channels;
	
	public Network(int id, String address, int port) {
		this.id = id;
		this.address = address;
		this.port = port;
	}
	
	public void change(String address, int port) {
		this.address = address;
		this.port = port;
	}
	
	public HashMap<String, Channel> getChannels() {
		return channels;
	}
	
	public Channel getChannel(String name) {
		return channels.get(name);
	}
	
	public int getId() {
		return id;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}

	public void addChannel(Channel channel) {
		channels.put(channel.getName(), channel);
	}
	
	public void removeChannel(Channel channel) {
		channels.remove(channel.getName());
	}
}
