package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;


public class SybaseUtil extends AbstractJDBCUtil {
 
	static final String DRIVER = 
			"net.sourceforge.jtds.jdbc.Driver";
		
	static final String CONNECT_STRING_TEMPLATE = 
			"jdbc:jtds:sybase://host:port/dbname?user=username&password=pwd";
		
		
	@Override
	protected String getDriver() {
		return DRIVER;
	}

	@Override
	protected String getConnectStringTemplate() {
		return CONNECT_STRING_TEMPLATE;
	}

	@Override
	public ArrayList<String> getListOfTables(Connection conn) 
			throws SQLException, ClassNotFoundException {
		
		ArrayList<String> tableNames = new ArrayList<String>();
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE","VIEW"});
		while (rs.next())
			tableNames.add(rs.getString(3));
		Collections.sort(tableNames);
		return tableNames;
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

	@Override
	public String prepareName(String name) {
		String s = name;
		s = name.replace('-', '_');
		s = "`" + s + "`";
		return s;
	}


}
