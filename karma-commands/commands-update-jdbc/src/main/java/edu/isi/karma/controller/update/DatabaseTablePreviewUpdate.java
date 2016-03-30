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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class DatabaseTablePreviewUpdate extends AbstractUpdate {

	private DBType dbType;
	private String 			hostname;
	private int 			portnumber;
	private String 			username;
	private String 			password;
	private String 			dBorSIDName;
	private String 			commandId;
	private String 			tableName;
	
	public enum JsonKeys {
		commandId, tableName, headers, rows
	}
	
	public DatabaseTablePreviewUpdate(DBType dbType, String hostname,
			int portnumber, String username, String password, String tableName,
			String dBorSIDName, String commandId) {
		super();
		this.dbType = dbType;
		this.hostname = hostname;
		this.portnumber = portnumber;
		this.username = username;
		this.password = password;
		this.dBorSIDName = dBorSIDName;
		this.commandId = commandId;
		this.tableName = tableName;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		try {
			AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);
			
			List<ArrayList<String>> data = dbUtil.getDataForLimitedRows(dbType, hostname, 
					portnumber, username, password, tableName, dBorSIDName, 10);
			
			JSONStringer jsonStr = new JSONStringer();
			JSONWriter writer = jsonStr.object().key(JsonKeys.commandId.name()).value(commandId)
				.key(GenericJsonKeys.updateType.name()).value("ImportDatabaseTablePreview").key(JsonKeys.tableName.name()).value(tableName);
			
			// Add the headers
			JSONArray arr = new JSONArray(data.get(0));
			writer.key(JsonKeys.headers.name()).value(arr);
			
			// Add the data
			JSONArray dataRows = new JSONArray();
			for(int i = 1; i<data.size(); i++) {
				dataRows.put(data.get(i));
			}
			writer.key(JsonKeys.rows.name()).value(dataRows);
			
			writer.endObject();
			pw.println(jsonStr.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "").replaceAll("\"","\\\"");
			ErrorUpdate er = new ErrorUpdate(message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (ClassNotFoundException e) {
			// TODO Send error update
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "").replaceAll("\"","\\\"");
			ErrorUpdate er = new ErrorUpdate(message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (JSONException e) {
			// TODO Send Error update
			e.printStackTrace();
		}
	}
}
