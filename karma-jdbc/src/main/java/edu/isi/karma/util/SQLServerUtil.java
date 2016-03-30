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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class SQLServerUtil extends AbstractJDBCUtil {

	private static Logger logger = LoggerFactory.getLogger(SQLServerUtil.class);

	static final String DRIVER = 
		"net.sourceforge.jtds.jdbc.Driver";
	
	//default port is 1433
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:jtds:sqlserver://host:port;databaseName=dbname;user=username;password=pwd;";
		
	@Override
	public ArrayList<String> getListOfTables(Connection conn) 
			throws SQLException, ClassNotFoundException {
		
		ArrayList<String> tableNames = new ArrayList<>();
		DatabaseMetaData dmd = conn.getMetaData();
		//Pedro: 2012/12/03 comment out to enable loading Views in Karma (on behalf of Maria)
		//ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE"});
		ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE","VIEW"});
		while (rs.next()) {
			String tablename = rs.getString(3);
			String schema = rs.getString(2);
			if(schema != null && schema.length() > 0)
				tableNames.add(schema + "." + tablename);
			else
				tableNames.add(tablename);
		}
		Collections.sort(tableNames);
		return tableNames;
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		String query = "Select TOP " + rowCount + " * from " + escapeTablename(tableName);
		logger.info("Execute:" + query);
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
	public ArrayList<ArrayList<String>> getSQLQueryDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String query, String dBorSIDName, int rowCount) throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		if(query.toUpperCase().indexOf("SELECT TOP") == -1) { //Add limit only if it doesnt exist, else sql will be invalid
			int idx = query.toUpperCase().indexOf("SELECT");
			query = query.substring(idx+6);
			query = "SELECT TOP " + rowCount + query;
		}
		logger.info("Execute:" + query);
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
		String s;
		s = name.replace('-', '_');
		s = "[" + s + "]";
		return s;
	}

	@Override
	public String escapeTablename(String name) {
		int idx = name.indexOf(".");
		if(idx != -1) {
			String schema = name.substring(0, idx);
			String tableName = name.substring(idx+1);
			return "[" + schema + "].[" + tableName + "]";
		}
		
		return "[" + name + "]";
	}
	
	@Override
	protected String getDriver() {
		return DRIVER;
	}

	@Override
	protected String getConnectStringTemplate() {
		return CONNECT_STRING_TEMPLATE;
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
		String schema = null;
		int idx = tableName.indexOf(".");
		if(idx != -1) {
			schema = tableName.substring(0, idx);
			tableName = tableName.substring(idx+1);
		}
		ResultSet rs = conn.getMetaData().getColumns(db, schema, tableName, null);
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
		String schema = null;
		int idx = tableName.indexOf(".");
		if(idx != -1) {
			schema = tableName.substring(0, idx);
			tableName = tableName.substring(idx+1);
		}
		ResultSet rs = conn.getMetaData().getColumns(db, schema, tableName, null);
		while(rs.next())
		{
			columnTypes.add(rs.getString("TYPE_NAME"));
		}
		return columnTypes;
	}
}
