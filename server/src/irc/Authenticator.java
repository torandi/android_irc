package irc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Authenticator {
	public static User authenticate(String username, String hashed_password) throws InvalidCredentialsException, UserNotAllowedException {
		//TODO: Authenticate
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("authorized_users"))));
			String line;
			while((line = br.readLine()) != null) {
				if(line.trim().equals(username)) {
					br.close();
					return User.find(username);
				}
			}
			br.close();
		} catch (Exception e) {}
		throw new UserNotAllowedException(username);
	}
	
	public static class InvalidCredentialsException extends Exception {
		private static final long serialVersionUID = -40163067637898184L;

		public InvalidCredentialsException(String username) {
			super("Invalid username or password");
		}
	}
	
	public static class UserNotAllowedException extends Exception {
		private static final long serialVersionUID = -3822874406792533179L;

		public UserNotAllowedException(String user) {
			super("User "+user+" is not authorized to use this server");
		}
	}
}
