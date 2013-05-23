package irc.server.model;

import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import irc.server.db.DatabaseObject;
import irc.server.db.ValidationException;

public class Server extends DatabaseObject<Server> {
	/* Data readers/writers */
	
	@Override
	protected String default_order() {
		return "id";
	}
	
	public Session connect(ConnectionManager connman, Profile profile) {
		System.out.println("Connecting to server "+getAddress()+":"+getPort());
		return connman.requestConnection(getAddress(), getPort(), profile);
	}
	
	public String getAddress() {
		return (String) get("address");
	}
	
	public void setAddress(String address) {
		set("address", address);
	}
	
	public int getNetworkId() {
		return get_int("network_id");
	}
	
	public void setNetworkId(int id) {
		set("network_id", id);
	}
	
	public Network getNetwork() {
		return Network.q().from_id(getNetworkId());
	}
	
	public int getPort() {
		return get_int("port");
	}
	
	public void setPort(int port) {
		set("port", port);
	}

	/* Object config */
	@Override
	protected Class<Server> cls() {
		return Server.class;
	}

	@Override
	protected String table_name() {
		return "servers";
	}
	
	public void validate() throws ValidationException {
		validateExistance("network_id");
		validateExistance("address");
		validateExistance("port");
	}
	

	/* Boilerplate */
	private static final Server query_obj = new Server();

	public static Server q() {
		return query_obj;
	}

}
