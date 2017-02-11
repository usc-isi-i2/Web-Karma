package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory.Arguments;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class DeleteLinkCommandFactory extends JSONInputCommandFactory {

	@Override
	public Command createCommand(JSONArray inputJson, String model,
			Workspace workspace) throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(
				Arguments.worksheetId.name(), inputJson);
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspace.getId(), worksheetId);
		JSONObject edge = HistoryJsonUtil.getJSONObjectWithName(
				Arguments.edge.name(), inputJson).getJSONObject("value");;

		DeleteLinkCommand cmd = new DeleteLinkCommand(
				getNewId(workspace), model, worksheetId, alignmentId, edge);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	public Command createCommand(String worksheetId, String alignmentId, JSONObject edge, String model, Workspace workspace)
	{
		DeleteLinkCommand cmd =  new DeleteLinkCommand(
				getNewId(workspace), model, worksheetId, alignmentId, edge);
		JSONArray inputParamJson = new JSONArray();
		inputParamJson.put(HistoryJsonUtil.createParamObject("worksheetId", HistoryJsonUtil.ParameterType.worksheetId, worksheetId));
		inputParamJson.put(HistoryJsonUtil.createParamObject("edge", HistoryJsonUtil.ParameterType.other, edge));
		cmd.setInputParameterJson(inputParamJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return DeleteLinkCommand.class;
	}

}
