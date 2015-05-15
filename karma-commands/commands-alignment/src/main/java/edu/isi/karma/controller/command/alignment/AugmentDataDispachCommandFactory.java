package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class AugmentDataDispachCommandFactory extends JSONInputCommandFactory {
	private enum Arguments {
		worksheetId, predicateIncoming, alignmentId, 
		columnUri, otherClassIncoming, tripleStoreUrl, 
		hNodeId, predicateOutgoing, otherClassOutgoing, 
		sameAsPredicate, selectionName
	}
	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String predicateIncoming = CommandInputJSONUtil.getStringValue(Arguments.predicateIncoming.name(), inputJson);
		String columnUri = CommandInputJSONUtil.getStringValue(Arguments.columnUri.name(), inputJson);
		String otherClassIncoming = CommandInputJSONUtil.getStringValue(Arguments.otherClassIncoming.name(), inputJson);
		String dataRepoUrl = CommandInputJSONUtil.getStringValue(Arguments.tripleStoreUrl.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String predicateOutgoing = CommandInputJSONUtil.getStringValue(Arguments.predicateOutgoing.name(), inputJson);
		String otherClassOutgoing = CommandInputJSONUtil.getStringValue(Arguments.otherClassOutgoing.name(), inputJson);
		String sameAsPredicate = CommandInputJSONUtil.getStringValue(Arguments.sameAsPredicate.name(), inputJson);
		this.normalizeSelectionId(worksheetId, inputJson, workspace);
		String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		AugmentDataDispachCommand cmd = new AugmentDataDispachCommand(getNewId(workspace), 
				model, dataRepoUrl, worksheetId, 
				columnUri, predicateIncoming, otherClassIncoming, 
				predicateOutgoing, otherClassOutgoing, hNodeId, sameAsPredicate, 
				selectionName);
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String predicateIncoming = request.getParameter(Arguments.predicateIncoming.name());
		String columnUri = request.getParameter(Arguments.columnUri.name());
		String otherClassIncoming = request.getParameter(Arguments.otherClassIncoming.name());
		String dataRepoUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String predicateOutgoing = request.getParameter(Arguments.predicateOutgoing.name());
		String otherClassOutgoing = request.getParameter(Arguments.otherClassOutgoing.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String sameAsPredicate = request.getParameter(Arguments.sameAsPredicate.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new AugmentDataDispachCommand(getNewId(workspace), 
				Command.NEW_MODEL, dataRepoUrl, worksheetId, 
				columnUri, predicateIncoming, otherClassIncoming, 
				predicateOutgoing, otherClassOutgoing, hNodeId, sameAsPredicate, 
				selectionName);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return AugmentDataDispachCommand.class;
	}

}
