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

public class DeleteNodeCommandFactory extends JSONInputCommandFactory {

	enum Arguments {
		worksheetId, label, id
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
		String id = HistoryJsonUtil.getStringValue(
				Arguments.id.name(), inputJson);

		DeleteNodeCommand cmd = new DeleteNodeCommand(getNewId(workspace), model,
				worksheetId, alignmentId, id, label);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return DeleteNodeCommand.class;
	}

}
