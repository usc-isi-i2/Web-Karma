package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class UnfoldCommandFactory extends CommandFactory implements JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hTableId, hNodeId, newColumnName, defaultValue
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		return new UnfoldCommand(getNewId(workspace), worksheetId, 
				hTableId, hNodeId);
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeID = null;
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hTableId = "";
		//System.out.println(worksheetId);
		UnfoldCommand unfoldCmd = new UnfoldCommand(getNewId(workspace), worksheetId,
				hTableId, hNodeID);
		unfoldCmd.setInputParameterJson(inputJson.toString());
		return unfoldCmd;
	}

}
