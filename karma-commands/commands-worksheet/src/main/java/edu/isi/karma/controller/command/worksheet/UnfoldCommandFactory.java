package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class UnfoldCommandFactory extends JSONInputCommandFactory{

	private enum Arguments {
		worksheetId, keyhNodeId, valuehNodeId, 
		selectionName, notOtherColumn
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String keyHNodeid = CommandInputJSONUtil.getStringValue(Arguments.keyhNodeId.name(), inputJson);
		String valueHNodeid = CommandInputJSONUtil.getStringValue(Arguments.valuehNodeId.name(), inputJson);
		boolean notOtherColumn = Boolean.parseBoolean(CommandInputJSONUtil.getStringValue(Arguments.notOtherColumn.name(), inputJson));
		String keyName = workspace.getFactory().getHNode(keyHNodeid).getAbsoluteColumnName(workspace.getFactory());
		String valueName = workspace.getFactory().getHNode(valueHNodeid).getAbsoluteColumnName(workspace.getFactory());
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		UnfoldCommand unfoldCmd = new UnfoldCommand(getNewId(workspace), model, worksheetId, 
				keyHNodeid, valueHNodeid, notOtherColumn, 
				selectionName);
		unfoldCmd.setInputParameterJson(inputJson.toString());
		unfoldCmd.setKeyName(keyName);
		unfoldCmd.setValueName(valueName);
		return unfoldCmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return UnfoldCommand.class;
	}

}
