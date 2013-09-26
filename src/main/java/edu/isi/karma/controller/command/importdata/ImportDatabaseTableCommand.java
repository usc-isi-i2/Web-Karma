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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.DatabaseTablePreviewUpdate;
import edu.isi.karma.controller.update.DatabaseTablesListUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.NewDatabaseCommandUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.database.DatabaseTableImport;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import javax.servlet.http.HttpServletRequest;

public class ImportDatabaseTableCommand extends ImportCommand implements IPreviewable {
    // Database Type

    private AbstractJDBCUtil.DBType dbType;
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

    protected ImportDatabaseTableCommand(String id) {
        super(id);
    }

    protected ImportDatabaseTableCommand(String id, String revisedId) {
        super(id, revisedId);
    }

    public ImportDatabaseTableCommand(String id, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName) {
        super(id);
        this.dbType = AbstractJDBCUtil.DBType.valueOf(dbType);
        this.hostname = hostname;
        this.portnumber = portNumber;
        this.username = userName;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
    }

    public ImportDatabaseTableCommand(String id, String revisedId, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName) {
        super(id, revisedId);
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
    public UpdateContainer showPreview()
            throws CommandException {
        UpdateContainer c = new UpdateContainer();
        if (requestedInteractionType == InteractionType.getPreferencesValues) {
            c.add(new FetchPreferencesUpdate(ImportDatabaseTableCommand.class.getSimpleName() + "Preferences"));
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
        // TODO Auto-generated method stub
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

        UpdateContainer c = new UpdateContainer();
        try {
            Import imp = new DatabaseTableImport(
                        dbType, hostname, portnumber, username, password, dBorSIDName,
                        tableName, workspace);
            
            Worksheet wsht = imp.generateWorksheet();
            
            if (hasRevisionId()) {
                Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
                wsht.setRevisedWorksheet(revisedWorksheet);
            } 

            c.add(new WorksheetListUpdate());
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
            // Create a new Database Import Command. The interface allows the user to import 
            // multiple tables
            ImportDatabaseTableCommand comm = new ImportDatabaseTableCommand(workspace.getFactory().getNewId("C"),
                    dbType.name(), hostname, portnumber, username, password, dBorSIDName);
            workspace.getCommandHistory().setCurrentCommand(comm);
            NewDatabaseCommandUpdate upd = new NewDatabaseCommandUpdate(comm);
            c.add(upd);
        } catch (Throwable e) {
            e.printStackTrace();
            String message = e.getMessage().replaceAll("\n", "").replaceAll("\"", "\\\"");
            ErrorUpdate errUpdt = new ErrorUpdate(message);
            c.add(errUpdt);
        }
        return c;
    }
}
