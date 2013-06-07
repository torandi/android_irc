package irc.server;

import irc.server.model.Channel;
import irc.server.model.LogLine;
import irc.server.model.User;
import irc.server.model.User.UserNotAllowedException;
import irc.server.model.UserNetwork;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.TimerTask;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.torandi.lib.net.*;
import com.torandi.lib.security.*;

public class ClientSession implements SSLSocketListener, HandshakeCompletedListener, SendEvent {
	private SSLSocket socket;
	private PrintStream output = null;
	private Thread thread = null;
	private RSA rsa = null;
	private PublicKey pkey = null; /* Public key of the client */

	private static int VERSION = 0;
	private static int MIN_VERSION = 0;
	private static int CHALLENGE_LENGTH = 100;
	private String challenge = null;
	private Server server = null;
	private PingTask ping_task = null;

	private User user = null;
	private UserSession session = null;

	private enum MODE {
		NEW,
		HANDSHAKED,
		VERSION_OK,
		CHALLENGED,
		ACTIVE,
		IDLE,
		CLOSED,
	};

	private MODE mode;

	public ClientSession(Server server, SSLSocket socket) {
		this.socket = socket;
		this.server = server;
		ping_task = new PingTask();
		rsa = new RSA();

		mode = MODE.NEW;
		socket.addHandshakeCompletedListener(this);
		try {
			socket.startHandshake();
		} catch (IOException e) {
			println("Handshake error: ");
			e.printStackTrace();
		}
	}

	public void close() {
		ping_task.cancel();
		if(output != null) {
			try {
				output.println("CLOSE");
				output.flush();
			} catch (Exception e) {}
			output = null;
		}

		try {
			session = server.userSession(user);
			session.setClientSession(null);
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(thread != null) thread.interrupt();
		mode = MODE.CLOSED;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	@Override
	public void dataRecived(String data, SSLSocket sck) {
		try {
			String[] split = data.split(" ");
			String cmd = split[0];
			
			if(cmd.equals("PONG")) {
				ping_task.recvPing(Integer.parseInt(split[1]));
				return;
			}

			switch(mode) {
			case NEW:
				println("Recieved data while in NEW state: "+data);
				return;
			case HANDSHAKED:
				if(cmd.equals("VERSION")) {
					int v = Integer.parseInt(split[1]);
					if(v < MIN_VERSION) {
						send("VERSION ERROR "+MIN_VERSION+" "+VERSION);
						close();
					} else {
						send("VERSION OK");
						mode = MODE.VERSION_OK;
					}
					return;
				}
				break;
			case VERSION_OK:
				if(cmd.equals("PUBKEY")) {
					BigInteger pubMod = new BigInteger(split[1], 16);
					BigInteger pubExp = new BigInteger(split[2], 16);
					pkey = rsa.createPublicKey(pubMod, pubExp);
					rsa.initEncryption(pkey);
					challenge = Util.randomString(CHALLENGE_LENGTH);
					mode = MODE.CHALLENGED;
					println("Challenge: "+challenge);
					send("CHALLENGE "+ Util.toHex(rsa.encrypt(challenge)));
					return;
				}
				break;
			case CHALLENGED:
				if(cmd.equals("AUTH")) {
					String username = split[1];
					String response = split[2];
					if(response.trim().equals(challenge)) {
						try {
							user = User.authenticate(username, RSA.getFingerprint(pkey));
							mode = MODE.IDLE;
							send("AUTH OK");
							initUser();
						} catch (UserNotAllowedException e) {
							println(e.getMessage());
							send("AUTH ERROR");
							close();
						}
					} else {
						send("CHALLENGE ERROR");
						close();
					}
					return;
				}
			case ACTIVE:
			case IDLE:
				if(cmd.equals("IDLE")) {
					mode = MODE.IDLE;
					return;
				}
				
				if(cmd.equals("NETWORK")) {
					String cmd2 = split[1];
					if(cmd2.equals("ADD")) {
						String addr = split[2];
						int port = split.length > 3 ? Integer.parseInt(split[3]) : 6667;
						session.addNetwork(addr, port);
						return;
					} else if(cmd2.equals("DEL")) {
						int id = Integer.parseInt(split[2]);
						session.deleteNetwork(id);
						return;
					} else if(cmd2.equals("LIST")) {
						session.listNetworks();
						
					} else if(cmd2.equals("CHANGE")) {
						int id = Integer.parseInt(split[2]);
						UserNetwork nw = session.getNetwork(id);
						if(nw == null) {
							send("ERROR Unknown network id");
						} else {
							String addr = split[3];
							int port = split.length > 4 ? Integer.parseInt(split[4]) : 6667;
							nw.setAddress(addr);
							nw.setPort(port);
							nw.commit();
							send("NETWORK CHANGE "+id+" "+addr+" "+port);
						}
						return;
					} else if(cmd2.equals("RECONN")) {
						int id = Integer.parseInt(split[2]);
						UserNetwork nw = session.getNetwork(id);
						session.reconnect(nw);
						return;
					}
				}
				
				if(cmd.equals("NICK")) {
					session.setNick(split[1]);
				}
				
				if(cmd.equals("DATA")) {
					int nwid = Integer.parseInt(split[1]);
					UserNetwork nw = session.getNetwork(nwid);
					String cmd2 = split[2];
					
					if(nw == null) {
						send("ERROR Unknown network id");
						return;
					}
					Channel channel = null;
					if(cmd2.equals("CHANNEL")) {
						String channel_name = split[3];
						String cmd3 = split[4];
						
						if(cmd3.equals("JOIN")) {
							channel = nw.findChannel(channel_name, false);
							nw.joinChannel(channel);
							return;
						}
						
						channel = nw.getChannel(channel_name);
						
						if(channel == null) {
							send("ERROR Unknown channel " +channel_name +" in network "+nw.getAddress());
							return;
						} else if(channel.ircChannel == null) {
							send("ERROR Not yet joined to " +channel_name +" in network "+nw.getAddress());
							return;
						}
					}
					
					if(cmd2.equals("PRIVMSG")) {
						String with = split[3];
						channel = nw.findChannel(with, true);
						
						//String cmd3 = split[4];
					}
					
					if(channel != null) {
						String cmd3 = split[4];
						
						if(cmd3.equals("LINES")) {
							int last_line = -1;
							if(split.length > 2) {
								last_line = Integer.parseInt(split[2]);
							}
							for(LogLine line : channel.getLines(last_line)) {
								line.send(nw);
							}
							return;
						} else if(cmd3.equals("SAY")) {
							String msg = split[5];
							channel.say(msg);
							return;
						} else if(cmd3.equals("PART")) {
							String msg = split.length > 5 ? split[5] : "User left the channel.";
							nw.partChannel(channel, msg);
						}
					}
				}
				
				
				if(cmd.equals("ACTIVATE")) {
					mode = MODE.ACTIVE;
					return;
				}
				
				if(cmd.equals("CLOSE")) {
					close();
					return;
				}
				break;
			case CLOSED:
			default:
				break;
			}
			println("Unhandled input: "+data+ " in mode "+mode.toString());
		} catch (Exception e) {
			println("Error while parsing data : ");
			e.printStackTrace();
		}
	}

	public void initUser() throws SQLException {
		session = server.userSession(user);
		session.setClientSession(this);
		session.sendInitials();
 	}

	public boolean alive() {
		return mode != MODE.CLOSED;
	}

	@Override
	public void newClient(SSLSocket client, SSLServerSocket srvr) { }

	@Override
	public void connectionClosed(SSLSocket sck, IOException e) {
		println("Client disconnected");
		mode = MODE.CLOSED;
	}


	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		try {
			output = new PrintStream(socket.getOutputStream());
			server.getPingTimer().schedule(ping_task, PingTask.PING_INTERVAL, PingTask.PING_INTERVAL);
			mode = MODE.HANDSHAKED;
			thread = SSLSocketManager.receive(socket, this);
			send("VERSION "+VERSION);
		} catch (IOException e) {
			println("Failed to create output stream for client: "+e.getMessage());
			close();
		}
	}

	private void println(String str) {
		System.out.println("[CLIENT] "+str);
	}

	private void send(String msg) {
		try {
			output.println(msg);
			output.flush();
		} catch (Exception e) {
			println("Connection lost.");
			close();
		}
	}

	@Override
	public void sendLine(Priority priority, String line) {
		println(line);
		switch(mode) {
		case ACTIVE:
			send(line);
			break;
		case IDLE:
			if(priority != Priority.NORMAL) {
				send(line);
			}
			break;
		default:
			break;
		}
	}
	
	private class PingTask extends TimerTask {
		public final static long PING_INTERVAL = 30000;
		private final static int MAX_MISSED_PINGS = 2;
		private int last_ping_response = -1;
		private int next_ping_seq = 0;
		
		public void recvPing(int seq) {
			last_ping_response = seq;
		}
		
		@Override
		public void run() {
			if(next_ping_seq - last_ping_response > MAX_MISSED_PINGS) {
				println("Ping timeout.");
				close();
			} else {
				send("PING "+(++next_ping_seq));
			}
		}
		
	}
}
