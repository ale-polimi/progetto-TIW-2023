package it.polimi.tiw.progetto2023.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;

public class ConnectionManager {

	/**
	 * Opens a connection to the database.
	 * @param context is the context of the servlet.
	 * @return the new connection.
	 * @throws UnavailableException if the database can't be reached or the driver can't be loaded.
	 */
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection connection = null;
		
		try {
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Unable to load database driver.");
		} catch (SQLException e) {
			throw new UnavailableException("Unable to reach the database.");
		}
		
		return connection;
	}
	
	/**
	 * Closes a database connection.
	 * @param connection is the database connection to close.
	 * @throws SQLException if the database can't be reached.
	 */
	public static void closeConnection(Connection connection) throws SQLException {
		if(connection != null) {
			connection.close();
		}
	}

}
