package irc.client.model;

import irc.client.ClientEventListener;

import java.util.ArrayList;
import java.util.TreeSet;

public class Channel {
	private String name;
	private boolean hasHighlight = false;
	private boolean hasNewMessage = false;
	private boolean privmsg;
	private TreeSet<LogLine> lines;
	private ClientEventListener client_event_listener;
	
	private ArrayList<String> users = new ArrayList<String>();
	
	public Channel(ClientEventListener listener, String name, boolean privmsg) {
		this.privmsg = privmsg;
		client_event_listener = listener;
		this.name = name;
	}
	
	public boolean isPrivMsg() {
		return privmsg;
	}
	
	public void addLine(LogLine line) {
		hasNewMessage = true;
		lines.add(line);
		client_event_listener.newLine(this, line);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasHighlight() {
		return hasHighlight;
	}
	
	public boolean hasNewMessage() {
		return hasNewMessage;
	}
	
	public void resetStatus() {
		hasHighlight = false;
		hasNewMessage = false;
	}
	
	public TreeSet<LogLine> getLinesSet() {
		return lines;
	}
	
	public ArrayList<LogLine> getLines() {
		return new ArrayList<LogLine>(lines);
	}

	public ArrayList<String> getUsers() {
		return users;
	}
}
