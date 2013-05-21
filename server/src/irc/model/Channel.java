package irc.model;

import irc.db.DatabaseObject;
import irc.db.ValidationException;

public class Channel extends DatabaseObject<Channel> {
	/* Data readers/writers */
	
	public String getName() {
		return (String) get("name");
	}
	
	public void setName(String name) {
		set("name", name);
	}
	
	public int getUserNetworkId() {
		return get_int("user_network_id");
	}
	
	public void setUserNetworkId(int id) {
		set("user_network_id", id);
	}
	
	public UserNetwork getUserNetwork() {
		return UserNetwork.q().from_id(getUserNetworkId());
	}

	/* Object config */
	@Override
	protected Class<Channel> cls() {
		return Channel.class;
	}

	@Override
	protected String table_name() {
		return "channels";
	}
	
	public void validate() throws ValidationException {
		validateExistance("name");
		validateExistance("user_network_id");
	}
	

	/* Boilerplate */
	private static final Channel query_obj = new Channel();

	public static Channel q() {
		return query_obj;
	}

}
