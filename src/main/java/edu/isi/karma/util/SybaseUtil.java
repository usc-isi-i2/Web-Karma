package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SybaseUtil extends AbstractJDBCUtil {
	private static Logger logger = LoggerFactory
			.getLogger(SybaseUtil.class);
	
	static final String DRIVER = 
			"net.sourceforge.jtds.jdbc.Driver";
		
	static final String CONNECT_STRING_TEMPLATE = 
			"jdbc:jtds:sybase://host:port/dbname";

		
	@Override
	protected String getDriver() {
		return DRIVER;
	}

	@Override
	protected String getConnectStringTemplate() {
		return CONNECT_STRING_TEMPLATE;
	}

	public Connection getConnection(String driver, String url, String username, String pwd) throws SQLException, ClassNotFoundException {
		Connection localConn = null;
		Class.forName(driver);
		localConn = DriverManager.getConnection(url, username, pwd);
		return localConn;
	}
	

	public Connection getConnection(String hostname,
			int portnumber, String username, String password, String dBorSIDName)
			throws SQLException, ClassNotFoundException {
		String connectString = getConnectStringTemplate();
		connectString = connectString.replaceAll("host", hostname);
		connectString = connectString.replaceAll("port", Integer.toString(portnumber));
		connectString = connectString.replaceAll("dbname", dBorSIDName);
		
		logger.info("Connect to:"+ connectString);
		Connection conn = getConnection(getDriver(), connectString, username, password);
		return conn;
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
	
		Connection conn = getConnection(hostname, portnumber, username, password, dBorSIDName);
		String query = "Select top " + rowCount + " * from " + tableName;
		
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

	@Override
	public String escapeTablename(String name) {
		return "`" + name + "`";
	}

}
