package com.torandi.irc.server;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.torandi.irc.server.model.User;
import com.torandi.lib.net.SSLSocketListener;
import com.torandi.lib.net.SSLSocketManager;
import com.torandi.lib.net.SSLUtil;

public class Server implements SSLSocketListener {

	private SSLUtil ssl;
	private SSLServerSocket socket;
	private Thread socket_thread;
	
	private HashMap<Integer, UserSession> userSessions = new HashMap<Integer, UserSession>();
	
	public Server(int port, String keystore, String password) {
		try {
			ArrayList<String> authorizedUsers = User.authorizedUsers();
			for(User u : User.q().all()) {
				if(authorizedUsers.contains(u.getUser())) {
					userSession(u);
				}
			}
		} catch (SQLException e) {
			println("SQL exception when starting user sessions: ");
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			ssl = new SSLUtil(keystore, password);
			socket = ssl.listen(port);

			socket.setWantClientAuth(false);
			socket.setNeedClientAuth(false);
		} catch (Exception e) {
			System.err.println("Failed to setup ssl: "+e.getMessage());
			System.exit(-1);
		}
		socket_thread = SSLSocketManager.listen(socket, this);
		println("Listening on port "+port);
	}
	
	@Override
	protected void finalize() throws Throwable {
		socket_thread.interrupt();
		socket.close();
		super.finalize();
	}
	
	protected UserSession userSession(User user) throws SQLException {
		UserSession us = userSessions.get(user.id());
		if(us == null) {
			us = new UserSession(user);
			userSessions.put(user.id(), us);
		}
		return us;
	}
	
	@Override
	public void dataRecived(String data, SSLSocket sck) { }

	@Override
	public void newClient(SSLSocket client, SSLServerSocket srvr) {
		println("New client connected.");
		new ClientSession(this, client);
	}

	@Override
	public void connectionClosed(SSLSocket sck, IOException e) { }
	
	private void println(String str) {
		System.out.println("[CLIENT] "+str);
	}
}
