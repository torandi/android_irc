package irc.server.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;

import jerklib.Profile;

import irc.server.db.DatabaseObject;
import irc.server.db.ValidationException;

public class User extends DatabaseObject<User> {
	
	public static Profile createProfile(String name) {
		return new Profile(name, name+"_", name+"__", name+"___");
	}
	
	public Profile createProfile() {
		return createProfile(getNick());
	}
	
	public boolean isHilight(String message) {
		/* TODO Handle a list of possible hilight nicks */
		return message.contains(getNick());
	}
	
	/* Data readers/writers */
	
	public String getNick() {
		return (String) get("nick");
	}
	
	public void setNick(String nick) {
		set("nick", nick);
	}
	
	public String getFingerprint() {
		return (String) get("fingerprint");
	}
	
	public void setFingerprint(String fingerprint) {
		set("fingerprint", fingerprint);
	}
	
	public ArrayList<UserNetwork> getNetworks() throws SQLException {
		return UserNetwork.q().find("user_id", id());
	}
	
	public static ArrayList<String> authorizedUsers() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("authorized_users"))));
			String line;
			while((line = br.readLine()) != null) {
				String[] split = line.split("@"); // nick@fingerprint
				list.add(split[1].trim());
			}
			br.close();
		} catch (Exception e) {
			System.err.println("Failed to read authorized_users");
		}
		return list;
	}
	
	public static User authenticate(String username, String fingerprint) throws UserNotAllowedException, SQLException, ValidationException {
		ArrayList<String> authorizedUsers = authorizedUsers();
	
		if(authorizedUsers.contains(fingerprint)) {
			User u = q().first("fingerprint", fingerprint);
			if(u == null) {
				/* Create new user */
				u = new User();
				u.setNick(username);
				u.setFingerprint(fingerprint);
				u.commit();
			}
			return u;
		}
		throw new UserNotAllowedException(username, fingerprint);
	}
	
	public static class UserNotAllowedException extends Exception {
		private static final long serialVersionUID = -3822874406792533179L;
		public String fingerprint;
		public String username;

		public UserNotAllowedException(String user, String fingerprint) {
			super("User "+user+"@"+fingerprint+" is not authorized to use this server.");
			this.fingerprint = fingerprint;
			this.username = user;
		}
	}

	/* Object config */
	@Override
	protected Class<User> cls() {
		return User.class;
	}

	@Override
	protected String table_name() {
		return "users";
	}
	
	public void validate() throws ValidationException {
		validateExistance("nick");
		validateExistance("fingerprint");
		validateUniqueness("nick");
		validateUniqueness("fingerprint");
	}
	

	/* Boilerplate */
	private static final User query_obj = new User();

	public static User q() {
		return query_obj;
	}

}
