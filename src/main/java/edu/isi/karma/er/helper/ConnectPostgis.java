package edu.isi.karma.er.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectPostgis {

	public ConnectPostgis() {
	}

	public Connection ConnectingPostgis() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {

			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();

		}
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/postgis", "postgres",
					"dd251314");
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		}
		return connection;

	}
	public Connection ConnectingPostgis(String ConnectionString, String user, String password) {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {

			System.out.println("Where is your PostgreSQL JDBC Driver? "
					+ "Include in your library path!");
			e.printStackTrace();

		}
		try {
			connection = DriverManager.getConnection(
					ConnectionString, user,
					password);
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		}
		return connection;

	}

}
