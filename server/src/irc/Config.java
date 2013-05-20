package irc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
	private static Properties properties = null;

	public static String get(String property) {
		return properties.getProperty(property);
	}
	
	public static String get(String property, String defaultValue) {
		return properties.getProperty(property, defaultValue);
	}
	
	public static boolean load() {
		properties = new Properties();
		try {
			InputStream is = new FileInputStream("server.cfg");
			properties.load(is);
			return true;
		} catch (IOException e) {
			System.out.println("Failed to load configuration: server.cfg");
			e.printStackTrace();
			return false;
		}
	}
}
