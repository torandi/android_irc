package irc;

import java.io.IOException;
import java.io.PrintStream;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.torandi.lib.net.SSLSocketListener;
import com.torandi.lib.net.SSLSocketManager;

public class ClientSession implements SSLSocketListener, HandshakeCompletedListener {
	private SSLSocket socket;
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

	public ClientSession(SSLSocket socket) {
		this.socket = socket;
		mode = MODE.NEW;
		socket.addHandshakeCompletedListener(this);
	}

	public void close() {
		if(output != null) {
			try { output.println("CLOSE"); } catch (Exception e) {}
			output.flush();
		}
		
		try {
			socket.close();
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
				if(cmd.equals("AUTH")) {
					
				}
			} 
			println("Unhandled input: "+data+ " in mode "+mode.toString());
		} catch (Exception e) {
			println("Error while parsing data: "+e.getMessage());
		}
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
}
