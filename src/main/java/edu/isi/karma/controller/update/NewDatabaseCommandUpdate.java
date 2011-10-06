package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ImportDatabaseTableCommand;
import edu.isi.karma.view.VWorkspace;

public class NewDatabaseCommandUpdate extends AbstractUpdate{
	private Command command;
	private static Logger logger = LoggerFactory.getLogger(NewDatabaseCommandUpdate.class);

	public NewDatabaseCommandUpdate(ImportDatabaseTableCommand command) {
		this.command = command;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject responseObj = new JSONObject();
		try {
			responseObj.put("commandId", command.getId());
			responseObj.put("updateType", "NewImportDatabaseTableCommandUpdate");
			pw.print(responseObj.toString(4));
		} catch (JSONException e) {
			logger.error("Error generating JSON!", e);
		}
	}

}
