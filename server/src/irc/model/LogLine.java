package irc.model;

import java.sql.Timestamp;

import irc.db.DatabaseObject;
import irc.db.ValidationException;

public class LogLine extends DatabaseObject<LogLine> {
	
	public static enum Type { MSG, TOPIC, PART, JOIN }
	
	/* Data readers/writers */
	
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
	

	/* Boilerplate */
	private static final LogLine query_obj = new LogLine();

	public static LogLine q() {
		return query_obj;
	}

}
