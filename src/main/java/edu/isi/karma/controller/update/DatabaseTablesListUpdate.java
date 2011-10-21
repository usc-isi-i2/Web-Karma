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
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class DatabaseTablesListUpdate extends AbstractUpdate {

	private AbstractJDBCUtil.DBType dbType;
	private String hostname;
	private int portnumber;
	private String username;
	private String password;
	private String dBorSIDName;
	private String commandId;

	public DatabaseTablesListUpdate(AbstractJDBCUtil.DBType dbType,
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
			prefObject.put("dbType", dbType.name());
			prefObject.put("hostname", hostname);
			prefObject.put("portnumber", portnumber);
			prefObject.put("username", username);
			prefObject.put("dBorSIDName", dBorSIDName);
			vWorkspace.getPreferences().setCommandPreferences(
					"ImportDatabaseTableCommand", prefObject);

			JSONStringer jsonStr = new JSONStringer();
			JSONWriter writer = jsonStr.object().key("commandId")
					.value(commandId).key("updateType")
					.value("GetDatabaseTableList");

			JSONArray dataRows = new JSONArray();
			dataRows.put(listOfTables);

			writer.key("TableList").value(dataRows);
			writer.endObject();
			pw.print(jsonStr.toString());

		} catch (SQLException e) {
			// TODO Send error update
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "")
					.replaceAll("\"", "\\\"");
			ErrorUpdate er = new ErrorUpdate("databaseImportError", message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (ClassNotFoundException e) {
			// TODO Send error update
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Send Error update
			e.printStackTrace();
		}
	}
}
