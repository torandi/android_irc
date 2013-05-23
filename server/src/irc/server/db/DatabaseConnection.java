package irc.server.db;

import java.sql.*;

public class DatabaseConnection {
	private Connection connection;
	private static DatabaseConnection singleton = null;
	private static String url = null, username = null, password = null;
	private static String driver = "com.mysql.jdbc.Driver";
	
	private DatabaseConnection() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName(driver).newInstance();
        connection = DriverManager.getConnection(url,username, password);
	}
	
	public static void setConfiguration(String url, String username, String password) {
		DatabaseConnection.url = url;
		DatabaseConnection.username = username;
		DatabaseConnection.password = password;
	}
	 
	public static DatabaseConnection get() {
		if(singleton == null) {
			try {
				singleton = new DatabaseConnection();
			} catch (Exception e) {
				System.out.println("Failed to connect to database");
				e.printStackTrace();
			}
		}
		return singleton;
	}
	
	public PreparedStatement prepareStatement(String query) throws SQLException {
		return connection.prepareStatement(query);
	}

	public PreparedStatement prepareStatement(String query,
			int returnGeneratedKeys) throws SQLException {
			return connection.prepareStatement(query, returnGeneratedKeys);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public DatabaseMetaData getMetaData() {
		try {
			return connection.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
