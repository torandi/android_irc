package irc.client.model;

import irc.client.ClientEventListener;

import java.util.HashMap;

public class Network {
	private int id, port;
	private String address;
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
	
	private ClientEventListener client_event_listener;
	
	public Network(ClientEventListener listener, int id, String address, int port) {
		client_event_listener = listener;
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
	
	public Channel getChannel(String name, boolean privmsg) {
		Channel c = channels.get(name);
		if(c == null) {
			c = new Channel(client_event_listener, name, privmsg);
			channels.put(name, c);
			client_event_listener.addChannel(c);
		}
		return c;
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
	
	public String toString() {
		return "(" + id +")" + address + ":" + port;
	}
}
