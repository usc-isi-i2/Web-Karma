package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public abstract class AbstractJDBCUtil {
	
	
	public enum DBType {
		Oracle, MySQL
	}

	public abstract ArrayList<String> getListOfTables(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String dBorSIDName) throws SQLException, ClassNotFoundException;
	
	public abstract ArrayList<ArrayList<String>> getDataForTable(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName) throws SQLException, ClassNotFoundException;
	
	public Connection getConnection(String driver, String connectString) throws SQLException, ClassNotFoundException {
		Connection localConn = null;
		Class.forName(driver);
		localConn = DriverManager.getConnection(connectString);
		return localConn;
	}
	
	public ArrayList<ArrayList<String>> getDataForTable(Connection conn, String tableName) throws SQLException {
		String query = "SELECT * FROM " + tableName;
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery(query);

		if (r == null) {
			s.close();
			return null;
		}
		
		ArrayList<ArrayList<String>> vals = parseResultSetIntoArrayListOfRows(r);
		
		r.close();
		s.close();
		return vals;
	}
	
	protected ArrayList<ArrayList<String>> parseResultSetIntoArrayListOfRows(ResultSet r) throws SQLException {
		ArrayList<ArrayList<String>> vals = new ArrayList<ArrayList<String>>();

		ResultSetMetaData meta = r.getMetaData();

		// Add the column names
		ArrayList<String> columnNamesRow = new ArrayList<String>();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			columnNamesRow.add(meta.getColumnName(i));
		}
		vals.add(columnNamesRow);
		
		// Add an ArrayList for each row
		while (r.next()) {
			ArrayList<String> row = new ArrayList<String>();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String val = r.getString(i);
				row.add(val);
			}
			vals.add(row);
		}
		return vals;
	}

	public abstract ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException;
}
