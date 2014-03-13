package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class PublishJSONCommandFactory extends CommandFactory implements JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, alignmentNodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		// Not needed
		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String alignmentNodeId = HistoryJsonUtil.getStringValue(Arguments.alignmentNodeId.name(), inputJson);
		PublishJSONCommand comm = new PublishJSONCommand(
				getNewId(workspace), alignmentNodeId, worksheetId);
		comm.setInputParameterJson(inputJson.toString());
		return comm;
	}
}
