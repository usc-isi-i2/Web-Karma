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
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.view.VWorkspace;

public class PublishDatabaseCommand extends WorksheetSelectionCommand {
	private String hostName;
	private String port;
	private String dbName;
	private String userName;
	private String password;
	private String tableName;
	/**
	 * drop table; create new table;
	 */
	private boolean overwrite;
	private boolean insert;
	private DBType dbType;
	private AbstractJDBCUtil dbUtil;

	int numRowsNotInserted = 0;

	public enum JsonKeys {
		updateType, worksheetId, numRowsNotInserted
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishDatabaseCommand.class);

	public enum PreferencesKeys {
		dbType, dbName, hostName, userName, tableName,port, overwriteTable, insertTable
	}

	/**
	 * dbType one of MySQL, SQLServer, Oracle
	 */
	protected PublishDatabaseCommand(String id, String worksheetId,
			String dbType, String hostName, String port, String dbName,String userName,String password, String tableName, 
			String overwrite, String insert, String selectionId) {
		super(id,worksheetId, selectionId);
		this.hostName=hostName;
		this.dbName=dbName;
		this.userName=userName;
		this.password=password;
		this.port=port;
		this.overwrite = Boolean.valueOf(overwrite);
		this.insert = Boolean.valueOf(insert);
		this.dbType= DBType.valueOf(dbType);
		dbUtil = JDBCUtilFactory.getInstance(this.dbType);
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		savePreferences(workspace);
		

		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		//for now don't save a nested table (we may want to save it as multiple tables with foreign keys)
		if (worksheet.getHeaders().hasNestedTables()) {
			return new UpdateContainer(new ErrorUpdate("Saving of nested tables not supported!"));				
		}
		
		Connection conn = null;
		try{
			conn = dbUtil.getConnection(hostName, Integer.valueOf(port).intValue(), userName, password, dbName);

			//get a map of corresponding hNodeIds with their semantic types
			//if more than one sem type with same name append indices
			Map<String,String> colNamesMap = getDbColumnNames(worksheet);
			//all semanticTypes
			Collection<String> colNames = colNamesMap.values();
			//ONLY columns with given semantic types will be saved; unassigned columns are ignored
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
					numRowsNotInserted= insertInTable(worksheet, tableName, colNamesMap,conn);
				}
				else if(insert){
					logger.info("Insert in table: " + tableName);
					//insert in existing table
					numRowsNotInserted=insertInTable(worksheet, tableName, colNamesMap,conn);
				}
			}
			else{
				logger.info("Create new table: " + tableName);
				//create a new table
				createTable(tableName, colNames, conn);
				numRowsNotInserted=insertInTable(worksheet, tableName, colNamesMap,conn);
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
						outputObject.put(JsonKeys.worksheetId.name(),
								worksheetId);
						outputObject.put(JsonKeys.numRowsNotInserted.name(),
								numRowsNotInserted);
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
	/**
	 * Create a table given the list of column names; all types are VARCHAR.
	 * @param tableName
	 * @param colNames
	 * @param conn
	 * @throws SQLException
	 */
	private void createTable(String tableName, Collection<String> colNames,Connection conn) throws SQLException{
		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);
		// create the table
		String createQ = "create table " + tableName + "(";
		int i=0;
		for(String semType: colNames){
			//for now always VARCHAR
			String dbType = getDbType(semType);
			if (i++ > 0)
				createQ += ",";
			createQ += dbUtil.prepareName(semType) + " " + dbType;
		}
		createQ += ")";

		logger.debug("createQ " + createQ);

		dbUtil.execute(conn, createQ);		
	}
	
	//column names are the semantic types
		/**
		 * Create a table given the list of column names; all types are VARCHAR.
		 * @param tableName
		 * @param colNames
		 * @param conn
		 * @throws SQLException
		 */
		@SuppressWarnings("unused")
		private void createSpatialTable(String tableName, Collection<String> colNames,Connection conn) throws SQLException{
			//add escaping in case we have unusual chars
			tableName=dbUtil.prepareName(tableName);
			// create the table
			String createQ = "create table " + tableName + "(";
			int i=0;
			for(String semType: colNames){
				//for now always VARCHAR
				String dbType = getDbType(semType);
				if (i++ > 0)
					createQ += ",";
				createQ += dbUtil.prepareName(semType) + " " + dbType;
			}
			createQ += ")";

			logger.debug("createQ " + createQ);

			dbUtil.execute(conn, createQ);		
		}
	/**
	 * Drop the given table.
	 * @param tableName
	 * @param conn
	 * @throws SQLException
	 */
	private void dropTable(String tableName, Connection conn) throws SQLException{
		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);
		String dropQ = "drop table " + tableName;
		dbUtil.execute(conn, dropQ);		
	}
	
	/**
	 * Inserts the worksheet data in the DB table.
	 * @param w
	 * @param tableName
	 * @param colNamesMap
	 * 	key=hNodeId; val=column name (same as semantic type for this column)
	 * @param conn
	 * @return
	 * 		number of rows not inserted. A row is not inserted if we have a type mismatch.
	 * @throws SQLException
	 */
	private int insertInTable(Worksheet w, String tableName, Map<String, String> colNamesMap,Connection conn) throws SQLException{
		int numOfRowsNotInserted = 0;
		SuperSelection selection = getSuperSelection(w);
		//get col names for existing table
		//some databases are case sensitive when referring to column/table names, so we have
		//to use the "real" case in the queries
		//I don't do this check for now; not sure for which DBs it is required
		List<String> existingColNames = dbUtil.getColumnNames(null, tableName, conn);
		List<String> existingColTypes = dbUtil.getColumnTypes(null, tableName, conn);
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
		
		ArrayList<Row> rows = w.getDataTable().getRows(0, w.getDataTable().getNumRows(), selection);
		for(Row r:rows){
			//insert one row
			String insertRow = insertInTableRow(r, tableName, addTheseColumns, addTheseTypes);
			//returns null if that particular row could not be inserted
			//because there is a number column for which the values are not numbers
			if(insertRow!=null)
				dbUtil.executeUpdate(conn, insertRow);
			else numOfRowsNotInserted++;
		}
		return numOfRowsNotInserted;
	}
	
	/**
	 * @param r
	 * @param tableName
	 * @param colNamesMap
	 * 	key=hNodeId; val=column name (same as semantic type for this column)
	 * @param colTypesMap
	 * 	key=hNodeId; val=column type 
	 * @return
	 * 		number of rows not inserted. A row is not inserted if we have a type mismatch.
	 */
	private String insertInTableRow(Row r, String tableName,
			Map<String, String> colNamesMap, Map<String, String> colTypesMap) {

		//add escaping in case we have unusual chars
		tableName=dbUtil.prepareName(tableName);

		String colNames = "";
		String colValues = "";
		boolean firstCol=true;
		for(Map.Entry<String, Node> node: r.getNodesMap().entrySet()){
			//get value
			String val = node.getValue().getValue().asString();
			String hNodeId = node.getKey();
			//get column name
			String colName = colNamesMap.get(hNodeId);
			if(colName!=null){
				if(!firstCol){
					colNames +=",";
					colValues +=",";
				}
				colNames += dbUtil.prepareName(colName);
				//handle null vaules in worksheets
				if(val==null)
					val="";
				//escape string
				val = val.replaceAll("'", "''");
				if (isDbTypeString(colTypesMap.get(hNodeId))) {
					val = "'" + val + "'";
				} else {
					// it's a number
					if (val.trim().equals(""))
						val = null;
					else{
						//check that it is really a number
						try{
							Double.valueOf(val);
						}catch(NumberFormatException e){
							logger.error("Row not inserted:" + val + " is not a number as required by the database");
							return null;
						}
					}
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
		String dbType = "VARCHAR(5000)";
		return dbType;
	}
	
	/**
	 * Returns true if the DB type is a string; false otherwise.
	 * @param type
	 * @return
	 */
	private boolean isDbTypeString(String type){
		if(type.toLowerCase().contains("varchar") || type.toLowerCase().contains("bit"))
			return true;
		return false;
	}
	
	/**
	 * Returns a map where key = hnodeId; value=the semantic type used as the column name.
	 * @param w
	 * @return
	 * 	a map where key = hnodeId; value=the semantic type used as the column name
	 * 	<br> If multiple sem types with same name, use an index to distinguish them
	 */
	private Map<String, String> getDbColumnNames(Worksheet w){
		Map<String, String> result = new HashMap<String, String>();
		//index used when we have duplicate semantic types
		//we can't have multiple columns with same name
		int ind=1;
		HashSet<String> duplicates = new HashSet<String>();
		//key = hNodeId
		//get all sem types for this worksheet
		Map<String, SemanticType> st = w.getSemanticTypes().getTypes();
		for(Map.Entry<String, SemanticType> e: st.entrySet()){
			String type = e.getValue().getType().getLocalName();
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


	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	private void savePreferences(Workspace workspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.dbType.name(), dbType);
			prefObject.put(PreferencesKeys.dbName.name(), dbName);
			prefObject.put(PreferencesKeys.hostName.name(), hostName);
			prefObject.put(PreferencesKeys.tableName.name(), tableName);
			prefObject.put(PreferencesKeys.userName.name(), userName);
			prefObject.put(PreferencesKeys.port.name(), port);
			//although we save these we don't reload them (I just left them here in case we want to in the future)
			//look in publishDatabse.js
			prefObject.put(PreferencesKeys.overwriteTable.name(), overwrite);
			prefObject.put(PreferencesKeys.insertTable.name(), overwrite);
			workspace.getCommandPreferences().setCommandPreferences(
					"PublishDatabaseCommandPreferences", prefObject);
			
			/*
			logger.trace("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishDatabaseCommandPreferences");
			logger.trace("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
