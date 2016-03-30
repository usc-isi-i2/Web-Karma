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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleUtil extends AbstractJDBCUtil {
	
	private static Logger logger = LoggerFactory.getLogger(OracleUtil.class);
	
	static final String DRIVER = 
		"oracle.jdbc.driver.OracleDriver";
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:oracle:thin:username/pwd@//host:port/dbname";

	@Override
	public ArrayList<String> getListOfTables(Connection conn)
			throws SQLException, ClassNotFoundException {
		ArrayList<String> tableNames = new ArrayList<>();
		
		Statement stmt = conn.createStatement(); 
	    ResultSet rs = stmt.executeQuery("select object_name from user_objects " +
	    		"where object_type = 'TABLE' or object_type = 'VIEW'");

	    while (rs.next()) {
	      String tableName = rs.getString(1);
	      tableNames.add(tableName);
	    }
		rs.close();
		stmt.close();
		
	    Collections.sort(tableNames);
		return tableNames;
	}

	@Override
	public ArrayList<ArrayList<String>> getDataForLimitedRows(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String tableName, String dBorSIDName, int rowCount)
			throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		Statement s = conn.createStatement();
		
		String query = "select * from " + escapeTablename(tableName) + " where rownum < " + rowCount;
		logger.info("Execute:" + query);
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
			String query, String dBorSIDName, int rowCount)
			throws SQLException, ClassNotFoundException {
		
		String connectString = getConnectString(hostname, portnumber, username, password, dBorSIDName);
		Connection conn = getConnection(DRIVER, connectString);
		
		Statement s = conn.createStatement();
		if(query.toLowerCase().indexOf(" where rownum") == -1) //Add limit only if it doesnt exist, else sql will be invalid
			query = query + " where rownum < " + rowCount;
		logger.info("Execute:" + query);
		
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
		return "\"" + name + "\"";
	}
	
	@Override
	protected String getDriver() {
		return DRIVER;
	}

	@Override
	protected String getConnectStringTemplate() {
		return CONNECT_STRING_TEMPLATE;
	}

}
