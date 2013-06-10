package com.torandi.irc.client;

import com.torandi.irc.client.model.Channel;
import com.torandi.irc.client.model.LogLine;
import com.torandi.irc.client.model.Network;

public interface ClientEventListener {

	void addNetwork(Network nw);
	void removeNetwork(Network nw);
	void changeNetwork(Network nw);
	
	void error(String message);
	void addChannel(Channel c);
	void nickListUpdate(Channel c);
	void newLine(Channel channel, LogLine line);
	void notAuthorized(String string);
}
