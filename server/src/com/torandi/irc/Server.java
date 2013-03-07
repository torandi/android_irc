package com.torandi.irc;

import java.io.IOException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.torandi.lib.net.SSLSocketListener;
import com.torandi.lib.net.SSLSocketManager;
import com.torandi.lib.net.SSLUtil;

public class Server implements SSLSocketListener {

	private SSLUtil ssl;
	private SSLServerSocket socket;
	private Thread socket_thread;
	
	
	public Server(int port, String keystore, String password) {
		try {
			ssl = new SSLUtil(keystore, password);
			socket = ssl.listen(port);
		} catch (Exception e) {
			System.err.println("Failed to setup ssl: "+e.getMessage());
			System.exit(-1);
		}
		socket_thread = SSLSocketManager.listen(socket, this);
	}
	
	@Override
	protected void finalize() throws Throwable {
		socket_thread.interrupt();
		socket.close();
		super.finalize();
	}
	
	@Override
	public void dataRecived(String data, SSLSocket sck) { }

	@Override
	public void newClient(SSLSocket client, SSLServerSocket srvr) {
		
	}

	@Override
	public void connectionClosed(SSLSocket sck, IOException e) { }

}
