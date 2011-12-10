package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

public class OracleUtil extends AbstractJDBCUtil {
	
	static final String DRIVER = 
		"oracle.jdbc.driver.OracleDriver";
	
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:oracle:thin:username/pwd@//host:port/sid";

	@Override
	public ArrayList<String> getListOfTables(DBType dbType, String hostname,
			int portnumber, String username, String password, String dBorSIDName)
			throws SQLException, ClassNotFoundException {
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		ArrayList<String> tableNames = new ArrayList<String>();
		
		Statement stmt = conn.createStatement(); 
	    ResultSet rs = stmt.executeQuery("select object_name from user_objects " +
	    		"where object_type = 'TABLE'");

	    while (rs.next()) {
	      String tableName = rs.getString(1);
	      tableNames.add(tableName);
	    }
	    Collections.sort(tableNames);
		return tableNames;
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForTable(DBType dbType, String hostname,
			int portnumber, String username, String password, String tableName,
			String dBorSIDName) throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		return getDataForTable(conn, tableName);
	}
	
	private String getConnectString (String hostname, int portnumber, String username, String password, String dBorSIDName) {
		return CONNECT_STRING_TEMPLATE.replaceAll("host", hostname)
		.replaceAll("port", Integer.toString(portnumber))
		.replaceAll("sid", dBorSIDName)
		.replaceAll("username", username)
		.replaceAll("pwd", password);
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount)
			throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		Statement s = conn.createStatement();
		ResultSet r = s.executeQuery("select * from " + tableName + " where rownum < " + rowCount);

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
