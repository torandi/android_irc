package com.torandi.irc.android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private IRCApplication application = null;
	private static final int LOGIN_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		application = (IRCApplication) getApplication();
		application.loadSettings();
		Log.d("AndroidIRC", "Username: "+application.getUsername() + ", server: "+application.getServer());
		if(application.getUsername() == null || application.getServer() == null) {
			Log.d("AndroidIRC", "Missing data, staring login intent");
			Intent intent = new Intent(this, LoginActivity.class);
			startActivityForResult(intent, LOGIN_REQUEST);
		} else {
			setupViewAndConnect();
		}
	}
	
	private void setupViewAndConnect() {
		setContentView(R.layout.activity_main);
		TextView server_and_port = (TextView) findViewById(R.id.server_and_port);
		server_and_port.setText(application.getServer() + ":" + application.getPort());
		Intent startClientIntent = new Intent();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK && requestCode == LOGIN_REQUEST) {
			setupViewAndConnect();
		}
	}

}
