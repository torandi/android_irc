package irc.client;
public class Main implements ClientEventListener {
	private static final String PASSWORD = "password";
	private static String nick = "Torandi";
	private static String host = "localhost";
	private static int port = 3167;
	
 	public static void main(String[] args) {
 		Main main = new Main();
 		new Client(main, host, port, nick, "clientstore", PASSWORD);
 	}
 	
 	public Main() {
 		
 	}
}
