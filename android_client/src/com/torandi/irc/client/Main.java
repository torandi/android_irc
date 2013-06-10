package com.torandi.irc.client;

import com.torandi.irc.client.model.Channel;
import com.torandi.irc.client.model.LogLine;
import com.torandi.irc.client.model.Network;

public class Main implements ClientEventListener {
	private static final String PASSWORD = "password";
	private static String nick = "Torandi";
	private static String host = "localhost";
	private static int port = 3167;
	
 	public static void main(String[] args) {
 		Main main = new Main();
 		new Client(main, host, port, nick, "clientstore", PASSWORD);
 	}
 	
 	public Main() {
 		
 	}

	public void addNetwork(Network nw) {
		System.out.println("addNetwork" + nw.toString());
	}

	public void removeNetwork(Network nw) {
		System.out.println("removeNetwork" + nw.toString());
	}

	public void changeNetwork(Network nw) {
		System.out.println("changeNetwork" + nw.toString());
	}

	public void error(String message) {
		System.err.println("ERROR: "+message);
	}

	public void addChannel(Channel c) {
		System.out.println("Add channel: "+c.getName());
	}

	public void nickListUpdate(Channel c) {
		System.out.println("NickList: "+c.getName()+":");
		for(String s : c.getUsers()) {
			System.out.println(s);
		}
	}

	public void newLine(Channel channel, LogLine line) {
		System.out.println(channel.getName()+" Line: "+line.getTime().toString()+" " + line.getType()+" "+line.getUser()+" "+line.getMessage());
	}

	@Override
	public void notAuthorized(String string) {
		System.out.println("You are not authorized, add "+string+" to authorized_users");
	}
}
