package edu.isi.karma.controller.command.importdata;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.FetchPreferencesUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.SQLCommandUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.database.SQLImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.DBType;

public class ImportSQLCommand extends ImportCommand implements IPreviewable {
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
    //SQl query used for import
    private String query;
    private InteractionType requestedInteractionType;
 
    private enum PreferenceKeys {
    	dbType, hostname, portnumber, username, dBorSIDName, query;
    }
    protected enum InteractionType {

        importSQL, getPreferencesValues, previewSQL
    }

    protected ImportSQLCommand(String id, String model) {
        super(id, model);
    }

    protected ImportSQLCommand(String id, String model, String revisedId) {
        super(id, revisedId);
    }

    public ImportSQLCommand(String id, String model, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName, String query) {
        super(id, model);
        this.dbType = DBType.valueOf(dbType);
        this.hostname = hostname;
        this.portnumber = portNumber;
        this.username = userName;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
        this.query = query;
    }

    public ImportSQLCommand(String id, String model, String revisedId, String dbType,
            String hostname, int portNumber, String userName, String password,
            String dBorSIDName, String query) {
        super(id, model, revisedId);
        this.dbType = DBType.valueOf(dbType);
        this.hostname = hostname;
        this.portnumber = portNumber;
        this.username = userName;
        this.password = password;
        this.dBorSIDName = dBorSIDName;
        this.query = query;
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
            c.add(new FetchPreferencesUpdate(ImportSQLCommand.class.getSimpleName() + "Preferences", this.getId()));
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
            
            case previewSQL: {
//                c.add(new DatabaseTablePreviewUpdate(dbType, hostname, portnumber, username, password,
//                        request.getParameter("tableName"), dBorSIDName, id));
                return c;
            }
            case importSQL: {
            	dbType = DBType.valueOf(request.getParameter("dBType"));
                hostname = request.getParameter("hostname");
                username = request.getParameter("username");
                password = request.getParameter("password");
                dBorSIDName = request.getParameter("dBorSIDName");
                // Should have been properly validated at the client
                portnumber = Integer.parseInt(request.getParameter("portNumber"));
                query = request.getParameter("query");
                break;
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
        return "Import Using SQL";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        setRequestedInteractionType(InteractionType.importSQL);

        UpdateContainer c = super.doIt(workspace);
        try {
            // Create a new Database Import Command. The interface allows the user to import 
            // multiple tables
            ImportSQLCommand comm = new ImportSQLCommand(workspace.getFactory().getNewId("C"), model,
                    dbType.name(), hostname, portnumber, username, password, dBorSIDName, query);
            workspace.getCommandHistory().addPreviewCommand(comm);
            c.add(new InfoUpdate("Sucessfully imported data using SQL"));
            c.add(new SQLCommandUpdate(comm.getId()));
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
		prefObject.put(PreferenceKeys.query.name(), query);
		prefObject.put(PreferenceKeys.username.name(), username);
		workspace.getCommandPreferences().setCommandPreferences(
				ImportSQLCommand.class.getSimpleName() + "Preferences", prefObject);
		
        return new SQLImport(
                    dbType, hostname, portnumber, username, password, dBorSIDName,
                    query, workspace, "");
    }
	
}
