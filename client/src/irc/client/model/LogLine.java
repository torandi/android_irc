package irc.client.model;

import java.util.Arrays;

import android.text.TextUtils;

public class LogLine implements Comparable<LogLine> {
	private String user, time, message, channel;
	private int id;
	
	public static enum Type { MSG, PART, JOIN };
	
	private Type type;
	
	public String getUser() {
		return user;
	}

	public String getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}

	public String getChannel() {
		return channel;
	}

	public int getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public LogLine(String[] from_server, int offset) {
		id = Integer.parseInt(from_server[offset+0]);
		time = from_server[offset+1];
		type = Type.valueOf(Type.class, from_server[offset+2]);
		channel = from_server[offset+3];
		user = from_server[offset+4];
		message = TextUtils.join(" ", Arrays.copyOfRange(from_server, offset+5, from_server.length));
	}

	@Override
	public int compareTo(LogLine o) {
		return o.id - id;
	}
	
}