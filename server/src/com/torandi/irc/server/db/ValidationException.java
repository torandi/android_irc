package com.torandi.irc.server.db;

public class ValidationException extends Exception {
	private static final long serialVersionUID = -8580488182659731429L;
	private String field, error;
	public ValidationException(String field, String error) {
		super("Error in field " + field + ": " + error);
		this.error = error;
		this.field = field;
	}
	
	public String getField() {
		return field;
	}

	public String getError() {
		return error;
	}
}
