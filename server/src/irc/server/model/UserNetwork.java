package irc.server.model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.*;
import jerklib.listeners.IRCEventListener;

import irc.server.SendEvent;
import irc.server.db.DatabaseObject;
import irc.server.db.ValidationException;

public class UserNetwork extends DatabaseObject<UserNetwork> implements IRCEventListener, SendEvent {
	private Session session = null;
	private Network network = null;
	private int server_index = 0;
	private ConnectionManager connman = null;
	private OutputStreamWriter log = null;
	private SendEvent receiver = null;
	
	private HashMap<String, Channel> channel_map = new HashMap<String, Channel>();
	
	public void connect(ConnectionManager connman, SendEvent rcvr) {
		this.connman = connman;
		receiver = rcvr;
		
		try {
			log = new FileWriter("logs/"+getNetwork().getName()+".log");
		} catch (IOException e1) {
			System.err.println("Failed to open logs/"+getNetwork().getName()+".log, logging to stdout");
			log = new OutputStreamWriter(System.out);
		}
		
		System.out.println("Connecting to network "+getNetwork().getName());
		ArrayList<Server> servers = new ArrayList<Server>();
		try {
			servers = getNetwork().getServers();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(servers.size() == 0) {
			System.err.println("No servers for network "+getNetwork().getName());
			return;
		}
		
		session = servers.get(server_index).connect(connman, createProfile());
		session.addIRCEventListener(this);
	}
	
	public void disconnect() {
		session.close("AndroidIRC: Quitting");
	}
	
	public Profile createProfile() {
		String nick = getNick();
		if(nick == null) return getUser().createProfile();
		return User.createProfile(nick);
	}
	
	public Channel findChannel(String name, boolean privmsg) {
		Channel c = channel_map.get(name);
		if(c == null) {
			c = new Channel();
			c.setName(name);
			c.setPrivMsgStatus(privmsg);
			c.setUserNetworkId(id());
			try {
				c.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return c;
	}
	
	private void handleLogLine(Channel c, LogLine ll) {
		try {
			c.pushLine(ll);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ll.send(this);
	}
	
	private LogLine createLogLine() {
		LogLine ll = new LogLine();
		Date date = new Date();
		ll.setTimestamp(new Timestamp(date.getTime()));
		return ll;
	}
	
	private LogLine logLineFromMessageEvent(MessageEvent e) {
		LogLine ll = createLogLine();
		ll.setContent(e.getMessage());
		ll.setType(LogLine.Type.MSG);
		ll.setUser(e.getNick());
		return ll;
	}
	
	@Override
	public void receiveEvent(IRCEvent e) {
		System.out.println("IRCEvent: "+e.getType());
		//System.out.println(e.getRawEventData());
		switch(e.getType()) {
		case PRIVATE_MESSAGE:
		{
			MessageEvent event = (MessageEvent) e;
			Channel channel = findChannel(event.getNick(), true);
			LogLine ll = logLineFromMessageEvent(event);
			handleLogLine(channel, ll);
			break;
		}
		case CHANNEL_MESSAGE:
		{
			MessageEvent event = (MessageEvent) e;
			Channel channel = findChannel(event.getChannel().getName(), false);
			LogLine ll = logLineFromMessageEvent(event);
			handleLogLine(channel, ll);
			break;
		}
		case PART:
		{
			PartEvent event = (PartEvent) e;
			Channel channel = findChannel(event.getChannel().getName(), false);
			LogLine ll = createLogLine();
			ll.setType(LogLine.Type.PART);
			handleLogLine(channel, ll);
			sendNickList(event.getChannel());
			break;
		}
		case JOIN:
		{
			JoinEvent event = (JoinEvent) e;
			Channel channel = findChannel(event.getChannel().getName(), false);
			LogLine ll = createLogLine();
			ll.setType(LogLine.Type.JOIN);
			handleLogLine(channel, ll);
			sendNickList(event.getChannel());
			break;
		}
		case AWAY_EVENT:
			break;
		case CHANNEL_LIST_EVENT:
			break;
		case CONNECT_COMPLETE:
			channel_map.clear();
			try { 
				for(Channel c : getChannels()) {
					channel_map.put(c.getName(), c);
					if(!c.isPrivMsg()) session.join(c.getName());
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			break;
		case CONNECTION_LOST:
			System.out.println("Lost connection to "+getNetwork().getName());
			sendLine(Priority.STATUS_CHANGE, "CONNLOST "+getNetwork().getName());
			try {
				Thread.sleep(5000);
				connect(connman, receiver);
			} catch (InterruptedException e2) { }
			break;
		case CTCP_EVENT:
			break;
		case DCC_EVENT:
			break;
		case ERROR:
		{
			ErrorEvent event = (ErrorEvent) e;
			switch(event.getErrorType()) {
			case NUMERIC_ERROR:
			{
				NumericErrorEvent ee = (NumericErrorEvent) event;
				System.out.println("Numeric error: "+ee.getNumeric()+" "+ee.getErrorMsg());
				System.out.println(ee.getRawEventData());
				break;
			}
			case UNRESOLVED_HOSTNAME:
				break;
			}
			break;
		}
		case INVITE_EVENT:
			break;
		case JOIN_COMPLETE:
		{
			JoinCompleteEvent event = (JoinCompleteEvent) e;
			sendChannel(event.getChannel());
			break;
		}
		case KICK_EVENT:
		{
			KickEvent event = (KickEvent) e;
			sendLine(event.getWho().equals(session.getNick()) ? Priority.STATUS_CHANGE : Priority.NORMAL,
					"KICK "+event.getChannel()+" "+event.getWho()+" "+event.byWho()+" "+event.getMessage());
			sendNickList(event.getChannel());
			break;
		}
		case NICK_LIST_EVENT:
		{
			NickListEvent event = (NickListEvent) e;
			sendNickList(event.getChannel());
			break;
		}
		case NOTICE:
		{
			NoticeEvent event = (NoticeEvent) e;
			String to;
			Priority priority;
			String from = "";
			if(event.getChannel() != null) {
				to = "CHANNEL "+event.getChannel();
				priority = Priority.NORMAL;
				from = " "+event.byWho();
			} else if(event.byWho() != null) {
				to = "USER";
				priority = Priority.HIGH;
				from = " "+event.byWho();
			} else {
				to = "STATUS";
				priority = Priority.NORMAL;
			}
			sendLine(priority, "NOTICE "+to+from+" "+event.getNoticeMessage());
			break;
		}
		case QUIT:
			break;
		case TOPIC:
		{
			TopicEvent event = (TopicEvent) e;
			sendTopic(event.getChannel());
			break;
		}
		case WHOIS_EVENT:
			break;
		case WHOWAS_EVENT:
			break;
		default:
			try {
				log.write(e.getRawEventData()+"\n");
				log.flush();
			} catch (IOException e1) {
				System.err.println(e.getRawEventData());
			}
			break;
		}
	}
	
	public jerklib.Channel getChannel(String name) {
		return session.getChannel(name);
	}
	
	public void sendTopic(jerklib.Channel channel) {
		if(!channel.getTopic().trim().isEmpty()) {
			sendLine(Priority.STATUS_CHANGE, "TOPIC "+channel.getName()+" "+channel.getTopicSetter()+" "+channel.getTopic());
		}
	}
	
	public void sendNickList(jerklib.Channel channel) {
		StringBuilder str = new StringBuilder();
		str.append("NICKLIST "+channel.getName());
		for(String nick : channel.getNicks()) {
			str.append(" "+nick);
		}
		sendLine(Priority.STATUS_CHANGE, str.toString());
	}
	
	public void sendChannel(jerklib.Channel channel) {
		sendLine(Priority.STATUS_CHANGE, "CHANNEL "+channel.getName());
	}

	/* Data readers/writers */
	
	public Session getSession() {
		return session;
	}
	
	public String getNick() {
		return (String) get("nick");
	}
	
	public void setNick(String nick) {
		set("nick", nick);
	}
	
	public int getNetworkId() {
		return get_int("network_id");
	}
	
	public void setNetworkId(int id) {
		set("network_id", id);
	}
	
	public int getUserId() {
		return get_int("user_id");
	}
	
	public void setUserId(int id) {
		set("user_id", id);
	}
	
	public Network getNetwork() {
		if(network == null) network = Network.q().from_id(getNetworkId());
		return network;
	}
	
	public User getUser() {
		return User.q().from_id(getUserId());
	}
	
	public ArrayList<Channel> getChannels() throws SQLException {
		return Channel.q().find("user_network_id", id());
	}

	/* Object config */
	@Override
	protected Class<UserNetwork> cls() {
		return UserNetwork.class;
	}

	@Override
	protected String table_name() {
		return "user_networks";
	}
	
	public void validate() throws ValidationException {
		validateExistance("network_id");
		validateExistance("user_id");
	}
	

	/* Boilerplate */
	private static final UserNetwork query_obj = new UserNetwork();

	public static UserNetwork q() {
		return query_obj;
	}

	@Override
	public void sendLine(Priority priority, String line) {
		receiver.sendLine(priority, "DATA " + getNetworkId()+" "+line);
	}

}
