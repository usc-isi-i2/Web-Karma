package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class DatabaseTablePreviewUpdate extends AbstractUpdate {

	private AbstractJDBCUtil.DBType 	dbType;
	private String 			hostname;
	private int 			portnumber;
	private String 			username;
	private String 			password;
	private String 			dBorSIDName;
	private String 			commandId;
	private String 			tableName;
	
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
			
			ArrayList<ArrayList<String>> data = dbUtil.getDataForLimitedRows(dbType, hostname, 
					portnumber, username, password, tableName, dBorSIDName, 10);
			
			JSONStringer jsonStr = new JSONStringer();
			JSONWriter writer = jsonStr.object().key("commandId").value(commandId)
				.key("updateType").value("ImportDatabaseTablePreview").key("tableName").value(tableName);
			
			// Add the headers
			JSONArray arr = new JSONArray(data.get(0));
			writer.key("headers").value(arr);
			
			// Add the data
			JSONArray dataRows = new JSONArray();
			for(int i = 1; i<data.size(); i++) {
				dataRows.put(data.get(i));
			}
			writer.key("rows").value(dataRows);
			
			writer.endObject();
			pw.println(jsonStr.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "").replaceAll("\"","\\\"");
			ErrorUpdate er = new ErrorUpdate("databaseImportError", message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (ClassNotFoundException e) {
			// TODO Send error update
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "").replaceAll("\"","\\\"");
			ErrorUpdate er = new ErrorUpdate("databaseImportError", message);
			er.generateJson(prefix, pw, vWorkspace);
		} catch (JSONException e) {
			// TODO Send Error update
			e.printStackTrace();
		}
	}
}
