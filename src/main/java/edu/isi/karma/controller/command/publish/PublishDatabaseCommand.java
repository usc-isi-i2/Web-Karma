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
package edu.isi.karma.controller.command.publish;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class PublishDatabaseCommand extends Command {
	private final String vWorksheetId;
	private String dbTypeStr;
	private String hostName;
	private String port;
	private String dbName;
	private String userName;
	private String password;
	private String tableName;
	private boolean overwrite;
	private boolean insert;
	private AbstractJDBCUtil.DBType dbType;
	private AbstractJDBCUtil dbUtil;
	
	public enum JsonKeys {
		updateType, vWorksheetId
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishDatabaseCommand.class);

	public enum PreferencesKeys {
		dbType, dbName, hostName, userName, tableName,port, overwriteTable, insertTable
	}

	protected PublishDatabaseCommand(String id, String vWorksheetId,
			String dbTypeStr, String hostName, String port, String dbName,String userName,String password, String tableName, 
			String overwrite, String insert) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		this.dbTypeStr = dbTypeStr;
		this.hostName=hostName;
		this.dbName=dbName;
		this.userName=userName;
		this.password=password;
		this.port=port;
		this.overwrite = Boolean.valueOf(overwrite);
		this.insert = Boolean.valueOf(insert);
		if(dbTypeStr.equals("MySQL"))
			dbType = AbstractJDBCUtil.DBType.MySQL;
		if(dbTypeStr.equals("SQLServer"))
			dbType = AbstractJDBCUtil.DBType.SQLServer;
		dbUtil = JDBCUtilFactory.getInstance(dbType);
		logger.info("host=" + hostName);
		this.tableName = tableName;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish Database";
	}

	@Override
	public String getDescription() {
		return tableName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		
		//save the preferences 
		savePreferences(vWorkspace);

		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		Connection conn = null;
		try{
			conn = dbUtil.getConnection(hostName, Integer.valueOf(port).intValue(), userName, password, dbName);

			//get a map of corresponding hNodeIds with their semantic types
			Map<String,String> colNamesMap = getDbColumnNames(worksheet);
			Collection<String> colNames = colNamesMap.values();
			if(colNames.isEmpty()){
				//no columns were modeled
				return new UpdateContainer(new ErrorUpdate("Please align the worksheet before saving."));				
			}
			
			//see if table exists
			if (dbUtil.tableExists(tableName, conn)) {
				if(!overwrite && !insert){
					if(conn!=null)
						conn.close();
					return new UpdateContainer(new ErrorUpdate(
					"Table exists! Please check one of \"Overwrite Table\" or \"Insert in Table\"."));					
				}
				else if(overwrite){
					logger.info("Overwrite table: " + tableName);
					//delete old table & create a new table
					dropTable(tableName,conn);
					createTable(tableName, colNames, conn);
					insertInTable(worksheet, tableName, colNamesMap,conn);
				}
				else if(insert){
					logger.info("Insert in table: " + tableName);
					//insert in existing table
					insertInTable(worksheet, tableName, colNamesMap,conn);
				}
			}
			else{
				logger.info("Create new table: " + tableName);
				//create a new table
				createTable(tableName, colNames, conn);
				insertInTable(worksheet, tableName, colNamesMap,conn);
			}

			if(conn!=null)
				conn.close();
			
			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"PublishDatabaseUpdate");
						outputObject.put(JsonKeys.vWorksheetId.name(),
								vWorksheetId);
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
		} catch (Exception e) {
			try{
			if(conn!=null)
				conn.close();
			}catch(SQLException ex){}
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	//column names are the semantic types
	private void createTable(String tableName, Collection<String> colNames,Connection conn) throws SQLException{
		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);
		// create the table
		String createQ = "create table " + tableName + "(";
		int i=0;
		for(String semType: colNames){
			String dbType = getDbType(semType);
			if (i++ > 0)
				createQ += ",";
			createQ += dbUtil.prepareName(semType) + " " + dbType;
		}
		createQ += ")";

		logger.debug("createQ " + createQ);

		dbUtil.execute(conn, createQ);		
	}
	
	private void dropTable(String tableName, Connection conn) throws SQLException{
		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);
		String dropQ = "drop table " + tableName;
		dbUtil.execute(conn, dropQ);		
	}
	
	private void insertInTable(Worksheet w, String tableName, Map<String, String> colNamesMap,Connection conn) throws SQLException{
		//get col names for existing table
		//some databases are case sensitive when referring to column/table names, so we have
		//to use the "real" case in the queries
		ArrayList<String> existingColNames = dbUtil.getColumnNames(tableName, conn);
		ArrayList<String> existingColTypes = dbUtil.getColumnTypes(tableName, conn);
		//add in the insert only values for these columns; other columns do not exist in the remote table
		Map<String, String> addTheseColumns = new HashMap<String, String>();
		//the types of the columns 
		Map<String, String> addTheseTypes = new HashMap<String, String>();

		for(Map.Entry<String, String> colNameInWorksheet: colNamesMap.entrySet()){
			String semType = colNameInWorksheet.getValue();
			//is it in the existing columns?
			int ind = existingColNames.indexOf(semType);
			if(ind>=0){
				//it is there
				addTheseColumns.put(colNameInWorksheet.getKey(), semType);
				addTheseTypes.put(colNameInWorksheet.getKey(),existingColTypes.get(ind));
			}
		}
		
		ArrayList<Row> rows = w.getDataTable().getRows(0, w.getDataTable().getNumRows());
		for(Row r:rows){
			//construct the values map
			String insertRow = insertInTableRow(r, tableName, addTheseColumns, addTheseTypes);
			dbUtil.executeUpdate(conn, insertRow);
		}
	}
	
	private String insertInTableRow(Row r, String tableName,
			Map<String, String> colNamesMap, Map<String, String> colTypesMap) {

		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);

		String colNames = "";
		String colValues = "";
		boolean firstCol=true;
		for(Map.Entry<String, Node> node: r.getNodesMap().entrySet()){
			String val = node.getValue().getValue().asString();
			String hNodeId = node.getKey();
			String colName = colNamesMap.get(hNodeId);
			if(colName!=null){
				if(!firstCol){
					colNames +=",";
					colValues +=",";
				}
				colNames += dbUtil.prepareName(colName);
				//escape string
				val = val.replaceAll("'", "''");
				if (isDbTypeString(colTypesMap.get(hNodeId))) {
					val = "'" + val + "'";
				} else {
					// it's a number
					if (val.trim().equals(""))
						val = null;
				}

				colValues += val;
			}
			else{
				//don't insert this column
			}			
			firstCol=false;
		}

		String insertQ="insert into " + tableName + "(" + colNames + ") values (" + colValues + ")";
		
		//logger.info("insertQ=" + insertQ);
		
		return insertQ;
	}

	// for now everything is a string
	private String getDbType(String semType) {
		String dbType = "VARCHAR(1000)";
		return dbType;
	}
	
	private boolean isDbTypeString(String type){
		if(type.toLowerCase().contains("varchar") || type.toLowerCase().contains("bit"))
			return true;
		return false;
	}
	
	//key = hnodeId; value=the semantic type used as the column name
	//if multiple sem types with same name, use an index to distinguish them
	private Map<String, String> getDbColumnNames(Worksheet w){
		Map<String, String> result = new HashMap<String, String>();
		//index used when we have duplicate semantic types
		//we can't have multiple columns with same name
		int ind=1;
		HashSet<String> duplicates = new HashSet<String>();
		//key = hNodeId
		Map<String, SemanticType> st = w.getSemanticTypes().getTypes();
		for(Map.Entry<String, SemanticType> e: st.entrySet()){
			String type = e.getValue().getType();
			int index = type.indexOf("#");
				type=type.substring(index+1);
			if(duplicates.add(type)){
				//not a duplicate
				result.put(e.getKey(),type);
			}
			else{
				//it's a duplicate
				result.put(e.getKey(),type+(ind++));
			}
		}
		return result;
	}

	private void savePreferences(VWorkspace vWorkspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.dbType.name(), dbTypeStr);
			prefObject.put(PreferencesKeys.dbName.name(), dbName);
			prefObject.put(PreferencesKeys.hostName.name(), hostName);
			prefObject.put(PreferencesKeys.tableName.name(), tableName);
			prefObject.put(PreferencesKeys.userName.name(), userName);
			prefObject.put(PreferencesKeys.port.name(), port);
			prefObject.put(PreferencesKeys.overwriteTable.name(), overwrite);
			prefObject.put(PreferencesKeys.insertTable.name(), overwrite);
			vWorkspace.getPreferences().setCommandPreferences(
					"PublishDatabaseCommandPreferences", prefObject);
			
			/*
			System.out.println("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishDatabaseCommandPreferences");
			System.out.println("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
