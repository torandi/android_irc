package irc;

import irc.db.DatabaseConnection;

import com.torandi.lib.GetOpt;
import com.torandi.lib.GetOpt.ArgumentException;
import com.torandi.lib.GetOpt.Option;
import com.torandi.lib.GetOpt.ParsePair;
import com.torandi.lib.GetOpt.Option.arg_style;
import com.torandi.lib.StringWrapper;

public class Main {

	private final static int DEFAULT_PORT = 3167;
	private final static String DEFAULT_PASSWORD = "password";
	private final static String DEFAULT_KEYSTORE = System.getProperty("user.home") + "/.keystore";
	
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		String password = DEFAULT_PASSWORD;
		String keystore = DEFAULT_KEYSTORE;
		final Option[] options = {
			new Option("port", 'p', arg_style.REQUIRED_ARGUMENT, "Port to listen on (Default: "+DEFAULT_PORT+")"),
			new Option("keystore", 'k', arg_style.REQUIRED_ARGUMENT, "Keystore for ssl certificate (Default: "+DEFAULT_KEYSTORE+")"),
			new Option("password", 'w', arg_style.REQUIRED_ARGUMENT, "Password for keystore (Default: "+DEFAULT_PASSWORD+")"),
			new Option("help", 'h', arg_style.NO_ARGUMENT,  "Show this help"),
		};
		
		final GetOpt getopt = new GetOpt(options);
		
		ParsePair parse_pair = new ParsePair(args);
		StringWrapper arg = new StringWrapper();
		
		try {
			int opt;
			while((opt = getopt.parse(parse_pair, arg)) != -1) {
				switch(opt) {
				case 'p':
					port = Integer.parseInt(arg.str);
					break;
				case 'k':
					keystore = arg.str;
					break;
				case 'w':
					keystore = arg.str;
					break;
				case 'h':
					getopt.print_usage();
					return;
				}
			}
		} catch (ArgumentException e) {
			System.out.println(e.getMessage());
			getopt.print_usage();
			return;
		}
		
		if(!Config.load()) return;
		
		DatabaseConnection.setConfiguration(Config.get("database.url"), Config.get("database.username"), Config.get("database.password"));
		
		//Initialize database
		if(DatabaseConnection.get() == null) return;
		System.out.println("Database connected");
		
		new Server(port, keystore, password);
	}

}
