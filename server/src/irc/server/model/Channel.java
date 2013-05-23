package irc.server.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import irc.server.db.DatabaseObject;
import irc.server.db.ValidationException;

public class Channel extends DatabaseObject<Channel> {
	private final static int LINE_LIMIT = 50;
	LogLine last_line = null;
	
	public void pushLine(LogLine line) throws SQLException, ValidationException {
		line.setChannel(id());
		line.commit();
		last_line = line;
	}
	
	public LogLine lastLine() throws SQLException {
		if(last_line != null) return last_line;
		last_line = LogLine.q().first("channel_id", id());
		return last_line;
	}
	
	public ArrayList<LogLine> getLines() throws SQLException {
		return LogLine.q().find("channel_id", id(), LINE_LIMIT);
	}
	
	public ArrayList<LogLine> getLines(int id_less_than) throws SQLException {
		PreparedStatement stmt = LogLine.q().statement("channel_id = ?  AND id < ?", ""+LINE_LIMIT);
		stmt.setInt(1, id());
		stmt.setInt(2, id_less_than);
		return LogLine.q().where(stmt);
	}
	
	/* Data readers/writers */
	
	public String getName() {
		return (String) get("name");
	}
	
	public void setName(String name) {
		set("name", name);
	}
	
	public void setPrivMsgStatus(boolean privmsg) {
		set("privmsg", privmsg);
	}
	
	public boolean isPrivMsg() {
		return (Boolean)get("privmsg");
	}
	
	public int getUserNetworkId() {
		return get_int("user_network_id");
	}
	
	public void setUserNetworkId(int id) {
		set("user_network_id", id);
	}
	
	public UserNetwork getUserNetwork() {
		return UserNetwork.q().from_id(getUserNetworkId());
	}

	/* Object config */
	@Override
	protected Class<Channel> cls() {
		return Channel.class;
	}

	@Override
	protected String table_name() {
		return "channels";
	}
	
	public void validate() throws ValidationException {
		validateExistance("name");
		validateExistance("user_network_id");
	}
	

	/* Boilerplate */
	private static final Channel query_obj = new Channel();

	public static Channel q() {
		return query_obj;
	}

}
