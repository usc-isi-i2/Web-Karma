package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class AugmentDataCommandFactory extends JSONInputCommandFactory{
	
	private enum Arguments {
		worksheetId, predicate, triplesMap, alignmentId, columnUri, otherClass, tripleStoreUrl, hNodeId, incoming
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		// TODO Auto-generated method stub
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String predicate = request.getParameter(Arguments.predicate.name());
		String columnUri = request.getParameter(Arguments.columnUri.name());
		String otherClass = request.getParameter(Arguments.otherClass.name());
		String dataRepoUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String incoming = request.getParameter(Arguments.incoming.name());
		return new AugmentDataCommand(getNewId(workspace), dataRepoUrl, worksheetId, columnUri, predicate, otherClass, hNodeId, Boolean.parseBoolean(incoming));
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// 
		return AugmentDataCommand.class;
	}

	@Override
	public Command createCommand(JSONArray inputJson, Workspace workspace)
			throws JSONException, KarmaException {
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String predicate = CommandInputJSONUtil.getStringValue(Arguments.predicate.name(), inputJson);
		String columnUri = CommandInputJSONUtil.getStringValue(Arguments.columnUri.name(), inputJson);
		String otherClass = CommandInputJSONUtil.getStringValue(Arguments.otherClass.name(), inputJson);
		String dataRepoUrl = CommandInputJSONUtil.getStringValue(Arguments.tripleStoreUrl.name(), inputJson);
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String incoming = CommandInputJSONUtil.getStringValue(Arguments.incoming.name(), inputJson);
		AugmentDataCommand cmd = new AugmentDataCommand(getNewId(workspace), dataRepoUrl, worksheetId, columnUri, predicate, otherClass, hNodeId, Boolean.parseBoolean(incoming));
		cmd.setInputParameterJson(inputJson.toString());
		return cmd;
	}

}
