package edu.isi.karma.er.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectPostgis {

	private static Logger logger = LoggerFactory.getLogger(ConnectPostgis.class);
	public ConnectPostgis() {
	}

	public Connection ConnectingPostgis() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {

			logger.error("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!", e);

		}
		try {
			//connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgis", "postgres","dd251314");
			//TODO remove this connection!
			connection = DriverManager.getConnection("jdbc:postgresql://fusion.isi.edu:54322/testGIS","karma","2xpd516");
		} catch (SQLException e) {
			logger.error("Connection Failed! Check output console", e);
		}
		return connection;

	}
	public Connection ConnectingPostgis(String ConnectionString, String user, String password) {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {

			logger.error("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!", e);

		}
		try {
			connection = DriverManager.getConnection(
					ConnectionString, user,
					password);
		} catch (SQLException e) {
			logger.error("Connection Failed! Check output console", e);
		}
		return connection;

	}

}
