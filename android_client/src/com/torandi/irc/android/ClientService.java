package com.torandi.irc.android;

import com.torandi.irc.client.Client;
import com.torandi.irc.client.ClientEventListener;
import com.torandi.irc.client.model.Channel;
import com.torandi.irc.client.model.LogLine;
import com.torandi.irc.client.model.Network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ClientService extends Service implements ClientEventListener {
	private IRCApplication application = null;
	
	public ClientService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		application = (IRCApplication) getApplication();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		application.setClient(new Client(this, application.getServer(), application.getPort(), application.getUsername(), "keystore", "password"));
		return START_STICKY;
	}

	@Override
	public void addNetwork(Network nw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNetwork(Network nw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeNetwork(Network nw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addChannel(Channel c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nickListUpdate(Channel c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newLine(Channel channel, LogLine line) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notAuthorized(String string) {
		// TODO Auto-generated method stub
		
	}
}
