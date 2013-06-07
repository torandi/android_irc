package irc.client.model;

import java.util.ArrayList;
import java.util.TreeSet;

public class Channel {
	private String name;
	private boolean hasHighlight = false;
	private boolean hasNewMessage = false;
	private TreeSet<LogLine> lines;
	
	public Channel(String name) {
		this.name = name;
	}
	
	public void addLine(LogLine line) {
		hasNewMessage = true;
		lines.add(line);
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
	
	
}
