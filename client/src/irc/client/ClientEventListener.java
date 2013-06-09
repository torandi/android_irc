package irc.client;

import irc.client.model.Channel;
import irc.client.model.LogLine;
import irc.client.model.Network;

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
