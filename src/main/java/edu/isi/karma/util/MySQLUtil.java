package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MySQLUtil extends AbstractJDBCUtil {

	static final String DRIVER = 
		"com.mysql.jdbc.Driver";
	
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:mysql://host:port/dbname?user=username&password=pwd";
	
	@Override
	public ArrayList<String> getListOfTables(DBType dbType, String hostname,
			int portnumber, String username, String password, String dBorSIDName) 
			throws SQLException, ClassNotFoundException {
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		ArrayList<String> tableNames = new ArrayList<String>();
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE"});
		while (rs.next())
			tableNames.add(rs.getString(3));
		
		return tableNames;
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForTable(DBType dbType, String hostname,
			int portnumber, String username, String password, String tableName, String dBorSIDName)
			throws SQLException, ClassNotFoundException {
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		return getDataForTable(conn, tableName);
	}

	private String getConnectString (String hostname, int portnumber, String username, String password, String dBorSIDName) {
		return CONNECT_STRING_TEMPLATE.replaceAll("host", hostname)
		.replaceAll("port", Integer.toString(portnumber))
		.replaceAll("dbname", dBorSIDName)
		.replaceAll("username", username)
		.replaceAll("pwd", password);
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		String query = "Select * from " + tableName + " limit " + rowCount;
		
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
}
