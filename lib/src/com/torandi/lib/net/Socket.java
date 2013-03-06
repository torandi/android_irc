package com.torandi.lib.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;

public class Socket extends java.net.Socket {
	private Thread thread = null;
	private PrintStream ostream = null;
	
	public Socket() {
		super();
	}
	
	public Socket(String server, int port) throws UnknownHostException, IOException {
		super(server, port);
	}

	public final PrintStream out() {
		if(ostream == null && isConnected()) {
			try {
				ostream = new PrintStream(getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ostream;
	}
	
	/**
	 * Starts receiving (non-blocking)
	 */
	public void receive(final SocketListener listener) {
		if(thread != null) {
			System.out.println("Can't recieve: Already receiving!");
			return;
		}
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				final Socket s = Socket.this;
				BufferedReader stream = null;

				while(!Thread.interrupted()) {
					try {
						if(s.isConnected()) {
							try {
								if(stream == null) {
									stream = new BufferedReader(new InputStreamReader(s.getInputStream()));
								}
								if(stream.ready()) {
									listener.dataRecived(stream.readLine(), s);
								} else {
								}
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
		
						Thread.sleep(100);
						
					} catch (InterruptedException e) {
						return;
					}
				}
				
			}
		});
		thread.start();
	}
	
	public void stop_recieve() {
		if(thread != null) {
			thread.interrupt();
			try {
				thread.join();
				thread = null;
			} catch (InterruptedException e) {
				thread = null;
				return;
			}
		}
	}
}
