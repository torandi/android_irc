package irc.db;

public class ValidationException extends Exception {
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
