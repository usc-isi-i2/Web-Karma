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

public class AddLiteralNodeCommandFactory extends JSONInputCommandFactory {


	enum Arguments {
		worksheetId, nodeId, literalType, literalValue, isUri
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {

		return null;
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		String literalType = HistoryJsonUtil.getStringValue(
				Arguments.literalType.name(), inputJson);
		String literalValue = HistoryJsonUtil.getStringValue(
				Arguments.literalValue.name(), inputJson);
		boolean isUri = HistoryJsonUtil.getBooleanValue(Arguments.isUri.name(), inputJson);
		String nodeId = null;
		if(HistoryJsonUtil.valueExits(Arguments.nodeId.name(), inputJson))
			nodeId = HistoryJsonUtil.getStringValue(Arguments.nodeId.name(), inputJson);
		AddLiteralNodeCommand cmd = new AddLiteralNodeCommand(getNewId(workspace), worksheetId, alignmentId, nodeId, literalValue, literalType, isUri);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return AddLiteralNodeCommand.class;
	}

}
