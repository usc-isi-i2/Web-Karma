package edu.isi.karma.controller.command.transformation;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class RepeatPythonTransformationCommandFactory extends
		JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, hNodeId, selectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = HistoryJsonUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hNodeId = HistoryJsonUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (worksheet == null) {
			throw new KarmaException("Worksheet not found");
		}
		String transformCode = worksheet.getMetadataContainer().getColumnMetadata().getColumnPython(hNodeId);
		if (transformCode == null) {
			throw new KarmaException("Transform code is null");
		}
		RepeatPythonTransformationCommand cmd = new RepeatPythonTransformationCommand(
				getNewId(workspace), model, worksheetId, 
				hNodeId, transformCode, selectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return RepeatPythonTransformationCommand.class;
	}

}
