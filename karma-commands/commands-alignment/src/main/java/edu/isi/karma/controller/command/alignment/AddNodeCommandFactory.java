package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class AddNodeCommandFactory extends JSONInputCommandFactory {

	enum Arguments {
		worksheetId, label, uri, id
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {

		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		String label = HistoryJsonUtil.getStringValue(
				Arguments.label.name(), inputJson);
		String uri = HistoryJsonUtil.getStringValue(
				Arguments.uri.name(), inputJson);
		String nodeId = HistoryJsonUtil.getStringValue(
				Arguments.id.name(), inputJson);
		AddNodeCommand cmd = new AddNodeCommand(getNewId(workspace), model, worksheetId, alignmentId, nodeId, uri, label);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return AddNodeCommand.class;
	}

}
