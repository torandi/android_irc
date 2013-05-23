package irc.server;

import irc.server.model.User;
import irc.server.model.User.UserNotAllowedException;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.PublicKey;
import java.sql.SQLException;

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
		if(output != null) {
			try { output.println("CLOSE"); } catch (Exception e) {}
			output.flush();
		}
		
		try {
			session = server.userSession(user);
			session.setClientSession(null);
			socket.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		mode = MODE.CLOSED;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
		if(thread != null) thread.interrupt();
		mode = MODE.CLOSED;
	}
	
	@Override
	public void dataRecived(String data, SSLSocket sck) {
		try {
			String[] split = data.split(" ");
			String cmd = split[0];
			
			switch(mode) {
			case NEW:
				println("Recieved data while in NEW state: "+data);
				return;
			case HANDSHAKED:
				if(cmd.equals("VERSION")) {
					int v = Integer.parseInt(split[1]);
					if(v < MIN_VERSION) {
						output.println("VERSION ERROR "+MIN_VERSION+" "+VERSION);
						close();
					} else {
						output.println("VERSION OK");
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
					output.println("CHALLENGE "+ Util.toHex(rsa.encrypt(challenge)));
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
							output.println("AUTH OK");
							initUser();
						} catch (UserNotAllowedException e) {
							println(e.getMessage());
							output.println("AUTH ERROR");
							close();
						}
					} else {
						output.println("CHALLENGE ERROR");
						close();
					}
					return;
				}
			case ACTIVE:
				if(cmd.equals("IDLE")) {
					mode = MODE.IDLE;
					return;
				}
				/* Get lines from channel */
				if(cmd.equals("LINES")) {
					
				}
			case IDLE:
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
			mode = MODE.HANDSHAKED;
			thread = SSLSocketManager.receive(socket, this);
			output.println("VERSION "+VERSION);
		} catch (IOException e) {
			println("Failed to create output stream for client: "+e.getMessage());
			close();
		}
	}
	
	private void println(String str) {
		System.out.println("[CLIENT] "+str);
	}

	@Override
	public void sendLine(Priority priority, String line) {
		println(line);
		switch(mode) {
		case ACTIVE:
			output.println(line);
			break;
		case IDLE:
			if(priority != Priority.NORMAL) {
				output.println(line);
			}
			break;
		default:
			break;
		}
	}
}
