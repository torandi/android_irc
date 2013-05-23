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
	
	public void addNetwork(Network network) throws SQLException, ValidationException {
		UserNetwork un = new UserNetwork();
		un.setNetworkId(network.id());
		un.setUserId(user.id());
		un.commit();
		
		synchronized(networks) {
			networks.put(un.id(), un);
		}
	}
	
	public void setClientSession(ClientSession session) {
		this.session = session;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		connman.quit("AndroidIrc server terminating");
	}
	
	public void sendInitials() throws SQLException {
		for(UserNetwork nw : networks.values()) {
			sendLine(Priority.STATUS_CHANGE, "NETWORK "+nw.getNetworkId()+" "+nw.getNetwork().getName());
			for(Channel c : nw.getChannels()) {
				if(!c.isPrivMsg()) {
					jerklib.Channel channel = nw.getChannel(c.getName());
					nw.sendChannel(channel);
					nw.sendTopic(channel);
					nw.sendNickList(channel);
				} else {
					nw.sendLine(Priority.STATUS_CHANGE, "PRIVMSG "+c.getName());
				}
			}
		}
	} 

	@Override
	public void sendLine(Priority priority, String line) {
		System.out.println("[UserSession] " +line);
		if(session != null) session.sendLine(priority, line);
	}
}
