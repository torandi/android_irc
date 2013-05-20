package irc;

public class User {
	
	private String username;
	
	public User(String username) {
		this.username = username;
	}
	
	public static User find(String username) {
		return new User(username);
	}
	
	public String username() {
		return username;
	}
}
