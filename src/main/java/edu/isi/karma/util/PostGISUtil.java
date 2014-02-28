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

public class PostGISUtil extends AbstractJDBCUtil {

	//private static Logger logger = LoggerFactory
	//.getLogger(MySQLUtil.class);

	static final String DRIVER = 
		"org.postgresql.Driver";
	
	//default port is 3306
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:postgresql://host:port/dbname?user=username&password=pwd";
		
	@Override
	public ArrayList<String> getListOfTables(Connection conn) 
			throws SQLException, ClassNotFoundException {
		
		ArrayList<String> tableNames = new ArrayList<String>();
		DatabaseMetaData dmd = conn.getMetaData();
		ResultSet rs = dmd.getTables(null, null, null, new String[] {"TABLE"});
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
		String query = "Select * from " + escapeTablename(tableName) + " limit " + rowCount;
		
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
		//s = name.replace('-', '_');
		s = "\"" + s + "\""; // use double quote for case-sensitive column names
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
