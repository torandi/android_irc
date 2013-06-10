package com.torandi.irc.client.model;

import java.util.Arrays;
import java.util.Date;

import android.text.TextUtils;
import android.text.format.Time;

public class LogLine implements Comparable<LogLine> {
	private String user, message, channel;
	private int id;
	private Date time;
	
	public static enum Type { MSG, PART, JOIN };
	
	private Type type;
	
	public String getUser() {
		return user;
	}

	public Date getTime() {
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
		time = new Date(Long.parseLong(from_server[offset+1]));
		type = Type.valueOf(Type.class, from_server[offset+2]);
		channel = from_server[offset+3];
		user = from_server[offset+4];
		message = TextUtils.join(" ", Arrays.copyOfRange(from_server, offset+5, from_server.length));
	}

	@Override
	public int compareTo(LogLine o) {
		return o.time.compareTo(time);
	}
	
}