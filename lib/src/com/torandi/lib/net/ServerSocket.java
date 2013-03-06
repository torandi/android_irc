package com.torandi.lib.net;

import java.io.IOException;

public class ServerSocket extends java.net.ServerSocket{
	private Thread thread = null;
	boolean listen = false;
	
	public ServerSocket(int port) throws IOException {
		super(port);
	}

	public Socket accept() throws IOException {
        Socket sck = new Socket();
        implAccept(sck);
        return sck;
	}
	
	private void handle_accept(final SocketListener listener) {
		try {
			listener.newClient(accept(), this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts listening 
	 */
	public void listen(final SocketListener listener, boolean blocking) {
		if(listen != false) {
			System.out.println("Can't listen: Already listening!");
			return;
		}
		listen = true;
		if(!blocking) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					final ServerSocket ss = ServerSocket.this;
					while(!Thread.interrupted()) {
						ss.handle_accept(listener);
					}
					try {
						ss.close();
					} catch (IOException e) { }
				}
			});
			thread.start();
		} else {
			while(listen) {
				handle_accept(listener);
			}
		}
	}
	
	public void stop_listen() {
		listen = false;
		if(thread != null) {
			thread.interrupt();
			try {
				thread.join();
				thread = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
