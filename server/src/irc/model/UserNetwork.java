package irc.model;

import irc.db.DatabaseObject;
import irc.db.ValidationException;

public class UserNetwork extends DatabaseObject<UserNetwork> {
	/* Data readers/writers */
	
	public String getNick() {
		return (String) get("nick");
	}
	
	public void setNick(String nick) {
		set("nick", nick);
	}
	
	public int getNetworkId() {
		return get_int("network_id");
	}
	
	public void setNetworkId(int id) {
		set("network_id", id);
	}
	
	public int getUserId() {
		return get_int("user_id");
	}
	
	public void setUserId(int id) {
		set("user_id", id);
	}
	
	public Network getNetwork() {
		return Network.q().from_id(getNetworkId());
	}
	
	public User getUser() {
		return User.q().from_id(getUserId());
	}

	/* Object config */
	@Override
	protected Class<UserNetwork> cls() {
		return UserNetwork.class;
	}

	@Override
	protected String table_name() {
		return "user_networks";
	}
	
	public void validate() throws ValidationException {
		validateExistance("network_id");
		validateExistance("user_id");
	}
	

	/* Boilerplate */
	private static final UserNetwork query_obj = new UserNetwork();

	public static UserNetwork q() {
		return query_obj;
	}

}
