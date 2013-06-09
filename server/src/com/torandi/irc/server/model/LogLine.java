package com.torandi.irc.server.model;

import java.sql.Timestamp;

import com.torandi.irc.server.SendEvent;
import com.torandi.irc.server.SendEvent.Priority;
import com.torandi.irc.server.db.DatabaseObject;
import com.torandi.irc.server.db.ValidationException;


public class LogLine extends DatabaseObject<LogLine> {
	
	public static enum Type { MSG, PART, JOIN }
	
	/* Send event to receiver
	 */
	public void send(SendEvent receiver) {
		Priority p = isHilight() ? Priority.HIGH : Priority.NORMAL;
		receiver.sendLine(p, "LINE "+id()+" "+getTimestamp().getTime()+" "+getType() +" "+getChannel().getName()+" "+getUser()+" "+getContent());
	}

	/* Data readers/writers */
	
	private User getActiveUser() {
		return getChannel().getUserNetwork().getUser();
	}
	
	public boolean isHilight() {
		return (getContent() != null && getActiveUser().isHilight(getContent()));
	}
	
	public String getUser() {
		return (String) get("user");
	}
	
	public void setUser(String user) {
		set("user", user);
	}
	
	public int getChannelId() {
		return get_int("channel_id");
	}
	
	public void setChannel(int id) {
		set("channel_id", id);
	}
	
	public String getContent() {
		return (String) get("content");
	}
	
	public void setContent(String content) {
		set("content", content);
	}
	
	public Timestamp getTimestamp() {
		return (Timestamp) get("timestamp");
	}

	public void setTimestamp(Timestamp time) {
		set("timestamp", time);
	}
	
	public Channel getChannel() {
		return Channel.q().from_id(getChannelId());
	}
	
	public Type getType() {
		return Type.valueOf(Type.class, (String)get("type"));
	}
	
	public void setType(Type type) {
		set("type", type.name());
	}
	
	/* Object config */
	@Override
	protected Class<LogLine> cls() {
		return LogLine.class;
	}

	@Override
	protected String table_name() {
		return "log_lines";
	}
	
	public void validate() throws ValidationException {
		validateExistance("channel_id");
		validateExistance("timestamp");
		validateExistance("type");
	}
	
	@Override
	protected String default_order() {
		return "id desc";
	}
	

	/* Boilerplate */
	private static final LogLine query_obj = new LogLine();

	public static LogLine q() {
		return query_obj;
	}

}
