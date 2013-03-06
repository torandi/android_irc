package com.torandi.lib.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class SocketManager {
	/**
	 * Starts receiving (non-blocking)
	 */
	public static Thread receive(final SSLSocket socket, final SSLSocketListener listener) {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				BufferedReader stream = null;

				while(!Thread.interrupted()) {
					try {
						if(socket.isConnected()) {
							try {
								if(stream == null) {
									stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								}
								if(stream.ready()) {
									listener.dataRecived(stream.readLine(), socket);
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
		return thread;
	}
	
	
	/**
	 * Starts listening 
	 */
	public static Thread listen(final SSLServerSocket server, final SSLSocketListener listener) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					try {
						SSLSocket socket = (SSLSocket) server.accept();
						listener.newClient(socket, server);
					} catch (IOException e) { }
				}
			}
		});
		thread.start();
		return thread;
	}
}
