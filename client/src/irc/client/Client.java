package irc.client;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.torandi.lib.net.*;
import com.torandi.lib.security.RSA;
import com.torandi.lib.security.Util;

public class Client implements SSLSocketListener, HandshakeCompletedListener {
	private String nick;

	private RSA rsa = null;
	private SSLSocket s = null;
	private PrintStream output = null;
	private Thread thread = null;
	
	private static int VERSION = 0;
	private static int MIN_VERSION = 0;
	
	private enum MODE {
		NEW,
		HANDSHAKED,
		VERSION_OK,
		ACTIVE,
		IDLE,
		CLOSED,
	};
	
	private MODE mode;

	public Client(String hostname, int port, String nick, String keystore_file, String password) {
		this.nick = nick;
		initRSA();
		
		try {
			InputStream is = null;
			try {
				is = new FileInputStream(new File(keystore_file));
			} catch (Exception e) {
				System.out.println("No client keystore found");
			}
			KeyStore keystore = SSLUtil.createKeyStore(is, password);
			if(is != null) is.close();
		
			SSLUtil ssl = new SSLUtil(keystore, password);
			
			s = ssl.connect(hostname, port);
			mode = MODE.NEW;
			s.addHandshakeCompletedListener(this);
			
			try {
				s.startHandshake();
			} catch (SSLException e) {
				X509Certificate c = ssl.getTrustManager().chain()[0];
				
				ssl.addTrustedCert(c);
				ssl.saveKeyStore(keystore_file, password);
		
				try { s.close(); } catch (Exception ex) {}
				
				s = ssl.connect(hostname, port);
			
				s.startHandshake();
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	private void initRSA() {
		rsa = new RSA();
		try {
			ObjectInputStream bi = new ObjectInputStream(new FileInputStream("rsa_key"));
			BigInteger pubMod, pubExp, privMod, privExp;
			pubMod = (BigInteger) bi.readObject();
			pubExp = (BigInteger) bi.readObject();
			privMod = (BigInteger) bi.readObject();
			privExp = (BigInteger) bi.readObject();
			rsa.setKeys(pubMod, pubExp, privMod, privExp);
			
			bi.close();
		} catch (Exception e) {
			e.printStackTrace();
			rsa.generateKeys();
			try {
				ObjectOutputStream bo = new ObjectOutputStream(new FileOutputStream("rsa_key"));
				bo.writeObject(rsa.getPublicKey().getModulus());
				bo.writeObject(rsa.getPublicKey().getPublicExponent());
				
				bo.writeObject(rsa.getPrivateKey().getModulus());
				bo.writeObject(rsa.getPrivateKey().getPrivateExponent());
				bo.close();
			} catch (IOException e1) {
				System.out.println("Failed to write rsa_key");
			}
			
		}
		rsa.init();
	}
	
	public void activate() {
		
	}
	
	private void sendPublicKey() {
		output.println("PUBKEY "+rsa.getPublicKey().getModulus().toString(16) + " "+rsa.getPublicKey().getPublicExponent().toString(16));
	}

	public void close() {
		if(output != null) {
			try { output.println("CLOSE"); } catch (Exception e) {}
			output.flush();
		}
		
		try {
			s.close();
		} catch (Exception e) { }
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
	public void handshakeCompleted(HandshakeCompletedEvent event) {
		try {
			output = new PrintStream(s.getOutputStream());
			mode = MODE.HANDSHAKED;
			thread = SSLSocketManager.receive(s, this);
		} catch (IOException e) {
			System.out.println("Failed to create output stream for client: "+e.getMessage());
		}
	}

	@Override
	public void dataRecived(String data, SSLSocket sck) {
		try {
			String[] split = data.split(" ");
			String cmd = split[0];
			System.out.println(">>> "+data);
			
			if(cmd.equals("PING")) {
				output.println("PONG "+split[1]);
				return;
			}
			
			switch(mode) {
			case NEW:
			case HANDSHAKED: /* Treat these as same, server might be handshaked a moment before us */
				if(cmd.equals("VERSION")) {
					if(split[1].equals("OK")) {
						mode = MODE.VERSION_OK;
						sendPublicKey();
					} else {
						int v = Integer.parseInt(split[1]);
						if(v < MIN_VERSION) {
							System.out.println("Server is to old.");
							close();
						} else {
							output.println("VERSION "+VERSION);
						}
					}
					return;
				}
				break;
			case VERSION_OK:
				if(cmd.equals("CHALLENGE")) {
					if(split[1].equals("ERROR")) {
						System.out.println("RSA verification failed");
						close();
					} else {
						output.println("AUTH " +nick + " "+rsa.decrypt(Util.fromHex(split[1])));
					}
					return;
				} else if(cmd.equals("AUTH")) {
					if(split[1].equals("ERROR")) {
						System.out.println("You are not authorized. Add "+nick+"@"+rsa.getFingerprint()+ " to authorized_users on the server");
						close();
						return;
					} else if(split[1].equals("OK")) {
						System.out.println("Authorized");
						mode = MODE.IDLE;
						return;
					}
				}
			} 
			System.out.println("Unhandled input: "+data+ " in mode "+mode.toString());
		} catch (Exception e) {
			System.out.println("Error while parsing data: ");
			e.printStackTrace();
		}
		
	}

	@Override
	public void newClient(SSLSocket client, SSLServerSocket srvr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosed(SSLSocket sck, IOException e) {
		// TODO Auto-generated method stub
		
	}

}
