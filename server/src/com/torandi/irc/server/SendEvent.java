package com.torandi.irc.server;

public interface SendEvent {
	public static enum Priority {
		NORMAL,			/* All normal chat lines, including user quits etc */
		STATUS_CHANGE,	/* Lost connection, kicked etc */
		HIGH			/* Hilights */
	};
	public void sendLine(Priority priority, String line);
}
