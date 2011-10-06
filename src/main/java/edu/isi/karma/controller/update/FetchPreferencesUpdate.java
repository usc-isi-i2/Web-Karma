package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.ImportDatabaseTableCommand;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

public class FetchPreferencesUpdate extends AbstractUpdate {
	VWorkspace vWorkspace;
	String commandName;

	private static Logger logger = LoggerFactory.getLogger(FetchPreferencesUpdate.class);
	
	public FetchPreferencesUpdate(VWorkspace vWorkspace, String commandName) {
		this.vWorkspace = vWorkspace;
		this.commandName = commandName;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject = prefs.getCommandPreferencesJSONObject(ImportDatabaseTableCommand.class.getSimpleName());
		JSONObject responseObj = new JSONObject();
		try {
			responseObj.put("commandId", vWorkspace.getWorkspace().getCommandHistory().getCurrentCommand().getId());
			responseObj.put("updateType", "ImportDatabaseTableCommandPreferences");
			
			// Populate the preferences if there are any
			if(prefObject != null) {
				responseObj.put("PreferenceValues", prefObject);
			}
			pw.print(responseObj.toString());
		} catch (JSONException e) {
			logger.error("Error writing JSON values for the preference update!", e);
		}
	}

}
