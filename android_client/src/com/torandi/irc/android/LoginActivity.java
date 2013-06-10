package com.torandi.irc.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;
import android.content.Intent;

public class LoginActivity extends Activity {
	private IRCApplication application = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (IRCApplication) getApplication();
		
		setContentView(R.layout.activity_login);
		
		final TextView username = (TextView) findViewById(R.id.username);
		final TextView server = (TextView) findViewById(R.id.server_address);
		final TextView port = (TextView) findViewById(R.id.server_port);
		
		username.setText(application.getUsername());
		server.setText(application.getServer());
		port.setText("" + application.getPort());
		
	
		final Button button = (Button) findViewById(R.id.connect_btn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				application.setUsername(username.getText().toString());
				application.setServer(server.getText().toString());
				application.setPort(Integer.parseInt(port.getText().toString()));
				application.saveSettings();
				Intent intent = new Intent();
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

}
