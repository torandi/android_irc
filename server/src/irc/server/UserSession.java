package irc.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import jerklib.ConnectionManager;

import irc.server.db.ValidationException;
import irc.server.model.*;

public class UserSession implements SendEvent {
	private User user;
	private ClientSession session = null;
	private ConnectionManager connman;

	private HashMap<Integer, UserNetwork> networks = new HashMap<Integer, UserNetwork>();
	
	public UserSession(User user) throws SQLException {
		this.user = user;
		
		connman = new ConnectionManager(user.createProfile());
		System.out.println("Initializing user session for "+user.getNick());
		ArrayList<UserNetwork> user_networks = user.getNetworks();
		for(UserNetwork nw : user_networks) {
			networks.put(nw.id(), nw);
			nw.connect(connman, this);
		}
	}
	
	public void addNetwork(String address, int port) throws SQLException, ValidationException {
		UserNetwork un = new UserNetwork();
		un.setAddress(address);
		un.setPort(port);
		un.setUserId(user.id());
		un.commit();
		
		sendNetwork(un);
		
		un.connect(connman, this);
		
		synchronized(networks) {
			networks.put(un.id(), un);
		}
		
	}
	
	public void reconnect(UserNetwork nw) {
		nw.disconnect();
		nw.connect(connman, this);
	}
	
	public void setClientSession(ClientSession session) {
		this.session = session;
	}
	
	public void sendNetwork(UserNetwork nw) {
		sendLine(Priority.STATUS_CHANGE, "NETWORK "+nw.id()+" "+nw.getAddress() + " "+nw.getPort());
	}
	
	public void setNick(String nick) {
		for(UserNetwork nw : networks.values()) {
			nw.setNick(nick);
		}
	}
	
	public void listNetworks() {
		for(UserNetwork nw : networks.values()) {
			sendNetwork(nw);
		}
	}
	
	public void sendChannels(UserNetwork nw) throws SQLException {
		for(Channel channel : nw.getChannels()) {
			if(!channel.isPrivMsg()) {
				nw.sendChannel(channel.ircChannel);
				nw.sendTopic(channel.ircChannel);
				nw.sendNickList(channel.ircChannel);
			} else {
				nw.sendLine(Priority.STATUS_CHANGE, "PRIVMSG "+channel.getName());
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		connman.quit("AndroidIrc server terminating");
	}
	
	public void sendInitials() throws SQLException {
		for(UserNetwork nw : networks.values()) {
			sendNetwork(nw);
			sendChannels(nw);
		}
	} 

	@Override
	public void sendLine(Priority priority, String line) {
		System.out.println("[UserSession] " +line);
		if(session != null) session.sendLine(priority, line);
	}
	
	public UserNetwork getNetwork(int id) {
		return networks.get(id);
	}
	
	public void deleteNetwork(int id) {
		UserNetwork nw = networks.remove(id);
		if(nw != null) {
			nw.disconnect();
			sendLine(Priority.STATUS_CHANGE, "NETWORK "+id+" DEL");
		}
	}
}
