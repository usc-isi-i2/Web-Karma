package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.worksheet.AddColumnCommand.JsonKeys;
import edu.isi.karma.view.VWorkspace;

public class WorksheetDeleteUpdate extends AbstractUpdate {
	private String worksheetId;
	private static Logger logger =LoggerFactory.getLogger(WorksheetDeleteUpdate.class);
	
	public WorksheetDeleteUpdate(String worksheetId) {
		this.worksheetId = worksheetId;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject outputObject = new JSONObject();
		try {
			outputObject.put(JsonKeys.updateType.name(),
					"WorksheetDeleteUpdate");
			
			outputObject.put(JsonKeys.worksheetId.name(),
					worksheetId);
			pw.println(outputObject.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while generating JSON!");
		}

	}

}
