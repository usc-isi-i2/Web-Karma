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
package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.update.DatabaseTablePreviewUpdate;
import edu.isi.karma.controller.update.DatabaseTablesListUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.NewDatabaseCommandUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.database.DatabaseTableImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ImportDatabaseTableCommand extends CommandWithPreview {
	// Database Type
	private AbstractJDBCUtil.DBType dbType;
	
	// Hostname
	private String 	hostname;
	
	// Port Number
	private int 	portnumber;
	
	// Username
	private String 	username;
	
	// Password
	private String 	password;
	
	// Database name
	private String 	dBorSIDName;
	
	// Table name
	private String 	tableName;
	
	private InteractionType requestedInteractionType;
	
	// private static Logger logger = LoggerFactory.getLogger(ImportDatabaseTableCommand.class);
	
	protected enum InteractionType {
		generateTableList, importTable, getPreferencesValues, previewTable
	}

	protected ImportDatabaseTableCommand(String id, VWorkspace vWorkspace) {
		super(id);
	}

	public ImportDatabaseTableCommand(String id, String dbType,
			String hostname, int portNumber, String userName, String password,
			String dBorSIDName, VWorkspace vWorkspace) {
		super(id);
		this.dbType = AbstractJDBCUtil.DBType.valueOf(dbType);
		this.hostname = hostname;
		this.portnumber = portNumber;
		this.username = userName;
		this.password = password;
		this.dBorSIDName = dBorSIDName;
	}
	
	public void setRequestedInteractionType(InteractionType requestedInteractionType) {
		this.requestedInteractionType = requestedInteractionType;
	}

	public InteractionType getRequestedInteractionType() {
		return requestedInteractionType;
	}

	@Override
	public UpdateContainer showPreview(VWorkspace vWorkspace)
			throws CommandException {
		UpdateContainer c = new UpdateContainer();
		if(requestedInteractionType == InteractionType.getPreferencesValues) {
			c.add(new FetchPreferencesUpdate(vWorkspace, ImportDatabaseTableCommand.class.getSimpleName()+"Preferences"));
			return c;
		}
		
		return c;
	}

	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		InteractionType type = InteractionType.valueOf(request.getParameter("interactionType"));
		UpdateContainer c = new UpdateContainer();
		setRequestedInteractionType(type);
		switch (type) {
		case generateTableList: {
			dbType = DBType.valueOf(request.getParameter("dBType"));
			hostname = request.getParameter("hostname");
			username = request.getParameter("username");
			password= request.getParameter("password");
			dBorSIDName = request.getParameter("dBorSIDName");
			// Should have been properly validated at the client
			portnumber = Integer.parseInt(request.getParameter("portNumber"));
			
			c.add(new DatabaseTablesListUpdate(dbType, hostname, portnumber, username, password, dBorSIDName, id));
			return c;
		}
		case previewTable: {
			c.add(new DatabaseTablePreviewUpdate(dbType, hostname, portnumber, username, password, 
						request.getParameter("tableName"), dBorSIDName, id));
			return c;
		}
		case importTable: {
			tableName = request.getParameter("tableName");
		}
		}
		return c;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Import Database Table";
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			
			return tableName + " imported";
		} else {
			return "";
		}
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		setRequestedInteractionType(InteractionType.importTable);
		
		UpdateContainer c = new UpdateContainer();
		try {
			DatabaseTableImport  dbTableImport = new DatabaseTableImport(
					dbType, hostname, portnumber, username, password, dBorSIDName, 
					tableName, vWorkspace.getWorkspace());
			
			Worksheet wsht = dbTableImport.generateWorksheet();
			vWorkspace.addAllWorksheets();
			
			c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
			VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
			vw.update(c);
			
			// Create a new Database Import Command. The interface allows the user to import 
			// multiple tables
			ImportDatabaseTableCommand comm = new ImportDatabaseTableCommand(vWorkspace.getRepFactory().getNewId("C"), 
					dbType.name(), hostname, portnumber, username, password, dBorSIDName, vWorkspace);
			vWorkspace.getWorkspace().getCommandHistory().setCurrentCommand(comm);
			NewDatabaseCommandUpdate upd = new NewDatabaseCommandUpdate(comm);
			c.add(upd);
		} catch (Throwable e) {
			e.printStackTrace();
			String message = e.getMessage().replaceAll("\n", "").replaceAll("\"","\\\"");
			ErrorUpdate errUpdt = new ErrorUpdate(message);
			c.add(errUpdt);
		}
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Nothing to do! This command can't be undone.
		return null;
	}

}
