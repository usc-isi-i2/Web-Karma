package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

public class PublishRDFCellCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId,
		nodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		//get the rdf prefix from the preferences
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		String rdfPrefix = prefObject.optString("rdfPrefix");
		if(rdfPrefix==null || rdfPrefix.trim().isEmpty())
			rdfPrefix = "http://localhost/source/"; 
		String nodeId = request.getParameter(Arguments.nodeId.name());
		return new PublishRDFCellCommand(getNewId(vWorkspace), vWorksheetId, nodeId,
				rdfPrefix);
	}

}
