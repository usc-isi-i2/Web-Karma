/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 * ****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.DatabaseTablePreviewUpdate;
import edu.isi.karma.controller.update.DatabaseTablesListUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.NewDatabaseCommandUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.database.DatabaseTableImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.DBType;

public class ImportDatabaseTableCommand extends ImportCommand implements IPreviewable {
    // Database Type

    private DBType dbType;
    // Hostname
    private String hostname;
    // Port Number
    private int portnumber;
    // Username
    private String username;
    // Password
    private String password;
    // Database name
    private String dBorSIDName;
    // Table name
    private String tableName;
    private InteractionType requestedInteractionType;
 
    // private static Logger logger = LoggerFactory.getLogger(ImportDatabaseTableCommand.class);
    protected enum InteractionType {

        generateTableList, importTable, getPreferencesValues, previewTable
    }

    private enum PreferenceKeys {
    	dbType, hostname, portnumber, username, dBorSIDName;
    }
    
    protected ImportDatabaseTableCommand(String id, String model) {
        super(id, model);
    }

    protected ImportDatabaseTableCommand(String id, String model, String revisedId) {
        super(id, model, revisedId);
    }

    public ImportDatabaseTableCommand(String id, String model, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName) {
        super(id, model);
        this.dbType = DBType.valueOf(dbType);
        this.hostname = hostname;
        this.portnumber = portNumber;
        this.username = userName;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
    }

    public ImportDatabaseTableCommand(String id, String model, String revisedId, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName) {
        super(id, model, revisedId);
        this.dbType = DBType.valueOf(dbType);
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
    public UpdateContainer showPreview(HttpServletRequest request)
            throws CommandException {
        UpdateContainer c = new UpdateContainer();
        if (requestedInteractionType == InteractionType.getPreferencesValues) {
            c.add(new FetchPreferencesUpdate(ImportDatabaseTableCommand.class.getSimpleName() + "Preferences", this.getId()));
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
                password = request.getParameter("password");
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
            case getPreferencesValues: {
                break;
            }
            default:
                break;
        }
       
        return c;
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Import Database Table";
    }

    @Override
    public String getDescription() {
        if (isExecuted()) {

            return tableName + " imported";
        }
        return "";
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        setRequestedInteractionType(InteractionType.importTable);

        UpdateContainer c = super.doIt(workspace);
        try {
            // Create a new Database Import Command. The interface allows the user to import 
            // multiple tables
            ImportDatabaseTableCommand comm = new ImportDatabaseTableCommand(workspace.getFactory().getNewId("C"),
                    model,
            		dbType.name(), hostname, portnumber, username, password, dBorSIDName);
            workspace.getCommandHistory().addPreviewCommand(comm);
            NewDatabaseCommandUpdate upd = new NewDatabaseCommandUpdate(comm.getId());
            c.add(upd);
        } catch (Throwable e) {
            String message = e.getMessage().replaceAll("\n", "").replaceAll("\"", "\\\"");
            ErrorUpdate errUpdt = new ErrorUpdate(message);
            c.add(errUpdt);
        }
        return c;
    }

    @Override
    protected Import createImport(Workspace workspace) {
    	JSONObject prefObject = new JSONObject();
		prefObject.put(PreferenceKeys.dBorSIDName.name(), dBorSIDName);
		prefObject.put(PreferenceKeys.dbType.name(), dbType);
		prefObject.put(PreferenceKeys.hostname.name(), hostname);
		prefObject.put(PreferenceKeys.portnumber.name(), portnumber);
		prefObject.put(PreferenceKeys.username.name(), username);
		workspace.getCommandPreferences().setCommandPreferences(
				ImportDatabaseTableCommand.class.getSimpleName() + "Preferences", prefObject);
		
        return new DatabaseTableImport(
                    dbType, hostname, portnumber, username, password, dBorSIDName,
                    tableName, workspace, "");
    }
}
