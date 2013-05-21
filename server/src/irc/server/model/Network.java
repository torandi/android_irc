package irc.server.model;

import irc.server.db.DatabaseObject;
import irc.server.db.ValidationException;

public class Network extends DatabaseObject<Network> {
	/* Data readers/writers */
	
	public String getName() {
		return (String) get("name");
	}
	
	public void setName(String name) {
		set("name", name);
	}

	/* Object config */
	@Override
	protected Class<Network> cls() {
		return Network.class;
	}

	@Override
	protected String table_name() {
		return "networks";
	}
	
	public void validate() throws ValidationException {
		validateExistance("name");
		validateUniqueness("name");
	}
	

	/* Boilerplate */
	private static final Network query_obj = new Network();

	public static Network q() {
		return query_obj;
	}

}
