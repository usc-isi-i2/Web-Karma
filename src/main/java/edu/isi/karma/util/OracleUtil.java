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

public class OracleUtil extends AbstractJDBCUtil {
	
	static final String DRIVER = 
		"oracle.jdbc.driver.OracleDriver";
	static final String CONNECT_STRING_TEMPLATE = 
		"jdbc:oracle:thin:username/pwd@//host:port/dbname";

	@Override
	public ArrayList<String> getListOfTables(Connection conn)
			throws SQLException, ClassNotFoundException {
		ArrayList<String> tableNames = new ArrayList<String>();
		
		Statement stmt = conn.createStatement(); 
	    ResultSet rs = stmt.executeQuery("select object_name from user_objects " +
	    		"where object_type = 'TABLE' or object_type = 'VIEW'");

	    while (rs.next()) {
	      String tableName = rs.getString(1);
	      tableNames.add(tableName);
	    }
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
	
	@Override
	public String prepareName(String name) {
		String s = name;
		s = name.replace('-', '_');
		s = "`" + s + "`";
		return s;
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
