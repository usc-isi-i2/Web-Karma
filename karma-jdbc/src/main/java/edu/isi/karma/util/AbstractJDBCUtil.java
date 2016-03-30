/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJDBCUtil {
	
	private static Logger logger = LoggerFactory
	.getLogger(AbstractJDBCUtil.class);

	protected abstract String getDriver();
	protected abstract String getConnectStringTemplate();
	public abstract String escapeTablename(String name);
	
	/**
	 * Enclose input string between escape chars specific for each type of DB.
	 * @param name
	 * @return
	 */
	public abstract String prepareName(String name);
	public abstract ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException;
	public abstract ArrayList<ArrayList<String>> getSQLQueryDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String query, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException;

	public abstract ArrayList<String> getListOfTables(Connection conn) throws SQLException, ClassNotFoundException;

	protected Connection getConnection(String driver, String connectString) throws SQLException, ClassNotFoundException {
		Connection localConn;
		Class.forName(driver);
		localConn = DriverManager.getConnection(connectString);
		return localConn;
	}
	

	public Connection getConnection(String hostname,
			int portnumber, String username, String password, String dBorSIDName)
			throws SQLException, ClassNotFoundException {
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		logger.debug("Connect to:" + hostname + ":" +portnumber + "/" + dBorSIDName);
		logger.debug("Conn string:"+ connectString);
		Connection conn = getConnection(getDriver(), connectString);
		return conn;
	}
	
	protected String getConnectString (String hostname, int portnumber, String username, String password, String dBorSIDName) {
		String connectString = getConnectStringTemplate();
		connectString = connectString.replaceAll("host", hostname);
		connectString = connectString.replaceAll("port", Integer.toString(portnumber));
		connectString = connectString.replaceAll("dbname", dBorSIDName);
		connectString = connectString.replaceAll("username", username);
		//passwords could have special chars that are not being handles properly in 
		//reg expr; so for pwd do the replace differently
		int pwdInd = connectString.indexOf("pwd");
		if(pwdInd>=0){
			connectString = connectString.substring(0,pwdInd)+password+connectString.substring(pwdInd+3);
		}
		return connectString;
	}

	public ArrayList<String> getListOfTables(DBType dbType, String hostname,
			int portnumber, String username, String password, String dBorSIDName) 
			throws SQLException, ClassNotFoundException {
		Connection conn = getConnection(hostname, portnumber, username, password, dBorSIDName);
		return getListOfTables(conn);
	}

	public ArrayList<ArrayList<String>> getDataForTable(DBType dbType, String hostname,
			int portnumber, String username, String password, String tableName, String dBorSIDName)
			throws SQLException, ClassNotFoundException {

		Connection conn = getConnection(hostname, portnumber, username, password, dBorSIDName);
		return getDataForTable(conn, tableName);
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
	

	public ArrayList<ArrayList<String>> getDataForQuery(DBType dbType, String hostname,
			int portnumber, String username, String password, String tableName, String dBorSIDName)
			throws SQLException, ClassNotFoundException {

		Connection conn = getConnection(hostname, portnumber, username, password, dBorSIDName);
		return getDataForQuery(conn, tableName);
	}
	
	ArrayList<ArrayList<String>> getDataForQuery(Connection conn, String query) throws SQLException {
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
	
	
	
	/*
	 * Only warn about SQL exception once. //Pedro
	 */
	private static boolean warnedSqlException = false;
	
	protected ArrayList<ArrayList<String>> parseResultSetIntoArrayListOfRows(ResultSet r) throws SQLException {
		ArrayList<ArrayList<String>> vals = new ArrayList<>();

		ResultSetMetaData meta = r.getMetaData();

		// Add the column names
		ArrayList<String> columnNamesRow = new ArrayList<>();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			columnNamesRow.add(meta.getColumnName(i));
		}
		vals.add(columnNamesRow);
		
		// Add an ArrayList for each row
		ArrayList<String> row;
		while ((row = parseResultSetRow(r)) != null) {
			vals.add(row);
		}
		
		return vals;
	}
	
	public ArrayList<String> parseResultSetRow(ResultSet r) throws SQLException {
		if(r.next()) {
			ResultSetMetaData meta = r.getMetaData();
			ArrayList<String> row = new ArrayList<>();
			for (int i = 1; i <= meta.getColumnCount(); i++) {

				String val;
				try {
					val = r.getString(i);
				} catch (SQLException e) {
					if (!warnedSqlException) {
						logger.warn(e.getMessage());
						warnedSqlException = true;
					}
					val = "SQLException";
				}
				row.add(val);
			}
			return row;
		}
		return null;
	}
	
	/**
	 * Returns true if given table exists in DB; false otherwise.
	 * @param tableName
	 * @param conn
	 * @return
	 * 		true if given table exists in DB; false otherwise.
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public boolean tableExists(String tableName, Connection conn) throws SQLException, ClassNotFoundException {
		List<String> tn = getListOfTables(conn);

		if (tn.contains(tableName)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns the names of the columns for the specified table
	 * @param db
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public List<String> getColumnNames(String db, String tableName, Connection conn) throws SQLException
	{
		List<String> columnNames = new ArrayList<>(10);
		ResultSet rs = conn.getMetaData().getColumns(db, null, tableName, null);
		while(rs.next())
		{
			columnNames.add(rs.getString("COLUMN_NAME"));
		}
		return columnNames;
	}

	/**
	 * Returns the column types for a given table.
	 * @param tableName
	 * @param conn
	 * @return
	 * 		column types for a given table.
	 * @throws SQLException
	 */
	public List<String> getColumnTypes(String db, String tableName, Connection conn) throws SQLException
	{
		List<String> columnTypes = new ArrayList<>(10);
		ResultSet rs = conn.getMetaData().getColumns(db, null, tableName, null);
		while(rs.next())
		{
			columnTypes.add(rs.getString("TYPE_NAME"));
		}
		return columnTypes;
	}
	
	/**
	 * 	Executes a SQL query.
	 * @param conn
	 * @param query
	 * @throws SQLException
	 */
	public void execute(Connection conn, String query) throws SQLException {

		if (conn != null) {
			//logger.debug("query=" + query);

			try {
				Statement s = conn.createStatement();
				s.execute(query);
				s.close();
			} catch (SQLException e) {
				logger.error("sendSQL ..." + query);
				logger.error("MSG=" + e.getMessage());
				logger.error("STATE=" + e.getSQLState());
				if (query.startsWith("drop")
						&& e.getMessage().startsWith("Unknown table")) {
				} else {
					throw e;
				}
			}
		}
	}

	/**
	 * Executes an update query. (e.g. insert ...)
	 * @param conn
	 * @param query
	 * @throws SQLException
	 */
	public void executeUpdate(Connection conn, String query) throws SQLException {
		if (conn != null) {
			logger.debug("query=" + query);

			try {
				Statement s = conn.createStatement();
				s.executeUpdate(query);
				s.close();
			} catch (SQLException e) {
				if (query.startsWith("insert")
						&& e.getMessage().startsWith("Duplicate entry")) {
				} else {
					logger.debug("sendSQL ..." + query);
					logger.debug("MSG=" + e.getMessage());
					logger.debug("STATE=" + e.getSQLState());
					logger.error("Error occured while executing update!", e);
					throw e;
				}
			}
		}
	}

}
