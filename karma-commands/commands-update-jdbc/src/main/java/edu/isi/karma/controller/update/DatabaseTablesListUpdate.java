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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;

import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class DatabaseTablesListUpdate extends AbstractUpdate {

	private DBType dbType;
	private String hostname;
	private int portnumber;
	private String username;
	private String password;
	private String dBorSIDName;
	private String commandId;

	public enum JsonKeys {
		commandId, headers, rows, fileName, dbType, hostname, portnumber, username, dBorSIDName, TableList
	}

	public DatabaseTablesListUpdate(DBType dbType,
			String hostname, int portnumber, String username, String password,
			String dBorSIDName, String commandId) {
		super();
		this.dbType = dbType;
		this.hostname = hostname;
		this.portnumber = portnumber;
		this.username = username;
		this.password = password;
		this.dBorSIDName = dBorSIDName;
		this.commandId = commandId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		ArrayList<String> listOfTables = null;
		try {
			AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);

			listOfTables = dbUtil.getListOfTables(dbType, hostname, portnumber,
					username, password, dBorSIDName);

			if (listOfTables == null) {
				// TODO Send special update
				return;
			}

			// Save the database connection preferences
			JSONObject prefObject = new JSONObject();
			prefObject.put(JsonKeys.dbType.name(), dbType.name());
			prefObject.put(JsonKeys.hostname.name(), hostname);
			prefObject.put(JsonKeys.portnumber.name(), portnumber);
			prefObject.put(JsonKeys.username.name(), username);
			prefObject.put(JsonKeys.dBorSIDName.name(), dBorSIDName);
			vWorkspace.getWorkspace().getCommandPreferences().setCommandPreferences(
					"ImportDatabaseTableCommand"+"Preferences", prefObject);

			JSONStringer jsonStr = new JSONStringer();
			JSONWriter writer = jsonStr.object().key(JsonKeys.commandId.name())
					.value(commandId).key(GenericJsonKeys.updateType.name())
					.value("GetDatabaseTableList");

			JSONArray dataRows = new JSONArray();
			dataRows.put(listOfTables);

			writer.key(JsonKeys.TableList.name()).value(dataRows);
			writer.endObject();
			pw.print(jsonStr.toString());

		} catch (SQLException e) {
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "")
					.replaceAll("\"", "\\\"");
			ErrorUpdate er = new ErrorUpdate(message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			ErrorUpdate er = new ErrorUpdate("The driver for " + dbType + " was not found. Please add the driver to the path and try again");
			er.generateJson(prefix, pw, vWorkspace);
		} catch (JSONException e) {
			String message = e.getMessage().replaceAll("\n", "")
					.replaceAll("\"", "\\\"");
			ErrorUpdate er = new ErrorUpdate(message);
			er.generateJson(prefix, pw, vWorkspace);
			e.printStackTrace();
		}
	}
}
